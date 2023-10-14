package ru.nsu.fit.usoltsev.network;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import ru.nsu.fit.usoltsev.listeners.NewGameListener;
import ru.nsu.fit.usoltsev.snakes.SnakesProto;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.util.Arrays;

import static  ru.nsu.fit.usoltsev.GameConstants.*;

@Slf4j
public class MulticastInputController implements Runnable {

    private final MulticastSocket multicastSocket;

    @Setter
    private NewGameListener newGameListener;


    public MulticastInputController() throws IOException {
        multicastSocket = new MulticastSocket(MULTICAST_PORT);
        multicastSocket.joinGroup(MULTICAST_IP);
        //multicastSocket.setSoTimeout(1000);
    }


    @Override
    public void run() {
        while (!Thread.interrupted()) {
            byte[] inputBuff = new byte[65536];
            DatagramPacket inputPacket = new DatagramPacket(inputBuff, inputBuff.length);
            try {
                multicastSocket.receive(inputPacket);

                SnakesProto.GameMessage gameMessage = SnakesProto.GameMessage.parseFrom
                        (Arrays.copyOfRange(inputPacket.getData(),0, inputPacket.getLength()));

               // log.info("Receive multicast message " + gameMessage.getTypeCase().name());

                if (gameMessage.getTypeCase() == SnakesProto.GameMessage.TypeCase.ANNOUNCEMENT) {
                   // log.info("New game came");

                    newGameListener.addNewGame(inputPacket.getAddress(), inputPacket.getPort(),gameMessage);
                }
                else{
                    log.warn("Incorrect message type in multicast");
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
