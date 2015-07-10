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


GPIO.setup(7,GPIO.OUT) #led controllo0
GPIO.setup(16,GPIO.OUT) #led controllo1
GPIO.setup(18,GPIO.OUT) #led controllo2
GPIO.setup(19,GPIO.OUT) #led controllo3
GPIO.setup(11,GPIO.IN, pull_up_down=GPIO.PUD_UP) #ingresso interruttore con pull up
GPIO.setup(12,GPIO.IN, pull_up_down=GPIO.PUD_UP)
GPIO.setup(13,GPIO.IN, pull_up_down=GPIO.PUD_UP)
GPIO.setup(15,GPIO.IN, pull_up_down=GPIO.PUD_UP)

DIS_INT = 0

station_id = 1 
status   = [0, 0, 0, 0, 0]
GPIO.output(7,False)  
GPIO.output(16,False)  
GPIO.output(18,False)  
GPIO.output(19,False)                        
def idPlaceZero_change(channel):
    check_and_lock(channel, 7 , 0)
   
    

def idPlaceOne_change(channel):
    check_and_lock(channel, 16 , 1)

def idPlaceTwo_change(channel):
    check_and_lock(channel, 18 , 2)
    
def idPlaceThree_change(channel):
     check_and_lock(channel, 19 , 3)


def start_alarm(pin_out):
    print "allarme acceso"
    os.system('omxplayer -o local /home/pi/Desktop/allarmi/Alert.mp3 &')
    GPIO.output(pin_out,True)
    pass

def stop_alarm(pin_out):
    print "allarme spento"
    os.system('killall omxplayer.bin')
    GPIO.output(pin_out,False)
    pass

def timer(START):
    global DIS_INT
    DELTA = 10000
    time_now=time.time()
    
    while(time_now - START)< 2:
 	time_now=time.time()
	DIS_INT = 1

    DIS_INT = 0 



def handlerInterrupts(channel):
    global DIS_INT
    status = GPIO.input(channel)
    print "DIS_INT"+ str(DIS_INT)
    if (DIS_INT == 0): 
        timer(time.time())       
        current_status = GPIO.input(channel)
        if(current_status == status):
            if(channel == 11):
                idPlaceZero_change(channel)
            elif(channel == 12):
                idPlaceOne_change(channel)
            elif(channel == 13):
                idPlaceTwo_change(channel)
            elif(channel == 15):
                idPlaceThree_change(channel)
    else:
        print "DISTRUGGO INTERRUPT"
        pass
    DIS_INT = 0


def check_and_lock(pin_in,pin_out,  place_id):
    objMyUser=MyUser()
    print 'checkLock'
    checker_securityKey = objMyUser.check_securityKey(place_id, station_id)
    print "security checker: "+str(checker_securityKey.json().get("security_checker"))
    if int(checker_securityKey.json().get("security_checker"))==1 or int(checker_securityKey.json().get("security_checker"))==0:
        if GPIO.input(pin_in)==False:   
            status[place_id]=1
	    return_procRqs = objMyUser.process_rqs(pin_in, status[place_id], place_id, station_id, int(checker_securityKey.json().get("security_checker")))
            print 'sono rpR '+str(return_procRqs)
            if return_procRqs==1:
            
	        print "non devo startare alarm"
                start_alarm(pin_out)
                #polling the server to know if the alarm can be stopped
                stop=0
                tim=0
                while(stop != 1 and tim < 10):
                    response=objMyUser.rqst_stop(status[place_id], place_id, station_id)
                    stop=int(response.json().get('stop_alarm'))
                    tim = tim + 1
                    time.sleep(0.5)     #in this way it can be computed a time interval
            
                stop_alarm(pin_out)
                status[place_id]= 0
        elif GPIO.input(pin_in)==True and int(checker_securityKey.json().get("security_checker"))==1:
            print " devo startare"
        
            status[place_id] = 0
            global DIS_INT
   	    if objMyUser.process_rqs(pin_in,status[place_id], place_id, station_id,int(checker_securityKey.json().get("security_checker")))==1:
                start_alarm(pin_out)
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
            
                stop_alarm(pin_out)
                objMyUser.update_dbServer(status[place_id],place_id, station_id)
                DIS_INT = 0


GPIO.add_event_detect(11, GPIO.BOTH, handlerInterrupts, bouncetime=1000)
GPIO.add_event_detect(12, GPIO.BOTH, handlerInterrupts, bouncetime=1000)
GPIO.add_event_detect(13, GPIO.BOTH, handlerInterrupts, bouncetime=1000)
GPIO.add_event_detect(15, GPIO.BOTH, handlerInterrupts, bouncetime=1000)

try:
    while 1:
        pass
    
finally:
    GPIO.cleanup()