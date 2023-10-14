package ru.nsu.fit.usoltsev.network.Udp;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import ru.nsu.fit.usoltsev.network.MessageInfo;
import ru.nsu.fit.usoltsev.snakes.SnakesProto;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.concurrent.*;


@Slf4j
public class UdpController {
    DatagramSocket udpSocket;
    private final UdpSender udpSender;
    private final UdpReceiver udpReceiver;
    private final AnnouncementAdder announcementAdder;
    private final ThreadPoolExecutor executor;

    //HashMap<String, MessageInfo> inputMessageStore;
    // HashMap<String, MessageInfo> outputMessageStore;
    private final BlockingQueue<MessageInfo> outputMessageStore;
    private final BlockingQueue<MessageInfo> inputMessageStore;


    public UdpController(ThreadPoolExecutor executor) throws SocketException {
        udpSocket = new DatagramSocket();
        this.executor = executor;
        udpSender = new UdpSender(udpSocket, this);
        udpReceiver = new UdpReceiver(udpSocket, this);
        announcementAdder = new AnnouncementAdder(this);

        // inputMessageStore = new HashMap<>();
        // outputMessageStore = new HashMap<>();
        outputMessageStore = new LinkedBlockingQueue<>();
        inputMessageStore = new LinkedBlockingQueue<>();
    }

    public void setOutputMessage(InetAddress ip, int port, SnakesProto.GameMessage gameMessage) throws InterruptedException {
        MessageInfo messageInfo = new MessageInfo(ip, port, gameMessage);
        outputMessageStore.put(messageInfo);
        //log.info("Set new output message");
    }

    public MessageInfo getOutputMessage() throws InterruptedException {
        //log.info("Take output message");
        return outputMessageStore.take();
    }

    public void setInputMessage(InetAddress ip, int port, SnakesProto.GameMessage gameMessage) throws InterruptedException {
        MessageInfo messageInfo = new MessageInfo(ip, port, gameMessage);
        inputMessageStore.put(messageInfo);
        //log.info("Set new input message");
    }

    public MessageInfo getInputMessage() throws InterruptedException {
        //log.info("Take input message");
        return inputMessageStore.take();
    }

    public void startAnnouncement() {
        executor.submit(announcementAdder);
    }

    public void startSendRecv() {
        executor.submit(udpSender);
        executor.submit(udpReceiver);
    }


}
