package ru.nsu.fit.usoltsev.network.Udp;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import ru.nsu.fit.usoltsev.controller.GameController;
import ru.nsu.fit.usoltsev.network.gameMessageCreators.AnnouncementMsg;
import ru.nsu.fit.usoltsev.snakes.SnakesProto;

import static ru.nsu.fit.usoltsev.GameConfig.*;
import static ru.nsu.fit.usoltsev.GameConstants.*;
@Slf4j
public class AnnouncementAdder implements Runnable {
    private final UdpController udpController;

    @Setter
    private GameController gamersInfo;

    public AnnouncementAdder(UdpController udpController) {
        this.udpController = udpController;
    }


    @Override
    public void run() {
        while (!Thread.interrupted()) {
            try {
                SnakesProto.GameMessage gameMessage;
                if (gamersInfo != null) {
                    gameMessage = AnnouncementMsg.createAnnouncement(WIDTH / SQUARE_SIZE,
                            HEIGHT / SQUARE_SIZE, FOOD_COUNT, TIME_DELAY, GAME_NAME,
                            gamersInfo.getPlayers(), gamersInfo.getViewers());
                } else {
                    gameMessage = AnnouncementMsg.createAnnouncement(WIDTH / SQUARE_SIZE,
                            HEIGHT / SQUARE_SIZE, FOOD_COUNT, TIME_DELAY, GAME_NAME, PLAYER_NAME, ROLE);
                }
                log.info("set anouns");
                udpController.setOutputMessage(MULTICAST_IP, MULTICAST_PORT, gameMessage);
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        }
    }
}
