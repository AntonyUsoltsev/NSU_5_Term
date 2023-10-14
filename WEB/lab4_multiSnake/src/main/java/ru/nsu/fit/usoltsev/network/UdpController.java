package ru.nsu.fit.usoltsev.network;

import lombok.extern.slf4j.Slf4j;
import ru.nsu.fit.usoltsev.network.Udp.ExistsGameSender;
import ru.nsu.fit.usoltsev.network.Udp.UdpReceiver;
import ru.nsu.fit.usoltsev.network.Udp.UdpSender;

import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;


@Slf4j
public class UdpController {
    DatagramSocket udpSocket;
    ExistsGameSender existsGameSender;
    UdpSender udpSender;
    UdpReceiver udpReceiver;
    ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(2);

    public UdpController() throws SocketException {
        udpSocket = new DatagramSocket();
        existsGameSender = new ExistsGameSender(udpSocket);
        udpSender = new UdpSender(udpSocket);
        udpReceiver = new UdpReceiver(udpSocket);

        executor.submit(udpSender);
        executor.submit(udpReceiver);
        executor.submit(existsGameSender);
    }

    public void setNewGame(int width, int height, int foodCount, int TimeDelay, String gameName, int role) {
        existsGameSender.setNewGame(width, height, foodCount, TimeDelay, gameName, role);
        log.info("Set new game");
    }

    public void sendJoinMsg(){

    }



}
