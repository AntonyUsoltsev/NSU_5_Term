package ru.nsu.fit.usoltsev.network;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import ru.nsu.fit.usoltsev.listeners.NewGameListener;
import ru.nsu.fit.usoltsev.snakes.SnakesProto;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.util.Arrays;

import static ru.nsu.fit.usoltsev.GameConstants.MULTICAST_IP;
import static ru.nsu.fit.usoltsev.GameConstants.MULTICAST_PORT;

@Slf4j
@SuppressWarnings("deprecation")
public class MulticastInputController implements Runnable {
    private final MulticastSocket multicastSocket;

    @Setter
    private NewGameListener newGameListener;

    public MulticastInputController() throws IOException {
        multicastSocket = new MulticastSocket(MULTICAST_PORT);
        multicastSocket.joinGroup(MULTICAST_IP);
    }

    @Override
    public void run() {
        while (!Thread.interrupted()) {
            byte[] inputBuff = new byte[65536];
            DatagramPacket inputPacket = new DatagramPacket(inputBuff, inputBuff.length);
            try {
                multicastSocket.receive(inputPacket);

                SnakesProto.GameMessage gameMessage = SnakesProto.GameMessage.parseFrom
                        (Arrays.copyOfRange(inputPacket.getData(), 0, inputPacket.getLength()));

                if (gameMessage.getTypeCase() == SnakesProto.GameMessage.TypeCase.ANNOUNCEMENT) {
                    newGameListener.addNewGame(inputPacket.getAddress(), inputPacket.getPort(), gameMessage);
                } else {
                    log.warn("Incorrect message type in multicast, got " + gameMessage.getTypeCase());
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
