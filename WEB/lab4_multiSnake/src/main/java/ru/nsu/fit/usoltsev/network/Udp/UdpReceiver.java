package ru.nsu.fit.usoltsev.network.Udp;

import java.net.DatagramSocket;

public class UdpReceiver implements Runnable{
    DatagramSocket udpSocket;

    public UdpReceiver(DatagramSocket udpSocket) {
        this.udpSocket = udpSocket;
    }

    @Override
    public void run() {

    }
}
