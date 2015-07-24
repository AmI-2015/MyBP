import os
import RPi.GPIO as GPIO

start    = 40
shutdown = 38

GPIO.setwarnings(False)
GPIO.setmode(GPIO.BOARD)
GPIO.setup(start,GPIO.IN, pull_up_down=GPIO.PUD_UP) #setup accensione
GPIO.setup(shutdown,GPIO.IN, pull_up_down=GPIO.PUD_UP) #setup spegnimento

def main():
    if(int(not(GPIO.input(start)))==1):
        os.system('sudo python /home/pi/Desktop/Raspberry/Raspberry.py')
    if(int(not(GPIO.input(shutdown)))==1):
        os.system('sudo killall 8223')
    while(1):
        pass

if __name__ == '__main__':
    main()