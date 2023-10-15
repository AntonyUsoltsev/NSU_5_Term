package ru.nsu.fit.usoltsev.network.Udp;

import ru.nsu.fit.usoltsev.network.MessageInfo;
import ru.nsu.fit.usoltsev.network.gameMessageCreators.CopyOfMessage;
import ru.nsu.fit.usoltsev.snakes.SnakesProto;

import java.net.InetAddress;
import java.util.HashMap;

import static ru.nsu.fit.usoltsev.GameConfig.*;

public class AckChecker implements Runnable {
    private final UdpController udpController;

    public AckChecker(UdpController udpController) {
        this.udpController = udpController;

    }

    @Override
    public void run() {
        while (!Thread.interrupted()) {
            HashMap<Long, MessageInfo> timeSendMessage = udpController.getMessageTimeSend();
            if (!timeSendMessage.isEmpty())

                synchronized (timeSendMessage) {
                    timeSendMessage.entrySet()
                            .removeIf(entry -> {
                                InetAddress ip = entry.getValue().ipAddr();
                                int port = entry.getValue().port();
                                SnakesProto.GameMessage message = entry.getValue().gameMessage();
                                String info = ip.toString() + " " + port + " " + message.getMsgSeq();

                                if (udpController.getAckStore().contains(info)) {
                                    udpController.getAckStore().remove(info);
                                    return true;
                                }

                                if (entry.getKey() < (System.currentTimeMillis() - TIME_DELAY / 10)) {
//                                    System.out.println(entry.getKey());
//                                    System.out.println("make copy");
                                    SnakesProto.GameMessage gameMessage = CopyOfMessage.makeMessageCopy(message);
                                    try {
                                        udpController.setOutputMessage(ip, port, gameMessage);
                                    } catch (InterruptedException e) {
                                        throw new RuntimeException(e);
                                    }
                                    return true; // Возвращаем true, чтобы элемент был удален
                                }
                                return false; // Возвращаем false, чтобы элемент остался в HashMap
                            });
                }


        }
    }
}
