package ru.nsu.fit.usoltsev;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import ru.nsu.fit.usoltsev.proxyServer.ProxyServer;

import java.io.IOException;

@Slf4j
public class Main {
    public static void main(String[] args) {
        try {
            int port = parseArgs(args);

            try (ProxyServer proxyServer = new ProxyServer(port)) {
                proxyServer.run();
            } catch (IllegalArgumentException | IOException e) {
                log.warn(e.getMessage(), e);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * Parse port number from command arguments
     * @param args array of command arguments
     * @return port number if it corrects, else throws IllegalArgumentException
     */
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
