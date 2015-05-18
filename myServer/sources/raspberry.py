'''
Created on 14/mag/2015

@author: damiannew
'''

import MySQLdb

class raspberry:
    #request of lock in will change some fields of the row of the table 
    '''
    Firstly, it searches for the row of the table whit station_id and place_id, then if security_key is set to None the server sets it to UNCHANGEABLE.
    In this way when a request of lockin from an app occurs, if the security_key is UNCHANGEABLE, then the user doesn't lock in that place.
    '''
    def rqstlckin_db(self, station_id=1, place_id=2, status_free=1): 
        # Open database connection
        db = MySQLdb.connect("localhost","root", "myBP", "myBP_DB")
        # prepare a cursor object using cursor() method
        cursor = db.cursor()
        search_sql="SELECT security_key FROM station WHERE station_id='"+str(station_id)+"' AND place_id='"+str(place_id)+"';"
        print search_sql
        
        try:
            cursor.execute(search_sql)
            print "SEARCH SUCCESFUL COMPLETED"
        except:
            print "SEARCH ERROR"
        
        try:
            security_key=cursor.fetchone()
            print security_key
            if (security_key[0]=="None"): #if the timeout is over
                update_sql="UPDATE station SET security_key='UNCHANGEABLE' WHERE station_id='"+str(station_id)+"' AND place_id='"+str(place_id)+"';"
                print update_sql
            
                try:
                    cursor.execute(update_sql)
                    db.commit()
                    print "UPDATING SUCCESFUL COMPLETED [1]"
                except:
                    print "UPDATING ERROR"
            else: #if the user has locked in
                update_sql="UPDATE station SET status_free='"+str(status_free)+"'WHERE station_id='"+str(station_id)+"' AND place_id='"+str(place_id)+"';"
                print update_sql
            
                try:
                    cursor.execute(update_sql)
                    db.commit()
                    print "UPDATING SUCCESFUL COMPLETED"
                except:
                    print "UPDATING ERROR"               
        except:
            print "ERROR FETCHING"
                
        # disconnect from server
        db.close()
        
    def stealing_controller(self, station_id, place_id):
        # Open database connection
        db = MySQLdb.connect("localhost","root", "myBP", "myBP_DB")
        # prepare a cursor object using cursor() method
        cursor = db.cursor()
        search_sql="SELECT security_key FROM station WHERE station_id='"+str(station_id)+"' AND place_id='"+str(place_id)+"';"
        print search_sql
        
        parking_data={}
        
        try:
            cursor.execute(search_sql)
            print "SEARCH SUCCESFUL COMPLETED"
        except:
            parking_data['error']="SEARCH_ERROR"
            print "SEARCH ERROR"
            
        try:
            row=cursor.fetchone()
            security_key=row[0]
            print row
            #the following if statement checks if the request is a theft
            if (security_key!="none"):
                #PROCEDURA ALLARME
                pass
            else:
                #NON C'È STEAL PERCHE HO PASSATO L'NFC
        except:
            parking_data['error']="FETCH_FAILED"
            
        pass
    
        # disconnect from server
        db.close()
        
    def __init__(self, params):
        '''
        Constructor
        '''
        