import socket
from _thread import *
import time


# 쓰레드에서 실행되는 코드입니다.
# 접속한 클라이언트마다 새로운 쓰레드가 생성되어 통신을 하게 됩니다.
def threaded(client_socket, addr):
    print('>> Connected by :', addr[0], ':', addr[1])

    # 클라이언트가 접속을 끊을 때 까지 반복합니다.
    while True:

        try:
            
            # 데이터가 수신되면 클라이언트에 다시 전송합니다.(에코)
            try:
                data = client_socket.recv(1024)
                print('>> Received from ' + addr[0], ':', addr[1], data)

            except socket.error as socketerror:
                data='1'
                data=data.encode()
            
            # 기본적으로 1을 계속 보내다 메시지 수정 시 수정된 메시지 전송
            for client in client_sockets :
                client.send(data)

        except ConnectionResetError as e:
            print('>> Disconnected by ' + addr[0], ':', addr[1])
            break

        except ConnectionAbortedError as e:
            print('>> Disconnected by ' + addr[0], ':', addr[1])
            break

    if client_socket in client_sockets :
        client_sockets.remove(client_socket)
        print("남은 참가자 수 : ", len(client_sockets))

    client_socket.close()

client_sockets = [] # 서버에 접속한 클라이언트 목록
 
# 서버 IP 및 열어줄 포트
HOST = '127.0.0.1'
PORT = 9999

# 서버 소켓 생성
print('>> Server Start')
server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
server_socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
server_socket.bind((HOST, PORT))
server_socket.listen()

try:
    while True:
        print('>> Wait')

        client_socket, addr = server_socket.accept()
        client_sockets.append(client_socket)
        client_socket.settimeout(1)
        start_new_thread(threaded, (client_socket, addr))
        print("참가자 수 : ", len(client_sockets))
        
except Exception as e :
    print ('에러는? : ',e)

finally:
    server_socket.close()
 
