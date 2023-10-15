package ru.nsu.fit.usoltsev.network.Udp;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import ru.nsu.fit.usoltsev.network.MessageInfo;
import ru.nsu.fit.usoltsev.snakes.SnakesProto;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;


@Slf4j
public class UdpController {
    DatagramSocket udpSocket;
    private final UdpSender udpSender;
    private final UdpReceiver udpReceiver;
    private final AnnouncementAdder announcementAdder;

    private final AckChecker ackChecker;
    private final ThreadPoolExecutor executor;

    //HashMap<String, MessageInfo> inputMessageStore;
    // HashMap<String, MessageInfo> outputMessageStore;
    private final BlockingQueue<MessageInfo> outputMessageStore;
    private final BlockingQueue<MessageInfo> inputMessageStore;
    @Getter
    private final BlockingQueue<String> ackStore;
    @Getter
    private final HashMap<Long, MessageInfo> messageTimeSend;


    public UdpController(ThreadPoolExecutor executor) throws SocketException {
        udpSocket = new DatagramSocket();
        this.executor = executor;

        udpSender = new UdpSender(udpSocket, this);
        udpReceiver = new UdpReceiver(udpSocket, this);
        announcementAdder = new AnnouncementAdder(this);
        ackChecker = new AckChecker(this);

        // inputMessageStore = new HashMap<>();
        // outputMessageStore = new HashMap<>();
        outputMessageStore = new LinkedBlockingQueue<>();
        inputMessageStore = new LinkedBlockingQueue<>();
        ackStore = new LinkedBlockingQueue<>();
        messageTimeSend = new HashMap<>();
    }

    public void setOutputMessage(InetAddress ip, int port, SnakesProto.GameMessage gameMessage) throws InterruptedException {
        MessageInfo messageInfo = new MessageInfo(ip, port, gameMessage);
        outputMessageStore.put(messageInfo);
//        i
//            setMessageTimeSend(messageInfo);
        //log.info("Set new output message");
    }

    public MessageInfo getOutputMessage() throws InterruptedException {
        //log.info("Take output message");
        return outputMessageStore.take();
    }

    public void setAck(InetAddress ip, int port, SnakesProto.GameMessage gameMessage) throws InterruptedException {
//        MessageInfo messageInfo = new MessageInfo(ip, port, gameMessage);
        String string = ip.toString() + " " + port + " " + gameMessage.getMsgSeq();
        ackStore.put(string);
        //log.info("Set new output message");
    }

//    public MessageInfo getAck() throws InterruptedException {
//        //log.info("Take output message");
//        return ackStore.take();
//    }

    public void setMessageTimeSend(MessageInfo messageInfo) {
        if (messageInfo.gameMessage().getTypeCase() != SnakesProto.GameMessage.TypeCase.ACK &&
                messageInfo.gameMessage().getTypeCase() != SnakesProto.GameMessage.TypeCase.ANNOUNCEMENT &&
                messageInfo.gameMessage().getTypeCase() != SnakesProto.GameMessage.TypeCase.DISCOVER) {
            synchronized (messageTimeSend) {
                messageTimeSend.put(System.currentTimeMillis(), messageInfo);
            }
        }
    }

//    public void getMessageTimeSend(MessageInfo messageInfo){
//        synchronized (messageTimeSend){
//            messageTimeSend.put(System.currentTimeMillis(),messageInfo);
//        }
//    }

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
        executor.submit(ackChecker);
    }


}
