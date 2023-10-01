package ru.nsu.fit.usoltsev;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.Callable;

import static ru.nsu.fit.usoltsev.Constants.*;

@Slf4j
public class ClientHandler implements Callable<Boolean> {

    /**
     * Socket with which this handler works
     */
    private final Socket clientSocket;

    /**
     * allBytesRead - all bytes read from socket in current time
     * prevBytesRead - bytes read from socket in previous time (~3s ago)
     */
    private long allBytesRead = 0, prevBytesRead = 0;

    private long curTime, prevTime, startTime;

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
        try (InputStream inputStream = clientSocket.getInputStream();
             OutputStream outputStream = clientSocket.getOutputStream();
             ObjectInputStream objectInputStream = new ObjectInputStream(inputStream)) {

            FileInfo fileInfo = (FileInfo) objectInputStream.readObject();

            log.info("Create file, start receiving data");

            receiveData(inputStream, fileInfo);

            clientSocket.shutdownInput();

            String successMsg = "success";
            outputStream.write(successMsg.getBytes());

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
     * @param inputStream input client-socket stream
     * @param fileInfo    FileInfo object
     * @throws IOException if it was not possible to read/write data from/to stream
     */
    private void receiveData(InputStream inputStream, FileInfo fileInfo) throws IOException {
        String filePath = "./src/main/resources/uploads/" + fileInfo.fileName();
        try (FileOutputStream fileOutputStream = new FileOutputStream(filePath)) {
            int bytesRead;
            byte[] buffer = new byte[BUFFER_SIZE];
            startTime = System.currentTimeMillis();
            prevTime = startTime;
            while ((bytesRead = inputStream.read(buffer)) != -1 || allBytesRead != fileInfo.fileSize()) {

                fileOutputStream.write(buffer, 0, bytesRead);

                allBytesRead += bytesRead;

                curTime = System.currentTimeMillis();

                speedCount(fileInfo);
            }
            curTime = System.currentTimeMillis();
            System.out.println("Total speed = " + (allBytesRead * 1000) / (curTime - startTime) + " byte/s");
        }
    }

    private void speedCount(FileInfo fileInfo) {

        if (curTime - prevTime > 3000) {
            long speed = ((allBytesRead - prevBytesRead) * 1000) / (curTime - prevTime);
            System.out.println("Current speed = " + speed + " byte/s; Appr wait time = "
                    + (fileInfo.fileSize() - allBytesRead) / speed + "s");
            prevBytesRead = allBytesRead;
            prevTime = curTime;
        }
    }
}
