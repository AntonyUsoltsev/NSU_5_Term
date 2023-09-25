package ru.nsu.fit.usoltsev;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
public class Server implements Constants{

    /**
     * Create thread pool, waiting income clients and append it to client-handler thread
     */
    public static void main(String[] args) {

        if (args.length != 1 || Integer.parseInt(args[0]) < MIN_PORT_NUMBER ||
                Integer.parseInt(args[0]) > MAX_PORT_NUMBER) {  // 65535 = Short.MAX_VALUE * 2 + 1
            log.warn("Incorrect port number");
            throw new IllegalArgumentException("Incorrect port number");
        }

        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();

        try (ServerSocket serverSocket = new ServerSocket(Integer.parseInt(args[0]))) {

            createDirectory();

            while (true) {
                Socket clientSocket = serverSocket.accept();
                Future<Boolean> future = executor.submit(new ClientHandler(clientSocket));
                log.info("Data receive " + ((future.get() == SUCCESS) ? "success" : "fail"));
                clientSocket.close();
            }

        } catch (IOException | IllegalArgumentException | InterruptedException | ExecutionException exc) {
            System.err.println(exc.getMessage());
            exc.printStackTrace(System.err);
        } finally {
            executor.shutdown();
        }
    }

    /**
     * Create an upload directory
     * @throws IOException if it was not possible to create directory
     */
    public static void createDirectory() throws IOException {
        Path dir = Path.of("./src/main/resources/uploads");
        Files.createDirectories(dir);
        if (Files.exists(dir)) {
            log.info("Directory created");
        } else {
            log.warn("Directory didnt create");
        }
    }
}
