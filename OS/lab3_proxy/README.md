# Caching Proxy Server
Прокси сервер работает на 80 порту и принимает HTTP 1.0 запросы.

How to use:
> gcc main.c -o main \
> sudo ./main

How to test:
> curl --http1.0 -i -x 127.0.0.1:80 http://www.google.com/ \
> (from another process) \
> or another link from "requests"