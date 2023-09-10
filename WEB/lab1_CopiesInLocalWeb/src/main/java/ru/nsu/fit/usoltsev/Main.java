package ru.nsu.fit.usoltsev;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Main {
    public static void main(String[] args) {
        try (MulticastSocket muSocket = new MulticastSocket(8000)) {
            muSocket.joinGroup(InetAddress.getByName("229.1.2.3"));    // 224.0.0.0 -- 239.255.255.255
            String uniqueId = UUID.randomUUID().toString();
            byte[] outputBuff = uniqueId.getBytes();
            DatagramPacket outputPacket = new DatagramPacket(outputBuff, outputBuff.length, InetAddress.getByName("229.1.2.3"), 8000);
            muSocket.send(outputPacket);

            byte[] inputBuff = new byte[128];
            Set<String> knownApps = new HashSet<>();

            while (true) {
                DatagramPacket inputPacket = new DatagramPacket(inputBuff, inputBuff.length);
                muSocket.receive(inputPacket);
                String senderId = new String(Arrays.copyOfRange(inputPacket.getData(),0,36));
                Integer senderPort = inputPacket.getPort();
                String senderHost = inputPacket.getAddress().getHostAddress();
                if (!knownApps.contains(senderId)) {
                    knownApps.add(senderId);
                    System.out.println("New app discovered: " + senderId);
                    muSocket.send(outputPacket);

                    System.out.println("All live apps:");
                    knownApps.forEach(System.out::println);
                    System.out.println();
                }
//                } else if (str.equals("Close app")) {
//                    knownApps.remove(senderHost);
//                }

            }


        } catch (IOException ioExc) {
            System.err.println("Failed to create socket");
            ioExc.printStackTrace();
        }

    }
}