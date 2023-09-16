package ru.nsu.fit.usoltsev;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class Main {
    public static void main(String[] args) {
        try {
            if (args.length != 3) {
                throw new IllegalArgumentException("Too few args");
            } else if (!InetAddress.getByName(args[1]).isMulticastAddress()) {
                throw new IllegalArgumentException("Incorrect ip address");
            } else if (!(0 < Integer.parseInt(args[2]) && Integer.parseInt(args[2]) < 65536)) {
                throw new IllegalArgumentException("Incorrect port");
            } else {
                switch (args[0]) {
                    case "-L" -> Listener.Listen(InetAddress.getByName(args[1]), Integer.parseInt(args[2]));
                    case "-S" -> Sender.Send(InetAddress.getByName(args[1]), Integer.parseInt(args[2]));
                }
            }
        } catch (IllegalArgumentException | UnknownHostException exc) {
            System.err.println(exc.getMessage());
        }
    }
}