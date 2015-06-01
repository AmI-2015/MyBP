'''
Created on 10/mag/2015

@author: MyBP
'''

from raspberry import raspberry
from user import user

def sign_in(user_code, pwd_code, registration_id):
    user_data={}
    user_connected=user()
    user_connected.set_userandpwd(user_code, pwd_code)

    user_data=user_connected.connect_signin_db(registration_id)
    
    if(user_data['error_str']=="ERROR_SIGNIN"):
        #debug
        print "SECRET_CODE DOES NOT MATCH\n"
    else:
        print "user authenticated\n"
    
    return user_data


def sign_up(user_code, pwd_code, registration_id):
    user_data={}
    user_connected=user()
    user_connected.set_userandpwd(user_code, pwd_code)
    user_data=user_connected.connect_signup_db(registration_id)
    
    if(user_data['error_str']=="ERROR_SIGNUP"):
        #debug
        print "registration failed\n"
    else:
        print "user registered\n"
        
    return user_data

def lockin_ras(station_id, place_id, status):
    raspberry_connected=raspberry()
    raspberry_connected.rqstlckin_db(station_id, place_id, status)
    
    pass

def lock_app(station_id, place_id, security_key, registration_id):
    app_connected = user()
    parking_data  = app_connected.connection_stationDB(station_id, place_id, security_key, registration_id)
    
    return parking_data

def stealing_controller(station_id, place_id):
    parking_data={}
    raspberry_connected=raspberry()
    parking_data = raspberry_connected.stealing_controller(station_id, place_id)
    
    return parking_data

def stop_alarmProcess(station_id, place_id):
    print station_id, place_id
    raspberry_connected=raspberry()
    stop_alarm = raspberry_connected.check_alarm(station_id, place_id)   
    return stop_alarm     

def stn_spcProcess():
    stn_spc = {}
    user_connected = user()
    stn_spc = user_connected.stn_spcDB()
    
    return stn_spc

if __name__ == '__main__':
    pass