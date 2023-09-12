package ru.nsu.fit.usoltsev;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Listener {

    public static void Listen(String Ip, String port) {
        try (MulticastSocket muSocket = new MulticastSocket(Integer.parseInt(port))) {
            muSocket.joinGroup(InetAddress.getByName(Ip));    // 224.0.0.0 -- 239.255.255.255
            Set<String> knownApps = new HashSet<>();
            while (true) {
                byte[] inputBuff = new byte[128];
                DatagramPacket inputPacket = new DatagramPacket(inputBuff, inputBuff.length);
                muSocket.receive(inputPacket);
                String status = new String(Arrays.copyOfRange(inputPacket.getData(), 0, 4));
                String senderId = new String(Arrays.copyOfRange(inputPacket.getData(), 4, 40));
                if (!knownApps.contains(senderId) && status.equals("live")) {
                    knownApps.add(senderId);
                    System.out.println("New app discovered: " + senderId);
                    muSocket.send(outputPacket);
                    System.out.println("All live apps:");
                    knownApps.forEach(System.out::println);
                    System.out.println();
                } else if (status.equals("died")) {
                    knownApps.remove(senderId);
                    System.out.println("App closed: " + senderId);
                    System.out.println("All live apps:");
                    knownApps.forEach(System.out::println);
                    System.out.println();
                }
            }


        } catch (IOException ioExc) {

        }
    }

}
