package ru.nsu.fit.usoltsev.network.Udp;

import java.net.DatagramSocket;
import java.util.HashMap;

public class UdpSender implements Runnable{

    DatagramSocket udpSocket;

    public UdpSender(DatagramSocket udpSocket) {
        this.udpSocket = udpSocket;
    }

    @Override
    public void run() {

    }
}
