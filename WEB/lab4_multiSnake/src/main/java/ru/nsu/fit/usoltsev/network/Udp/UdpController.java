package ru.nsu.fit.usoltsev.network.Udp;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import ru.nsu.fit.usoltsev.controller.GameController;
import ru.nsu.fit.usoltsev.listeners.*;
import ru.nsu.fit.usoltsev.network.MessageInfo;
import ru.nsu.fit.usoltsev.snakes.SnakesProto;

import java.net.*;
import java.util.HashMap;
import java.util.concurrent.*;

import static ru.nsu.fit.usoltsev.network.NetworkUtils.MASTER_IP;
import static ru.nsu.fit.usoltsev.network.NetworkUtils.MASTER_PORT;

@Slf4j
@Setter
@Getter
public class UdpController {

    private HostAddListener snakeAddListener;
    private SteerListener steerListener;
    private GameStateListener gameStateListener;
    private RoleChangeListener roleChangeListener;
    private DisconnectListener disconnectListener;

    DatagramSocket udpSocket;
    private final UdpSender udpSender;
    private final UdpReceiver udpReceiver;
    private final AnnouncementAdder announcementAdder;
    private final AckChecker ackChecker;
    private final PingChecker pingChecker;
    private final DisconnectChecker disconnectChecker;
    private ThreadPoolExecutor executor;
    private ThreadPoolExecutor pingExecutor;

    private final LinkedBlockingQueue<MessageInfo> outputMessageStore;

//    private final BlockingQueue<MessageInfo> inputMessageStore;

    private final BlockingQueue<String> ackStore;

//    private final BlockingQueue<String> successMsgStore;

    private final HashMap<Long, MessageInfo> messageTimeSend;  // time - msgInfo

    private final ConcurrentHashMap<String, Long> lastMessageSendTime; // ip:port - time

    private final ConcurrentHashMap<String, Long> lastMessageReceiveTime; // ip:port - time

    Future<?> pingFuture;

    public UdpController() throws SocketException {
        udpSocket = new DatagramSocket();

        this.executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(50);
        this.pingExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);

        udpSender = new UdpSender(udpSocket, this);
        udpReceiver = new UdpReceiver(udpSocket, this);
        announcementAdder = new AnnouncementAdder(this);
        ackChecker = new AckChecker(this);
        pingChecker = new PingChecker(this);
        disconnectChecker = new DisconnectChecker(this);

        outputMessageStore = new LinkedBlockingQueue<>();
//        inputMessageStore = new LinkedBlockingQueue<>();
        ackStore = new LinkedBlockingQueue<>();
//        successMsgStore = new LinkedBlockingQueue<>();
        messageTimeSend = new HashMap<>();
        lastMessageSendTime = new ConcurrentHashMap<>();
        lastMessageReceiveTime = new ConcurrentHashMap<>();
    }

    public void setListeners(GameController gameController) {
        setSnakeAddListener(gameController);
        setSteerListener(gameController);
        setGameStateListener(gameController);
        setRoleChangeListener(gameController);
        setDisconnectListener(gameController);
    }

    public void setGamerInfoToAnons(GameController gameController) {
        announcementAdder.setGamersInfo(gameController);
    }

    public void setMasterIpToMaster() {
        try {
            SocketAddress localSocketAddress = udpSocket.getLocalSocketAddress();
            InetAddress localIpAddress = ((InetSocketAddress) localSocketAddress).getAddress();
            int localPort = ((InetSocketAddress) localSocketAddress).getPort();
            MASTER_IP = InetAddress.getByName(localIpAddress.getHostAddress());
            MASTER_PORT = localPort;
        } catch (UnknownHostException e) {
            log.warn("Filed to parse ip in master to master ip", e);
        }
    }

    public void setOutputMessage(InetAddress ip, int port, SnakesProto.GameMessage gameMessage) {
        try {
            log.info("add message " + gameMessage.getTypeCase().name() + ", msg seq = " + gameMessage.getMsgSeq() + ", time = " + System.currentTimeMillis());
            log.info("Storage size:" + outputMessageStore.size());
            MessageInfo messageInfo = new MessageInfo(ip, port, gameMessage);
            outputMessageStore.put(messageInfo);
        } catch (InterruptedException e) {
            log.warn("Failed to set output message", e);
        }
    }

    public MessageInfo getOutputMessage() throws InterruptedException {
        return outputMessageStore.take();
    }

    public void setAck(InetAddress ip, int port, SnakesProto.GameMessage gameMessage) {
        try {
            String string = ip.toString() + " " + port + " " + gameMessage.getMsgSeq();
            ackStore.put(string);
        } catch (InterruptedException e) {
            log.warn("Failed to put ack in ackStore", e);
        }
    }

