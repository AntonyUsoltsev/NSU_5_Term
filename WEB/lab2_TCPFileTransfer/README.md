# TCP file tranfer

This lab contains two main classes: client and server

Client - send file to the server

    Command args:
        1) <String> absolute path to the send file
        2) <InetAddress> ip address of the server
        3) <Integer> port of the server

Server - receive file from client and save it in upload directory
    
    Command args:
        1) <Integer> port