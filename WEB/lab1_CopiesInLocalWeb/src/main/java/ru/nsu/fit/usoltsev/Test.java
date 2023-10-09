package ru.nsu.fit.usoltsev;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Test {
    public static void main(String[] args) {
        try (MulticastSocket muSocket = new MulticastSocket(8000)) {

            muSocket.joinGroup(InetAddress.getByName("229.1.2.3"));    // 224.0.0.0 -- 239.255.255.255
            String uniqueId = UUID.randomUUID().toString();
            long pid = ProcessHandle.current().pid();
            String hi = "live" + uniqueId;
            byte[] hiBuff = hi.getBytes();
            DatagramPacket outputPacket = new DatagramPacket(hiBuff, hiBuff.length, InetAddress.getByName("229.1.2.3"), 8000);
            muSocket.send(outputPacket);

            byte[] inputBuff = new byte[128];
            Set<String> knownApps = new HashSet<>();

            Thread printingHook = new Thread(() -> {
                System.out.println("In the middle of a shutdown");
                System.out.flush();
            });
            Runtime.getRuntime().addShutdownHook(printingHook);

//            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
//                try {
//                    String bye = "died" + uniqueId;
//                    byte[] byeBuff = bye.getBytes();
//                    DatagramPacket byePacket = new DatagramPacket(byeBuff, byeBuff.length, InetAddress.getByName("229.1.2.3"), 8000);
//                    muSocket.send(byePacket);
//                    muSocket.leaveGroup(InetAddress.getByName("229.1.2.3"));
//                    muSocket.close();
//                } catch (IOException e) {
//                    throw new RuntimeException(e);
//                }
//            }));

            while (true) {
                DatagramPacket inputPacket = new DatagramPacket(inputBuff, inputBuff.length);
                muSocket.receive(inputPacket);
                String status = new String(Arrays.copyOfRange(inputPacket.getData(), 0, 4));
                String senderId = new String(Arrays.copyOfRange(inputPacket.getData(), 4, 40));
//                Integer senderPort = inputPacket.getPort();
//                String senderHost = inputPacket.getAddress().getHostAddress();
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

        }
        catch (IOException ioExc) {
            System.err.println("Failed to create socket");
            ioExc.printStackTrace();
        }

    }
}