package ru.nsu.fit.usoltsev;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

@Slf4j
public class Client implements Constants {
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
            log.info("Data send completed");
        } catch (IOException | IllegalArgumentException ioExc) {
            System.err.println(ioExc.getMessage());
            ioExc.printStackTrace(System.err);
        }
    }

    private static void sendData(String filePath, InetAddress ipAddr, int port) throws IOException {
        try (Socket client = new Socket(ipAddr, port);
             OutputStream outputStream = client.getOutputStream()) {

            File sendfile = new File(filePath);
            String fileName = sendfile.getName();
            FileInputStream fileDataStream = new FileInputStream(sendfile);

            FileInfo fileInfo = new FileInfo(fileName, sendfile.length());

            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
            objectOutputStream.writeObject(fileInfo);
            log.info("Send file info, start sending file data");
            int bytesRead;
            byte[] buffer = new byte[BUFFER_SIZE];
            while ((bytesRead = fileDataStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }
    }
}
