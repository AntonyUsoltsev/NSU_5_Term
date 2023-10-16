# TCP file tranfer

This lab contains two main classes: client and server

Client - send file to the server

    Command args:
        1) <String> absolute path to the send file
        2) <InetAddress> ip address of the server
        3) <Integer> port of the server

    Example: D:\Antony\NSU_Education\5_Term\WEB\lab2_TCPFileTransfer\src\main\resources\sendFiles\pycharm.exe 192.168.50.189 8000

Server - receive file from client and save it in upload directory
    
    Command args:
        1) <Integer> port

    Example: 8000
