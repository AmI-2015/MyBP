'''
Created on 14/mag/2015

@author: damiannew
'''
import MySQLdb

def sql_scrpt():
    i=0
    j=0
    db = MySQLdb.connect("localhost","root", "myBP", "myBP_DB")
    cursor = db.cursor()
    for i in range (0, 20):
        if(i<10):
            insert_sql="INSERT INTO station VALUES ('1','"+str(i)+"','None', 'False');"
            print(insert_sql)
        else:
            j=j+1
            insert_sql="INSERT INTO station VALUES ('2','"+str(j)+"','None', 'False');"
            print(insert_sql)
        cursor.execute(insert_sql)
        db.commit()
    
    db.close()

if __name__ == '__main__':
    sql_scrpt()