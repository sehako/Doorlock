import socket
 
HOST = '127.0.0.1'
PORT = 9999
 

client_socket = socket.socket(socket.AF_INET,socket.SOCK_STREAM)
client_socket.connect((HOST, PORT))