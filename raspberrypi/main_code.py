# 메인코드
# This is a demo of running face recognition on a Raspberry Pi.
# This program will print out the names of anyone it recognizes to the console.

# To run this, you need a Raspberry Pi 2 (or greater) with face_recognition and
# the picamera[array] module installed.
# You can follow this installation instructions to get your RPi set up:
# https://gist.github.com/ageitgey/1ac8dbe8572f3f533df6269dab35df65

from PIL import Image
import os
import face_recognition
import picamera
import numpy as np
import os
import sys
import io
import datetime
import RPi.GPIO as GPIO
import time
import socket
import threading
import requests


# DB 설정
DB_HOST="http://52.79.155.171/DbLog.php"

# 소켓 설정

HOST='ec2-52-79-155-171.ap-northeast-2.compute.amazonaws.com'
PORT=9000
'''
HOST = '127.0.0.1'
PORT = 9999
'''
client_socket = socket.socket(socket.AF_INET,socket.SOCK_STREAM)
client_socket.connect((HOST, PORT))


# 부저 설정
scale = [262,294,330,349,392,440,494,523]

buzzer=18
GPIO.setwarnings(False)
GPIO.setmode(GPIO.BCM)
GPIO.setup(buzzer,GPIO.OUT)

# 초음파 설정
trig = 2
echo = 3
GPIO.setup(trig, GPIO.OUT)
GPIO.setup(echo, GPIO.IN)

# 모터 설정
in1 = 19
in2 = 13
en = 26
GPIO.setup(in1,GPIO.OUT)
GPIO.setup(in2,GPIO.OUT)
GPIO.setup(en,GPIO.OUT)

# 버튼 설정
button_pin = 4
GPIO.setup(button_pin, GPIO.IN, pull_up_down=GPIO.PUD_UP)
button_state=0 # 0이 열림, 1이 닫힘

# 카메라 설정
camera = picamera.PiCamera()
camera.resolution = (320, 240)
output = np.empty((240, 320, 3), dtype=np.uint8)

