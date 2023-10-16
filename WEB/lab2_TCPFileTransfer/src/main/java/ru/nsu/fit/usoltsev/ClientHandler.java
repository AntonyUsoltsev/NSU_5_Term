package ru.nsu.fit.usoltsev;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.Callable;

import static ru.nsu.fit.usoltsev.Constants.*;

@Slf4j
public class ClientHandler extends Thread{

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
    public void run() {
        try (InputStream inputStream = clientSocket.getInputStream();
             OutputStream outputStream = clientSocket.getOutputStream();
             ObjectInputStream objectInputStream = new ObjectInputStream(inputStream)) {

            FileInfo fileInfo = (FileInfo) objectInputStream.readObject();

            log.info("Create file, start receiving data");

            receiveData(inputStream, fileInfo);

            clientSocket.shutdownInput();

            String successMsg = "success";
            outputStream.write(successMsg.getBytes());

            clientSocket.close();
            //return SUCCESS;

        } catch (IOException | ClassNotFoundException e) {
            System.err.println(e.getMessage());
            e.printStackTrace(System.err);
            try {
                clientSocket.close();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            // return FAIL;
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
        File receiveFile = new File(filePath);
        if (receiveFile.exists()) {
            throw new IOException("Receive file already exists");
        }
        try (FileOutputStream fileOutputStream = new FileOutputStream(filePath)) {
            int bytesRead;
            byte[] buffer = new byte[BUFFER_SIZE];
            startTime = System.currentTimeMillis();
            prevTime = startTime;
            while ((bytesRead = inputStream.read(buffer)) != -1 || allBytesRead != fileInfo.fileSize()) {

                fileOutputStream.write(buffer, 0, bytesRead);

                allBytesRead += bytesRead;

                curTime = System.currentTimeMillis();

                speedCount(fileInfo.fileSize(), fileInfo.fileName());
            }
            curTime = System.currentTimeMillis();
            log.info(fileInfo.fileName() + " Total speed = " + (allBytesRead * 1000) / (1024 * 1024 * (curTime - startTime)) + " MB/s");
        }
    }


    /**
     * Count current speed of receiving
     * @param fileSize - size of received file
     */
    private void speedCount(Long fileSize, String fileName) {
        if (curTime - prevTime > 3000) {
            long speed = ((allBytesRead - prevBytesRead) * 1000) / (curTime - prevTime);
            log.info(fileName + " Current speed = " + speed / (1024 * 1024) + " MB/s; Appr wait time = "
                    + (fileSize - allBytesRead) / speed + "s");
            prevBytesRead = allBytesRead;
            prevTime = curTime;
        }
    }
}
