package ru.nsu.fit.usoltsev.network;

import lombok.Setter;
import ru.nsu.fit.usoltsev.listeners.NewGameListener;
import ru.nsu.fit.usoltsev.snakes.SnakesProto;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

import static ru.nsu.fit.usoltsev.GameConfig.*;


public class NetworkController implements Runnable {

    private final MulticastSocket multicastSocket;

    @Setter
    private NewGameListener newGameListener;

    public NetworkController() throws IOException {
        multicastSocket = new MulticastSocket(9192);
        multicastSocket.joinGroup(InetAddress.getByName("239.192.0.4"));
    }

    public void MulticastAnnouncement() {

        SnakesProto.GameConfig gameConfig = SnakesProto.GameConfig.newBuilder()
                .setWidth(WIDTH)
                .setHeight(HEIGHT)
                .setFoodStatic(FOOD_COUNT)
                .setStateDelayMs(TIME_DELAY)
                .build();

        SnakesProto.GamePlayer gamePlayer = SnakesProto.GamePlayer.newBuilder()
                .setName("test1")
                .setId(1).build();

    }


    @Override
    public void run() {

        while (true) {
            byte[] inputBuff = new byte[65536];
            DatagramPacket inputPacket = new DatagramPacket(inputBuff, inputBuff.length);
            try {
                multicastSocket.receive(inputPacket);

                SnakesProto.GameMessage gameMessage = SnakesProto.GameMessage.parseFrom(inputPacket.getData());

                switch (gameMessage.getTypeCase()) {
                    case ANNOUNCEMENT -> {
                        System.out.println("new game came");
                        newGameListener.addNewGame(gameMessage.getAnnouncement().getGames(0).getGameName());
                    }
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
