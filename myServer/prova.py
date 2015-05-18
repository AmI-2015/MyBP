'''
Created on 18/mag/2015

@author: damiannew
'''
import MySQLdb

def rqstlckin_db(station_id, place_id, status_free): 
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
        
if __name__ == '__main__':
    rqstlckin_db(1, 2, 1)