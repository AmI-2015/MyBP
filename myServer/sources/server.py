'''
Created on 04/mag/2015

@author: MyBP

'''
#!/usr/bin/env python
# -*- coding: utf-8 -*- 
import request_processor, json, time
from flask import Flask, jsonify, request
from gcm import *

app= Flask(__name__)

LOCK_FLAG = 0

#the following method is to sign in 
'''
header json 
{
    "user_code": "username",
    "pwd_code":  "password",
    "registration_id": "registration_id"
}
'''
@app.route('/myBP_server/users/sign_in', methods=['POST'])
def sign_in():
    secret_packet = request.json
    print "SECRET PACKET RECEIVED IN sign_in:"
    print(secret_packet)    #print the packet, just for debugging
    
    user_code=secret_packet.get('user_code')
    pwd_code=secret_packet.get('pwd_code')
    registration_id=secret_packet.get('registration_id')
    #pwd_code and user_code has to be at fixed length
    print len(user_code)
    print pwd_code
    user_data=request_processor.sign_in(user_code, pwd_code, registration_id)
    

    return jsonify({"user_code": user_data['username_code'], "pwd_code": user_data['pwd_code'], "error_str": user_data['error_str']})
    
#the following method is to sign up
'''
header json 
{
    "user_code": "username",
    "pwd_code":  "password",
    "registration_id": "registraion_id"
}
'''
@app.route('/myBP_server/users/sign_up', methods=['POST'])
def sign_up():
    secret_packet = request.json
    print(secret_packet)
    
    pwd_code=secret_packet.get('pwd_code')
    user_code=secret_packet.get('user_code')
    registration_id = secret_packet.get('registration_id')
    
    user_data=request_processor.sign_up(user_code, pwd_code, registration_id)
    return jsonify({"pwd_code": user_data['pwd_code'], "error_str": user_data['error_str']})
    

@app.route('/myBP_server/users/get_info', methods=['POST'])
def  get_info():
    secret_packet=request.json
    print "SECRET PACKET RECEIVED in get_info"
    print(secret_packet)
    user_data={}
    
    pwd_code=secret_packet.get('pwd_code')
    user_code=secret_packet.get('user_code')
    registration_id = secret_packet.get('registration_id')
    security_key=user_code+pwd_code
    
    user_data=request_processor.sign_in(user_code, pwd_code, registration_id)
    
    diz_to_jsonify={}
    
    print user_data['error_str']
    if(user_data['error_str']=="NO_ERROR"):
        diz_to_jsonify = {'station_id': user_data['station_id'], 'place_id': user_data['place_id'], 'status': user_data['status']}
        print diz_to_jsonify
    else:
        return jsonify({'station_id': -1, 'place_id': -1, 'status': -1})
    
    return jsonify(diz_to_jsonify)

#the following method is to log in "LOCK IN" from raspberry
'''
example
header json
{
    "station_id": "ID_STATION",
    "place_id": "ID_PLACE",
    "status": "1"
}

This request comes from the Raspberry located on the station, it is sent after the computing of the TIME OUT
'''
@app.route('/myBP_server/users/lock_ras', methods=['POST']) 
def lock_raspberry():
    global LOCK_FLAG
    request_packet=request.json
    station_id=request_packet.get("station_id")
    place_id=request_packet.get("place_id")
    status=request_packet.get("status")
    request_processor.lockin_ras(station_id, place_id, status)
    LOCK_FLAG = 1
    return jsonify({"error_str": "OK"})

#the following method is to log in "LOCK IN" by the app
'''
example
header json
{
    "station_id": "ID_STATION",
    "place_id": "ID_PLACE",
    "security_key": "xx",
    "registration_id": "registraion_id"
}


It returns an header json
{
    "station_id": "ID_STATION",    <--------- set -1 if the locked in it hasn't happened
    "place_id": "ID_PLACE",        <--------- set -1 if the locked in it hasn't happened
    "security_key": "xx",
    "registration_id": "registration_id"
}

OSSERVAZIONE [FATTO]:
BISOGNA MODIFICARE QUESTA FUNZIONE IN MODO CHE CONTROLLI SE A RICHIESTA E' DI LOCKIN O LOCKUP, QUESTO DEVE ESSERE FATTO CERCANDO NEL
DATABASE SE LA TABELLA station CONTIENE GIA' LA CASELLA security_key [FATTO]
'''
@app.route('/myBP_server/users/lock_app', methods=['POST']) 
def lock_app():
    global LOCK_FLAG
    request_packet=request.json
    print "PACKET RECEIVED in lock_app:"
    print(request_packet)

    station_id      = request_packet.get("station_id")
    place_id        = request_packet.get("place_id")
    security_key    = request_packet.get("security_key")
    print "security_key:"+security_key
    registration_id = request_packet.get("registration_id")
    parking_data=request_processor.lock_app(station_id, place_id, security_key, registration_id)
