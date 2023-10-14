package ru.nsu.fit.usoltsev.network.Udp;

import lombok.extern.slf4j.Slf4j;
import ru.nsu.fit.usoltsev.snakes.SnakesProto;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;

@Slf4j
public class JoinMessage implements Runnable {
    DatagramSocket udpSocket;
    ArrayList<SnakesProto.GameAnnouncement> curGame = new ArrayList<>();

    public JoinMessage() throws SocketException {
        udpSocket = new DatagramSocket();
    }

    public synchronized void setNewGame(int width, int height, int foodCount, int TimeDelay, String gameName, int role) {
        SnakesProto.GameAnnouncement gameAnnouncement = createAnnouncement(width, height, foodCount, TimeDelay,gameName, role);
        curGame.add(gameAnnouncement);
        notify();
    }

    public synchronized ArrayList<SnakesProto.GameAnnouncement> getCurGame() throws InterruptedException {
        while (curGame.isEmpty()) {
            wait();
        }
        return curGame;
    }

    public SnakesProto.GameAnnouncement createAnnouncement(int width, int height, int foodCount, int TimeDelay,String gameName, int role) {

        SnakesProto.GameConfig gameConfig = SnakesProto.GameConfig.newBuilder()
                .setWidth(width)
                .setHeight(height)
                .setFoodStatic(foodCount)
                .setStateDelayMs(TimeDelay)
                .build();

        SnakesProto.GamePlayer gamePlayer = SnakesProto.GamePlayer.newBuilder()
                .setName("test1")
                .setId(1)
                .setRole(SnakesProto.NodeRole.forNumber(role))
                .setScore(0)
                .build();

        SnakesProto.GamePlayers gamePlayers = SnakesProto.GamePlayers.newBuilder()
                .addPlayers(gamePlayer)
                .build();

        SnakesProto.GameAnnouncement gameAnnouncement = SnakesProto.GameAnnouncement.newBuilder()
                .setConfig(gameConfig)
                .setPlayers(gamePlayers)
                .setCanJoin(true)
                .setGameName(gameName)
                .build();

        return gameAnnouncement;
    }

    @Override
    public void run() {
        while (true) {
            try {
                ArrayList<SnakesProto.GameAnnouncement> game = getCurGame();

                SnakesProto.GameMessage.AnnouncementMsg am = SnakesProto.GameMessage.AnnouncementMsg
                        .newBuilder()
                        .addGames(game.get(0))
                        .build();

                SnakesProto.GameMessage message = SnakesProto.GameMessage.newBuilder()
                        .setAnnouncement(am)
                        .setMsgSeq(1)
                        .build();

                byte[] newAppBuff = message.toByteArray();
                DatagramPacket outputPacket = new DatagramPacket(newAppBuff, newAppBuff.length, InetAddress.getByName("239.192.0.4"), 9192);
                udpSocket.send(outputPacket);

                log.info("Send Announcement");

                Thread.sleep(1000);

            } catch (IOException | InterruptedException exc) {
                System.err.println(exc.getMessage());
                exc.printStackTrace(System.err);
                return;
            }


        }
    }
}