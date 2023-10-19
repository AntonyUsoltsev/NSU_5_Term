package ru.nsu.fit.usoltsev;

import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.net.InetSocketAddress;
import java.io.IOException;

@Slf4j
public class Client {
    public static void main(String[] args) throws IOException {
        // Создаем клиентский сокс
        SocketChannel clientChannel = SocketChannel.open();

        // Устанавливаем неблокирующий режим
        clientChannel.configureBlocking(false);

        // Подключаемся к серверу
        clientChannel.connect(new InetSocketAddress(1080));

        log.info("Client created");
        // Ожидаем завершения соединения
        while (!clientChannel.finishConnect()) {
            // Можно выполнять другие задачи, пока соединение не завершится
        }

        // Теперь clientChannel подключен к серверному ServerSocketChannel

        ByteBuffer buffer = ByteBuffer.allocate(1024);
        String message = "Hello, Server!";
        buffer.put(message.getBytes());
        buffer.flip();

        // Отправляем данные на сервер
        while (buffer.hasRemaining()) {
            clientChannel.write(buffer);
        }

        // Закрываем клиентский сокс
        clientChannel.close();
        log.info("Client finished");
    }
}