package ru.nsu.fit.usoltsev.network;

import lombok.extern.slf4j.Slf4j;
import ru.nsu.fit.usoltsev.network.Udp.UdpReceiver;
import ru.nsu.fit.usoltsev.network.Udp.UdpSender;
import ru.nsu.fit.usoltsev.snakes.SnakesProto;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.HashMap;
import java.util.concurrent.*;


@Slf4j
public class UdpController {
    DatagramSocket udpSocket;
    ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(3);
    HashMap<String, MessageInfo> inputMessageStore;
    BlockingQueue<MessageInfo> outputMessageStoreQueue;
    HashMap<String, MessageInfo> outputMessageStore;

    public UdpController() throws SocketException {
        udpSocket = new DatagramSocket();

       //existsGameSender = new ExistsGameSender(udpSocket);

        UdpSender udpSender = new UdpSender(udpSocket, this);
        UdpReceiver udpReceiver = new UdpReceiver(udpSocket, this);

        executor.submit(udpSender);
        executor.submit(udpReceiver);

        inputMessageStore = new HashMap<>();
        outputMessageStore = new HashMap<>();
        outputMessageStoreQueue = new LinkedBlockingQueue<>();
        // executor.submit(existsGameSender);
    }


    public void setOutputMessage(InetAddress ip, int port, SnakesProto.GameMessage gameMessage) throws InterruptedException {
        MessageInfo messageInfo = new MessageInfo(ip, port, gameMessage);
        outputMessageStoreQueue.put(messageInfo);
        //outputMessageStore.put(gameMessage.getTypeCase().name(), messageInfo);
        log.info("Set new message");
    }

    public MessageInfo getOutputMessage() throws InterruptedException {
        log.info("take message");
        return outputMessageStoreQueue.take();
    }


}
