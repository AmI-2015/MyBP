'''
Created on 10/mag/2015

@author: damiannew
'''
######################################################################
# ERRORE SIGN IN
######################################################################
def error_sign_in(username_code, pwd_code):
    data_error={}
    data_error['error_str']='ERROR_SIGNIN'
    data_error['username_code']=username_code
    data_error['pwd_code']=pwd_code
    data_error['myBP_station']=-1
    data_error['myBP_board']=-1
    data_error['myBP_status']=0

    return data_error

######################################################################
#ERRORE SIGNUP
######################################################################
def error_alreadyExist(username_code, pwd_code):
    data_error={}
    data_error['error_str']='ERROR_SIGNUP'
    data_error['username_code']=username_code
    data_error['pwd_code']=pwd_code
    data_error['myBP_station']=-1
    data_error['myBP_board']=-1
    data_error['myBP_status']=-1
    
    return data_error
    
    