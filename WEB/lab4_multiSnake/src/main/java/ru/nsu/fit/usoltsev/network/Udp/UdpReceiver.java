package ru.nsu.fit.usoltsev.network.Udp;

import lombok.extern.slf4j.Slf4j;
import ru.nsu.fit.usoltsev.GameConfig;
import ru.nsu.fit.usoltsev.network.gameMessageCreators.AckMsg;
import ru.nsu.fit.usoltsev.network.gameMessageCreators.AnnouncementMsg;
import ru.nsu.fit.usoltsev.network.gameMessageCreators.ErrorMsg;
import ru.nsu.fit.usoltsev.snakes.SnakesProto;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.HashMap;

import static ru.nsu.fit.usoltsev.GameConfig.*;
import static ru.nsu.fit.usoltsev.GameConstants.SQUARE_SIZE;

@Slf4j
public class UdpReceiver implements Runnable {
    private final DatagramSocket udpSocket;
    private final UdpController udpController;
    public final HashMap<Integer, InetAddress> hostsPortIp;

    public UdpReceiver(DatagramSocket udpSocket, UdpController udpController) {
        this.udpSocket = udpSocket;
        this.udpController = udpController;
        hostsPortIp = new HashMap<>();
    }

    @Override
    public void run() {
        while (!Thread.interrupted()) {
            byte[] inputBuff = new byte[65536];
            DatagramPacket inputPacket = new DatagramPacket(inputBuff, inputBuff.length);
            try {
                udpSocket.receive(inputPacket);

                SnakesProto.GameMessage gameMessage = SnakesProto.GameMessage.parseFrom
                        (Arrays.copyOfRange(inputPacket.getData(), 0, inputPacket.getLength()));
                if (gameMessage.getTypeCase() != SnakesProto.GameMessage.TypeCase.STATE) {
                    log.info("Receive message " + gameMessage.getTypeCase().name() + ", msg seq = " + gameMessage.getMsgSeq() + ", time = " + System.currentTimeMillis());
                }
                switch (gameMessage.getTypeCase()) {
                    case ACK -> {
                        udpController.setAck(inputPacket.getAddress(), inputPacket.getPort(), gameMessage);
                        log.info(String.format("ACK: GC.Id = %d, gm.rid = %d", GameConfig.ID, gameMessage.getReceiverId()));
                        if (GameConfig.ID == -1 && gameMessage.getReceiverId() != -1) {
                            GameConfig.ID = gameMessage.getReceiverId();
                            GameConfig.MASTER_IP = inputPacket.getAddress();
                            GameConfig.MASTER_PORT = inputPacket.getPort();
                            log.info("Get new id from Master after join: " + GameConfig.ID);
                            GameConfig.countDownLatch.countDown();
                        }
                    }
                    case PING -> {
                        SnakesProto.GameMessage gameAnswer = AckMsg.createAck(gameMessage.getMsgSeq(), gameMessage.getSenderId());
                        udpController.setOutputMessage(inputPacket.getAddress(), inputPacket.getPort(), gameAnswer);
                    }
                    case ROLE_CHANGE -> {

                    }
                    case STATE -> {
                        SnakesProto.GameMessage gameAnswer = AckMsg.createAck(gameMessage.getMsgSeq(), gameMessage.getSenderId());
                        udpController.setOutputMessage(inputPacket.getAddress(), inputPacket.getPort(), gameAnswer);
                        udpController.notifyStateListener(gameMessage.getState());
                    }
                    case STEER -> {
                        SnakesProto.GameMessage gameAnswer = AckMsg.createAck(gameMessage.getMsgSeq(), gameMessage.getSenderId());
                        udpController.setOutputMessage(inputPacket.getAddress(), inputPacket.getPort(), gameAnswer);
                        udpController.notifySteerListener(gameMessage.getSteer().getDirection().getNumber(), gameMessage.getSenderId());
                    }
                    case JOIN -> {
                        if (hostsPortIp.containsKey(inputPacket.getPort())) {
                            SnakesProto.GameMessage gameAnswer = AckMsg.createAck(gameMessage.getMsgSeq(), -1);
                            udpController.setOutputMessage(inputPacket.getAddress(), inputPacket.getPort(), gameAnswer);
                        } else {
                            hostsPortIp.put(inputPacket.getPort(), inputPacket.getAddress());
                            int newId = GameConfig.ID_JOIN.getAndIncrement();
                            boolean placingResult = false;
                            if (gameMessage.getJoin().getRequestedRole() == SnakesProto.NodeRole.NORMAL ||
                                    gameMessage.getJoin().getRequestedRole() == SnakesProto.NodeRole.DEPUTY) {
                                placingResult = udpController.notifyAddListener(gameMessage.getJoin().getPlayerName(), newId, inputPacket.getPort(), inputPacket.getAddress(), gameMessage.getJoin().getRequestedRole().getNumber());
                                log.info("Normal placing = " + placingResult);
                            } else {
                                placingResult = udpController.notifyViewListener(gameMessage.getJoin().getPlayerName(), newId, inputPacket.getPort(), inputPacket.getAddress(), gameMessage.getJoin().getRequestedRole().getNumber());
                                log.info("Viewer placing = " + placingResult);
                            }

                            if (placingResult) {
                                SnakesProto.GameMessage gameAnswer = AckMsg.createAck(gameMessage.getMsgSeq(), newId);
                                udpController.setOutputMessage(inputPacket.getAddress(), inputPacket.getPort(), gameAnswer);
                            } else {
                                SnakesProto.GameMessage gameAnswer = ErrorMsg.createError(gameMessage.getMsgSeq(), -1, "Fail to place new snake");
                                udpController.setOutputMessage(inputPacket.getAddress(), inputPacket.getPort(), gameAnswer);
                            }
                        }
                    }
                    case ERROR -> {
                        udpController.setAck(inputPacket.getAddress(), inputPacket.getPort(), gameMessage);
                        SnakesProto.GameMessage gameAnswer = AckMsg.createAck(gameMessage.getMsgSeq(), gameMessage.getSenderId());
                        udpController.setOutputMessage(inputPacket.getAddress(), inputPacket.getPort(), gameAnswer);
                    }
                    case DISCOVER -> {
                        SnakesProto.GameMessage gameAnswer = AnnouncementMsg.createAnnouncement(WIDTH / SQUARE_SIZE, HEIGHT / SQUARE_SIZE, FOOD_COUNT, TIME_DELAY, GAME_NAME, PLAYER_NAME, ROLE);
                        udpController.setOutputMessage(inputPacket.getAddress(), inputPacket.getPort(), gameAnswer);
                    }
                }
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
