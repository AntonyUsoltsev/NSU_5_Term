package ru.nsu.fit.usoltsev;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;
import java.util.concurrent.Callable;

public class ClientHandler implements Callable<Integer> {

    private final Socket clientSocket;
    private static final Integer SUCCESS = 1;
    private static final Integer FAIL = -1;

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public Integer call() {
        try {
            InputStream inputStream = clientSocket.getInputStream();
           // BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);

            FileInfo fileInfo = (FileInfo) objectInputStream.readObject();
//            objectInputStream.close();
//            char[] fileName = new char[4096];
//            int fileNameLen = reader.read(fileName);
//            String name = new String(Arrays.copyOfRange(fileName, 0, fileNameLen));
            FileOutputStream fileOutputStream = new FileOutputStream("./src/main/resources/uploads/" + fileInfo.getFileName());
            int bytesRead;
            long allBytesRead = 0;
            byte[] buffer = new byte[4096];
            while ((bytesRead = inputStream.read(buffer)) != -1 && allBytesRead != fileInfo.getFileSize()) {
                fileOutputStream.write(buffer, 0, bytesRead);
                allBytesRead += bytesRead;
            }
            clientSocket.close();
            return SUCCESS;
        } catch (IOException | ClassNotFoundException e) {
            System.err.println(e.getMessage());
            e.printStackTrace(System.err);
            return FAIL;
        }
    }


}
