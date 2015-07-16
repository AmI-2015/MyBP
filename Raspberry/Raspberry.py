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


GPIO.setup(7 ,GPIO.IN, pull_up_down=GPIO.PUD_UP) #ingresso interruttore con pull up place_id n 0
GPIO.setup(11,GPIO.OUT) #GREEN LED place_id n 0
GPIO.setup(12,GPIO.OUT) #RED LED place_id  n 0

GPIO.setup(13 ,GPIO.IN, pull_up_down=GPIO.PUD_UP) #ingresso interruttore con pull up place_id n 1
GPIO.setup(15,GPIO.OUT) #GREEN LED place_id n 1
GPIO.setup(16,GPIO.OUT) #RED LED place_id  n 1

GPIO.setup(8 ,GPIO.IN, pull_up_down=GPIO.PUD_UP) #ingresso interruttore con pull up place_id n 2
GPIO.setup(18,GPIO.OUT) #GREEN LED place_id n 2
GPIO.setup(22,GPIO.OUT) #RED LED place_id  n 2

DIS_INT = 0

station_id = 1 
status   = [int(not(GPIO.input(7))), int(not(GPIO.input(13))), int(not(GPIO.input(12)))]
#station number 1
GPIO.output(11,False)  
GPIO.output(12,False)  
                       
def idPlaceZero_change(channel):
    check_and_lock(channel, 11, 12 , 0)

def idPlaceOne_change(channel):
    check_and_lock(channel, 15, 16, 1)

def idPlaceTwo_change(channel):
    check_and_lock(channel, 18, 22, 2)
    
#def idPlaceThree_change(channel):
#     check_and_lock(channel, 19 , 3)


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
            if(channel == 7):
                idPlaceZero_change(channel)
            elif(channel == 13):
                idPlaceOne_change(channel)
            elif(channel == 8):
                idPlaceTwo_change(channel)
            else:
                pass
            #elif(channel == 15):
            #    idPlaceThree_change(channel)
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

GPIO.add_event_detect(7, GPIO.BOTH, handlerInterrupts, bouncetime=1000)
GPIO.add_event_detect(13, GPIO.BOTH, handlerInterrupts, bouncetime=1000)
GPIO.add_event_detect(8, GPIO.BOTH, handlerInterrupts, bouncetime=1000)
#GPIO.add_event_detect(15, GPIO.BOTH, handlerInterrupts, bouncetime=1000)

try:
    while 1:
        pass
    
finally:
    GPIO.cleanup()