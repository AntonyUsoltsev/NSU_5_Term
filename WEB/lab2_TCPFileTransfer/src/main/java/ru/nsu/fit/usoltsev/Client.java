package ru.nsu.fit.usoltsev;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;
import java.nio.charset.StandardCharsets;

import static ru.nsu.fit.usoltsev.Constants.*;

/**
 * Establishes a connection to the server, sends it the file name and its size, and then the file itself
 */
@Slf4j
public class Client {
    public static void main(String[] args) {
        try {
            if (args.length != 3) {
                throw new IllegalArgumentException("Too few command args");
            }
            String filePath = args[0];
            InetAddress ipAddr = InetAddress.getByName(args[1]);
            int port = Integer.parseInt(args[2]);
            if (port < MIN_PORT_NUMBER || port > MAX_PORT_NUMBER) {
                throw new IllegalArgumentException("Incorrect port number");
            }

            log.info("Valid command arguments");

            sendData(filePath, ipAddr, port);

        } catch (IOException | IllegalArgumentException ioExc) {
            System.err.println(ioExc.getMessage());
            ioExc.printStackTrace(System.err);
        }
    }

    /**
     * Create client socket, create FileInfo object, serialize it and send to the server and then send file data
     *
     * @param filePath path to the send file
     * @param ipAddr   ip address of the server
     * @param port     port of the server
     * @throws IOException if it was not possible to create a socket, open an input stream, serialize object
     *                     or read data from a file
     */
    private static void sendData(String filePath, InetAddress ipAddr, int port) throws IOException {
        try (Socket client = new Socket(ipAddr, port);
             OutputStream outputStream = client.getOutputStream();
             InputStream inputStream = client.getInputStream()) {

            File sendfile = new File(filePath);
            String fileName = sendfile.getName();
            if (sendfile.length() >= TERABYTE){
                throw new RuntimeException("Bad file size");
            }
            if (fileName.getBytes(StandardCharsets.UTF_8).length >= MAX_FILE_NAME_SIZE){
                throw new RuntimeException("Bad file name size");
            }

            FileInputStream fileDataStream = new FileInputStream(sendfile);
            FileInfo fileInfo = new FileInfo(new String(fileName.getBytes(), StandardCharsets.UTF_8), sendfile.length());

            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
            objectOutputStream.writeObject(fileInfo);
            log.info("Send file info, start sending file data");

            int bytesRead;
            byte[] buffer = new byte[BUFFER_SIZE];
            while ((bytesRead = fileDataStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            client.shutdownOutput();

            receiveAnswer(inputStream);
        }
    }

    /**
     * Receive success-answer from server
     *
     * @param inputStream socket input stream
     * @throws IOException if it was not possible read data from a socket
     */
    private static void receiveAnswer(@NotNull InputStream inputStream) throws IOException {
        byte[] successBuffer = new byte[BUFFER_SIZE];
        int successBytesRead = inputStream.read(successBuffer);
        String successMsg = new String(Arrays.copyOf(successBuffer, successBytesRead));
        if (successMsg.equals("success")) {
            log.info("Data send completed");
        } else {
            log.error("Data send failed on server");
        }
    }
}
