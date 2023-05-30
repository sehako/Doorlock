# face_recog.py

import cv2
import camera
import os
import numpy as np
import datetime
from picamera import PiCamera
import face_recognition


class FaceRecog():
    def __init__(self):
        # Using OpenCV to capture from device 0. If you have trouble capturing
        # from a webcam, comment the line below out and use a video file
        # instead.
        self.camera = picamera.PiCamera()

        self.known_face_encodings = []
        self.known_face_names = []

        # Load sample pictures and learn how to recognize it.
        dirname = 'knowns'
        files = os.listdir(dirname)
        for filename in files:
            name, ext = os.path.splitext(filename)
            if ext == '.jpg':
                self.known_face_names.append(name)
                pathname = os.path.join(dirname, filename)
                img = face_recognition.load_image_file(pathname)
                face_encoding = face_recognition.face_encodings(img)[0]
                self.known_face_encodings.append(face_encoding)

        # Initialize some variables
        self.face_locations = []
        self.face_encodings = []
        self.face_names = []
        self.process_this_frame = True

    def __del__(self):
        self.camera.close()

    def get_frame(self):
        # Grab a single frame of video
        frame = self.camera.capture()

        # Resize frame of video to 1/4 size for faster face recognition processing
        small_frame = cv2.resize(frame, (0, 0), fx=0.25, fy=0.25)

        # Convert the image from BGR color (which OpenCV uses) to RGB color (which face_recognition uses)
        rgb_small_frame = small_frame[:, :, ::-1]

        # Only process every other frame of video to save time
        if self.process_this_frame:
            # Find all the faces and face encodings in the current frame of video
            self.face_locations = face_recognition.face_locations(rgb_small_frame)
            self.face_encodings = face_recognition.face_encodings(rgb_small_frame, self.face_locations)

            self.face_names = []
            for face_encoding in self.face_encodings:
                # See if the face is a match for the known face(s)
                distances = face_recognition.face_distance(self.known_face_encodings, face_encoding)
                min_value = min(distances)

                # tolerance: How much distance between faces to consider it a match. Lower is more strict.
                # 0.6 is typical best performance.
                name = "Unknown"
                if min_value < 0.5: #정확도 조절
                    index = np.argmin(distances)
                    name = self.known_face_names[index]

                self.face_names.append(name)

        self.process_this_frame = not self.process_this_frame

        # Display the results
        for (top, right, bottom, left), name in zip(self.face_locations, self.face_names):
            # Scale back up face locations since the frame we detected in was scaled to 1/4 size
            top *= 4
            right *= 4
            bottom *= 4
            left *= 4

            # Draw a box around the face
            cv2.rectangle(frame, (left, top), (right, bottom), (0, 0, 255), 2)

            # Draw a label with a name below the face
            cv2.rectangle(frame, (left, bottom - 35), (right, bottom), (0, 0, 255), cv2.FILLED)
            font = cv2.FONT_HERSHEY_DUPLEX
            cv2.putText(frame, name, (left + 6, bottom - 6), font, 1.0, (255, 255, 255), 1)

        return frame

    def get_jpg_bytes(self):
        frame = self.get_frame()
        # We are using Motion JPEG, but OpenCV defaults to capture raw images,
        # so we must encode it into JPEG in order to correctly display the
        # video stream.
        ret, jpg = cv2.imencode('.jpg', frame)
        return jpg.tobytes()


if __name__ == '__main__':
    face_recog = FaceRecog()
    # print(face_recog.known_face_names) //현재 knowns 폴더의 이름들을 출력
    start_time=datetime.datetime.now()
    while True:
        frame = face_recog.get_frame()

        # show the frame
        cv2.imshow("Frame", frame)
        key = cv2.waitKey(1) & 0xFF
        # 카메라 작동 제어 부분
        
        # if the `q` key was pressed, break from the loop
        if key == ord("q"):
            break
            
        face_recog.camera.close()
        cv2.destroyAllWindows()
        
        # 5초 후에 카메라 종료
        '''
        seconds_to_run=5
        if((datetime.datetime.now()-start_time).seconds)>=seconds_to_run:
            break
        '''

    # 카메라 종료후 실행 부분
    # cv2.destroyAllWindows() 위에서 쓰려고 잠깐 주석 달아놨음 /이종석
    if (''.join(face_recog.face_names)==('')):
        ent_list="can't find" #인식 실패한 경우 메시지 출력
    else:
        ent_list=''.join(face_recog.face_names) + " " + datetime.datetime.today().strftime("%Y/%m/%d %H:%M:%S")# 인식 성공 시 현재 인식된 얼굴 이름과 현재 날짜를 스트링 형식으로 저장
    print(ent_list)
    '''
        종료 시 ent_list를 서버DB로 전송? or 리스트 형식으로 계속 저장 서버에서 요청시 db로 전송?
        ent_list가 unknown가 아닐시 문을 열고, unknown이면 문을 열지 않고 사용자에게 알람보내기?안드로이드로 구현?

    '''
