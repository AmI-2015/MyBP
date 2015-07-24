'''
Created on 30/mag/2015

@author: MyBP
'''
import os
import RPi.GPIO as GPIO
import time, requests
from MyUser import MyUser
from Timing import Timing

GPIO.setwarnings(False)
GPIO.setmode(GPIO.BOARD)


def setup_pin(channel,green_led,red_led):
    GPIO.setup(channel ,GPIO.IN, pull_up_down=GPIO.PUD_UP) #ingresso interruttore con pull up place_id 
    GPIO.setup(green_led,GPIO.OUT) #GREEN LED place_id 
    GPIO.setup(red_led,GPIO.OUT) #RED LED place_id  
    GPIO.add_event_detect(channel, GPIO.BOTH, handlerInterrupts, bouncetime=500)


DIS_INT = 0

tot_places=0
place=[7,13,-1,-1,-1,-1,-1,-1,-1,-1]
green_led=[11,15,-1,-1,-1,-1,-1,-1,-1,-1]
red_led=[12,16,-1,-1,-1,-1,-1,-1,-1,-1]
system_on=36
stop_system=40
restart_system=38

station_id = 1 


def start_system():
    global tot_places
    global station_id
    objMyUser=MyUser()
    tot_places=0
    free_places=0
    GPIO.setup(stop_system,GPIO.IN, pull_up_down=GPIO.PUD_UP) #ingresso interruttore stop

    GPIO.setup(system_on,GPIO.OUT) #working LED   
    for i in range (0, 10):
        channel=place[i]
        if(channel!=-1):
            print channel
            print green_led[i]
            print red_led[i]
            setup_pin(channel,green_led[i],red_led[i])
            GPIO.output(green_led[i],False)  
            GPIO.output(red_led[i],False)
            objMyUser.update_dbServer(int(not(GPIO.input(channel))),i, station_id)
            status=int(not(GPIO.input(channel)))
            print status
            tot_places=tot_places+1
            if(status==0):
                free_places=free_places+1
                objMyUser.reset_station(i, station_id)
        else:
            pass
    print tot_places
    print free_places    
    objMyUser.update_stationSpec(station_id,free_places,tot_places)

def update_stationSpc():
    global tot_places
    global station_id
    objMyUser=MyUser()
    tot_places=0
    free_places=0
  
    for i in range (0, 10):
        channel=place[i]
	print "Aggiornamento station_id: "+str(station_id)+ "--- place_id: "+str(i)
        if(channel!=-1):
            status=int(not(GPIO.input(channel)))
            tot_places=tot_places+1
            if(status==0):
                free_places=free_places+1
        else:
            pass
    objMyUser.update_stationSpec(station_id,free_places,tot_places)
     

def start_alarm(pin_out):
    print "allarme acceso"
    os.system('omxplayer -o local /home/pi/Desktop/allarmi/Alert.mp3 &')
    Timing.blinkLed(time.time(), 3, pinout)
    pass

def stop_alarm(REDLED):
    print "allarme spento"
    os.system('killall omxplayer.bin')
    GPIO.output(REDLED,False)
    pass

def timer(START,elapsed):
    time_now=time.time()
    
    while(time_now - START)< elapsed:
 	time_now=time.time()

    

def blinker(START,elapsed,LED):
    flag=1
    
    DELTA = 80000
    time_now=time.time()
    
    while(time_now - START)< elapsed:
        i=0
 	time_now=time.time()
	GPIO.output(LED,flag)
        flag=flag^1
        while(i)< DELTA:
            i=i+1
    GPIO.output(LED,True)

def handlerInterrupts(channel):  
    global station_id
    objMyUser = MyUser()
    
    for i in range(0,tot_places):
        if(channel==place[i]):
            status_from_server = objMyUser.get_status(i, station_id)
            print "status_from_server: "+str(int(status_from_server.json().get("status")))
            print "status board: "+str(int(not(GPIO.input(channel))))
            timer(time.time(), 4)
            if(int(not(GPIO.input(channel)))!= status_from_server.json().get("status")):
            	check_and_lock(channel,green_led[i],red_led[i],i)
	    	break
   
def check_and_lock(pin_in, GREENLED, REDLED,  place_id):
    objMyUser=MyUser()
    print "checkLock"
    print "status: "+str(not(GPIO.input(pin_in)))
    checker_securityKey = objMyUser.check_securityKey(place_id, station_id)
    print "security checker: "+str(checker_securityKey.json().get("security_checker"))
    
    
    if GPIO.input(pin_in)==False: 
        print "Ho messo la bici"  
        return_procRqs = objMyUser.process_rqs(pin_in, GREENLED, REDLED, int(not(GPIO.input(pin_in))), place_id, station_id, int(checker_securityKey.json().get("security_checker")))
        #print 'sono rpR '+str(return_procRqs)
            
    elif GPIO.input(pin_in)==True and int(checker_securityKey.json().get("security_checker"))==1:
        GPIO.output(GREENLED, False)
        print " devo startare"
        if objMyUser.process_rqs(pin_in, GREENLED, REDLED, int(not(GPIO.input(pin_in))), place_id, station_id,int(checker_securityKey.json().get("security_checker")))==1:
            start_alarm(REDLED)
            #polling the server to know if the alarm can be stopped
            stop=0
            tim=0
            while(stop != 1 and tim < 30):
   	        print "Polling"
                
    	        response=objMyUser.rqst_stop(int(not(GPIO.input(pin_in))), place_id, station_id)
                stop=int(response.json().get('stop_alarm'))
                print "stop dopo il polling e'"+str(stop)
  	        tim = tim + 1
                time.sleep(0.1)     #in this way it can be compute a time interval
            
            stop_alarm(REDLED)
            objMyUser.reset_after_alarm(int(not(GPIO.input(pin_in))),place_id, station_id)
        else:
            print "allarme non partito, bici tolta da utente"
                
    elif GPIO.input(pin_in)==True:
        print "Ho tolto la bici non deve partire allarm"
        GPIO.output(GREENLED, False)
        GPIO.output(REDLED, False)
        objMyUser.process_rqs(pin_in, GREENLED, REDLED, int(not(GPIO.input(pin_in))), place_id, station_id, int(checker_securityKey.json().get("security_checker")))
        
	

try:
    start_system()
    GPIO.output(system_on, True)
    start_update=time.time()
    while 1:
        if(int(not(GPIO.input(stop_system)))==1):
            break
        if((time.time()-start_update)>10):
            update_stationSpc()
            start_update=time.time()
        pass


except :
    pass
    print "hola"
    GPIO.cleanup()
    GPIO.setwarnings(False)
    GPIO.setmode(GPIO.BOARD)
    GPIO.setup(restart_system,GPIO.IN, pull_up_down=GPIO.PUD_UP) #ingresso interruttore restart
    while 1:
        if(int(not(GPIO.input(restart_system)))==1):
            os.system('sudo python /home/pi/Desktop/Raspberry/Raspberry.py')
            GPIO.output(system_on,flag)
	    flag=flag^1


finally:
    print "ciao"
    GPIO.cleanup()
    GPIO.setwarnings(False)
    GPIO.setmode(GPIO.BOARD)
    GPIO.setup(restart_system,GPIO.IN, pull_up_down=GPIO.PUD_UP) #ingresso interruttore restart
    while 1:
        if(int(not(GPIO.input(restart_system)))==1):
            os.system('sudo python /home/pi/Desktop/Raspberry/Raspberry.py')
            GPIO.output(system_on,flag)
	    flag=flag^1
        pass