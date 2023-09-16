package ru.nsu.fit.usoltsev;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.UUID;

public class Sender {

    /**
     * ID number to identify the running app
     */
    private static final String appID = UUID.randomUUID().toString();

    /**
     * Some key that allows to distinguish packets from others on the network
     */
    private static final String packetKey = "web_lab";

    /**
     * <p>In send mode sends UDP-messages to the multicast group.</p>
     *
     * @param ipAddr Ip of the multicast group
     * @param port   Sending port
     */
    @SuppressWarnings({"deprecation"})
    public static void Send(InetAddress ipAddr, Integer port) {
        try (MulticastSocket muSocket = new MulticastSocket(port)) {

            muSocket.joinGroup(ipAddr);                                        // 224.0.0.0 -- 239.255.255.255
            byte[] newAppBuff = (packetKey + appID).getBytes();
            DatagramPacket outputPacket = new DatagramPacket(newAppBuff, newAppBuff.length, ipAddr, port);

            while (true) {
                muSocket.send(outputPacket);
                Thread.sleep(2000);
            }

        } catch (IOException | InterruptedException ioExc) {
            System.err.println(ioExc.getCause() + " " + ioExc.getMessage());
        }
    }
}
