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

def start_timer(user_code, pwd_code, set_):
    print "sono in starta timer e set vale: "+str(set_)
    user_connected = user()
    start_time = user_connected.set_start_time(user_code, pwd_code, set_)
    return start_time

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

def lockin_ras(station_id, place_id, status, lock_flag):
    raspberry_connected=raspberry()
    raspberry_connected.rqstlckin_db(station_id, place_id, status, lock_flag)
    pass

def get_data_from_user(security_key):
    user_connected = user()
    user_data = {}
    user_data = user_connected.get_data_from_user(security_key)
    return user_data

def get_security_key_in_usersDB(station_id, place_id):
    user_connected = user()
    security_key = user_connected.get_security_key_in_usersDB(station_id, place_id)
    return security_key

def get_data_valid_in_usersDB(security_key):
    user_connected = user()
    data_valid = user_connected.get_data_valid_in_usersDB(security_key)
    return data_valid

def get_data_valid_from_station_place_id_in_usersDB(station_id, place_id):
    user_connected = user()
    data_valid = user_connected.get_data_valid_from_station_place_id_in_usersDB(station_id, place_id)
    return data_valid

def get_lock_flag_from_station_place_id_in_usersDB(station_id, place_id):
    user_connected = user()
    lock_flag = user_connected.get_lock_flag_from_station_place_id_in_usersDB(station_id, place_id)
    return lock_flag

def get_security_key_in_stationDB(station_id,place_id):
    user_connected = user()
    security_key = user_connected.get_security_key_in_stationDB(station_id,place_id)
    return security_key

def set_place_station_id(station_id, place_id, security_key):
    user_connected = user()
    parking_data = user_connected.set_place_station_id(station_id, place_id, security_key)
    return parking_data

def set_lock_flag(security_key, all, lock_flag, read):
    user_connected = user()
    lock_flag = user_connected.set_lock_flag(security_key, all, lock_flag, read)
    return lock_flag

def set_user_place_station_id(security_key, place_id, station_id):
    user_connected = user()
    user_connected.set_user_place_station_id(security_key, place_id, station_id)
    pass

def set_data_valid(security_key, data_valid):
    user_connected = user()
    user_connected.set_data_valid(security_key, data_valid)
    pass

def set_status_in_usersDB(security_key, status):
    user_connected = user()
    user_connected.set_status_in_usersDB(security_key, status)
    pass

def set_security_key_reg_id_in_stationDB(station_id, place_id, reset):
    user_connected = user()
    user_connected.set_security_key_reg_id_in_stationDB(station_id, place_id, reset)
    pass

def set_unchangeable_in_stationDB(station_id, place_id):
    user_connected = user()
    user_connected.set_unchangeable_in_stationDB(station_id, place_id)
    pass
   
def set_status_in_stationDB(station_id, place_id, status):
    user_connected = user()
    user_connected.set_status_in_stationDB(station_id, place_id, status)
    pass
def controlPlace(place_id, station_id):
    user_connected = user()
    security_key = user_connected.controlPlace(place_id, station_id)
    return security_key

def reset_users_after_alarm(station_id, place_id, status):
    raspberry_connected = raspberry()
    raspberry_connected.reset_users_after_alarm(station_id, place_id, status)
    
    pass

def set_security_key_in_user(station_id, place_id,reset):
    user_connected = user()
    user_connected.set_security_key_in_user(station_id, place_id, reset)
    pass

def get_status_from_stationDB(place_id, station_id):
    user_connected = user()
    status = user_connected.get_status_from_stationDB(station_id, place_id)
    return status
 
def stealing_controller(station_id, place_id):
    parking_data={}
    raspberry_connected=raspberry()
    parking_data = raspberry_connected.stealing_controller(station_id, place_id)
    
    return parking_data

def stop_alarmProcess(station_id, place_id):
    raspberry_connected=raspberry()
    stop_alarm = raspberry_connected.check_alarm(station_id, place_id)   
    return stop_alarm     

def stop_alarm_fromApp_Process(station_id, place_id):
    user_connected = user()
    stop_alarm = user_connected.stop_alarm_fromApp(station_id, place_id)
    return stop_alarm
    
def check_securityKeyProcess(station_id, place_id):
    raspberry_connected=raspberry()
    security_checker = raspberry_connected.check_securityKey(station_id, place_id)   
    return security_checker 

def stn_spcProcess():
    stn_spc = {}
    user_connected = user()
    stn_spc = user_connected.stn_spcDB()
    
    return stn_spc

def stn_updStnSpc(station_id, free_places, tot_places):
    raspberry_connected = raspberry()
    
    stn_updSpc = raspberry_connected.updStnSpcStart(station_id, free_places, tot_places)
    return stn_updSpc

def reset_station(station_id, place_id):
    raspberry_connected = raspberry()
    raspberry_connected.reset_station(station_id, place_id)
    pass

def stn_updDbProcess(station_id,place_id,status):
    raspberry_connected = raspberry()
    
    stn_updSpc = raspberry_connected.upd_stnSpcTbl(station_id, place_id, status)
    raspberry_connected.upd_stationTbl(station_id, place_id, status)
    return stn_updSpc

if __name__ == '__main__':
    pass
