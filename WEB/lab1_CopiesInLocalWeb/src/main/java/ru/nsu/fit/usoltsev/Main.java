package ru.nsu.fit.usoltsev;

import java.net.InetAddress;
import java.net.UnknownHostException;


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