#while문 조건 설정
restart=1
while True:
    if (restart==1):
        restart=0
        # 변수 설정
        face_locations = []
        face_encodings = []
        
        replay=0
        count=0
        unknown_count=0
        seconds_to_run=5
        start_time=datetime.datetime.now()
        wave_count=0
        
        # 라즈베리 내에서 이미지를 받아와서 얼굴인식을 수행, 결과를 배열로 저장 마지막 print문은 서버쪽으로 초기설정 완료 신호 보내도?
        print("Loading known face image(s)")
        
        known_face_encodings = []
        known_face_names = []
        
        
        origin_dirname = 'origin_file'
        dirname = 'knowns'
        origin_files = os.listdir(origin_dirname)
        for filename in origin_files:
            os.remove('origin_file/'+filename)
        os.system('scp -i /home/doorlock/key77.pem ubuntu@52.79.155.171:/var/www/html/uploads/* /home/doorlock/origin_file')    
        files = os.listdir(dirname)
        for filename in files:
            os.remove('knowns/'+filename)
        # origin_file에서 원본영상들을 인코딩하여 knowns에 저장
        origin_files = os.listdir(origin_dirname)
        for filename in origin_files:
            img= Image.open('origin_file/'+filename).convert('RGB')
            name, ext = os.path.splitext(filename)
            img_size=img.size
            image = img.resize((int(img_size[0]*(0.3)), int(img_size[1]*(0.3))))
            image=image.rotate(270)
            image.save('knowns/'+filename)
        # knowns이미지 받아와서 얼굴인식 수행
        files = os.listdir(dirname)
        for filename in files:
            try:
                name, ext = os.path.splitext(filename)
                known_face_names.append(name)
                pathname = os.path.join(dirname, filename)
                img = face_recognition.load_image_file(pathname)
                face_encoding = face_recognition.face_encodings(img)[0]
                known_face_encodings.append(face_encoding)
            except:
                pass
        print(known_face_names)
        '''                
        obama_image = face_recognition.load_image_file("image.jpg")
        obama_face_encoding = face_recognition.face_encodings(obama_image)[0]
        '''
            
    # 초음파로 일정 거리 이상 가까워지면 아래 코드로 넘어간다 or 서버에서 사진이 전송되면 continue로 다시 처음으로 돌아간다
    while True:
        
        # 내부에서 버튼으로 모터제어
        # 열림
        input_state = GPIO.input(button_pin)
        if input_state == True and button_state == 0:
            GPIO.output(in1,True)
            GPIO.output(in2,False)
            GPIO.output(en,True)
            time.sleep(0.06)
            GPIO.output(in1,False)
            GPIO.output(in2,False)
            GPIO.output(en,False)
            button_state=1
            time.sleep(1)
        # 닫힘
        elif input_state == True and button_state == 1:
            GPIO.output(in1,False)
            GPIO.output(in2,True)
            GPIO.output(en,True)
            time.sleep(0.06)
            GPIO.output(in1,False)
            GPIO.output(in2,False)
            GPIO.output(en,False)
            button_state=0
            time.sleep(1)
        
        # 초음파 출력 실패 시 0.5초의 시간을 가지고 다시 전송
        GPIO.output(trig, False)
        time.sleep(0.5)
        # 초음파 전송 성공 시 0.001초 후에 정지, 이후 거리 측정
        GPIO.output(trig, True)
        time.sleep(0.001)
        GPIO.output(trig, False)
        
        while GPIO.input(echo) == 0:
            pulse_start = time.time()
    	
        while GPIO.input(echo) == 1:
            pulse_end = time.time()
        
        pulse_duration = pulse_end - pulse_start
        distance = pulse_duration*17000
        distance = round(distance,2)
        print("거리",distance,"cm")
        
        # 초음파 30cm이내로 연속 3회 인식 시 짧은 부저음 후 얼굴인식으로 넘어감
        if (distance < 40):
            wave_count+=1
        else:
            wave_count=0
    	
        if (wave_count>=3):
            print("얼굴인식을 시작합니다")
            name_origin = "Capture Failed"
            name_compare=""
            pwm=GPIO.PWM(buzzer,scale[6])
            pwm.start(30)
            time.sleep(0.1)
            
            pwm.stop()
            
            time.sleep(1)
            break
        # 서버와 통신 부분, 성공시 continue로 시작으로 돌아가게 설정
        
        data = client_socket.recv(1024)
        msg=data.decode()
        print(msg)
        # 소켓 통신 수신 결과 origin폴더를 지우고 서버에서 사진을 다시 받아온다
        
        if (msg.find('file_download') != -1):
            replay=1
            break
        '''
            origin_files = os.listdir(origin_dirname)
            for filename in origin_files:
                os.remove('origin_file/'+filename)
            os.system('scp -i /home/doorlock/key77.pem ubuntu@52.79.155.171:/var/www/html/uploads/* /home/doorlock/origin_file')    
            files = os.listdir(dirname)
            for filename in files:
                os.remove('knowns/'+filename)
        '''
            
        # 소켓 통신 수신 결과 모터를 돌려 문을 연다.
        if (msg.find('motor_operate') != -1 and button_state==0):
            GPIO.output(in1,True)
            GPIO.output(in2,False)
            GPIO.output(en,True)
            time.sleep(0.06)
            GPIO.output(in1,False)
            GPIO.output(in2,False)
            GPIO.output(en,False)
            button_state=1
            
            pwm=GPIO.PWM(buzzer,100)
            pwm.start(30)
            for i in range(3):
                pwm.ChangeFrequency(scale[i+3])
                time.sleep(0.2)
    
            pwm.stop()

        if (msg.find('raspi_exit') != -1):
            client_socket.send(msg.encode())
            client_socket.close()
            sys.exit()
        
    if replay==1:
        replay=0
        restart=1
        continue
        
    
    # 실시간 얼굴 캡처 및 인식하는 부분
    while True:
        # 카메라 캡처 및 얼굴 인식 과정
        camera.capture(output, format="rgb")
    
        # Find all the faces and face encodings in the current frame of video
        face_locations = face_recognition.face_locations(output)
        print("Found {} faces in image.".format(len(face_locations)))
        face_encodings = face_recognition.face_encodings(output, face_locations)
        if len(face_encodings) == 0:
            pwm=GPIO.PWM(buzzer,262)
            pwm.start(50.0)
            time.sleep(1)
    
            pwm.stop()
            unknown_count=0
            count=0
            restart=0
            time.sleep(1)
            break
            
        
        # 라즈베리 내의 얼굴들과 비교
        for face_encoding in face_encodings:
            # disatances는 라즈베리내의 얼굴들과 현재 캡처한 얼굴과의 비교점수
            distances = face_recognition.face_distance(known_face_encodings, face_encoding)
            try:
                min_value = min(distances)
            except:
                pwm=GPIO.PWM(buzzer,262)
                pwm.start(50.0)
                time.sleep(1)
    
                pwm.stop()
                unknown_count=0
                count=0
                restart=0
                time.sleep(1)
                break
    
            # 정확도 비교하여 맞는 얼굴인지 판단, 아니면 unknowns로 설정
            if min_value < 0.4: #정확도 조절
                name_compare=name_origin
                index = np.argmin(distances)
                name_origin = known_face_names[index]
                unknown_count=0
                
            else:
                name_origin="Unknowns"
                unknown_count+=1
        # 얼굴인식 실패 시 capture failed로 설정
        if len(face_locations)==0:
            name_origin="Capture Failed"
        
        print(name_origin)
        count+=1
        # 미등록 얼굴이 2회 성공 시 실패 및 부저음
        if name_origin=="Unknowns" and unknown_count==3:
            in_out_fail="잠금해제 실패(미등록 사용자)" + " " + datetime.datetime.today().strftime("%Y/%m/%d %H:%M:%S")
            db_request=requests.post(DB_HOST, data={'name':"잠금해제 실패(미등록 사용자)"})
            print(in_out_fail)
            pwm=GPIO.PWM(buzzer,262)
            pwm.start(50.0)
            time.sleep(1)
    
            pwm.stop()
            unknown_count=0
            count=0
            restart=0
            time.sleep(1)
            break
            
        
        # 얼굴 인식 연속 2회 성공 시 통과 및 부저음과 모터 돌리기
        if name_origin!="Unknowns" and name_origin!="Capture Failed" and name_origin==name_compare:
            in_out_success=name_origin + " " + datetime.datetime.today().strftime("%Y/%m/%d %H:%M:%S")
            db_request=requests.post(DB_HOST, data={'name':name_origin})
            print(in_out_success)
            pwm=GPIO.PWM(buzzer,100)
            pwm.start(30)
            for i in range(3):
                pwm.ChangeFrequency(scale[i+3])
                time.sleep(0.2)
    
            pwm.stop()

            # 모터 돌아가는 자리
            if button_state==0:
                GPIO.output(in1,True)
                GPIO.output(in2,False)
                GPIO.output(en,True)
                time.sleep(0.06)
                GPIO.output(in1,False)
                GPIO.output(in2,False)
                GPIO.output(en,False)
                button_state=1
                
                restart=0
                count=0
                time.sleep(1)
                break
            else:
                restart=0
                count=0
                time.sleep(1)
                break
        # 얼굴인식 5번 실패 시 종료 및 부저 울리기
        if count==5:
            in_out_fail="잠금해제 실패(얼굴 인식 실패)" + " " + datetime.datetime.today().strftime("%Y/%m/%d %H:%M:%S")
            db_request=requests.post(DB_HOST, data={'name':"잠금해제 실패(얼굴 인식 실패)"})
            print(in_out_fail)
            pwm=GPIO.PWM(buzzer,262)
            pwm.start(50.0)
            time.sleep(1)
    
            pwm.stop()
            count=0
            restart=0
            time.sleep(1)
            break
    
    
    
    '''
    # 모터코드
    import RPi.GPIO as GPIO
    import time
     
    in1 = 19
    in2 = 13
    en = 26
     
    GPIO.setmode(GPIO.BCM)
    GPIO.setwarnings(False)
    GPIO.setup(in1,GPIO.OUT)
    GPIO.setup(in2,GPIO.OUT)
    GPIO.setup(en,GPIO.OUT)
    i=0
    while i==0:
    	print("forward")
    	GPIO.output(in1,True)
    	GPIO.output(in2,False)
    	GPIO.output(en,True)
    	time.sleep(2)
    	GPIO.output(in1,False)
    	GPIO.output(in2,False)
    	GPIO.output(en,False)
    	i=1
	'''

#GPIO.cleanup()
