# SOCKS-5 Proxy Server

Implementation of a proxy server working according to the protocol SOCKS-5

The server supports:
1) establishing a TSP connection
2) processing IPv4, IPv6 
3) resolving DNS addresses

The server does not support:
1) authentication during connection setup
2) UDP sockets

The server works on localhost 127.0.0.1, on port given in command arguments

Command arguments:

    1) <Integer> port of the server

Example:  1080