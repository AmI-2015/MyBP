'''
Created on 30/mag/2015

@author: MyBP
'''
import os
import RPi.GPIO as GPIO
import time, requests
from MyUser import MyUser

GPIO.setwarnings(False)
GPIO.setmode(GPIO.BOARD)


def setup_pin(channel,green_led,red_led):
    GPIO.setup(channel ,GPIO.IN, pull_up_down=GPIO.PUD_UP) #ingresso interruttore con pull up place_id 
    GPIO.setup(green_led,GPIO.OUT) #GREEN LED place_id 
    GPIO.setup(red_led,GPIO.OUT) #RED LED place_id  
    GPIO.add_event_detect(channel, GPIO.BOTH, handlerInterrupts, bouncetime=1000)


DIS_INT = 0

tot_places=0
place=[7,13,-1,-1,-1,-1,-1,-1,-1,-1]
status=[0,0,0,0,0,0,0,0,0,0]
green_led=[11,15,-1,-1,-1,-1,-1,-1,-1,-1]
red_led=[12,16,-1,-1,-1,-1,-1,-1,-1,-1]
system_on=36
stop_system=40

station_id = 1 
print status


def start_system():
    global tot_places
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
            status[i]=int(not(GPIO.input(channel)))
            print status[i]
            tot_places=tot_places+1
            if(status[i]==0):
                free_places=free_places+1
        else:
            pass
    print tot_places
    print free_places    
    objMyUser.update_stationSpec(station_id,free_places,tot_places)

def start_alarm(pin_out):
    print "allarme acceso"
    os.system('omxplayer -o local /home/pi/Desktop/allarmi/Alert.mp3 &')
    GPIO.output(pin_out,True)
    pass

def stop_alarm(REDLED):
    print "allarme spento"
    os.system('killall omxplayer.bin')
    GPIO.output(REDLED,False)
    pass

def timer(START,elapsed):
    global DIS_INT
   
    time_now=time.time()
    
    while(time_now - START)< elapsed:
 	time_now=time.time()
	DIS_INT = 1

    DIS_INT = 0 

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
    global DIS_INT
    status = GPIO.input(channel)
    print "DIS_INT"+ str(DIS_INT)
    if (DIS_INT == 0): 
        timer(time.time(),2)       
        current_status = GPIO.input(channel)
        if(current_status == status):
            for i in range(0,tot_places):
                if(channel==place[i]):
                    check_and_lock(channel,green_led[i],red_led[i],i)
                    break
    else:
        print "DISTRUGGO INTERRUPT"
        pass
    DIS_INT = 0


def check_and_lock(pin_in, GREENLED, REDLED,  place_id):
    objMyUser=MyUser()
    print 'checkLock'
    print "status: "+str((GPIO.input(pin_in)))
    checker_securityKey = objMyUser.check_securityKey(place_id, station_id)
    print "security checker: "+str(checker_securityKey.json().get("security_checker"))
    if int(checker_securityKey.json().get("security_checker"))==1 or int(checker_securityKey.json().get("security_checker"))==0:
        if GPIO.input(pin_in)==False:   
            blinker(time.time(),1,GREENLED)
            status[place_id]=1
	    return_procRqs = objMyUser.process_rqs(pin_in, GREENLED, REDLED, status[place_id], place_id, station_id, int(checker_securityKey.json().get("security_checker")))
            print 'sono rpR '+str(return_procRqs)
           
        elif GPIO.input(pin_in)==True and int(checker_securityKey.json().get("security_checker"))==1:
            GPIO.output(GREENLED, False)
            timer(time.time(),5)
            print " devo startare"
            status[place_id] = 0
            global DIS_INT
   	    if objMyUser.process_rqs(pin_in, GREENLED, REDLED, status[place_id], place_id, station_id,int(checker_securityKey.json().get("security_checker")))==1:
                start_alarm(REDLED)
                #polling the server to know if the alarm can be stopped
                stop=0
                tim=0
                while(stop != 1 and tim < 30):
		    print "Polling"
                    DIS_INT = 1
                    response=objMyUser.rqst_stop(status[place_id], place_id, station_id)
                    stop=int(response.json().get('stop_alarm'))
                    print "stop dopo il polling e'"+str(stop)
		    tim = tim + 1
                    time.sleep(0.1)     #in this way it can be compute a time interval
            
                stop_alarm(REDLED)
                objMyUser.update_dbServer(status[place_id],place_id, station_id)
                DIS_INT = 0
        elif GPIO.input(pin_in)==True:
	    GPIO.output(GREENLED, False)
            blinker(time.time(),2 ,REDLED)
            GPIO.output(REDLED, False)
            objMyUser.update_dbServer(0,place_id, station_id)   



try:
    start_system()
    blinker(time.time(),1,system_on)
    while 1:
        if(int(not(GPIO.input(stop_system)))==1):
            break
        pass
    
finally:
    print "ciao"
    GPIO.cleanup()