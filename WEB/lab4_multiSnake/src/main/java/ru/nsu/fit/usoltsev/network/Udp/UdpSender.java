package ru.nsu.fit.usoltsev.network.Udp;

import ru.nsu.fit.usoltsev.network.MessageInfo;
import ru.nsu.fit.usoltsev.network.UdpController;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class UdpSender implements Runnable{
    private final DatagramSocket udpSocket;
    private final UdpController udpController;
    public UdpSender(DatagramSocket udpSocket, UdpController udpController) {
        this.udpSocket = udpSocket;
        this.udpController = udpController;
    }

    @Override
    public void run() {
        while (!Thread.interrupted()){
            try {
                MessageInfo messageInfo = udpController.getOutputMessage();
                byte[] newAppBuff = messageInfo.gameMessage().toByteArray();
                DatagramPacket outputPacket = new DatagramPacket(newAppBuff, newAppBuff.length, messageInfo.ipAddr(), messageInfo.port());
                udpSocket.send(outputPacket);

            } catch (InterruptedException | IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}