//    public MessageInfo getAck() throws InterruptedException {
//        //log.info("Take output message");
//        return ackStore.take();
//    }

    public void setMessageTimeSend(MessageInfo messageInfo) {
        synchronized (messageTimeSend) {
            messageTimeSend.put(System.currentTimeMillis(), messageInfo);
        }
    }

    public void removeDisconnectMessages(InetAddress ip, int port) {
        synchronized (messageTimeSend) {
            messageTimeSend.entrySet()
                    .removeIf(entry -> entry.getValue().ipAddr().equals(ip) && entry.getValue().port() == port);
        }
    }

    public void setLastMessageSendTime(String inetInfo) {
        lastMessageSendTime.put(inetInfo, System.currentTimeMillis());
    }

    public void setLastMessageReceiveTime(String inetInfo) {
        lastMessageReceiveTime.put(inetInfo, System.currentTimeMillis());
    }


//    public void getMessageTimeSend(MessageInfo messageInfo){
//        synchronized (messageTimeSend){
//            messageTimeSend.put(System.currentTimeMillis(),messageInfo);
//        }
//    }

    //    public void setInputMessage(InetAddress ip, int port, SnakesProto.GameMessage gameMessage) throws InterruptedException {
//        MessageInfo messageInfo = new MessageInfo(ip, port, gameMessage);
//        inputMessageStore.put(messageInfo);
//        //log.info("Set new input message");
//    }
//
//    public MessageInfo getInputMessage() throws InterruptedException {
//        //log.info("Take input message");
//        return inputMessageStore.take();
//    }
//
//    public void setSuccessMsg() {
//
//    }
    public void notifyDisconnectListener(String inetInfo) {
        disconnectListener.disconnectPlayer(inetInfo);
    }

    public boolean notifyAddListener(String name, int playerID, int port, InetAddress ip, int role) {
        return snakeAddListener.addNewSnake(name, playerID, port, ip, role);
    }

    public boolean notifyViewListener(String name, int playerID, int port, InetAddress ip, int role) {
        return snakeAddListener.addNewViewer(name, playerID, port, ip, role);
    }


    public void notifySteerListener(int direction, int id) {
        steerListener.setNewSteer(direction, id);
    }

    public void notifySteerListener(int direction, String ipPortInfo) {
        steerListener.setNewSteer(direction, ipPortInfo);
    }

    public void notifyStateListener(SnakesProto.GameMessage.StateMsg msg) {
        if (gameStateListener != null) {
            gameStateListener.setNewState(msg);
        }
    }

    public void notifyRoleChangeListener(SnakesProto.GameMessage.RoleChangeMsg roleChange) {
        if (gameStateListener != null) {
            roleChangeListener.setRoleChange(roleChange);
        }
    }

    public void startAnnouncement() {
        log.info("Start Anouns");
        executor.execute(announcementAdder);
    }

    public void startSendRecv() {
        if (executor.isShutdown()) {
            executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(50);
        }
        executor.submit(udpSender);
        executor.submit(udpReceiver);
        executor.submit(ackChecker);
        executor.submit(disconnectChecker);

    }

    public void startSendRecv(InetAddress ip, int port) {
        if (executor.isShutdown()) {
            executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(50);
        }
        setLastMessageSendTime(ip.toString() + ":" + port);
        pingFuture = pingExecutor.submit(pingChecker);
        executor.submit(udpSender);
        executor.submit(udpReceiver);
        executor.submit(ackChecker);
        executor.submit(disconnectChecker);
    }

    public void stopThreads() {
        if (!executor.isShutdown()) {
            System.out.println(executor.getActiveCount());
            System.out.println(executor.shutdownNow());
        }
    }

    public void stopPing() {
        System.out.println(pingExecutor.getActiveCount());
        System.out.println(pingExecutor.shutdownNow());
    }
}
