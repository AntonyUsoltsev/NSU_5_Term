package ru.nsu.fit.usoltsev.network.Udp;

import lombok.extern.slf4j.Slf4j;
import ru.nsu.fit.usoltsev.network.gameMessageCreators.AckMsg;
import ru.nsu.fit.usoltsev.snakes.SnakesProto;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Arrays;

@Slf4j
public class UdpReceiver implements Runnable {
    private final DatagramSocket udpSocket;
    private final UdpController udpController;

    public UdpReceiver(DatagramSocket udpSocket, UdpController udpController) {
        this.udpSocket = udpSocket;
        this.udpController = udpController;
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

                log.info("Receive message " + gameMessage.getTypeCase().name() + ", msg seq = " + gameMessage.getMsgSeq() + ", time = " + System.currentTimeMillis());

                //  udpController.setInputMessage(inputPacket.getAddress(), inputPacket.getPort(), gameMessage);

                if(gameMessage.getTypeCase() == SnakesProto.GameMessage.TypeCase.ACK) {
                    udpController.setAck(inputPacket.getAddress(), inputPacket.getPort(), gameMessage);
                }

                else if (gameMessage.getTypeCase() == SnakesProto.GameMessage.TypeCase.JOIN ||
                        gameMessage.getTypeCase() == SnakesProto.GameMessage.TypeCase.PING ||
                        gameMessage.getTypeCase() == SnakesProto.GameMessage.TypeCase.STEER ||
                        gameMessage.getTypeCase() == SnakesProto.GameMessage.TypeCase.STATE ||
                        gameMessage.getTypeCase() == SnakesProto.GameMessage.TypeCase.ERROR ||
                        gameMessage.getTypeCase() == SnakesProto.GameMessage.TypeCase.ROLE_CHANGE) {

                    SnakesProto.GameMessage gameAnswer = AckMsg.createAck(gameMessage.getMsgSeq(), gameMessage.getSenderId());
                    udpController.setOutputMessage(inputPacket.getAddress(), inputPacket.getPort(), gameAnswer);

                }

            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
