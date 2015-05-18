'''
Created on 05/mag/2015

@author: damiannew
'''
import json

data = [ { 'a':'A', 'b':(2, 4), 'c':3.0 } ]
print 'DATA:', repr(data)

data_string = json.dumps(data)
print 'JSON:', data_string

if __name__ == '__main__':
    pass