#    if(LOCK_FLAG == 0):
#        parking_data['station_id']=-1
#        parking_data['place_id'] = -1
#        parking_data['security_key'] = -1
#    else:
#        LOCK_FLAG = 0
    
    return jsonify({"station_id":parking_data['station_id'], "place_id": parking_data['place_id'], "security_key": security_key, "registration_id": registration_id})

'''
#the following method checks that a steal doesn't occur

the Raspberry sends a json header 
{
    "station_id": "ID_STATION",
    "place_id": "ID_PLACE"
}

if the allarm has to start the json response
{
    "station_id": -1,
    "place_id": -1
}
'''
@app.route('/myBP_server/users/stealing_controller', methods=['POST'])
def stealing_controller():
    request_packet=request.json
    station_id=request_packet.get("station_id")
    place_id=request_packet.get("place_id")
    
    parking_data=request_processor.stealing_controller(station_id, place_id)

    autorization_key="AIzaSyCHX63txYHN9kcICuJzYVg26Q2bHWPjASU"
    
    if parking_data['action']=="ALARM":
        #USE GOOGLE CLOUD MESSAGING API
        gcm = GCM(autorization_key)
        data = {'the_message': 'ALARM'}
        dataj = json.dumps(data)
        
        # plaintext request
        reg_id = parking_data['registration_id']
        print reg_id
        
        if(parking_data['registration_id'] != 'None'):
            gcm.plaintext_request(registration_id=reg_id, data=data)

        return jsonify({"station_id":parking_data['station_id'], "place_id": parking_data['place_id']})
        #return jsonify({"station_id":parking_data['station_id'], "place_id": parking_data['place_id']})
    elif parking_data['action']=="OK":
        #USE GOOGLE CLOUD MESSAGING API
        gcm = GCM(autorization_key)
        data = {'the_message': 'ALARM'}
        dataj = json.dumps(data)
        
        # plaintext request
        reg_id = parking_data['registration_id']
        print reg_id        
        gcm.plaintext_request(registration_id=reg_id, data=data)
        
        return jsonify({"station_id":parking_data['station_id'], "place_id": parking_data['place_id']})
    else:
        return jsonify({"station_id":parking_data['station_id'], "place_id": parking_data['place_id']})

'''
The raspberry sends a json packet through POST http
{
    "station_id": "4",
    "place_id": "3",
    "stop_alarm": "0"
}

the server's response has to set stop_alarm to 1 iff the alarm has to be stopped on the RaspPi
'''    
@app.route('/myBP_server/users/stop_alarm', methods=['POST'])
def stop_alarm():
    request_packet=request.json
    station_id = request_packet.get("station_id")
    place_id   = request_packet.get("place_id")
    
    print station_id, place_id
    stop_alarm=request_processor.stop_alarmProcess(station_id, place_id)
    print stop_alarm
    #stop_alarm = 1
    return jsonify({"station_id":station_id, "place_id": place_id, "stop_alarm": str(stop_alarm)})

'''
The app sends a json packet through POST http only when it wants that MyBP System Alarm has to be stopped
{
    "station_id": "4",
    "place_id": "3",
    "stop_alarm": "1"
}

the server's response has to set stop_alarm to 1 so that the alarm stops on RaspPi
'''    
@app.route('/myBP_server/users/stop_alarm_fromApp', methods=['POST'])
def stop_alarm_fromApp():
    request_packet=request.json
    station_id = request_packet.get("station_id")
    place_id   = request_packet.get("place_id")
    print "station_id, place_id:  "+str(station_id)+"  "+str(place_id)
    stop_alarm=request_processor.stop_alarm_fromApp_Process(station_id, place_id)
    print "stop_alarm_fromApp: "+str(stop_alarm)
    return jsonify({"station_id":station_id, "place_id": place_id, "stop_alarm": str(stop_alarm)})

@app.route('/myBP_server/users/check_securityKey', methods=['POST'])
def check_securityKey():
    print "RICHIESTA CHECKER SECURITY KEY"
    request_packet=request.json
    station_id = request_packet.get("station_id")
    place_id   = request_packet.get("place_id")
    
    security_checker=request_processor.check_securityKeyProcess(station_id, place_id)
    print "security: "+str(security_checker)
    return jsonify({"security_checker": str(security_checker)})

'''
The app request is a json 
{}

the server responses 
'''
@app.route('/myBP_server/users/station_spec', methods=['POST'])
def sation_spec():
    stn_spc=request_processor.stn_spcProcess()
    
    return jsonify({"d": stn_spc})

'''
the raspberry sends a json
{
    "station_id": "5",
    "status": "1"
}
'''
@app.route('/myBP_server/users/update_dbServer', methods = ['POST'])
def update_station_spec_table():
    request_packet=request.json
    station_id = request_packet.get("station_id")
    status   = request_packet.get("status")
    place_id = request_packet.get("place_id")
       
    stn_updSpc = request_processor.stn_updDbProcess(station_id, place_id, status)
    
    return jsonify({"station_id":stn_updSpc['station_id'], "place_id": stn_updSpc['free_places']})

if __name__ == '__main__':
    app.run(host = '0.0.0.0', debug=True, port=7000)
