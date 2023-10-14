package ru.nsu.fit.usoltsev.network.Udp;

import ru.nsu.fit.usoltsev.network.UdpController;

import java.net.DatagramSocket;

public class UdpReceiver implements Runnable{
    private final DatagramSocket udpSocket;
    private final UdpController udpController;

    public UdpReceiver(DatagramSocket udpSocket, UdpController udpController) {
        this.udpSocket = udpSocket;
        this.udpController = udpController;
    }

    @Override
    public void run() {

    }
}
