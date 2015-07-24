'''
Created on 04/mag/2015

@author: MyBP

'''
#!/usr/bin/env python
# -*- coding: utf-8 -*- 
import request_processor, json, time, MySQLdb
from flask import Flask, jsonify, request
from gcm import *

VALID = 1
NOT_VALID = 0
ERROR_STATION=-1
ERROR_VALID=-1
SINGLE = 0
MULTIPLE = 1
READ = 1
WRITE = 0
LOCK_IN = 1
LOCK_OUT = -1
DEFAULT = 0
RESET = 1
TIME_OUT = 20

app= Flask(__name__)

request_processor.set_lock_flag(DEFAULT, MULTIPLE, DEFAULT, WRITE)
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
    
    security_key = pwd_code+user_code
    
    #CONTROLLO DA FINIRE NEL CASO DI SIGN IN SBAGLIATO
    user_data=request_processor.sign_in(user_code, pwd_code, registration_id)
    
    start_time = request_processor.start_timer(user_code, pwd_code, 0)
    elapsed = time.time() - start_time
    
    data_valid = request_processor.get_data_valid_in_usersDB(security_key)

    if(data_valid == 1):
        pass
    else:
        if(elapsed> TIME_OUT):
            #LOCK IN MANCATO
            if(user_data['lock_flag']==LOCK_IN):
                #Settaggio lock flag default in DB users
                request_processor.set_lock_flag(security_key, SINGLE, DEFAULT, WRITE)
                #Settaggio place_id station_id utente non lockato in DB users
                request_processor.set_user_place_station_id(security_key, -1, -1)
                #Dati validi
                request_processor.set_data_valid(security_key, VALID)
            elif(user_data['lock_flag']== LOCK_OUT):
                #Settaggio lock flag default in DB users
                request_processor.set_lock_flag(security_key, SINGLE, DEFAULT, WRITE)
                #Dati validi
                request_processor.set_data_valid(security_key, VALID)
        else:
            pass
    
    user_data=request_processor.get_data_from_user(security_key)
    print user_data
    json_app = {"station_id": user_data['station_id'], "place_id": user_data['place_id'], "status": user_data['status'], "data_valid": user_data['data_valid']}
    return jsonify(json_app)
    
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
    print "request_packet [lock_ras]: "+str(request_packet)
    station_id = request_packet.get("station_id")
    place_id   = request_packet.get("place_id")
    status     = request_packet.get("status")
    print "status in lock_ras:"+str(status)
    security_found = request_processor.controlPlace(place_id, station_id)
    #Riceve security_key
    security_key = request_processor.get_security_key_in_usersDB(station_id, place_id)
    #Riceve data_valid con station_id e place_id  in users DB
    data_valid = request_processor.get_data_valid_from_station_place_id_in_usersDB(station_id, place_id)
    #Riceve lock_flag con station:id e place_id in users DB
    lock_flag = request_processor.get_lock_flag_from_station_place_id_in_usersDB(station_id, place_id)
    print "data_valid:" +str(data_valid)
    print "lock_flag:" +str(lock_flag)
    if(data_valid ==0):
        #OPERAZIONE IN CORSO
        if(lock_flag == LOCK_IN):
            #LOCK IN
            request_processor.set_security_key_reg_id_in_stationDB(station_id, place_id, int(not(RESET)))
            request_processor.set_status_in_stationDB(station_id, place_id, 1)
            request_processor.set_status_in_usersDB(security_key, 1)
        elif(lock_flag == LOCK_OUT):
            #LOCK OUT
            request_processor.set_security_key_reg_id_in_stationDB(station_id, place_id, RESET)
            request_processor.set_status_in_stationDB(station_id, place_id, 0)
            request_processor.set_status_in_usersDB(security_key, 0)
            request_processor.set_user_place_station_id(security_key, -1, -1)
        
        request_processor.set_status_in_stationDB(status, station_id, place_id)
        request_processor.set_data_valid(security_key, VALID)
        #Settaggio lock flag in DB users
        request_processor.set_lock_flag(security_key, SINGLE, 0 , WRITE)
    else:
        #No operazione in corso
        if(request_processor.get_security_key_in_stationDB(station_id,place_id)== 'UNCHANGEABLE'):
            #LOCK OUT UTENTE NON REGISTRATO
            request_processor.set_status_in_stationDB(station_id, place_id, status)
            request_processor.set_security_key_reg_id_in_stationDB(station_id, place_id, RESET)
        elif(request_processor.get_security_key_in_stationDB(station_id,place_id)== 'None'):
            #LOCK IN UTENTE NON REGISTRATO
            request_processor.set_status_in_stationDB(station_id, place_id, status)
            request_processor.set_unchangeable_in_stationDB(station_id, place_id)
            
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
    "lock" : "1,-1"
}

