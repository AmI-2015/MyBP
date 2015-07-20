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

request_processor.set_lock_flag(-1, 1, 1)
request_processor.set_ras_flag(-1, -1, 0, -1)
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
    
    user_data=request_processor.sign_in(user_code, pwd_code, registration_id)
    
    print "user_data in sign_in: "+str(user_data)
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
    global LOCK_FLAG
    secret_packet=request.json
    print "SECRET PACKET RECEIVED in get_info"
    print(secret_packet)
    user_data={}
    
    pwd_code=secret_packet.get('pwd_code').replace(" ", "")
    user_code=secret_packet.get('user_code').replace(" ", "")
    registration_id = secret_packet.get('registration_id')
    
    user_data=request_processor.sign_in(user_code, pwd_code, registration_id)
    security_key = pwd_code+user_code
    diz_to_jsonify={}
    start_time = request_processor.start_timer(user_code, pwd_code, 0)
    print start_time
    elapsed = time.time() - start_time
    print "elapsed:"+str(elapsed)
    
    #leggo il valore di ras_flag
    ras_flag = request_processor.set_ras_flag(user_code, pwd_code, -1, 1) 
    
    #TIMER ON THE SERVER
    TIME_OUT = 20
    print "user_data: "+str(user_data)
    if(elapsed > TIME_OUT):
        print "elapse lock_flag: "+str(user_data['lock_flag'])
        
        try:
            return_data= request_processor.lock_app(user_data['station_id'], user_data['place_id'], security_key, registration_id, 0)
            print return_data
        
            if(return_data['lock']==0):
                #request_processor.set_security_key_in_user(user_data['station_id'], user_data['place_id'], 1)
                print "station_id: "+ str(user_data['station_id'])
                print "HO RILEVATO LOCK OUT"
            else:
                request_processor.set_security_key_in_user(user_data['station_id'], user_data['place_id'], 1)
                user_data['station_id']= -1
                user_data['place_id'] = -1
                print "HO RILEVATO LOCK IN"
        except:
            print "CANNOT DETECT A LOCK IN OR LOCK OUT"
        user_data['lock_flag'] = 1

    parking_data={}
    print "lock_flag: "+str(user_data['lock_flag'])
    print "ras_flag: "+str(ras_flag)
    if(int(user_data['lock_flag'])==1):
        #if(elapsed >= TIME_OUT):
            #request_processor.set_security_key_in_user(user_data['station_id'], user_data['place_id'], 1)
        if(int(ras_flag) ==1):
            if(elapsed < TIME_OUT):
                parking_data = request_processor.lock_app(user_data['station_id'], user_data['place_id'], security_key, registration_id,1)
                if(parking_data['lock']==0):
                    request_processor.set_place_station_id(-1, -1, security_key)
            else:
                parking_data = request_processor.lock_app(-1, -1, security_key, registration_id,1)
            request_processor.set_lock_flag(security_key, 0, 1)
            print "parking_data: "+str(parking_data)
            print "user_data: "+str(user_data)
            if(user_data['error_str']=="NO_ERROR"):
                diz_to_jsonify = {'station_id': user_data['station_id'], 'place_id': user_data['place_id'], 'status': parking_data['status'], 'data_valid': str(user_data['lock_flag'])}
                #request_processor.set_security_key_in_user(user_data['station_id'], user_data['place_id'], 0)
            else:
                diz_to_jsonify = {'station_id': -1, 'place_id': -1, 'status': -1, 'data_valid': str(user_data['lock_flag'])}
            request_processor.set_ras_flag(user_code, pwd_code, 0, 0)
        else:
            request_processor.set_place_station_id(user_data['station_id'], user_data['place_id'], security_key)
            request_processor.set_security_key_in_user(user_data['station_id'], user_data['place_id'], 0)
            if(elapsed >= TIME_OUT):
                parking_data = request_processor.lock_app(user_data['station_id'], user_data['place_id'], security_key, registration_id,0)
            else:
                parking_data = request_processor.lock_app(-1, -1, security_key, registration_id,0)
            print "parking_data: "+str(parking_data)
            print "user_data: "+str(user_data)
            if(user_data['error_str']=="NO_ERROR"):
                diz_to_jsonify = {'station_id': parking_data['station_id'], 'place_id': parking_data['place_id'], 'status': user_data['status'], 'data_valid': str(user_data['lock_flag'])}
            else:
                diz_to_jsonify = {'station_id': -1, 'place_id': -1, 'status': -1, 'data_valid': str(user_data['lock_flag'])}      
    elif(int(user_data['lock_flag'])==0):
        if(int(ras_flag) == 1):
            request_processor.set_lock_flag(security_key, 0, 1)
            request_processor.set_ras_flag(user_code, pwd_code, 0, 1) #$$
        diz_to_jsonify = {'station_id': -1, 'place_id': -1, 'status': -1, 'data_valid': str(user_data['lock_flag'])}
    elif(user_data['lock_flag'] == -1):
        parking_data = request_processor.lock_app(user_data['station_id'], user_data['place_id'], security_key, registration_id,0)
        if(parking_data['lock']==1):
            diz_to_jsonify = {'station_id': -1, 'place_id': -1, 'status': -1, 'data_valid': str(user_data['lock_flag'])}
        request_processor.set_ras_flag(user_code, pwd_code, 0, 0)
        request_processor.set_lock_flag(security_key, 0, 1)
    print "diz_to_jsonify: "+ str(diz_to_jsonify)
    
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
    request_packet=request.json
    station_id=request_packet.get("station_id")
    place_id=request_packet.get("place_id")
    status=request_packet.get("status")
    
    lock_flag = request_processor.set_ras_flag_from_raspberry(station_id, place_id, 1)
    print "lock_raspberry... lock_flag: "+str(lock_flag)
    request_processor.lockin_ras(station_id, place_id, status, lock_flag)
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
    request_packet=request.json
    print "PACKET RECEIVED in lock_app:"
    print(request_packet)
    station_id      = request_packet.get("station_id")
    place_id        = request_packet.get("place_id")
    security_key    = request_packet.get("security_key").replace(" ", "")
    registration_id = request_packet.get("registration_id")
    print "entro in start_timer"
    request_processor.start_timer(security_key[0:25], security_key[25:50], 1)
    parking_data=request_processor.lock_app(station_id, place_id, security_key, registration_id, 0)
    security_found = request_processor.controlPlace(place_id, station_id)
    print "security_found:" + security_found
    print "lock: "+str(parking_data['lock'])
    if(parking_data['lock'] == 1):
        request_processor.set_place_station_id(station_id, place_id, security_key)
        request_processor.set_security_key_in_user(station_id, place_id, 0)
    elif(parking_data['lock'] == 0):
        request_processor.set_security_key_in_user(station_id, place_id, 2)
    else:
        request_processor.set_security_key_in_user(station_id, place_id, 2)
        
    if(security_found != 'None' and parking_data['lock']==1):
        request_processor.set_lock_flag(security_key, 0, -1)
    else:
        print "sto per entrare in set_lock_flag"
        request_processor.set_lock_flag(security_key, 0, 0)
        
    #return jsonify({"station_id":parking_data['station_id'], "place_id": parking_data['place_id'], "security_key": security_key, "registration_id": registration_id})
    return jsonify({"station_id":station_id, "place_id": place_id, "security_key": security_key, "registration_id": registration_id})

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

        #return jsonify({"station_id":parking_data['station_id'], "place_id": parking_data['place_id']})
    elif parking_data['action']=="OK":
        #USE GOOGLE CLOUD MESSAGING API
        gcm = GCM(autorization_key)
        data = {'the_message': 'OK'}
        dataj = json.dumps(data)
        
        gcm.plaintext_request
        reg_id = parking_data['registration_id']
        print reg_id        
        try:
            gcm.plaintext_request(registration_id=reg_id, data=data)
        except:
            pass
        pass
    else:
        pass
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
def update_dbServer():
    request_packet=request.json
    station_id = request_packet.get("station_id")
    status   = request_packet.get("status")
    place_id = request_packet.get("place_id")
       
    stn_updSpc = request_processor.stn_updDbProcess(station_id, place_id, status)
    
    return jsonify({"station_id":stn_updSpc['station_id'], "free_places": stn_updSpc['free_places']})

'''
the raspberry sends a json
{
    "station_id": "1",
    "tot_places": "5",
    "free_places": "1"
}
'''
@app.route('/myBP_server/users/update_stationSpec', methods = ['POST'])
def update_station_spec():
    request_packet=request.json
    station_id = request_packet.get("station_id")
    free_places   = request_packet.get("free_places")
    tot_places = request_packet.get("tot_places")
       
    stn_updSpc = request_processor.stn_updStnSpc(station_id, free_places, tot_places)
    
    return jsonify({"station_id":stn_updSpc['station_id'], "free_places": stn_updSpc['free_places'], "tot_places": stn_updSpc['tot_places']})

@app.route('/myBP_server/users/reset_after_alarm', methods = ['POST'])
def reset_after_alarm():
    request_packet = request.json
    station_id = request_packet.get("station_id")
    place_id   = request_packet.get("place_id")
    status = request_packet.get("status")
    stop = request_packet.get("stop")
    
    request_processor.reset_users_after_alarm(station_id, place_id, status)
    
    return jsonify({"station_id": station_id, "place_id": place_id}) 
if __name__ == '__main__':
    app.run(host = '0.0.0.0', debug=True, port=7000)
