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


station_id = 1 
status   = [0, 0, 0, 0, 0]
GPIO.output(7,False)                        
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
    #os.system('omxplayer -o local /home/pi/Desktop/allarmi/Alert.mp3 &')
    GPIO.output(pin_out,True)
    pass

def stop_alarm(pin_out):
    print "allarme spento"
    #os.system('killall omxplayer.bin')
    GPIO.output(pin_out,False)
    pass

def check_and_lock(pin_in,pin_out,  place_id):
    objMyUser=MyUser()
    print 'checkLock'
    if GPIO.input(pin_in)==False:   
        status[place_id]=1
	return_procRqs = objMyUser.process_rqs(pin_in,status[place_id], place_id, station_id)
        print 'sono rpR '+str(return_procRqs)
        if return_procRqs==1:
            
	    print "non devo startare alarm"
            start_alarm(pin_out)
            #polling the server to know if the alarm can be stopped
            stop=0
            tim=0
            while(stop != 1 and tim < 10):
                response=objMyUser.rqst_stop(status[place_id], place_id, station_id)
		print '!!!!!!!!!!!!!!!'
		print str(response)
		print '!!!!!!!!!!!!!!!'
                stop=int(response.json().get('stop_alarm'))
                tim = tim + 1
                time.sleep(0.5)     #in this way it can be computed a time interval
            
            stop_alarm(pin_out)
            status[place_id]= 0
    elif GPIO.input(pin_in)==True:
        print " devo startare"
        
        status[place_id] = 0
        if objMyUser.process_rqs(pin_in,status[place_id], place_id, station_id)==1:
            start_alarm(pin_out)
            #polling the server to know if the alarm can be stopped
            stop=0
            tim=0
            while(stop != 1 and tim < 30):
		print "faccio il polling"
                response=objMyUser.rqst_stop(status[place_id], place_id, station_id)
                stop=int(response.json().get('stop_alarm'))
                print "stop dopo il polling e'"+str(stop)
		tim = tim + 1
                time.sleep(0.5)     #in this way it can be compute a time interval
            
            stop_alarm(pin_out)
            objMyUser.update_dbServer(status[place_id],place_id, station_id)


GPIO.add_event_detect(11, GPIO.BOTH, idPlaceZero_change, bouncetime=1000)
GPIO.add_event_detect(12, GPIO.BOTH, idPlaceOne_change, bouncetime=1000)
GPIO.add_event_detect(13, GPIO.BOTH, idPlaceTwo_change, bouncetime=1000)
GPIO.add_event_detect(15, GPIO.BOTH, idPlaceThree_change, bouncetime=1000)

while 1:
    pass