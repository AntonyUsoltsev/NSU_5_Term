package ru.nsu.fit.usoltsev;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

public class Client {
    public static void main(String[] args) {
        try {
            String filePath = args[0];
            InetAddress ipAddr = InetAddress.getByName(args[1]);
            int port = Integer.parseInt(args[2]);

            try (Socket client = new Socket(ipAddr, port)) {
                OutputStream outputStream = client.getOutputStream();
                File sendfile = new File(filePath);
                String fileName = sendfile.getName();
//                byte[] messageBytes = fileName.getBytes(StandardCharsets.UTF_8);
//                outputStream.write(messageBytes);
                FileInputStream fileData = new FileInputStream(sendfile);
                FileInfo fileInfo = new FileInfo(fileName, sendfile.length());
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
                objectOutputStream.writeObject(fileInfo);
                int bytesRead;
                byte[] buffer = new byte[4096];
                while ((bytesRead = fileData.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            } catch (IOException ioException) {
                System.err.println(ioException.getMessage());
            }
        } catch (IOException ue) {
            System.err.println(ue.getMessage());
        }

    }
}
