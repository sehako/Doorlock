# 라즈베리쪽 코드
import socket
import threading

HOST = '127.0.0.1'
PORT = 9999

client_socket = socket.socket(socket.AF_INET,socket.SOCK_STREAM)
client_socket.connect((HOST, PORT))

# 서버로부터 1을 계속 받다가
# 안드로이드에서 2를 쏴주면 2를 받는다
def recv_data(client_socket) :
    while True :
        data = client_socket.recv(1024)

        print("recive : ",repr(data.decode()))

            
t = threading.Thread(target=recv_data, args=(client_socket,))
t.start()
print ('>> Connect Server')

while True:
    message = input('')
    if message == 'quit':
        close_data = message
        break

    client_socket.send(message.encode())


client_socket.close()