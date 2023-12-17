package ru.nsu.fit.usoltsev.network.Udp;

import lombok.extern.slf4j.Slf4j;
import ru.nsu.fit.usoltsev.network.MessageInfo;
import ru.nsu.fit.usoltsev.snakes.SnakesProto;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Arrays;

@Slf4j
public class UdpSender implements Runnable {
    private final DatagramSocket udpSocket;
    private final UdpController udpController;

    public UdpSender(DatagramSocket udpSocket, UdpController udpController) {
        this.udpSocket = udpSocket;
        this.udpController = udpController;
    }

    @Override
    public void run() {
        while (!Thread.interrupted()) {
            try {
                MessageInfo messageInfo = udpController.getOutputMessage();
                byte[] newAppBuff = messageInfo.gameMessage().toByteArray();
                DatagramPacket outputPacket = new DatagramPacket(newAppBuff, newAppBuff.length, messageInfo.ipAddr(), messageInfo.port());
                udpSocket.send(outputPacket);

                if (messageInfo.gameMessage().getTypeCase() != SnakesProto.GameMessage.TypeCase.STATE
                        && messageInfo.gameMessage().getTypeCase() != SnakesProto.GameMessage.TypeCase.ACK
                        && messageInfo.gameMessage().getTypeCase() != SnakesProto.GameMessage.TypeCase.PING
                ) {
                    log.info("Send message " + messageInfo.gameMessage().getTypeCase().name() + ", msg seq = " + messageInfo.gameMessage().getMsgSeq() + ", time = " + System.currentTimeMillis());
                }

                if (messageInfo.gameMessage().getTypeCase() != SnakesProto.GameMessage.TypeCase.ACK &&
                        messageInfo.gameMessage().getTypeCase() != SnakesProto.GameMessage.TypeCase.PING &&
                        messageInfo.gameMessage().getTypeCase() != SnakesProto.GameMessage.TypeCase.ANNOUNCEMENT &&
                        messageInfo.gameMessage().getTypeCase() != SnakesProto.GameMessage.TypeCase.DISCOVER &&
                        messageInfo.gameMessage().getTypeCase() != SnakesProto.GameMessage.TypeCase.JOIN
                ) {
                    udpController.setMessageTimeSend(messageInfo);
                }
                if (messageInfo.gameMessage().getTypeCase() != SnakesProto.GameMessage.TypeCase.ANNOUNCEMENT &&
                        messageInfo.gameMessage().getTypeCase() != SnakesProto.GameMessage.TypeCase.JOIN) {
                    udpController.setLastMessageSendTime(messageInfo.ipAddr().toString() + ":" + messageInfo.port());
                }

            } catch (InterruptedException | IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
