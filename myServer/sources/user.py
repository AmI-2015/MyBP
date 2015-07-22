'''
Created on 04/mag/2015

@author: MyBP
'''
import error, time
import MySQLdb

#####################################################################
class user:
    def __init__(self):
            
        # the username code
        self.username_code=None
        # the password code
        self.pwd_code=None
        #package the data from database in a dictionary
        self.data = self.getdata()
         
    def set_userandpwd(self, user_code, pwd_code):
        self.username_code=user_code
        self.pwd_code=pwd_code
             
    def getdata(self):
        user_data={}
        
        #secret code of the user
        user_data['username_code']=None
        user_data['pwd_code']=None
        user_data['registration_id']=None
        #we set defaulT values    
        #bike station
        user_data['station_id']=-1
            
        #bike place
        user_data['place_id']=-1
        
        #user status on the board
        user_data['status'] = 0
        #schedule to insert
        
        user_data['error_str']="NO_ERROR"
        return user_data
    #connect_signin_db() is called whenever a request of sign in is received        

    def set_lock_flag(self, security_key, all, lock_flag, read):
        db = MySQLdb.connect("localhost","root", "myBP", "myBP_DB")
        user_data={}   
        cursor = db.cursor()
        
        if(all == 0):
            if(read ==0):
                try:
                    update_lock_flag="UPDATE users SET lock_flag='"+str(lock_flag)+"' WHERE username_code='"+security_key[0:25]+"' and pwd_code='"+security_key[25:50]+"';"    
                    print update_lock_flag
                    cursor.execute(update_lock_flag)
                    db.commit()
                    print "UPDATING lock_flag SUCCESFULLY COMPLETED"
                except:
                    print "UPDATING lock_flag ERROR [set_lock_flag]"
            else:
                try:
                    update_lock_flag="SELECT lock_flag FROM users WHERE username_code='"+security_key[0:25]+"' and pwd_code='"+security_key[25:50]+"';"    
                    print update_lock_flag
                    cursor.execute(update_lock_flag)
                    print "READING lock_flag SUCCESFULLY COMPLETED"
                except:
                    print "READING lock_flag ERROR [set_lock_flag]"
                    
                try:
                    lock_flag_fetched=cursor.fetchone()
                    print lock_flag_fetched
                    lock_flag = lock_flag_fetched[0]
                    print "FETCH in reading DONE"
                except:
                    print "FETCH in reading FAILED"
        else:
            try:
                update_lock_flag="UPDATE users SET lock_flag='"+str(lock_flag)+"';"    
                cursor.execute(update_lock_flag)
                db.commit()
                print "UPDATING lock_flag SUCCESFULLY COMPLETED"
            except:
                print "UPDATING lock_flag ERROR [set_lock_flag]"
        
        db.close()
        
        return lock_flag
    '''
    if any exceptions occur the station_id and place_id are forced to -1
    '''    
    def connect_signin_db(self, registration_id):
        # Open database connection
        db = MySQLdb.connect("localhost","root", "myBP", "myBP_DB")
        user_data={}   
        # prepare a cursor object using cursor() method
        cursor = db.cursor()
    
        try:
            sql="SELECT * FROM users where username_code='"+self.username_code+"' and pwd_code='"+self.pwd_code+"';"
            print sql
            cursor.execute(sql)
        except:
            print "ERROR IN SEARCH connect_signin_db()"
            
        try:
            data = cursor.fetchone()
                
            if(data != None):
                user_data['username_code']=data[0]
                user_data['pwd_code']=data[1]
                user_data['registration_id']=data[2]
                user_data['status']=int(data[3])
                user_data['station_id']= int(data[4])
                user_data['place_id']=int(data[5])
                user_data['lock_flag']=data[6]
                user_data['start_time']=data[7]
                user_data['error_str']=data[8]
                user_data['data_valid'] = data[9]
                # Now print fetched result to debug
                try:
                    update_sql="UPDATE users SET station_id='"+str(user_data['station_id'])+"', place_id='"+str(user_data['place_id'])+"' WHERE username_code='"+str(user_data['username_code'])+"' AND pwd_code='"+str(user_data['pwd_code'])+"';"
                    print update_sql
                    cursor.execute(update_sql)
                    db.commit()
                    print "successful updated station_id, place_id"
                except:
                    print "station_id, place_id updating failed"
                    user_data['place_id']=-1
                    user_data['station_id']=-1
                ################################################################################
                # REGISTRATION ID HAS TO BE UPDATED TO TALK TO THE GCM (GOOGLE CLOUD MESSAGING)#
                # IF AN UPDATING ERROR OCCURS REGISTRATION_ID IS SET TO -1                     #
                ################################################################################
                if(user_data['registration_id']!=registration_id):
                    update_sql="UPDATE users SET registration_id='"+registration_id+"' WHERE username_code='"+str(self.username_code)+"' AND pwd_code='"+str(self.pwd_code)+"';"
                    user_data['registration_id']=registration_id
                    
                    try:
                        cursor.execute(update_sql)
                        db.commit()
                        print "successful updated registration_id"
                    except:
                        print "registration_id updating failed"
                        user_data['registration_id']=-1
            else:
                user_data=error.error_sign_in(self.username_code, self.pwd_code)
        except:
            print "Error: unable to fetch data [connect_signin_db()]"
            
        # disconnect from server
        db.close()
        return user_data
    
    #connect_signup_db() is called whenever a sign up request is RECEIVED_SHUTDOWN
    def connect_signup_db(self,registration_id):
        # Open database connection
        db = MySQLdb.connect("localhost","root", "myBP", "myBP_DB")
        user_data=self.data
        user_data['username_code']=self.username_code
        user_data['pwd_code']=self.pwd_code
        user_data['registration_id']=registration_id
        # prepare a cursor object using cursor() method
        cursor = db.cursor()
        insert_sql="INSERT INTO users VALUES ("+"'"+str(user_data['username_code'])+"','"+str(user_data['pwd_code'])+"',"+str(user_data['station_id'])+","+str(user_data['place_id'])+",'"+str(user_data['status'])+"','"+str(user_data['registration_id'])+"','"+str(user_data['error_str'])+"', '0', '0', '0');"
        search_sql="SELECT username_code FROM users WHERE username_code='"+self.username_code+"'"
        print insert_sql
        cursor.execute(search_sql)
        
        try:
            data=cursor.fetchone()
            print data
            if data==None:
                print insert_sql
                cursor.execute(insert_sql)
                db.commit()
                
            else:
                print "It already exists\n"
                user_data=error.error_alreadyExist(self.username_code, self.pwd_code)
        except:
            print "unable to fetch data [connect_sign_up_db()]"
            
        db.close()
        return user_data
    
    def controlPlace(self, place_id, station_id):
        db = MySQLdb.connect("localhost", "root", "myBP", "myBP_DB")
        cursor = db.cursor()
        
        try:
            search_sql="SELECT security_key FROM station WHERE place_id='"+str(place_id)+"' and station_id ='"+str(station_id)+"';"
            print search_sql
            cursor.execute(search_sql)
        except:
            print "Search problem in controlPlace"
            
        try:
            security_key=cursor.fetchone()
            print security_key
        except:
            print "unable to fetch data [controlPlace()]"
            
        db.close()
        return security_key[0]
    
    def set_start_time(self, user_code, pwd_code, set_):
        db = MySQLdb.connect("localhost", "root", "myBP", "myBP_DB")
        cursor = db.cursor()
        start_time = time.time()
        print "set: "+ str(set_)
        if set_ == 1:
            start_time = time.time()
            print start_time
            try:
                update_users_sql = "UPDATE users SET start_time='"+str(start_time)+"' WHERE username_code='"+user_code+"' and pwd_code='"+pwd_code+"';"
                print update_users_sql
                cursor.execute(update_users_sql)
                db.commit()
                print "UPDATE  start_time SUCCESSFULLY COMPLETED IN start_timer"
            except:
                print "UPDATE ERROR in set_start_timer()"
        else:
            try:
                search_users_sql = "SELECT start_time FROM users WHERE username_code='"+user_code+"' and pwd_code = '"+pwd_code+"';"
                print search_users_sql
                cursor.execute(search_users_sql)
                print "SEARCH SUCCESSFULLY COMPLETED IN start_timer"
                try:
                    start=cursor.fetchone()
                    start_time = start[0]
                except:
                    print "start_time not found"
            except:
                print "UPDATE ERROR in start_timer()"
                
        db.close()
        
        return start_time
    
    def reset_DB(self, station_id, place_id, security_keyFromApp, registration_id):
        db = MySQLdb.connect("localhost", "root", "myBP", "myBP_DB")
        cursor = db.cursor()
        parking_data={}
        
        search_sql="SELECT security_key, status FROM station WHERE station_id='"+str(station_id)+"' AND place_id='"+str(place_id)+"';"
        print search_sql
        
        try:
            cursor.execute(search_sql)
            print "SEARCHING SUCCESFUL COMPLETED [connection_stationDB()]"
        except:
            print "SEARCHING ERROR [connection_stationDB()]"
        
        try:
            row=cursor.fetchone()
            security_key=row[0]
            status=int(row[1])
            print security_key
            if (security_key=="None" and status==0):  
                parking_data['station_id'] = station_id
                parking_data['place_id']   = place_id
                update_station_sql="UPDATE station SET security_key='"+security_keyFromApp+"', status = '1', stop_alarm='0',  registration_id='"+registration_id+"' WHERE station_id='"+str(station_id)+"' AND place_id='"+str(place_id)+"';"
                update_users_sql = "UPDATE users SET status='1', place_id = '"+str(place_id)+"', station_id = '"+str(station_id)+"' WHERE username_code='"+security_keyFromApp[0:25]+"' AND pwd_code='"+security_keyFromApp[25:50]+"';"
                try:
                    search_in_station_spec = "SELECT free_places FROM station_spec WHERE station_id ='"+str(station_id)+"';"
                    cursor.execute(search_in_station_spec)
                    print "SEARCH SUCCESSFULLY COMPLETED IN connection_stationDB()"
                except:
                    print "SEARCH ERROR in station_spec [connection_stationDB()]"
                try:
                    free_places = cursor.fetchone()
                    int_free_places = int(free_places[0])-1
                    update_station_spec = "UPDATE station_spec SET free_places = '"+str(int_free_places)+"' WHERE station_id='"+str(station_id)+"';"
                    print update_station_spec;
                    try:
                        cursor.execute(update_station_spec)
                        db.commit()
                    except:
                        "station_spec UPDATING ERROR in connection_statiodnDB()"
                except:
                    print "station_spec FETCH ERROR in connection_stationDB()"
                    
                try:
                    cursor.execute(update_station_sql)
                    cursor.execute(update_users_sql)
                    db.commit()
                except:
                    print "station NOT updated [connection_stationDB()]"
            elif (security_key == security_keyFromApp):
                update_station_sql="UPDATE station SET security_key='None', status=0, stop_alarm=1, registration_id='None'  WHERE station_id='"+str(station_id)+"' AND place_id='"+str(place_id)+"';"
                update_users_sql = "UPDATE users SET status=0, , place_id = '-1', station_id = '-1' WHERE username_code='"+security_keyFromApp[0:25]+"' AND pwd_code='"+security_keyFromApp[25:50]+"';"
                try:
                    search_in_station_spec = "SELECT free_places FROM station_spec WHERE station_id ='"+str(station_id)+"';"
                    cursor.execute(search_in_station_spec)
                except:
                    print "SEARCH ERROR in station_spec [connection_stationDB()]"
                    
                try:
                    free_places = cursor.fetchone()
                    int_free_places = int(free_places[0])+1
                    update_station_spec = "UPDATE station_spec SET free_places = '"+str(int_free_places)+"' WHERE station_id='"+str(station_id)+"';"
                    cursor.execute(update_station_spec)
                    db.commit()
                except:
                    print "station_spec NOT UPDATED in connection_stationDB()"
                
                cursor.execute(update_station_sql)
                cursor.execute(update_users_sql)
                db.commit()
                parking_data['station_id'] = station_id
                parking_data['place_id']   = place_id
            else:
                parking_data['status'] = -1
                parking_data['station_id'] = -1
                parking_data['place_id']   = -1
        except:
            print "FETCH EXCEPTION"
            parking_data['status'] = -1
            parking_data['station_id'] = -1
            parking_data['place_id']   = -1
    
        db.close()
     
    def search_place_id_station_id(self, station_id, place_id, security_key):
        db = MySQLdb.connect("localhost", "root", "myBP", "myBP_DB")
        cursor = db.cursor()
        
        try:
            update_sql = "SELECT place_id, station_id FROM users WHERE username_code='"+str(username_code)+"', pwd_code = '"+str(station_id)+"' WHERE username_code='"+security_key[0:25]+"' and pwd_code='"+security_key[25:50]+"';"
            print update_sql
            cursor.execute(update_sql)
            db.commit()
            print "UPDATE SET DONE IN set_place_station_id()"
        except:
            print "UPDATE SET FAILED IN set_place_station_id()"
         
        try:
            search_sql="SELECT place_id, station_id FROM users WHERE username_code='"+security_key[0:25]+"' AND pwd_code='"+security_key[25:50]+"';"
            cursor.execute(search_sql)
            print "SEARCH DONE in set_place_station_id()"
        except:
            print "SEARCH FAILED in set_place_station_id()"
            
        parking_data = {}
        try:
            row=cursor.fetchone()
            parking_data['place_id'] = row[0]
            parking_data['station_id'] =row[1]
        except:
            print "FETCH FAILED IN set_place_station_id()"
        db.close()
        print parking_data
        return parking_data
     
    def set_security_key_in_user(self, station_id, place_id, reset):
        db = MySQLdb.connect("localhost", "root", "myBP", "myBP_DB")
        cursor = db.cursor()
        
        try:
            search_sql="SELECT username_code, pwd_code FROM users WHERE station_id='"+str(station_id)+"' AND place_id='"+str(place_id)+"';"
            cursor.execute(search_sql)
            print "SEARCH DONE in set_security_key_in_user()"
        except:
            print "SEARCH FAILED in set_security_key_in_user()"
            
        try:
            row=cursor.fetchone()
            security_key = row[0]+row[1]
            print "security_key fetchata:"+security_key
        except:
            print "FETCH FAILED IN set_security_key_in_user()"
         
        if(reset == 0):
            pass
        elif(reset ==1):
            try:
                update_sql = "UPDATE users SET place_id='"+str(place_id)+"', station_id = '"+str(station_id)+"', status= '0' WHERE security_key='"+security_key+"';"
                print update_sql
                cursor.execute(update_sql)
                db.commit()
                print "UPDATE RESET DONE IN set_security_key_in_user()"
            except:
                print "UPDATE RESET FAILED IN set_security_key_in_user()"
            security_key = 'None'
        else:
            try:
                update_sql = "UPDATE users SET place_id='-1', station_id = '-1', status= '0' WHERE security_key='"+security_key+"';"
                print update_sql
                cursor.execute(update_sql)
                db.commit()
                print "UPDATE RESET DONE IN set_security_key_in_user()"
            except:
                print "UPDATE RESET FAILED IN set_security_key_in_user()"
            security_key = 'None'
            
        fetch_ras_flag = " SELECT ras_flag FROM users WHERE username_code='"+row[0]+"' and pwd_code = '"+row[1]+"';"
        
        try:
            cursor.execute(fetch_ras_flag)
            print "SEARCHING SUCCESFUL ras_flag COMPLETED [set_security_key()]"
        except:
            print "SEARCHING ras_flag ERROR [set_security_key()]"
        
        try:
            row=cursor.fetchone()
            ras_flag = row[0]
        except:
            print "fetch exception"
            
        #if(ras_flag==1):
        try:
            update_sql = "UPDATE station SET security_key='"+security_key+"' WHERE place_id='"+str(place_id)+"' and station_id = '"+str(station_id)+"';"
            print update_sql
            cursor.execute(update_sql)
            db.commit()
            print "UPDATE DONE IN set_security_key_in_user()"
        except:
             print "UPDATE FAILED IN set_security_key_in_user()"
        
        db.close()
        pass       
    
    def connection_stationDB(self, station_id, place_id, security_keyFromApp, registration_id, lock_flag):
        db = MySQLdb.connect("localhost", "root", "myBP", "myBP_DB")
        cursor = db.cursor()
        parking_data={}
        
        search_sql="SELECT security_key, status FROM station WHERE station_id='"+str(station_id)+"' AND place_id='"+str(place_id)+"';"
        print search_sql
        fetch_ras_flag = "SELECT ras_flag FROM users WHERE username_code='"+security_keyFromApp[0:25]+"' and pwd_code = '"+security_keyFromApp[25:50]+"'"
        
        try:
            cursor.execute(fetch_ras_flag)
            print "SEARCHING SUCCESFUL ras_flag COMPLETED [connection_stationDB()]"
        except:
            print "SEARCHING ras_flag ERROR [connection_stationDB()]"
        
        try:
            row=cursor.fetchone()
            ras_flag = row[0]
        except:
            print "fetch exception"
                        
        try:
            cursor.execute(search_sql)
            print "SEARCHING SUCCESFUL COMPLETED [connection_stationDB()]"
        except:
            print "SEARCHING ERROR [connection_stationDB()]"
        
        try:
            row=cursor.fetchone()
            security_key=row[0]
            status=int(row[1])
            print "security_key: " +security_key
            print "security_keyFromApp: "+security_keyFromApp
            print "status: "+str(status)
            print "ras_flag: "+str(ras_flag)
            if (lock_flag == 1):
                print "STO FACENDO IL LOCK IN"
                parking_data['station_id'] = station_id
                parking_data['place_id']   = place_id
                parking_data['status'] = 1
                update_station_sql="UPDATE station SET security_key='"+security_keyFromApp+"', status = '"+str(parking_data['status'])+"', stop_alarm='0',  registration_id='"+registration_id+"' WHERE station_id='"+str(station_id)+"' AND place_id='"+str(place_id)+"';"
                update_users_sql = "UPDATE users SET status='"+str(parking_data['status'])+"', place_id = '"+str(station_id)+"', station_id = '"+str(place_id)+"' WHERE username_code='"+security_keyFromApp[0:25]+"' AND pwd_code='"+security_keyFromApp[25:50]+"';"
                parking_data['lock'] = 1 #lock IN
                
            elif (lock_flag == -1):
                print "Sono in LOCK OUT"
                parking_data['status'] = 0
                update_station_sql="UPDATE station SET security_key='None', status=0, stop_alarm=1, registration_id='None'  WHERE station_id='"+str(station_id)+"' AND place_id='"+str(place_id)+"';"
                update_users_sql = "UPDATE users SET status= '"+str(parking_data['status'])+"', place_id = '"+str(place_id)+"', station_id = '"+str(station_id)+"' WHERE username_code='"+security_keyFromApp[0:25]+"' AND pwd_code='"+security_keyFromApp[25:50]+"';"
                parking_data['lock'] = 0 #lock OUT
                parking_data['station_id'] = station_id
                parking_data['place_id'] = place_id
            else:
                
                parking_data['status']     = status
                parking_data['station_id'] = station_id
                parking_data['place_id']   = place_id
                parking_data['lock']     = 0
        except:
            print "FETCH EXCEPTION"
            parking_data['status']     = 0
            parking_data['station_id'] = -1
            parking_data['place_id']   = -1
            parking_data['lock']     = -1
            
        '''
        try:
            update_sql="UPDATE station SET  stop_alarm=0 WHERE station_id='"+str(station_id)+"' AND place_id='"+str(place_id)+"';"
            cursor.execute(update_sql)
            db.commit()
        except:
            print "UPDATING ERROR in [stealing_controller()]"     
        '''
        db.close() 
        return parking_data
    
    def stn_spcDB(self):
        db = MySQLdb.connect("localhost", "root", "myBP", "myBP_DB")
        cursor = db.cursor()
        
        stnSpec_list = []
        
        search_sql = "SELECT * FROM station_spec ;"
        
        try:
            tot_line = cursor.execute(search_sql)
            print "SEARCHING SUCCESFUL COMPLETED [stn_spcDB()]"
        except:
            print "SEARCHING ERROR"
            
        try:
            line = cursor.fetchall()
            
            for i in range (0, (tot_line)):
                stnSpec_list.append({'station_id': line[i][0], 'latitude': line[i][1], 'longitude': line[i][2], 'tot_places': line[i][3], 'free_places': line[i][4]})
            
        except:
            print "FETCH FAILED [stn_specDB()]"
             
        db.close()
        return stnSpec_list 
    
    def stop_alarm_fromApp(self, station_id, place_id):
        stop_alarm = -1
        # Open database connection
        db = MySQLdb.connect("localhost","root", "myBP", "myBP_DB")
        # prepare a cursor object using cursor() method
        cursor = db.cursor()
        search_sql="SELECT stop_alarm FROM station WHERE station_id="+str(station_id)+" AND place_id="+str(place_id)+";"
        print search_sql
        
        try:
            cursor.execute(search_sql)
            print "SEARCH SUCCESFUL COMPLETED [check_alarm_fromApp()]"
        except:
            stop_alarm = -1
            print "SEARCH ERROR [check_alarm_fromApp()]"
        
        try:
            stop_alarm = cursor.fetchone()
            print "stop_alarm (after fetching):"+str(stop_alarm)
        except:
            stop_alarm = -1
            print "FETCH ERROR [check_alarm_fromApp()]"
        
        try:
            update_sql = "UPDATE station SET stop_alarm = '1' WHERE station_id='"+str(station_id)+"' and place_id ='"+str(place_id)+"';" 
            update_users = "UPDATE users SET status = '0' WHERE station_id='"+str(station_id)+"' and place_id = '"+str(place_id)+"';"
            print update_sql
            cursor.execute(update_sql)
            cursor.execute(update_users)
            db.commit()
        except:
            print "UPDATE in station FAILED  [check_alarm_fromApp()]"    
        
        db.close()  
        
    
        return stop_alarm
