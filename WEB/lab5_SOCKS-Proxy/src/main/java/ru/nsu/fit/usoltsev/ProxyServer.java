package ru.nsu.fit.usoltsev;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

@Slf4j
public class ProxyServer {

//    Selector selector;
//    SocketChannel channel;


    public static void main(String[] args) {
        try {
            int port = parseArgs(args);

            ServerSocketChannel serverChannel = ServerSocketChannel.open();
            serverChannel.socket().bind(new InetSocketAddress(port));
            serverChannel.configureBlocking(false); // Устанавливаем неблокирующий режим

            Selector selector = Selector.open();
            serverChannel.register(selector, SelectionKey.OP_ACCEPT); //Здесь же устанавливается ключ в селектор

            ByteBuffer buffer = ByteBuffer.allocate(1024);
            log.info("Server start working");

            while (true) {
                int readyChannels = selector.select(); // Блокируется, пока не появится событие

                if (readyChannels > 0) {
                    for (SelectionKey key : selector.selectedKeys()) {
                        if (key.isAcceptable()) {
                            // Принимаем входящее соединение
                            SocketChannel clientChannel = serverChannel.accept();
                            clientChannel.configureBlocking(false);
                            clientChannel.register(selector, SelectionKey.OP_READ);
                            log.info("Client connected");
                        } else if (key.isReadable()) {
                            // Чтение данных
                            SocketChannel clientChannel = (SocketChannel) key.channel();
                            int bytesRead = clientChannel.read(buffer);
                            if (bytesRead == -1) {
                                clientChannel.close();
                            } else if (bytesRead > 0) {
                                buffer.flip();
                                // Обработка данных
                                log.info("Read data from client");

                                while (buffer.hasRemaining()) {
                                    System.out.println((char) buffer.get());
                                }
                                buffer.clear();
                            }
                        }
                    }
                    selector.selectedKeys().clear();
                }
            }
        } catch (IOException | IllegalArgumentException ioExc) {
            log.warn(ioExc.getMessage());
            ioExc.printStackTrace(System.err);
        }
    }


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