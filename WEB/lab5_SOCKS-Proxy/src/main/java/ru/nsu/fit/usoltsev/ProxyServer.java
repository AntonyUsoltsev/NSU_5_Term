package ru.nsu.fit.usoltsev;

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

@Slf4j
public class ProxyServer {

    public static void main(String[] args) {
        try {
            int port = parseArgs(args);

            ServerSocketChannel serverChannel = ServerSocketChannel.open();
            serverChannel.socket().bind(new InetSocketAddress(port));
            serverChannel.configureBlocking(false);                     // Устанавливаем неблокирующий режим

            Selector selector = Selector.open();
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);   //Здесь же устанавливается ключ в селектор

            log.info("Proxy server start working");

            while (true) {

                int readyChannels = selector.select();                   // Блокируется, пока не появится событие

                if (readyChannels > 0) {
                    for (SelectionKey key : selector.selectedKeys()) {
                        if (key.isValid()) {
                            try {
                                if (key.isAcceptable()) {
                                    acceptClient(serverChannel, selector);    // Принимаем входящее соединение
                                } else if (key.isReadable()) {
                                    readData(key);
                                } else if (key.isWritable()) {
                                    writeData(key);
                                }
                            } catch (IOException | IllegalArgumentException exc) {
                                log.warn(exc.getMessage(), exc);
                                closeConnection(key);
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

    private static void acceptClient(@NotNull ServerSocketChannel serverChannel, Selector selector) throws IOException {
        SocketChannel clientChannel = serverChannel.accept();
        clientChannel.configureBlocking(false);
        clientChannel.register(selector, SelectionKey.OP_READ);
        log.info("Client connected");
    }

    private static void writeData(@NotNull SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        Attachment attachment = (Attachment) key.attachment();
        int bytesWrite = channel.write(attachment.getOutputBuffer());
        if (bytesWrite == -1) {
            throw new IllegalArgumentException("Bytes write = -1");
        } else {
            attachment.getDstKey().interestOps(attachment.getDstKey().interestOps() | SelectionKey.OP_READ);
            key.interestOps(key.interestOps() ^ SelectionKey.OP_WRITE);
            attachment.getOutputBuffer().clear();
        }
    }


    private static void readData(@NotNull SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        Attachment attachment = (Attachment) key.attachment();
        if (attachment == null) {
            Attachment newAttch = new Attachment();
            key.attach(newAttch);
        }
        attachment = (Attachment) key.attachment();
        int bytesRead = channel.read(attachment.getInputBuffer());
        if (bytesRead < 1) {
            throw new IllegalArgumentException("Bytes read < 1");
        } else if (attachment.getDstKey() == null) { // если нету второго конца значит читаем заголовок
            readHeader(key, bytesRead);
        } else {
            Attachment dstAttach = (Attachment) attachment.getDstKey().attachment();

            attachment.getDstKey().interestOps(attachment.getDstKey().interestOps() | SelectionKey.OP_WRITE);
            key.interestOps(key.interestOps() ^ SelectionKey.OP_READ);
            attachment.getInputBuffer().flip();
            dstAttach.getOutputBuffer().put(attachment.getInputBuffer());
            attachment.getInputBuffer().clear();
        }
    }

    public static void readHeader(@NotNull SelectionKey key, int length) throws IOException {
        SocketChannel clientChannel = (SocketChannel) key.channel();
        Attachment attachment = (Attachment) key.attachment();
        byte[] header = attachment.getInputBuffer().array();
        if (header.length < 3) {
            log.warn("Incorrect header");
            sendFailAnswer(clientChannel, (byte) 0x01);
            throw new IllegalArgumentException("Incorrect header");
        }
        if (header[0] != (byte) 0x05) {
            log.warn("Incorrect SOCKS version");
            sendFailAnswer(clientChannel, (byte) 0x01);
            throw new IllegalArgumentException("Incorrect SOCKS version");
        }

        switch (attachment.getStatus()) {
            case Attachment.AUTH -> {
                log.info("Auth, first header");
                int NMethods = header[1];
                boolean noAuthFlag = false;
                for (int i = 0; i < NMethods; i++) {
                    if (header[i + 2] == 0x00) {
                        noAuthFlag = true;
                        break;
                    }
                }
                if (noAuthFlag) {
                    clientChannel.write((ByteBuffer.wrap(new byte[]{5, 0})));
                    attachment.setStatus(Attachment.REQUEST);
                    attachment.getInputBuffer().clear();

                } else {
                    log.warn("Client has not NoAuth method");
                    sendFailAnswer(clientChannel, (byte) 0x02);
                    throw new IllegalArgumentException("Client has not NoAuth method");
                }
            }
            case Attachment.REQUEST -> {
                log.info("Request, second header");
                if (header[1] == (byte) 0x01) {
                    byte[] addr;
                    int port;
                    switch (header[3]) {
                        case (byte) 0x01 -> {
                            addr = new byte[]{header[4], header[5], header[6], header[7]};
                            port = (header[8] & 0xFF) << 8 | (header[9] & 0xFF);
                            connectToSite(addr, port, key);
                            sendSuccessAnswer(clientChannel, (byte) 0x01, addr, new byte[]{header[8], header[9]});
                        }
                        case (byte) 0x03 -> {
                            int domainLength = header[4] & 0xFF;
                            addr = new byte[domainLength];
                            System.arraycopy(header, 5, addr, 0, domainLength);
                            port = (header[length - 2] & 0xFF) << 8 | (header[length - 1] & 0xFF);
                            //   String domainName = new String(addr, StandardCharsets.UTF_8);
                            connectToSite(addr, port, key);
                            sendSuccessAnswer(clientChannel, (byte) 0x03, addr, new byte[]{header[length - 2], header[length - 1]});
                            //TODO: RESOLVE DNS
                        }
                        default -> {
                            log.warn("Bad Ip address type");
                            sendFailAnswer(clientChannel, (byte) 0x08);
                            throw new IllegalArgumentException("Bad Ip address type");
                        }
                    }

                } else {
                    log.warn("Bad CMD in second header");
                    sendFailAnswer(clientChannel, (byte) 0x07);
                    throw new IllegalArgumentException("Bad CMD in second header");
                }
            }
        }
    }

    private static void connectToSite(byte[] addr, int port, @NotNull SelectionKey key) throws IOException {
        try {
            SocketChannel siteChanel = SocketChannel.open();
            siteChanel.configureBlocking(false);

            //  String address = new String(addr, StandardCharsets.UTF_8);
            log.info("ip: " + InetAddress.getByAddress(addr) + ", port: " + port);
            siteChanel.connect(new InetSocketAddress(InetAddress.getByAddress(addr), port));
            SelectionKey dstKey = siteChanel.register(key.selector(), SelectionKey.OP_CONNECT);

            Attachment attachment = (Attachment) key.attachment();
            attachment.setDstKey(dstKey);

            Attachment dstAttachment = new Attachment();
            dstAttachment.setDstKey(key);
            dstKey.attach(dstAttachment);

        } catch (UnknownHostException exc) {
            log.warn("Unknown Host Exception");
            sendFailAnswer((SocketChannel) key.channel(), (byte) 0x04);
            throw new IllegalArgumentException("Unknown Host Exception");
        }
    }

    private static void closeConnection(SelectionKey key) throws IOException {
        log.info("Close connection");
        key.cancel();
        key.channel().close();

        SelectionKey dstKey = ((Attachment) key.attachment()).getDstKey();
        if (dstKey != null) {
            ((Attachment) dstKey.attachment()).setDstKey(null);
            if ((dstKey.interestOps() & SelectionKey.OP_WRITE) == 0) {
                ((Attachment) dstKey.attachment()).getOutputBuffer().flip();
            }
            dstKey.interestOps(SelectionKey.OP_WRITE);
        }

    }

    private static void sendFailAnswer(SocketChannel channel, byte flag) throws IOException {
        byte[] ans = new byte[]{(byte) 0x05, flag, (byte) 0x00};
        channel.write(ByteBuffer.wrap(ans));
    }

    private static void sendSuccessAnswer(SocketChannel channel, byte type, byte[] address, byte[] port) throws IOException {
        ByteBuffer responseBuffer = ByteBuffer.allocate(256);
        responseBuffer.put((byte) 0x05);
        responseBuffer.put((byte) 0x00);
        responseBuffer.put((byte) 0x00);
        responseBuffer.put(type);
        responseBuffer.put(address);
        responseBuffer.put(port);
        responseBuffer.flip();

        channel.write(responseBuffer);
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