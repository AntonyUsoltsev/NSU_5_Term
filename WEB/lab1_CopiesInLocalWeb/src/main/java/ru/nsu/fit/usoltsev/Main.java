package ru.nsu.fit.usoltsev;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;

// prog args :  1: [-L, -l] to listen, [-S, -s] to send
//              2: [<String>] ip-addr
//                    in range 224.0.0.0 -- 239.255.255.255 for IPv4
//                    in range FF02::1/
//              3: [<Integer>] port


public class Main {
    public static void main(String[] args) {
        try {
            if (args.length != 3) {
                throw new IllegalArgumentException("Too few args");
            } else if (!InetAddress.getByName(args[1]).isMulticastAddress()) {
                System.out.println("ITS NOT OKAY");
                throw new IllegalArgumentException("Incorrect ip address");
            } else if (!(0 < Integer.parseInt(args[2]) && Integer.parseInt(args[2]) < 65536)) {
                throw new IllegalArgumentException("Incorrect port");
            } else {
                switch (args[0]) {
                    case "L" -> Listener.Listen(args[1], args[2]);
                    case "S" -> Sender.Send(args[1], args[2]);
                }
            }
        }
//        try (MulticastSocket muSocket = new MulticastSocket(8000)) {
//
//            muSocket.joinGroup(InetAddress.getByName("229.1.2.3"));    // 224.0.0.0 -- 239.255.255.255
//            String uniqueId = UUID.randomUUID().toString();
//            String hi = "live" + uniqueId;
//            byte[] hiBuff = hi.getBytes();
//            DatagramPacket outputPacket = new DatagramPacket(hiBuff, hiBuff.length, InetAddress.getByName("229.1.2.3"), 8000);
//            muSocket.send(outputPacket);
//
//            byte[] inputBuff = new byte[128];
//            Set<String> knownApps = new HashSet<>();
//
//            Thread printingHook = new Thread(() -> {
//                    System.out.println("In the middle of a shutdown");
//                    System.out.flush();
//            });
//            Runtime.getRuntime().addShutdownHook(printingHook);
//
////            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
////                try {
////                    String bye = "died" + uniqueId;
////                    byte[] byeBuff = bye.getBytes();
////                    DatagramPacket byePacket = new DatagramPacket(byeBuff, byeBuff.length, InetAddress.getByName("229.1.2.3"), 8000);
////                    muSocket.send(byePacket);
////                    muSocket.leaveGroup(InetAddress.getByName("229.1.2.3"));
////                    muSocket.close();
////                } catch (IOException e) {
////                    throw new RuntimeException(e);
////                }
////            }));
//
//            while (true) {
//                DatagramPacket inputPacket = new DatagramPacket(inputBuff, inputBuff.length);
//                muSocket.receive(inputPacket);
//                String status = new String(Arrays.copyOfRange(inputPacket.getData(), 0, 4));
//                String senderId = new String(Arrays.copyOfRange(inputPacket.getData(), 4, 40));
////                Integer senderPort = inputPacket.getPort();
////                String senderHost = inputPacket.getAddress().getHostAddress();
//                if (!knownApps.contains(senderId) && status.equals("live")) {
//                    knownApps.add(senderId);
//                    System.out.println("New app discovered: " + senderId);
//                    muSocket.send(outputPacket);
//
//                    System.out.println("All live apps:");
//                    knownApps.forEach(System.out::println);
//                    System.out.println();
//                } else if (status.equals("died")) {
//                    knownApps.remove(senderId);
//                    System.out.println("App closed: " + senderId);
//                    System.out.println("All live apps:");
//                    knownApps.forEach(System.out::println);
//                    System.out.println();
//                }
//            }
//
////
        catch (IllegalArgumentException rx) {
            System.err.println("Failed to create socket");
            rx.printStackTrace();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }

    }
}