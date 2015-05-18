'''
Created on 10/mag/2015

@author: damiannew
'''
from user import user
from raspberry import raspberry

def sign_in(secret_code):
    user_data={}
    user_connected=user()
    user_connected.set_secret_code(secret_code)

    user_data=user_connected.connect_signin_db()
    
    if(user_data['error_str']=="ERROR_SIGNIN"):
        #debug
        print "SECRET_CODE DOES NOT MATCH\n"
    else:
        print "user authenticated\n"
    
    return user_data


def sign_up(secret_code):
    user_data={}
    user_connected=user()
    print "sono qui"
    user_connected.set_secret_code(secret_code)
    user_data=user_connected.connect_signup_db()
    
    if(user_data['error_str']=="ERROR_SIGNUP"):
        #debug
        print "registration failed\n"
    else:
        print "user registered\n"
        
    return user_data

def lockin_ras(station_id, place_id, status_free):
    raspberry_connected=raspberry()
    raspberry_connected.rqstlckin_db(station_id, place_id, status_free)
    
    pass

def lock_app(station_id, place_id, security_key):
    app_connected = user()
    parking_data  = app_connected.connection_stationDB(station_id, place_id, security_key)
    
    return parking_data

def stealing_controller(station_id, place_id):
    raspberry_connected=raspberry()
    raspberry_connected.stealing_controller(station_id, place_id)
    
    pass

if __name__ == '__main__':
    pass