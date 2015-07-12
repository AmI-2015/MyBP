'''
Created on 14/mag/2015

@author: MyBP
'''

import MySQLdb
import time

class raspberry:
    #request of lock in will change some fields of the row of the table 
    '''
    Firstly, it searches for the row of the table with station_id and place_id, then if security_key is set to None the server sets it to UNCHANGEABLE.
    In this way when a request of lockin from an app occurs, if the security_key is UNCHANGEABLE, then the user doesn't lock in that place.
    '''
    def rqstlckin_db(self, station_id, place_id, status): 
        # Open database connection
        db = MySQLdb.connect("localhost","root", "myBP", "myBP_DB")
        # prepare a cursor object using cursor() method
        cursor = db.cursor()
        search_sql="SELECT security_key FROM station WHERE station_id="+str(station_id)+" AND place_id="+str(place_id)+";"
        print search_sql
        
        try:
            cursor.execute(search_sql)
            print "SEARCH SUCCESFUL COMPLETED [rqstlckin_db()]"
        except:
            print "SEARCH ERROR [rqstlckin_db()]"
        
        try:
            security_key=cursor.fetchone()
            print "security_key:"+str(security_key)
            
            start = time.time()
            #this loop wait for 10 seconds, namely it gives time to the user to lock his bike
            while(time.time()-start < 4):
                pass
            
            if (security_key[0]=="None"): #if the timeout is over
                update_sql="UPDATE station SET security_key='UNCHANGEABLE' WHERE station_id="+str(station_id)+" AND place_id="+str(place_id)+";"
                
                try:
                    cursor.execute(update_sql)
                    db.commit()
                    print "UPDATING SUCCESFUL COMPLETED [rqstlckin_db()]"
                except:
                    print "UPDATING ERROR [rqstlckin_db()]"
            else: #if the user has locked in
                update_sql="UPDATE station SET status="+str(status)+" WHERE station_id="+str(station_id)+" AND place_id="+str(place_id)+";"

                try:
                    cursor.execute(update_sql)
                    db.commit()
                    print "UPDATING SUCCESFUL COMPLETED [rqstlckin_db()]"
                except:
                    print "UPDATING ERROR [rqstlckin_db()]"               
        except:
            print "ERROR FETCHING [rqstlckin_db()]"
                
        # disconnect from server
        db.close()
        
    def stealing_controller(self, station_id, place_id):
        # Open database connection
        db = MySQLdb.connect("localhost","root", "myBP", "myBP_DB")
        # prepare a cursor object using cursor() method
        cursor = db.cursor()
        search_sql="SELECT security_key, stop_alarm, registration_id FROM station WHERE station_id="+str(station_id)+" AND place_id="+str(place_id)+";"

        parking_data={}
        
        try:
            cursor.execute(search_sql)
            print "SEARCH SUCCESFUL COMPLETED [stealing_controller()]"
        except:
            parking_data['error']="SEARCH_ERROR"
            print "SEARCH ERROR [stealing_controller()]"
            
        try:
            row=cursor.fetchone()
            security_key=row[0]
            stop_alarm = row[1]
            registration_id=row[2]
            #the following if statement checks if the request is a theft
            if (security_key!="none"):
                parking_data['station_id']=-1
                parking_data['place_id']=-1
                parking_data['registration_id']=registration_id
                parking_data['security_key']=security_key
                parking_data['stop_alarm']=stop_alarm
                parking_data['action']="ALARM"
                print parking_data['action']
            else:
                parking_data['station_id']=station_id
                parking_data['place_id']=place_id
                parking_data['registration_id']=registration_id
                parking_data['security_key']=security_key
                parking_data['stop_alarm'] = stop_alarm
                parking_data['action']="OK"
                print parking_data['action']
        except:
            parking_data['error']="FETCH_FAILED [stealing_controller()]"
    
        # disconnect from server
        db.close()
        
        return parking_data

    def check_alarm(self, station_id, place_id):
        stop_alarm = -1
        print stop_alarm
        # Open database connection
        db = MySQLdb.connect("localhost","root", "myBP", "myBP_DB")
        # prepare a cursor object using cursor() method
        cursor = db.cursor()
        search_sql="SELECT stop_alarm FROM station WHERE station_id="+str(station_id)+" AND place_id="+str(place_id)+";"
        
        try:
            cursor.execute(search_sql)
            print "SEARCH SUCCESFUL COMPLETED [check_alarm()]"
        except:
            stop_alarm = -1
            print "SEARCH ERROR [check_alarm()]"
        
        try:
            stop_alarm = cursor.fetchone()
        except:
            stop_alarm = -1
            print "FETCH ERROR [check_alarm()]"
        
        try:
            update_sql = "UPDATE station SET stop_alarm = 0 WHERE station_id="+str(station_id)+" and place_id ="+str(place_id)+";" 
            cursor.execute(update_sql)
            db.commit()
        except:
            print "UPDATE in station FAILED  [upd_checkAlarm()]"   
        
        db.close()  
        
        return stop_alarm
    
    
    def check_securityKey(self, station_id, place_id):
        # Open database connection
        db = MySQLdb.connect("localhost","root", "myBP", "myBP_DB")
        # prepare a cursor object using cursor() method
        cursor = db.cursor()
        search_sql="SELECT security_key FROM station WHERE station_id="+str(station_id)+" AND place_id="+str(place_id)+";"
        checker= -1
        
        try:
            cursor.execute(search_sql) 
            print "SEARCH SUCCESFUL COMPLETED [check_securityKey()]"
        except:
            security_key = -1    
            print "SEARCH ERROR [check_securityKey()]"
        
        try:
            security_key= cursor.fetchone()
        except:
            checker = -1
            print "FETCH ERROR [check_securityKey()]"
        
        print "SECURIT_KEY[0]:"+security_key[0]
        if security_key[0] != 'None' and security_key[0] != 'UNCHANGEABLE' :
            checker = 1
        elif security_key[0] == 'None' or security_key[0] == 'UNCHANGEABLE':
            checker = 0
        else:
            checker = -1 
        
        db.close()  
        return checker
    
    def upd_stnSpcTbl(self, station_id, place_id, status):
        # Open database connection
        db = MySQLdb.connect("localhost","root", "myBP", "myBP_DB")
        # prepare a cursor object using cursor() method
        cursor  = db.cursor()
        search_stnSpc_sql = "SELECT free_places, tot_places FROM station_spec WHERE station_id='"+str(station_id)+"';"
        
        response_data = {}
        
        try:
            cursor.execute(search_stnSpc_sql)
            print "SEARCH SUCCESFUL COMPLETED [upd_stnSpcTbl()]"
        except:
            stop_alarm = -1
            print "SEARCH ERROR [upd_stnSpcTbl()]]"

        try:
            row=cursor.fetchone()
            free_places=int(row[0])
            tot_places = int(row[1])
            
            if int(status) == 1:
                free_places = free_places - 1
                update_station_spec = "UPDATE station_spec SET free_places = '"+str(free_places)+"' WHERE station_id='"+str(station_id)+"';" 
                try:
                    cursor.execute(update_station_spec)
                    db.commit()
                    print "UPDATING SUCCESFUL COMPLETED [upd_stnSpcTbl()]"
                except:
                    print "UPDATING ERROR [upd_stnSpcTbl()]"
            elif int(status) == 0:
                free_places = free_places + 1
                update_station_spec  = "UPDATE station_spec SET free_places = '"+str(free_places)+"' WHERE station_id='"+str(station_id)+"';" 
                try:
                    cursor.execute(update_station_spec)
                    db.commit()
                    print "UPDATING SUCCESFUL COMPLETED [upd_stnSpcTbl()]"
                except:
                    status = -1
                    print "UPDATING ERROR [upd_stnSpcTbl()]"            
            else:
                print "Updating in [upd_stnSpcTbl()] not done... retry"    
            print row
            #the following if statement checks if the request is a theft

        except:
            print "FETCH_FAILED [upd_stnSpcTbl()]"
        
        #####################################
        
        try:
            update_sql = "UPDATE station SET status = '"+str(status)+"' WHERE station_id= '"+str(station_id)+"' and place_id = '"+str(place_id)+"';" 
            print update_sql
            cursor.execute(update_sql)
            db.commit()
            print status
            if(status==0):
                update_station_sql="UPDATE station SET security_key='None', stop_alarm=0, registration_id='None'  WHERE station_id= '"+str(station_id)+"' AND place_id= '"+str(place_id)+"';"
                cursor.execute(update_station_sql)
                db.commit()
        except:
            print "UPDATE in station FAILED  [upd_stnSpcTbl()]"   
              
        db.close()
        
        response_data['station_id']  = station_id
        response_data['tot_places']  = tot_places
        response_data['free_places'] = free_places
        
        return response_data
    
    def upd_stationTbl(self, station_id, place_id, status):
        # Open database connection
        db = MySQLdb.connect("localhost","root", "myBP", "myBP_DB")
        # prepare a cursor object using cursor() method
        cursor  = db.cursor()

        try:
            if int(status) == 1:
                pass
            elif int(status) == 0:
                update_station  = "UPDATE station SET security_key = 'None', registration_id = 'None' WHERE station_id='"+str(station_id)+"' and place_id='"+str(place_id)+"';" 
                try:
                    cursor.execute(update_station)
                    db.commit()
                    print "UPDATING SUCCESFUL COMPLETED [upd_stnSpcTbl()]"
                except:
                    status = -1
                    print "UPDATING ERROR [upd_stnSpcTbl()]"            
            else:
                print "Updating in [upd_stnSpcTbl()] not done... retry"    
            #the following if statement checks if the request is a theft

        except:
            print "DB_ACCESS_FAILED [upd_stationTbl()]"
        
        #####################################
        
        try:
            update_sql = "UPDATE station SET status = '"+str(status)+"' WHERE station_id= '"+str(station_id)+"' and place_id ='"+str(place_id)+"';" 
            print update_sql
            cursor.execute(update_sql)
            db.commit()
            print status
            if(status==0):
                update_station_sql="UPDATE station SET security_key='None', stop_alarm=0, registration_id='None'  WHERE station_id= '"+str(station_id)+"' AND place_id= '"+str(place_id)+"';"
                cursor.execute(update_station_sql)
                db.commit()
        except:
            print "UPDATE in station FAILED  [upd_stnSpcTbl()]"   
              
        db.close()

    def __init__(self):
        pass