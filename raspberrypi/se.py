import socket
class TimeOutException(Exception):
    pass
 
host = '127.0.0.1'
port = 9999
server_sock = socket.socket(socket.AF_INET)
server_sock.settimeout(3)
server_sock.bind((host, port))
server_sock.listen()
 
print("기다리는 중")
try:
    client_sock, addr = server_sock.accept()
except TimeoutError as e:
    pass
print("제발")
 
print('Connected by', addr)
 
server_sock.close()