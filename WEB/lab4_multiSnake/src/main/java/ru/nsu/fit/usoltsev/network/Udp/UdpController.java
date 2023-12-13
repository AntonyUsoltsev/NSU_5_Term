package ru.nsu.fit.usoltsev.network.Udp;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import ru.nsu.fit.usoltsev.listeners.GameStateListener;
import ru.nsu.fit.usoltsev.listeners.HostAddListener;
import ru.nsu.fit.usoltsev.listeners.SteerListener;
import ru.nsu.fit.usoltsev.network.MessageInfo;
import ru.nsu.fit.usoltsev.snakes.SnakesProto;

import java.net.*;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

import static ru.nsu.fit.usoltsev.network.NetworkUtils.*;

@Slf4j
public class UdpController {
    @Setter
    @Getter
    private HostAddListener snakeAddListener;

    @Setter
    @Getter
    private SteerListener steerListener;

    @Setter
    @Getter
    private GameStateListener gameStateListener;

    DatagramSocket udpSocket;
    private final UdpSender udpSender;
    private final UdpReceiver udpReceiver;
    private final AnnouncementAdder announcementAdder;
    private final AckChecker ackChecker;
    private final ThreadPoolExecutor executor;
    private final LinkedBlockingQueue<MessageInfo> outputMessageStore;
    private final BlockingQueue<MessageInfo> inputMessageStore;

    @Getter
    private final BlockingQueue<String> ackStore;

    @Getter
    private final BlockingQueue<String> successMsgStore;

    @Getter
    private final HashMap<Long, MessageInfo> messageTimeSend;


    public UdpController(ThreadPoolExecutor executor) throws SocketException {
        udpSocket = new DatagramSocket();

        this.executor = executor;

        udpSender = new UdpSender(udpSocket, this);
        udpReceiver = new UdpReceiver(udpSocket, this);
        announcementAdder = new AnnouncementAdder(this);
        ackChecker = new AckChecker(this);
        outputMessageStore = new LinkedBlockingQueue<>();
        inputMessageStore = new LinkedBlockingQueue<>();
        ackStore = new LinkedBlockingQueue<>();
        successMsgStore = new LinkedBlockingQueue<>();
        messageTimeSend = new HashMap<>();
    }

    public void setMasterIpToMaster()  {
        try {
            SocketAddress localSocketAddress = udpSocket.getLocalSocketAddress();
            InetAddress localIpAddress = ((InetSocketAddress) localSocketAddress).getAddress();
            int localPort = ((InetSocketAddress) localSocketAddress).getPort();
            MASTER_IP = InetAddress.getByName(localIpAddress.getHostAddress());
            MASTER_PORT = localPort;
        }catch (UnknownHostException e){
            log.warn("Filed to parse ip in master to master ip", e);
        }
    }

    public void setOutputMessage(InetAddress ip, int port, SnakesProto.GameMessage gameMessage) throws InterruptedException {
        MessageInfo messageInfo = new MessageInfo(ip, port, gameMessage);
        outputMessageStore.put(messageInfo);

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

    public void setSuccessMsg() {

    }

    public boolean notifyAddListener(String name, int playerID, int port, InetAddress ip, int role) {
        boolean result = snakeAddListener.addNewSnake(name, playerID, port, ip, role);
        return result;
    }

    public boolean notifyViewListener(String name, int playerID, int port, InetAddress ip, int role) {
        boolean result = snakeAddListener.addNewViewer(name, playerID, port, ip, role);
        return result;
    }



    public void notifySteerListener(int direction, int id) {
        steerListener.setNewSteer(direction, id);
    }

    public void notifyStateListener(SnakesProto.GameMessage.StateMsg msg) {
        if (gameStateListener != null) {
            gameStateListener.setNewState(msg);
        }
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
