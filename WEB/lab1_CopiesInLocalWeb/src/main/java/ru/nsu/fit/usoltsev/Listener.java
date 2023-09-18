package ru.nsu.fit.usoltsev;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.*;

public class Listener {

    /**
     * Time after which the application will be considered inactive (if sending packets stops)
     */
    private static final int LiveTime = 5000;

    /**
     * A table that stores the application ID and the time of last packet send
     */
    private static final Map<String, Long> knownApps = new HashMap<>();

    /**
     * <p>In receive mode listen to the multicast group and monitors
     * the appearance and disappearance of other copies of the application.
     * App is disappear if it doesn't send messages for "LiveTime" milliseconds</p>
     *
     * @param ipAddr Ip of the multicast group
     * @param port   Listening port
     */
    @SuppressWarnings({"deprecation"})
    public static void Listen(InetAddress ipAddr, Integer port) {
        try (MulticastSocket muSocket = new MulticastSocket(port)) {           // cause IOException
            muSocket.setSoTimeout(LiveTime);
            muSocket.joinGroup(ipAddr);        // 224.0.0.0 -- 239.255.255.255 // cause UnknownHostException

            String packetKey, senderId;

            while (true) {

                byte[] inputBuff = new byte[128];
                DatagramPacket inputPacket = new DatagramPacket(inputBuff, inputBuff.length);

                try {
                    muSocket.receive(inputPacket);                              // cause IOException
                    packetKey = new String(Arrays.copyOfRange(inputPacket.getData(), 0, 7));
                    senderId = new String(Arrays.copyOfRange(inputPacket.getData(), 7, 43));
                } catch (IOException ioexc) {
                    System.err.println(ioexc.getMessage());
                    packetKey = "";
                    senderId = "";
                }

                modifyAppsMap(senderId, packetKey);

            }
        } catch (IOException ioExc) {
            System.err.println(ioExc.getCause().getMessage() + " " + ioExc.getMessage());
        }
    }

    /**
     * <p>
     * Check for died apps, append new apps or refresh the last packet receive packet.
     * If the map has been changed it is printed.
     * </p>
     *
     * @param senderId  ID of app that send packet
     * @param packetKey key of packet to check rightness
     */
    private static void modifyAppsMap(String senderId, String packetKey) {
        boolean appDeleted = knownApps.entrySet().removeIf(entry ->
                (System.currentTimeMillis() - entry.getValue()) > LiveTime);
        if (appDeleted) {
            printHashMap();
        }

        if (!knownApps.containsKey(senderId) && packetKey.equals("web_lab")) {
            knownApps.put(senderId, System.currentTimeMillis());
            System.out.println("New app discovered: " + senderId);
            printHashMap();
        } else if (packetKey.equals("web_lab")) {
            knownApps.replace(senderId, System.currentTimeMillis());
        }
    }

    /**
     * Print map with live apps
     */
    private static void printHashMap() {
        System.out.println("All live apps:");
        knownApps.forEach((key, value) -> System.out.println(key));
        System.out.println();
    }
}