OSSERVAZIONE [FATTO]:
BISOGNA MODIFICARE QUESTA FUNZIONE IN MODO CHE CONTROLLI SE A RICHIESTA E' DI LOCKIN O LOCKUP, QUESTO DEVE ESSERE FATTO CERCANDO NEL
DATABASE SE LA TABELLA station CONTIENE GIA' LA CASELLA security_key [FATTO]
'''
@app.route('/myBP_server/users/lock_app', methods=['POST']) 
def lock_app():
    #RILEVATO lock_app() otteniamo dati dagli utenti
    __ERROR__ = 0
    global NOT_VALID, VALID, ERROR_STATION, ERROR_PLACE, SINGLE, LOCK_IN, LOCK_OUT, WRITE, READ
    request_packet=request.json
    print "PACKET RECEIVED in lock_app:"
    print(request_packet)
    
    station_id      = request_packet.get("station_id")
    place_id        = request_packet.get("place_id")
    security_key    = request_packet.get("security_key").replace(" ", "")
    registration_id = request_packet.get("registration_id")
    lock_flag       = request_packet.get("lock_flag")
    
    #Controllo condizioni posto
    status_found = request_processor.get_status_from_stationDB(place_id, station_id)
    
    if(status_found == 0):
        print "RILEVO POSTO LIBERO"
    elif(status_found == 1):
        print "RILEVO POSTO OCCUPATO"
        security_found = request_processor.controlPlace(place_id, station_id)
        print "security_found:" + security_found
    else:
        #CONDIZIONI DI ALLARME se status_found != 0/1
        pass
    
    print "lock_flag: "+str(lock_flag)
    if(int(lock_flag) == 1):
        #LOCK IN
        if(status_found == 0):
            #Settaggio lock flag in DB users
            request_processor.set_lock_flag(security_key, SINGLE, LOCK_IN, WRITE)
            #Settaggio campi dell'utente in DB users
            request_processor.set_user_place_station_id(security_key, place_id, station_id)
            #Dati non validi
            request_processor.set_data_valid(security_key, NOT_VALID)
            #Salva istante iniziale in db users
            request_processor.start_timer(security_key[0:25], security_key[25:50], 1)
            
            start_time = request_processor.start_timer(security_key[0:25], security_key[25:50], 0)
        else:
            __ERROR__ = 1        
    elif (int(lock_flag) == LOCK_OUT):
        #LOCK OUT
        if(status_found == 1):
            #Settaggio lock flag in DB users
            request_processor.set_lock_flag(security_key, SINGLE, LOCK_OUT, WRITE)
            #Dati non validi
            request_processor.set_data_valid(security_key, NOT_VALID)
            #Salva istante iniziale in db users
            request_processor.start_timer(security_key[0:25], security_key[25:50], 1)
            
            start_time = request_processor.start_timer(security_key[0:25], security_key[25:50], 0)
        else:
            __ERROR__ = 1
    
            
    
    return jsonify({"end": int(not(__ERROR__))})

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
    
    if parking_data['action']=="ALARM" and parking_data['security_key'] != "UNCHANGEABLE":
        #USE GOOGLE CLOUD MESSAGING API
        gcm = GCM(autorization_key)
        data = {'the_message': 'ALARM'}
        dataj = json.dumps(data)
        
        # plaintext request
        reg_id = parking_data['registration_id']
        print reg_id
        
        if(parking_data['registration_id'] != 'None'):
            gcm.plaintext_request(registration_id=reg_id, data=data)

        json_rasp = parking_data['action']
        #return jsonify({"station_id":parking_data['station_id'], "place_id": parking_data['place_id']})
    elif parking_data['action']=="OK" and parking_data['security_key'] != "UNCHANGEABLE":
        #USE GOOGLE CLOUD MESSAGING API
        gcm = GCM(autorization_key)
        data = {'the_message': 'OK'}
        dataj = json.dumps(data)
        
        # plaintext request
        reg_id = parking_data['registration_id']
        print reg_id        
        gcm.plaintext_request(registration_id=reg_id, data=data)
        
        json_rasp = parking_data['action']
    else:
        json_rasp = parking_data['action']
        
    return jsonify({"alarm": json_rasp})

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
    
    return jsonify({})

'''
the raspberry sends a json
{
    "station_id": "1",
    "tot_places": "5",
    "free_places": "1",
    "reset"      : "1"
}
'''
@app.route('/myBP_server/users/update_stationSpec', methods = ['POST'])
def update_station_spec():
    request_packet=request.json
    station_id = request_packet.get("station_id")
    free_places   = request_packet.get("free_places")
    tot_places = request_packet.get("tot_places")
    reset     = request_packet.get("reset")
    stn_updSpc = request_processor.stn_updStnSpc(station_id, free_places, tot_places)
    
    return jsonify({})


'''
json request

{
    "station_id" : "STATION_ID",
    "place_id"   : "PLACE_ID",
    "status"     : "STATUS"
}
'''
@app.route('/myBP_server/users/reset_after_alarm', methods = ['POST'])
def reset_after_alarm():
    request_packet = request.json
    station_id = request_packet.get("station_id")
    place_id   = request_packet.get("place_id")
    status = request_packet.get("status")
    
    request_processor.reset_users_after_alarm(station_id, place_id, status)
    
    return jsonify({"station_id": station_id, "place_id": place_id}) 

'''
json request

{
    "station_id" : "STATION_ID",
    "place_id"   : "PLACE_ID"
}
'''
@app.route('/myBP_server/users/reset_station', methods = ['POST'])
def reset_station():
    request_packet = request.json
    station_id = request_packet.get("station_id")
    place_id   = request_packet.get("place_id")
    
    request_processor.reset_station(station_id, place_id)
    
    return jsonify({"station_id": station_id, "place_id": place_id}) 

@app.route('/myBP_server/users/get_status_from_raspberry', methods = ['POST'])
def get_status_from_raspberry():
    request_packet = request.json
    print request_packet
    station_id = request_packet.get("station_id")
    place_id = request_packet.get("place_id")
    
    status = request_processor.get_status_from_stationDB(place_id, station_id)
    print "status: " +str(status)
    return jsonify({"status": status})

if __name__ == '__main__':
    app.run(host = '0.0.0.0', debug=True, port=7000)
