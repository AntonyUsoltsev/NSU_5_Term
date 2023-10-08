package ru.nsu.fit.usoltsev.network;

import static ru.nsu.fit.usoltsev.GameConstants.*;

import lombok.extern.slf4j.Slf4j;
import ru.nsu.fit.usoltsev.snakes.SnakesProto;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;


public class NetworkController {
    public void MulticastAnnouncement() {
        try (MulticastSocket multicastSocket = new MulticastSocket(9192)) {
            multicastSocket.joinGroup(InetAddress.getByName("239.192.0.4"));
            SnakesProto.GameConfig gameConfig = SnakesProto.GameConfig.newBuilder()
                    .setWidth(WIDTH)
                    .setHeight(HEIGHT)
                    .setFoodStatic(FOOD_COUNT)
                    .setStateDelayMs(TIME_DELAY)
                    .build();

            SnakesProto.GamePlayer gamePlayer = SnakesProto.GamePlayer.newBuilder()
                    .setName("test1")
                    .setId(1).build();


        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
