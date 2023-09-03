# 안드로이드에서 실행되는 코드부분
# 버튼 클릭시 소켓 연결해서 메시지 전송하고 종료

import socket

HOST = '127.0.0.1'
PORT = 9999

client_socket = socket.socket(socket.AF_INET,socket.SOCK_STREAM)
client_socket.connect((HOST, PORT))

# 라즈베리 부분에 사진 업로드 2, 원격 잠금 해제 3, 라즈베리 종료 5 설정해놨음
# 사용 부분에 따라 메시지 변경해서 입력
message = '2'
client_socket.send(message.encode())

client_socket.close()