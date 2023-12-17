package ru.nsu.fit.usoltsev.network.Udp;

import lombok.extern.slf4j.Slf4j;
import ru.nsu.fit.usoltsev.network.NetworkUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import static ru.nsu.fit.usoltsev.GameConfig.*;
import static ru.nsu.fit.usoltsev.GameConstants.roles;

@Slf4j
public class DisconnectChecker implements Runnable {
    private final UdpController udpController;

    public DisconnectChecker(UdpController udpController) {
        this.udpController = udpController;
    }

    @Override
    public void run() {
        ArrayList<String> valueToDelete = new ArrayList<>();
        while (!Thread.interrupted()) {
            ConcurrentHashMap<String, Long> lastMessageReceiveTime = udpController.getLastMessageReceiveTime();
//            System.out.println("SIZE = " + lastMessageReceiveTime.size());
          //  log.info("CHECK AFK");
            if (!lastMessageReceiveTime.isEmpty()) {
                lastMessageReceiveTime.forEach(((inetInfo, time) -> {
                    if (time < (System.currentTimeMillis() - (TIME_DELAY * 8L) / 10)) {
                        try {
                            log.info(String.format("%d(%s) find that %s is AFK (time = %d)", ID, roles.get(ROLE), inetInfo, System.currentTimeMillis()));
                            String[] ipPort = inetInfo.split(":");
                            InetAddress ip = NetworkUtils.parseIp(ipPort[0]);
                            int port = Integer.parseInt(ipPort[1]);
                            valueToDelete.add(inetInfo);
                            udpController.notifyDisconnectListener(inetInfo);
                            udpController.removeDisconnectMessages(ip, port);
                        } catch (UnknownHostException | NumberFormatException e) {
                            log.warn("Failed to parse inet info in disconnect checker", e);
                        }
                    }
                }));
                Iterator<String> iterator = valueToDelete.iterator();
                while (iterator.hasNext()) {
                    String value = iterator.next();
                    lastMessageReceiveTime.remove(value);
                    iterator.remove();
                }
            }
            try {
                Thread.sleep(TIME_DELAY / 10);
            } catch (InterruptedException e) {
                log.warn("Failed to sleep in ping checker", e);
            }
        }
    }
}
