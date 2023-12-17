package ru.nsu.fit.usoltsev.network.Udp;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;

import static ru.nsu.fit.usoltsev.GameConfig.*;

@Slf4j
public class DisconnectChecker implements Runnable {
    private final UdpController udpController;

    public DisconnectChecker(UdpController udpController) {
        this.udpController = udpController;
    }

    @Override
    public void run() {
        while (!Thread.interrupted()) {
            ConcurrentHashMap<String, Long> lastMessageReceiveTime = udpController.getLastMessageReceiveTime();
            if (!lastMessageReceiveTime.isEmpty()) {
                lastMessageReceiveTime.forEach(((inetInfo, time) -> {
                    if (time < (System.currentTimeMillis() - (TIME_DELAY * 8L) / 10)) {
                        log.info(String.format("%d(%s) find that %s is AFK", ID, ROLE, inetInfo));
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
