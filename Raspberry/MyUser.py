
'''
Created on 30/mag/2015

@author: damiannew
'''
import requests, json, time
import RPi.GPIO as GPIO
from Timing import Timing

class MyUser():

    def __init__(self):
        self.status   = -1
        self.place_id = -1

    def timer(self, START, elapsed):
   
        time_now=time.time()
    	
    	while((time_now - START)< elapsed):
		
 		time_now=time.time()

    #QUI C E BLINKER

    def process_rqs(self, pin_in, GREENLED, REDLED, status, place_id, station_id, security_checker):
        alarm = 0
        wait  = 0.5          #wait time in second, in this time the user has to pass the smartphone on NFC, otherwise
                            #he is registered as an unregistered user
        if status==1: #falling edge
	    print str(status)+' ho messo la bici'
            time_now = time.time()
            print time_now
            self.timer(time_now, wait)
	    Timing.blinkLed(time.time(), 3, GREENLED)
	    print GPIO.input(pin_in)
            if GPIO.input(pin_in) == False:
                self.lock_rqst(status, place_id, station_id)
            else: 
                pass
        else:
            print str(status)+ '   ho tolto la bici'
            Timing.blinkLed(time.time(), 3, REDLED)
            GPIO.output(REDLED, False)
            response = self.steal_cntrl(status, place_id, station_id).json().get("alarm")
            if(response.lower()=="alarm"):
                alarm=1
            else:
                alarm=0

            self.lock_rqst(status, place_id, station_id)
                  
        return alarm
    
    def lock_rqst(self, status, place_id, station_id):
        url      = "http://10.42.0.1:7000/myBP_server/users/lock_ras"
        payload = { 'station_id': str(station_id), 'place_id': str(place_id), 'status': str(status) }
        headers = {'Content-type': 'application/json', 'Accept': 'text/plain'}
        requests.post(url, data=json.dumps(payload), headers = headers)
        
    def steal_cntrl(self, status, place_id, station_id):
        url      = "http://10.42.0.1:7000/myBP_server/users/stealing_controller"
        payload = { 'station_id': str(station_id), 'place_id': str(place_id), 'status': str(status) }
        headers = {'Content-type': 'application/json', 'Accept': 'text/plain'}
        response = requests.post(url, data=json.dumps(payload), headers = headers)  
        return response

    def check_securityKey(self, place_id, station_id):
        url      = "http://10.42.0.1:7000/myBP_server/users/check_securityKey"
        payload = { 'station_id': str(station_id), 'place_id': str(place_id) }
        headers = {'Content-type': 'application/json', 'Accept': 'text/plain'}
        response = requests.post(url, data=json.dumps(payload), headers = headers)  
        return response
     
    def rqst_stop(self, status, place_id, station_id):  
        url      = "http://10.42.0.1:7000/myBP_server/users/stop_alarm"
        payload = { 'station_id': str(station_id), 'place_id': str(place_id), 'status': str(status) }
        headers = {'Content-type': 'application/json', 'Accept': 'text/plain'}
        response = requests.post(url, data=json.dumps(payload), headers = headers) 
        return response
        
    def update_dbServer(self, status,place_id, station_id):
        url     = "http://10.42.0.1:7000/myBP_server/users/update_dbServer"  
        payload = { 'station_id': str(station_id), 'place_id': str(place_id), 'status': str(status) }
        headers = {'Content-type': 'application/json', 'Accept': 'text/plain'}
        print payload
	response = requests.post(url, data=json.dumps(payload), headers = headers) 
        return response   

    def update_stationSpec(self, station_id,free_places,tot_places):
        url     = "http://10.42.0.1:7000/myBP_server/users/update_stationSpec"  
        payload = { 'station_id': str(station_id), 'free_places': str(free_places), 'tot_places': str(tot_places)}
        headers = {'Content-type': 'application/json', 'Accept': 'text/plain'}
        print payload
	response = requests.post(url, data=json.dumps(payload), headers = headers) 
        return response   
    
    def reset_after_alarm(self, status,place_id, station_id):
        url     = "http://10.42.0.1:7000/myBP_server/users/reset_after_alarm"  
        payload = { 'station_id': str(station_id), 'place_id': str(place_id), 'status': str(status) }
        headers = {'Content-type': 'application/json', 'Accept': 'text/plain'}
        print payload
	response = requests.post(url, data=json.dumps(payload), headers = headers) 
        return response
 
    def reset_station(self,place_id, station_id):
        url     = "http://10.42.0.1:7000/myBP_server/users/reset_station"  
        payload = { 'station_id': str(station_id), 'place_id': str(place_id) }
        headers = {'Content-type': 'application/json', 'Accept': 'text/plain'}
        print payload
	response = requests.post(url, data=json.dumps(payload), headers = headers) 
        return response

    def get_status(self,place_id, station_id):
        url     = "http://10.42.0.1:7000/myBP_server/users/get_status_from_raspberry"  
        payload = { 'station_id': str(station_id), 'place_id': str(place_id) }
        headers = {'Content-type': 'application/json', 'Accept': 'text/plain'}
        print payload
	response = requests.post(url, data=json.dumps(payload), headers = headers) 
        return response          
    