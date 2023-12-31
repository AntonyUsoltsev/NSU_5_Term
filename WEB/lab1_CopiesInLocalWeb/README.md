**Info**

    Приложение поддерживает два режима работы: слушающий и отправляющий.

    В слушающем режиме приложение прослушывает сокет, принимает входящие датаграммы из мультикаст группы
    и исходя из их информации делает записи в таблице обнаруженных копий. На принятие сообщения из сокета 
    установлен timeout = 5000ms, по истечение которого приложение перестает ждать сообщение и проверяет таблицу 
    на наличие неработающих приложенений (приложение считается неработающим, если оно не отправляло сообщение 
    больше чем LiveTime = 5000mc).

    В отправляющем режиме приложение раз в 2 секунды отправляет датаграмму в мультикаст группу. В случае закрытия 
    приложения сокет закрывается автоматически.

    Устройство передаваемого пакета: сообщение предаствляет собой экземпляр класса DatagramPacket и содержит 
    следующую информацию: первые 7 символов - ключ пакета, которые равен "web_lab", 
    затем идут 36 символов - ID приложения отправителя.

**Command arguments:**

    1: [-L] to listen, [-S] to send
    2: <InetAddress> ip-addr
        in range 224.0.0.0 -- 239.255.255.255 for IPv4  
        in range FF02::1 for IPv6
    3: <Integer> port
