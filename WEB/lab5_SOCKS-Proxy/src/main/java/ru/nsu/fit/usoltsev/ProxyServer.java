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
import java.util.Arrays;

@Slf4j
public class ProxyServer {
    private static final byte SOCKS_5 = 0x05;
    private static final byte NO_AUTH_REQUIRED = 0x00;
    private static final byte IPV4 = 0x01;
    private static final byte DNS = 0x03;
    private static final byte RSV = 0x00;
    private static final byte CONNECT = 0x01;
    private static final byte SUCCESS = 0x00;
    private static final byte SERVER_FAIL = 0x01;
    private static final byte CONNECTION_NOT_ALLOWED = 0x02;
    private static final byte HOST_UNREACHABLE = 0x04;
    private static final byte COMMAND_NOT_SUP = 0x07;
    private static final byte ADDR_TYPE_NOT_SUP = 0x08;

    private static byte IP_TYPE;

    public void run(int port) {
        try {
            ServerSocketChannel serverChannel = ServerSocketChannel.open();
            serverChannel.socket().bind(new InetSocketAddress(InetAddress.getByName("localhost"), port));
            serverChannel.configureBlocking(false);                      // Устанавливаем неблокирующий режим

            Selector selector = Selector.open();
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);    //Здесь же устанавливается ключ в селектор

            log.info("Proxy server start working");

            while (!Thread.currentThread().isInterrupted()) {

                int readyChannels = selector.select();                   // Блокируется, пока не появится событие

                if (readyChannels > 0) {
                    for (SelectionKey key : selector.selectedKeys()) {
                        if (key.isValid()) {
                            try {
                                if (key.isAcceptable()) {
                                    acceptClient(serverChannel, selector);    // Принимаем входящее соединение
                                } else if (key.isConnectable()) {
                                    connect(key);
                                } else if (key.isReadable()) {
                                    readData(key);
                                } else if (key.isWritable()) {
                                    writeData(key);
                                }
                            } catch (IOException | IllegalArgumentException exc) {
                                //log.warn(new String(exc.getMessage().getBytes(StandardCharsets.UTF_8)), exc);
                                //  closeConnection(key);
                            }
                        }
                    }
                    selector.selectedKeys().clear();
                }

            }
        } catch (IOException | IllegalArgumentException ioExc) {
            //log.warn(ioExc.getMessage());
            ioExc.printStackTrace(System.err);
        }
    }

    private void acceptClient(@NotNull ServerSocketChannel serverChannel, Selector selector) throws IOException {
        SocketChannel clientChannel = serverChannel.accept();
        clientChannel.configureBlocking(false);
        clientChannel.register(selector, SelectionKey.OP_READ);
        //log.info("Client connected");
    }

    private void connect(SelectionKey key) throws IOException {
        SocketChannel channel = ((SocketChannel) key.channel());
        Attachment attachment = ((Attachment) key.attachment());
        channel.finishConnect();

        sendSuccessAnswer((SocketChannel) ((Attachment) key.attachment()).getDstKey().channel(), IPV4);//??????????????
        //  sendSuccessAnswer((SocketChannel) key.channel(), IPV4);
        //log.info("Finish connect");

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
        channel.write(dstAtch.getBuffer());

        log.info("Write from: " + channel.getLocalAddress() + " to: " + channel.getRemoteAddress() + " data: " + Arrays.toString(dstAtch.getBuffer().array()));

        int bytesWrite = channel.write(((Attachment) attachment.getDstKey().attachment()).getBuffer());
        if (bytesWrite == -1) {
            throw new IllegalArgumentException("Bytes write = -1");
        } else {
            dstAtch.getBuffer().flip();
            dstAtch.getBuffer().clear();
            attachment.getDstKey().interestOps(SelectionKey.OP_READ);
            key.interestOps(key.interestOps() ^ SelectionKey.OP_WRITE);
            //key.interestOps(SelectionKey.OP_READ);
            //  attachment.getDstKey().interestOps(SelectionKey.OP_READ);
            key.interestOps(SelectionKey.OP_READ);

        }
    }


    private void readData(@NotNull SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        Attachment attachment = (Attachment) key.attachment();
        if (attachment == null) {
            Attachment newAttch = new Attachment();
            key.attach(newAttch);
        }
        attachment = (Attachment) key.attachment();
        int bytesRead = channel.read(attachment.getBuffer());

        if (bytesRead == 0) {
            //log.warn("Bytes read == 0");
            // throw new IllegalArgumentException("Bytes read < 1");
            return;
        } else if (bytesRead == -1) {
            //  //log.warn("Bytes read == -1");
            // throw new IllegalArgumentException("Bytes read < 1");
            return;
        } else if (attachment.getDstKey() == null) { // если нет второго конца значит читаем заголовок
            readHeader(key, bytesRead);
        } else {
            log.info("Read from: " + channel.getRemoteAddress() + " to: " + channel.getLocalAddress() + " data: " + Arrays.toString(attachment.getBuffer().array()));            //log.info("Read data:");
            key.interestOps(key.interestOps() ^ SelectionKey.OP_READ);
            attachment.getDstKey().interestOps(attachment.getDstKey().interestOps() | SelectionKey.OP_WRITE);
            attachment.getBuffer().flip();
        }
    }

    public void readHeader(@NotNull SelectionKey key, int length) throws IOException {
        SocketChannel clientChannel = (SocketChannel) key.channel();
        Attachment attachment = (Attachment) key.attachment();
        byte[] header = attachment.getBuffer().array();
        if (header.length < 3) {
            //log.warn("Incorrect header");
            sendFailAnswer(clientChannel, SERVER_FAIL);
            throw new IllegalArgumentException("Incorrect header");
        }
        if (header[0] != SOCKS_5) {
            //log.warn("Incorrect SOCKS version");
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
                requestHandler(header, key, clientChannel, length);
                attachment.getBuffer().flip();
                attachment.getBuffer().clear();
            }
        }
    }

    private void authenticationHandler(byte[] header, SocketChannel clientChannel, Attachment attachment) throws IOException {
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
            ByteBuffer answer = ByteBuffer.allocate(2);
            answer.put(SOCKS_5);
            answer.put(NO_AUTH_REQUIRED);
            answer.flip();
            clientChannel.write(answer);
            attachment.setStatus(Attachment.REQUEST);

        } else {
            //log.warn("Client has not NoAuth method");
            sendFailAnswer(clientChannel, CONNECTION_NOT_ALLOWED);
            throw new IllegalArgumentException("Client has not NoAuth method");
        }
    }

    private void requestHandler(byte[] header, SelectionKey key, SocketChannel clientChannel, int length) throws IOException {
        //log.info("Request, second header");
        if (header[1] == CONNECT) {
            byte[] addr;
            int port;
            switch (header[3]) {
                case IPV4 -> {
                    IP_TYPE = IPV4;
                    addr = new byte[]{header[4], header[5], header[6], header[7]};
                    port = (header[8] & 0xFF) << 8 | (header[9] & 0xFF);
                    connectToSite(addr, port, key);
                    //   sendSuccessAnswer(clientChannel, (byte) 0x01, addr, new byte[]{header[8], header[9]});
                }
                case DNS -> {
                    IP_TYPE = DNS;
                    int domainLength = header[4] & 0xFF;
                    addr = new byte[domainLength];
                    System.arraycopy(header, 5, addr, 0, domainLength);
                    port = (header[length - 2] & 0xFF) << 8 | (header[length - 1] & 0xFF);
                    //   String domainName = new String(addr, StandardCharsets.UTF_8);
                    connectToSite(addr, port, key);
                    //   sendSuccessAnswer(clientChannel, (byte) 0x03, addr, new byte[]{header[length - 2], header[length - 1]});
                    //TODO: RESOLVE DNS
                }
                default -> {
                    //log.warn("Bad Ip address type");
                    sendFailAnswer(clientChannel, ADDR_TYPE_NOT_SUP);
                    throw new IllegalArgumentException("Bad Ip address type");
                }
            }

        } else {
            //log.warn("Bad CMD in second header");
            sendFailAnswer(clientChannel, COMMAND_NOT_SUP);
            throw new IllegalArgumentException("Bad CMD in second header");
        }
    }

    private void connectToSite(byte[] addr, int port, @NotNull SelectionKey key) throws IOException {
        try {
            SocketChannel siteChanel = SocketChannel.open();
            siteChanel.configureBlocking(false);

            //  String address = new String(addr, StandardCharsets.UTF_8);
            //log.info("ip: " + InetAddress.getByAddress(addr) + ", port: " + port);
            switch (IP_TYPE) {
                case IPV4 -> siteChanel.connect(new InetSocketAddress(InetAddress.getByAddress(addr), port));
                case DNS -> siteChanel.connect(new InetSocketAddress(InetAddress.getByName(new String(addr)), port));
            }

            SelectionKey dstKey = siteChanel.register(key.selector(), SelectionKey.OP_CONNECT);
            key.interestOps(0);

            Attachment attachment = (Attachment) key.attachment();
            attachment.setDstKey(dstKey);

            Attachment dstAttachment = new Attachment();
            dstAttachment.setDstKey(key);
            dstKey.attach(dstAttachment);

        } catch (UnknownHostException exc) {
            //log.warn("Unknown Host Exception");
            sendFailAnswer((SocketChannel) key.channel(), HOST_UNREACHABLE);
            throw new IllegalArgumentException("Unknown Host Exception");
        }
    }

    private void closeConnection(SelectionKey key) throws IOException {
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

    private void sendFailAnswer(SocketChannel channel, byte flag) throws IOException {
        byte[] ans = new byte[]{SOCKS_5, flag, RSV};
        log.info("send file");
        channel.write(ByteBuffer.wrap(ans));
    }

    private static void sendSuccessAnswer(SocketChannel channel, byte type) throws IOException {
        ByteBuffer responseBuffer = ByteBuffer.allocate(256);
        responseBuffer.put(SOCKS_5);
        responseBuffer.put(SUCCESS);
        responseBuffer.put(RSV);
        responseBuffer.put(type);
        responseBuffer.put(InetAddress.getLoopbackAddress().getAddress());
        responseBuffer.putInt(1080);
        responseBuffer.flip();
        channel.write(responseBuffer);
    }


}