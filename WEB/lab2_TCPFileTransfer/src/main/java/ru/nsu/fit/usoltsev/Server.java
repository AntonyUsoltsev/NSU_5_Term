package ru.nsu.fit.usoltsev;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
public class Server {

    public static void main(String[] args) {

        if (args.length != 1 || Integer.parseInt(args[0]) < 1 || Integer.parseInt(args[0]) > 65535) {  // 65535 = Short.MAX_VALUE * 2 + 1
            log.warn("Incorrect port number");
            throw new IllegalArgumentException("Incorrect port number");
        }

        createDirectory();

        ThreadPoolExecutor executor  = (ThreadPoolExecutor) Executors.newCachedThreadPool();;
        try (ServerSocket serverSocket = new ServerSocket(Integer.parseInt(args[0]))) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                executor.submit(new ClientHandler(clientSocket));
            }

        } catch (IOException | IllegalArgumentException exc) {
            System.err.println(exc.getMessage());
        }
        finally {
            executor.shutdown();
        }
    }

    public static void createDirectory(){
        try {
            Files.createDirectories(Path.of("./src/main/resources/uploads"));
            if (Files.exists(Path.of("./src/main/resources/uploads"))) {
                System.out.println("Directory created");
            } else {
                System.err.println("Directory didnt create");
            }
        }
        catch (IOException ioexc){
            System.err.println(ioexc.getMessage());
        }
    }
}
