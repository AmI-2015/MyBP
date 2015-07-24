import threading
import time
import RPi.GPIO as GPIO

class Timing(threading.Thread):
    def __init__(self):
        pass
    
    @classmethod
    def blinkLed(self, START,elapsed,LED):
        flag=1
	
	DELTA = 100000
	time_now=time.time()
	
	while(time_now - START)< elapsed:
	    i=0
	    time_now=time.time()
	    GPIO.output(LED,flag)
	    flag=flag^1
	    while(i< DELTA):
	        i=i+1
        
        GPIO.output(LED,True)