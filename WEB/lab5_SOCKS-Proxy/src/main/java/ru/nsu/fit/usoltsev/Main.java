package ru.nsu.fit.usoltsev;

import org.jetbrains.annotations.NotNull;

public class Main {
    public static void main(String[] args) {
        try {
            int port = parseArgs(args);
            ProxyServer proxyServer = new ProxyServer();
            proxyServer.run(port);

        } catch (IllegalArgumentException e) {

        }

    }

    public static int parseArgs(String @NotNull [] args) {
        try {
            if (args.length != 1) {
                throw new IllegalArgumentException("Wrong args count");
            }
            int port = Integer.parseInt(args[0]);
            if (port < 0 || port > 65536) {
                throw new IllegalArgumentException("Invalid port");
            } else {
                return port;
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Port isn't a number, exc: " + e.getMessage());
        }
    }
}
