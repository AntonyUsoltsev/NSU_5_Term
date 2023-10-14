package ru.nsu.fit.usoltsev.network;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import ru.nsu.fit.usoltsev.listeners.NewGameListener;
import ru.nsu.fit.usoltsev.snakes.SnakesProto;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Arrays;

@Slf4j
public class MulticastInputController implements Runnable {

    private final MulticastSocket multicastSocket;

    @Setter
    private NewGameListener newGameListener;


    public MulticastInputController() throws IOException {
        multicastSocket = new MulticastSocket(9192);
        multicastSocket.joinGroup(InetAddress.getByName("239.192.0.4"));
        //multicastSocket.setSoTimeout(1000);
    }


    @Override
    public void run() {

        while (true) {
            byte[] inputBuff = new byte[65536];
            DatagramPacket inputPacket = new DatagramPacket(inputBuff, inputBuff.length);
            try {
                multicastSocket.receive(inputPacket);

                SnakesProto.GameMessage gameMessage = SnakesProto.GameMessage.parseFrom
                        (Arrays.copyOfRange(inputPacket.getData(),0, inputPacket.getLength()));

                log.info("Receive Message " + gameMessage.getTypeCase().name());
                switch (gameMessage.getTypeCase()) {
                    case ANNOUNCEMENT -> {
                        log.info("New game came");
                        newGameListener.addNewGame(gameMessage.getAnnouncement().getGames(0));
                    }
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
