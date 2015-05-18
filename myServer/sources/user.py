'''
Created on 04/mag/2015

@author: MyBP
'''

import MySQLdb
import error

#####################################################################
class user:
    def __init__(self):
            
        # the username code
        self.username_code=None
        # the password code
        self.pwd_code=None
        #package the data from database in a dictionary
        self.data = self.getdata()
        
        
    def set_secret_code(self, code):
        self.username_code=code[0:2]
        self.pwd_code=code[2:4] 
             
    def getdata(self):
        user_data={}
        
        #secret code of the user
        user_data['username_code']=None
        user_data['pwd_code']=None
        #we set defaul values    
        #bike station
        user_data['myBP_station']=-1
            
        #bike place
        user_data['myBP_board']=-1
        
        #user status on the board
        user_data['myBP_status']=-1
        #schedule to insert
        
        user_data['error_str']="NO_ERROR"
        return user_data
    #connect_signin_db() is called whenever a request of sign in is received        

'''
if any exceptions occur the station_id and place_id are forced to -1
'''    
    def connect_signin_db(self):
        # Open database connection
        db = MySQLdb.connect("localhost","root", "myBP", "myBP_DB")
        user_data={}   
        # prepare a cursor object using cursor() method
        cursor = db.cursor()
    
        sql="SELECT * FROM users where username_code="+"'"+self.username_code+"' and pwd_code='"+self.pwd_code+"';"
        print sql
        cursor.execute(sql)
    
        try:
            data = cursor.fetchone()
                
            if(data != None):
                user_data['username_code']=data[0]
                user_data['pwd_code']=data[1]
                user_data['myBP_station']= int(data[2])
                user_data['myBP_board']=int(data[3])
                user_data['myBP_status']=data[4]
                user_data['error_str']=data[5]
                # Now print fetched result to debug
                print user_data 
            else:
                user_data=error.error_sign_in(self.username_code, self.pwd_code)
        except:
            print "(!)Error: unable to fetch data"
            
        # disconnect from server
        db.close()
        return user_data
    
    #connect_signup_db() is called whenever a sign up request is RECEIVED_SHUTDOWN
    def connect_signup_db(self):
        # Open database connection
        db = MySQLdb.connect("localhost","root", "myBP", "myBP_DB")
        user_data=self.data
        user_data['username_code']=self.username_code
        user_data['pwd_code']=self.pwd_code
        # prepare a cursor object using cursor() method
        cursor = db.cursor()
        insert_sql="INSERT INTO users VALUES ("+"'"+str(user_data['username_code'])+"','"+str(user_data['pwd_code'])+"',"+str(user_data['myBP_station'])+","+str(user_data['myBP_board'])+",'"+str(user_data['myBP_status'])+"','"+str(user_data['error_str'])+"');"
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
            print "(!!)unable to fetch data"
            
        db.close()
        return user_data
    
    def connection_stationDB(self, station_id, place_id, security_keyFromApp):
        db = MySQLdb.connect("localhost", "root", "myBP", "myBP_DB")
        cursor = db.cursor()
        parking_data={}
        
        search_sql="SELECT security_key FROM station WHERE station_id='"+str(station_id)+"' AND place_id='"+str(place_id)+"';"
        print search_sql
        
        try:
            cursor.execute(search_sql)
            print "SEARCHING SUCCESFUL COMPLETED"
        except:
            print "SEARCHING ERROR"
        
        try:
            security_key=cursor.fetchone()
            print security_key
            
            if (security_key[0]=="none"):
                parking_data['station_id'] = station_id
                parking_data['place_id']   = place_id
                update_sql="UPDATE station SET security_key='"+security_keyFromApp+"' WHERE station_id='"+str(station_id)+"' AND place_id='"+str(place_id)+"';"
            elif (security_key[0]==security_keyFromApp):
                update_sql="UPDATE station SET security_key='none' WHERE station_id='"+str(station_id)+"' AND place_id='"+str(place_id)+"';"
                update_sql="UPDATE station SET status_free=0 WHERE station_id='"+str(station_id)+"' AND place_id='"+str(place_id)+"';"              
            else:
                parking_data['station_id'] = -1
                parking_data['place_id']   = -1
        except:
            parking_data['station_id'] = -1
            parking_data['place_id']   = -1
              
        return parking_data 