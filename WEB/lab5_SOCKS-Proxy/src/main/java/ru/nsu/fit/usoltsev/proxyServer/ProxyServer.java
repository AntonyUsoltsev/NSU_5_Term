package ru.nsu.fit.usoltsev.proxyServer;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static ru.nsu.fit.usoltsev.proxyServer.SOCKS_Constants.*;

@Slf4j
public class ProxyServer implements AutoCloseable {

    private final Selector selector;
    private final ServerSocketChannel serverChannel;
    private final int port;

    public ProxyServer(int port) throws IOException {
        this.selector = SelectorProvider.provider().openSelector();
        this.serverChannel = ServerSocketChannel.open();
        serverChannel.socket().bind(new InetSocketAddress(InetAddress.getByName("localhost"), port));
        serverChannel.configureBlocking(false);                      // Устанавливаем неблокирующий режим
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);    // Регистрация в селекторе и получение ключа
        this.port = port;
    }

    public void run() {
        try {
            log.info("Proxy server start working at port: " + port);

            while (!Thread.currentThread().isInterrupted()) {

                int readyChannels = selector.select();            // Блокируется, пока не появится событие

                if (readyChannels >= 0) {
                    for (SelectionKey key : selector.selectedKeys()) {
                        try {
                            if (key.isValid()) {
                                if (key.isAcceptable()) {
                                    acceptClient();               // Принимаем входящее соединение
                                } else if (key.isConnectable()) {
                                    connect(key);                 // Завершаем соединение с сайтом
                                } else if (key.isReadable()) {
                                    readData(key);                // Читаем данные от канала
                                } else if (key.isWritable()) {
                                    writeData(key);               // Пишем данные в канал
                                }

                            }

                        } catch (IOException ignore) {
                        } catch (IllegalArgumentException exc) {
                            log.warn(new String(exc.getMessage().getBytes(StandardCharsets.UTF_8)), exc);
                        }
                    }

                    selector.selectedKeys().clear();
                }

            }
        } catch (IllegalArgumentException | IOException ioExc) {
            log.warn(ioExc.getMessage());
            ioExc.printStackTrace(System.err);
        }
    }

    private void acceptClient() throws IOException {
        SocketChannel clientChannel = serverChannel.accept();
        clientChannel.configureBlocking(false);
        clientChannel.register(selector, SelectionKey.OP_READ);
    }

    private void connect(@NotNull SelectionKey key) throws IOException {
        SocketChannel channel = ((SocketChannel) key.channel());
        Attachment attachment = ((Attachment) key.attachment());
        channel.finishConnect();

        sendSuccessAnswer((SocketChannel) attachment.getDstKey().channel());

        attachment.getDstKey().interestOps(SelectionKey.OP_READ);
        key.interestOps(0);
    }


    private void writeData(@NotNull SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        Attachment attachment = (Attachment) key.attachment();
        if (!attachment.getBuffer().hasRemaining()) {
            return;
        }

        Attachment dstAtch = (Attachment) attachment.getDstKey().attachment();
        int bytesWrite = channel.write(dstAtch.getBuffer());

        //log.info("Write from: " + channel.getLocalAddress() + " to: " + channel.getRemoteAddress() + " data: " + Arrays.toString(dstAtch.getBuffer().array()));

        if (bytesWrite == -1) {
            throw new IllegalArgumentException("Bytes write = -1");
        } else {
            dstAtch.getBuffer().flip();
            dstAtch.getBuffer().clear();
            attachment.getDstKey().interestOps(attachment.getDstKey().interestOps() | SelectionKey.OP_READ);
            key.interestOps(key.interestOps() ^ SelectionKey.OP_WRITE);
        }
    }


    private void readData(@NotNull SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        Attachment attachment = (Attachment) key.attachment();

        if (attachment == null) {
            attachment = new Attachment();
            key.attach(attachment);
        }
        int bytesRead = channel.read(attachment.getBuffer());

        if (bytesRead == 0 || bytesRead == -1) {
            return;
        } else if (attachment.getDstKey() == null) {           // Если нет второго конца значит читаем заголовок
            readHeader(channel, attachment, key, bytesRead);
            // log.info("Read header from: " + channel.getRemoteAddress() + " to: " + channel.getLocalAddress() + " data: " + Arrays.toString(attachment.getBuffer().array()));
        } else {
            // log.info("Read from: " + channel.getRemoteAddress() + " to: " + channel.getLocalAddress() + " data: " + Arrays.toString(attachment.getBuffer().array()));
            key.interestOps(key.interestOps() ^ SelectionKey.OP_READ);
            attachment.getDstKey().interestOps(attachment.getDstKey().interestOps() | SelectionKey.OP_WRITE);
            attachment.getBuffer().flip();
        }
    }

    public void readHeader(SocketChannel clientChannel, @NotNull Attachment attachment, SelectionKey key, int length) throws IOException {
        byte[] header = attachment.getBuffer().array();
        if (header.length < 3) {
            sendFailAnswer(clientChannel, SERVER_FAIL);
            throw new IllegalArgumentException("Incorrect header");
        }
        if (header[0] != SOCKS_5) {
            log.warn("Incorrect SOCKS version");
            sendFailAnswer(clientChannel, SERVER_FAIL);
            throw new IllegalArgumentException("Incorrect SOCKS version");
        }

        switch (attachment.getStatus()) {
            case Attachment.AUTH -> {
                authenticationHandler(header, clientChannel, attachment);
                attachment.getBuffer().flip();
                attachment.getBuffer().clear();
            }
            case Attachment.REQUEST -> {
                requestHandler(header, key, length);
                attachment.getBuffer().flip();
                attachment.getBuffer().clear();
            }
        }
    }

    private void authenticationHandler(byte @NotNull [] header, SocketChannel clientChannel, Attachment attachment) throws IOException {
        //log.info("Auth, first header");
        int NMethods = header[1];
        boolean noAuthFlag = false;
        for (int i = 0; i < NMethods; i++) {
            if (header[i + 2] == NO_AUTH_REQUIRED) {
                noAuthFlag = true;
                break;
            }
        }
        if (noAuthFlag) {
            clientChannel.write((ByteBuffer.wrap(new byte[]{SOCKS_5, NO_AUTH_REQUIRED})));
            attachment.setStatus(Attachment.REQUEST);

        } else {
            log.warn("Client has not NoAuth method");
            sendFailAnswer(clientChannel, CONNECTION_NOT_ALLOWED);
            throw new IllegalArgumentException("Client has not NoAuth method");
        }
    }

    private void requestHandler(byte @NotNull [] header, SelectionKey key, int length) throws IOException {
        //log.info("Request, second header");
        if (header[1] == CONNECT) {
            byte[] addr;
            int port;
            switch (header[3]) {
                case IPV4 -> {
                    addr = new byte[]{header[4], header[5], header[6], header[7]};
                    port = (header[8] & 0xFF) << 8 | (header[9] & 0xFF);
                    connectToSite(addr, port, IPV4, key);
                }
                case DNS -> {
                    int domainLength = header[4] & 0xFF;
                    addr = Arrays.copyOfRange(header, 5, 5 + domainLength);
                    port = (header[length - 2] & 0xFF) << 8 | (header[length - 1] & 0xFF);
                    //   String domainName = new String(addr, StandardCharsets.UTF_8);
                    connectToSite(addr, port, DNS, key);

                    //TODO: RESOLVE DNS
                }
                default -> {
                    log.warn("Bad Ip address type");
                    sendFailAnswer((SocketChannel) key.channel(), ADDR_TYPE_NOT_SUP);
                    throw new IllegalArgumentException("Bad Ip address type");
                }
            }

        } else {
            log.warn("Bad CMD in second header");
            sendFailAnswer((SocketChannel) key.channel(), COMMAND_NOT_SUP);
            throw new IllegalArgumentException("Bad CMD in second header");
        }
    }

    private void connectToSite(byte[] addr, int port, byte type, SelectionKey key) throws IOException {
        try {
            SocketChannel siteChanel = SocketChannel.open();
            siteChanel.configureBlocking(false);

            switch (type) {
                case IPV4 -> siteChanel.connect(new InetSocketAddress(InetAddress.getByAddress(addr), port));
                case DNS -> siteChanel.connect(new InetSocketAddress(InetAddress.getByName(new String(addr)), port));
            }

            SelectionKey dstKey = siteChanel.register(selector, SelectionKey.OP_CONNECT);
            key.interestOps(0);

            Attachment attachment = (Attachment) key.attachment();
            attachment.setDstKey(dstKey);

            Attachment dstAttachment = new Attachment();
            dstAttachment.setDstKey(key);
            dstKey.attach(dstAttachment);

        } catch (UnknownHostException exc) {
            log.warn("Unknown Host Exception");
            sendFailAnswer((SocketChannel) key.channel(), HOST_UNREACHABLE);
            throw new IllegalArgumentException("Unknown Host Exception");
        }
    }

    private void closeConnection(@NotNull SelectionKey key) throws IOException {
        SelectionKey dstKey = ((Attachment) key.attachment()).getDstKey();
        if (dstKey != null) {
            dstKey.channel().close();
            dstKey.cancel();
        }
        //log.info("Close connection");
        key.cancel();
        key.channel().close();


//        if (dstKey != null) {
//            ((Attachment) dstKey.attachment()).setDstKey(null);
//            if ((dstKey.interestOps() & SelectionKey.OP_WRITE) == 0) {
//                ((Attachment) dstKey.attachment()).getOutputBuffer().flip();
//            }
//            dstKey.interestOps(SelectionKey.OP_WRITE);
//        }

    }

    private void sendFailAnswer(@NotNull SocketChannel channel, byte flag) throws IOException {
        byte[] ans = new byte[]{SOCKS_5, flag, RSV};
        log.info("send fail");
        channel.write(ByteBuffer.wrap(ans));
    }

    private void sendSuccessAnswer(@NotNull SocketChannel channel) throws IOException {
        ByteBuffer responseBuffer = ByteBuffer.allocate(256);
        responseBuffer.put(SOCKS_5);
        responseBuffer.put(SUCCESS);
        responseBuffer.put(RSV);
        responseBuffer.put(IPV4);
        responseBuffer.put(InetAddress.getLoopbackAddress().getAddress());

        responseBuffer.putShort((short) port); ////////!!!!!!!!!!!!!!!!!

        responseBuffer.flip();
        channel.write(responseBuffer);
    }


    @Override
    public void close() throws Exception {
        selector.close();
        serverChannel.close();
    }


}