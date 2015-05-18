'''
Created on 04/mag/2015

@author: MyBP
'''
import request_processor
from flask import Flask, jsonify, request

app= Flask(__name__)

#the following method is to sign in 
'''
header json 
{
    "user_code": "username",
    "pwd_code":  "password"
}
'''
@app.route('/myBP_server/users/sign_in', methods=['POST'])
def sign_in():
    secret_packet = request.json
    print(secret_packet)    #print the packet, just for debugging
    
    pwd_code=secret_packet.get('pwd_code')
    user_code=secret_packet.get('user_code')
    #pwd_code and user_code has to be at fixed length
    secret_code=user_code+pwd_code
    print user_code
    print pwd_code
    print secret_code
    user_data=request_processor.sign_in(secret_code)
    

    return jsonify({"pwd_code": user_data['pwd_code'], "error_str": user_data['error_str']})
    
#the following method is to sign up
'''
header json 
{
    "user_code": "username",
    "pwd_code":  "password"
}
'''
@app.route('/myBP_server/users/sign_up', methods=['POST'])
def sign_up():
    secret_packet = request.json
    print(secret_packet)
    
    pwd_code=secret_packet.get('pwd_code')
    user_code=secret_packet.get('user_code')
    secret_code=user_code+pwd_code
    
    user_data=request_processor.sign_up(secret_code)
    return jsonify({"pwd_code": user_data['pwd_code'], "error_str": user_data['error_str']})
    
#the following method is to get the settings
'''
header json 
{
    "user_code": "username",
    "pwd_code":  "password"
}
'''
@app.route('/myBP_server/users/get_status', methods=['POST'])
def  get_settings():
    secret_packet=request.json
    print(secret_packet)
    user_data={}
    
    pwd_code=secret_packet.get('pwd_code')
    user_code=secret_packet.get('user_code')
    secret_code=user_code+pwd_code
    
    user_data=request_processor.sign_in(secret_code)
    
    if(user_data['error_str']!="ERROR_SIGNIN"):
        return jsonify({"myBP_status": user_data['myBP_status']})
    else:
        return jsonify({"myBP_status": "ERROR_STATUS"})
    


#the following method is to log in "LOCK IN"
'''
example
header json
{
    "station_id": "ID_STATION",
    "place_id": "ID_PLACE",
    "status_free": "1"
}

This request comes from the Raspberry located on the station, it is sent after the computing of the TIME OUT
'''
@app.route('/myBP_server/users/lockin_ras', methods=['POST']) 
def lockin_raspberry():
    request_packet=request.json
    station_id=request_packet.get("station_id")
    place_id=request_packet.get("place_id")
    status_free=request_packet.get("status_free")
    
    request_processor.lockin_ras(station_id, place_id, status_free)
    
    pass

#the following method is to log in "LOCK IN" by app
'''
example
header json
{
    "station_id": "ID_STATION",
    "place_id": "ID_PLACE",
    "security_key": "xx"
}

This request comes from the Raspberry located on the station, it is sent after the computing of the TIME OUT

It returns an header json
{
    "station_id": "ID_STATION",    <--------- set -1 if the locked in it hasn't happened
    "place_id": "ID_PLACE",        <--------- set -1 if the locked in it hasn't happened
    "security_key": "xx"
}

OSSERVAZIONE:
BISOGNA MODIFICARE QUESTA FUNZIONE IN MODO CHE CONTROLLI SE A RICHIESTA E' DI LOCKIN O LOCKUP, QUESTO DEVE ESSERE FATTO CERCANDO NEL
DATABASE SE LA TABELLA station CONTIENE GIA' LA CASELLA security_key
'''
@app.route('/myBP_server/users/lock_app', methods=['POST']) 
def lock_app():
    request_packet=request.json
    station_id=request_packet.get("station_id")
    place_id=request_packet.get("place_id")
    security_key=request_packet.get("security_key")
    
    parking_data={}
    parking_data=request_processor.lock_app(station_id, place_id, security_key)
    
    return jsonify({"station_id":parking_data['station_id'], "place_id": parking_data['place_id'], "security_key": security_key})

'''
#the following method checks that a steal doesn't occur

the Raspberry sends a json header 
{
    "station_id": "ID_STATION",
    "place_id": "ID_PLACE",
    "error": "error"
}

if the allarm has to start the json response
{
    "station_id": -1,
    "place_id": -1,
    "error": "FATCH FAILED"
}
'''
@app.route('/myBP_server/users/stealing_controller', methods=['POST'])
def stealing_controller():
    request_packet=request.json
    station_id=request_packet.get("station_id")
    place_id=request_packet.get("place_id")
    status_free=request_packet.get("status_free")
    
    request_processor.stealing_controller(station_id, place_id)
    
if __name__ == '__main__':
    app.run(debug=True)