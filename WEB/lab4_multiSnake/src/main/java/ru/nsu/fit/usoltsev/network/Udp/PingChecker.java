package ru.nsu.fit.usoltsev.network.Udp;

import lombok.extern.slf4j.Slf4j;
import ru.nsu.fit.usoltsev.network.gameMessageCreators.PingMsg;
import ru.nsu.fit.usoltsev.snakes.SnakesProto;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ConcurrentHashMap;

import static ru.nsu.fit.usoltsev.GameConfig.TIME_DELAY;

@Slf4j
public class PingChecker implements Runnable {
    private final UdpController udpController;

    public PingChecker(UdpController udpController) {
        this.udpController = udpController;
    }

    @Override
    public void run() {
        while (!Thread.interrupted()) {
            ConcurrentHashMap<String, Long> lastMessageSendTime = udpController.getLastMessageSendTime();
            if (!lastMessageSendTime.isEmpty()) {
                //log.info("size = " + lastMessageSendTime.size());
                lastMessageSendTime.forEach(((inetInfo, time) -> {
                    if (time < (System.currentTimeMillis() - TIME_DELAY / 10)) {
                        try {
                            SnakesProto.GameMessage message = PingMsg.createPing();
                            String[] ipPort = inetInfo.split(":");;
                            InetAddress ip = InetAddress.getByName(ipPort[0].substring(1));
                            int port = Integer.parseInt(ipPort[1]);
                            udpController.setOutputMessage(ip, port, message);
                        } catch (UnknownHostException | NumberFormatException e) {
                            log.warn("Failed to set ping msg", e);
                        }
                    }
                }));
            }
            try {
                Thread.sleep(TIME_DELAY / 10);
            } catch (InterruptedException e) {
                log.warn("Failed to sleep in ping checker", e);
            }
        }
    }
}
