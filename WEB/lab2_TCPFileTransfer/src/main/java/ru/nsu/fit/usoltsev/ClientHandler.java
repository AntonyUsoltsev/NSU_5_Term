package ru.nsu.fit.usoltsev;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.Callable;

@Slf4j
public class ClientHandler implements Callable<Boolean>, Constants {

    /**
     * Socket with which this handler works
     */
    private final Socket clientSocket;

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    /**
     * Create file with same name as send file from client, and then receive file data
     * and also count the speed of file transfer
     *
     * @return SUCCESS == 1 if file receive done correctly, else return FAIL == 0
     */
    @Override
    public Boolean call() {
        log.info("New client connected");
        try (InputStream inputStream = clientSocket.getInputStream();
             ObjectInputStream objectInputStream = new ObjectInputStream(inputStream)) {

            FileInfo fileInfo = (FileInfo) objectInputStream.readObject();

            FileOutputStream fileOutputStream = new FileOutputStream("./src/main/resources/uploads/" + fileInfo.fileName());
            log.info("Create file, start receiving data");

            receiveData(fileOutputStream, inputStream, fileInfo);

            fileOutputStream.close();

            return SUCCESS;
        } catch (IOException | ClassNotFoundException e) {
            System.err.println(e.getMessage());
            e.printStackTrace(System.err);
            return FAIL;
        }
    }

    /**
     * Receive file data from client-socket and write it to result file
     *
     * @param fileOutputStream output file stream into which write input data
     * @param inputStream input client-socket stream
     * @param fileInfo FileInfo object
     * @throws IOException if it was not possible to read/write data from/to stream
     */
    private void receiveData(FileOutputStream fileOutputStream, InputStream inputStream, FileInfo fileInfo) throws IOException {
        int bytesRead;
        long allBytesRead = 0, prevBytesRead = 0;
        byte[] buffer = new byte[BUFFER_SIZE];

        long curTime, prevTime = System.currentTimeMillis(), startTime = System.currentTimeMillis();

        while ((bytesRead = inputStream.read(buffer)) != -1 && allBytesRead != fileInfo.fileSize()) {
            fileOutputStream.write(buffer, 0, bytesRead);
            allBytesRead += bytesRead;

            curTime = System.currentTimeMillis();
            if (curTime - prevTime > 3000) {
                long speed = ((allBytesRead - prevBytesRead) * 1000) / (curTime - prevTime);
                System.out.println("Current speed = " + speed + " byte/s; Appr wait time = "
                        + (fileInfo.fileSize() - allBytesRead) / speed + "s");
                prevBytesRead = allBytesRead;
                prevTime = curTime;
            }

        }
        curTime = System.currentTimeMillis();
        System.out.println("Total speed = " + (allBytesRead * 1000) / (curTime - startTime) + " byte/s");
    }
}
