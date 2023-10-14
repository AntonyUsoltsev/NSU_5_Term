package ru.nsu.fit.usoltsev.network.Udp;

import ru.nsu.fit.usoltsev.network.gameMessageCreators.AnnouncementMsg;
import ru.nsu.fit.usoltsev.snakes.SnakesProto;

import static ru.nsu.fit.usoltsev.GameConstants.*;
import static ru.nsu.fit.usoltsev.GameConfig.*;

public class AnnouncementAdder implements Runnable {
    private final UdpController udpController;

    public AnnouncementAdder(UdpController udpController) {
        this.udpController = udpController;
    }

    @Override
    public void run() {
        while (!Thread.interrupted()) {
            try {
                SnakesProto.GameMessage gameMessage = AnnouncementMsg.createAnnouncement(WIDTH/SQUARE_SIZE, HEIGHT/SQUARE_SIZE, FOOD_COUNT, TIME_DELAY, GAME_NAME,PLAYER_NAME, ROLE);
                udpController.setOutputMessage(MULTICAST_IP, MULTICAST_PORT, gameMessage);
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        }
    }
}
