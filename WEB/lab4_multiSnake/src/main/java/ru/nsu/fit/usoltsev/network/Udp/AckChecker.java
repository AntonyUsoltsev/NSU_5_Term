package ru.nsu.fit.usoltsev.network.Udp;

import lombok.extern.slf4j.Slf4j;
import ru.nsu.fit.usoltsev.network.MessageInfo;
import ru.nsu.fit.usoltsev.network.gameMessageCreators.CopyOfMessage;
import ru.nsu.fit.usoltsev.snakes.SnakesProto;

import java.net.InetAddress;
import java.util.HashMap;

import static ru.nsu.fit.usoltsev.GameConfig.TIME_DELAY;

@Slf4j
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
//                                    udpController.setSuccessMsg(info);
                                    return true;
                                }

                                if (entry.getKey() < (System.currentTimeMillis() - TIME_DELAY / 10)) {
//                                    System.out.println(entry.getKey());
//                                    System.out.println("make copy of " + message.getTypeCase());
                                    SnakesProto.GameMessage gameMessage = CopyOfMessage.makeMessageCopy(message);

                                    udpController.setOutputMessage(ip, port, gameMessage);

                                    return true; // Возвращаем true, чтобы элемент был удален
                                }
                                return false; // Возвращаем false, чтобы элемент остался в HashMap
                            });
                }


        }
    }
}
