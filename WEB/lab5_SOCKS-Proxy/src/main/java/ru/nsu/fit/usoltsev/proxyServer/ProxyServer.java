package ru.nsu.fit.usoltsev.proxyServer;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.channels.spi.SelectorProvider;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.xbill.DNS.*;
import org.xbill.DNS.Record;
import ru.nsu.fit.usoltsev.DNS.dnsResolver;

import static ru.nsu.fit.usoltsev.proxyServer.SOCKS_Constants.*;

@Slf4j
public class ProxyServer implements AutoCloseable {
    private final Selector selector;
    private final ServerSocketChannel serverChannel;
    private final dnsResolver dnsResolver;
    private final int port;

    public ProxyServer(int port) throws IOException {
        this.selector = SelectorProvider.provider().openSelector();
        this.serverChannel = ServerSocketChannel.open();
        this.serverChannel.socket().bind(new InetSocketAddress(InetAddress.getByName("localhost"), port));
        this.serverChannel.configureBlocking(false);                           // Устанавливаем неблокирующий режим
        this.serverChannel.register(this.selector, SelectionKey.OP_ACCEPT);    //Здесь же устанавливается ключ в селектор
        this.port = port;

        DatagramChannel dnsChannel = DatagramChannel.open();
        dnsChannel.configureBlocking(false);
        dnsChannel.register(this.selector, SelectionKey.OP_READ);
        SocketAddress dnsServerAddress = new InetSocketAddress(ResolverConfig.getCurrentConfig().servers().get(0).getAddress(), 53);
        dnsChannel.connect(dnsServerAddress);

        dnsResolver = new dnsResolver(dnsChannel);

    }

    public void run() {
        try {
            log.info("Proxy server start working");

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
                                    if (key.channel() instanceof DatagramChannel) {
                                        //log.info("DADAD");
                                        readDnsAnswer(key);
                                    } else {
                                        readData(key);            // Читаем данные от канала
                                    }
                                } else if (key.isWritable()) {
                                    writeData(key);               // Пишем данные в канал
                                }
                            }
                        } catch (IOException ioExc) {
                            //  log.warn(new String(ignore.getMessage().getBytes(StandardCharsets.UTF_8)), ignore);
                            closeConnection(key);
                        } catch (IllegalArgumentException iaExc) {
                            log.warn(new String(iaExc.getMessage().getBytes(StandardCharsets.UTF_8)), iaExc);
                            closeConnection(key);
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
        // log.info("Client connected");
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
        if (!attachment.getOutputBuffer().hasRemaining()) {
            return;
        }

        int bytesWrite = channel.write(attachment.getOutputBuffer());

        //log.info("Write from: " + channel.getLocalAddress() + " to: " + channel.getRemoteAddress() + " data: " + Arrays.toString(attachment.getOutputBuffer().array()));

        if (bytesWrite == -1) {
            throw new IllegalArgumentException("Bytes write = -1");
        } else {
            attachment.getOutputBuffer().flip();
            attachment.getOutputBuffer().clear();
            attachment.getDstKey().interestOps(attachment.getDstKey().interestOps() | SelectionKey.OP_READ);
            //key.interestOps(key.interestOps() ^ SelectionKey.OP_WRITE);
            key.interestOps(SelectionKey.OP_READ);
        }
    }

    private void readData(@NotNull SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        Attachment attachment = (Attachment) key.attachment();

        if (attachment == null) {
            attachment = new Attachment();
            key.attach(attachment);
        }

        int bytesRead = channel.read(attachment.getInputBuffer());

        if (bytesRead == 0 || bytesRead == -1) {
            return;
        } else if (attachment.getDstKey() == null) { // если нет второго конца значит читаем заголовок
            readHeader(key, bytesRead);
            // log.info("Read header from: " + channel.getRemoteAddress() + " to: " + channel.getLocalAddress() + " data: " + Arrays.toString(attachment.getBuffer().array()));
        } else {
            // log.info("Read from: " + channel.getRemoteAddress() + " to: " + channel.getLocalAddress() + " data: " + Arrays.toString(attachment.getBuffer().array()));
            key.interestOps(key.interestOps() ^ SelectionKey.OP_READ);
            attachment.getDstKey().interestOps(attachment.getDstKey().interestOps() | SelectionKey.OP_WRITE);
            attachment.getInputBuffer().flip();
        }
    }

    public void readHeader(@NotNull SelectionKey key, int length) throws IOException {
        SocketChannel clientChannel = (SocketChannel) key.channel();
        Attachment attachment = (Attachment) key.attachment();
        byte[] header = attachment.getInputBuffer().array();
        if (header.length < 3) {
            //log.warn("Incorrect header");
            sendFailAnswer(clientChannel, SERVER_FAIL);
            throw new IllegalArgumentException("Incorrect header");
        }
        if (header[0] != SOCKS_5) {  //TODO: REVIEW
            //log.warn("Incorrect SOCKS version");
            sendFailAnswer(clientChannel, SERVER_FAIL);
            throw new IllegalArgumentException("Incorrect SOCKS version");
        }

        switch (attachment.getStatus()) {
            case Attachment.AUTH -> {
                authenticationHandler(header, clientChannel, attachment);
                attachment.getInputBuffer().flip();
                attachment.getInputBuffer().clear();
            }
            case Attachment.REQUEST -> {
                requestHandler(header, key, clientChannel, length);
                attachment.getInputBuffer().flip();
                attachment.getInputBuffer().clear();
            }
        }
    }

    private boolean checkAuthMethod(byte @NotNull [] header) {
        int NMethods = header[1];
        for (int i = 0; i < NMethods; i++) {
            if (header[i + 2] == NO_AUTH_REQUIRED) {
                return true;
            }
        }
        return false;
    }

    private void authenticationHandler(byte @NotNull [] header, SocketChannel clientChannel, Attachment attachment) throws IOException {
        //log.info("Auth, first header");
        if (checkAuthMethod(header)) {
            clientChannel.write(ByteBuffer.wrap(new byte[]{SOCKS_5, NO_AUTH_REQUIRED}));
            attachment.setStatus(Attachment.REQUEST);
        } else {
            log.warn("Client has not NoAuth method");
            sendFailAnswer(clientChannel, CONNECTION_NOT_ALLOWED);
            throw new IllegalArgumentException("Client has not NoAuth method");
        }
    }

    private void requestHandler(byte @NotNull [] header, SelectionKey key, SocketChannel clientChannel, int length) throws IOException {
        //log.info("Request, second header");
        if (header[1] == CONNECT) {
            byte[] addr;
            int port = (header[length - 2] & 0xFF) << 8 | (header[length - 1] & 0xFF);
            switch (header[3]) {
                case IPV4 -> {
                    addr = Arrays.copyOfRange(header, 4, 4 + IPV4_LENGTH);
                    connectToSite(addr, port, key);
                }
                case DNS -> {
                    int domainLength = header[4] & 0xFF;
                    addr = Arrays.copyOfRange(header, 5, 5 + domainLength);
                    dnsResolver.resolve(addr, port, key);
                }
                case IPV6 -> {
                    addr = Arrays.copyOfRange(header, 4, 4 + IPV6_LENGTH);
                    connectToSite(addr, port, key);
                }
                default -> {
                    sendFailAnswer(clientChannel, ADDR_TYPE_NOT_SUP);
                    throw new IllegalArgumentException("Bad Ip address type = " + header[3]);
                }
            }

        } else {
            log.warn("Bad CMD in second header");
            sendFailAnswer(clientChannel, COMMAND_NOT_SUP);
            throw new IllegalArgumentException("Bad CMD in second header");
        }
    }

    private void readDnsAnswer(@NotNull SelectionKey key) throws IOException {
        ByteBuffer ans = ByteBuffer.allocate(Attachment.BUFFER_SIZE);
        DatagramChannel datagramChannel = (DatagramChannel) key.channel();
        int bytesRead = datagramChannel.read(ans);
        ans.flip();

        if (bytesRead > 0) {
            Message message = new Message(ans);
            int senderId = message.getHeader().getID();
            List<Record> answerRecords = message.getSection(Section.ANSWER);
            for (Record record : answerRecords) {
                if (record.getType() == Type.A) {
                    ARecord aRecord = (ARecord) record;
                    InetAddress ipAddress = aRecord.getAddress();

                    connectToSite(ipAddress.getAddress(), dnsResolver.getClientMatch().get(senderId).getKey(), dnsResolver.getClientMatch().get(senderId).getValue());
                    break;
                }
            }
        }
    }

    private void connectToSite(byte[] addr, int port, SelectionKey key) throws IOException {
        try {
            SocketChannel siteChanel = SocketChannel.open();
            siteChanel.configureBlocking(false);
            siteChanel.connect(new InetSocketAddress(InetAddress.getByAddress(addr), port));

            SelectionKey dstKey = siteChanel.register(key.selector(), SelectionKey.OP_CONNECT);
            key.interestOps(0);

            Attachment attachment = (Attachment) key.attachment();
            attachment.setDstKey(dstKey);

            Attachment dstAttachment = new Attachment();
            dstAttachment.setDstKey(key);
            dstKey.attach(dstAttachment);

            attachment.setOutputBuffer(dstAttachment.getInputBuffer());
            dstAttachment.setOutputBuffer(attachment.getInputBuffer());


        } catch (UnknownHostException exc) {
            log.warn("Unknown Host Exception");
            sendFailAnswer((SocketChannel) key.channel(), HOST_UNREACHABLE);
            throw new IllegalArgumentException("Unknown Host Exception");
        }
    }

    private void sendFailAnswer(@NotNull SocketChannel channel, byte flag) throws IOException {
        byte[] ans = new byte[]{SOCKS_5, flag, RSV};
        log.info("Send fail");
        channel.write(ByteBuffer.wrap(ans));
    }

    private void sendSuccessAnswer(@NotNull SocketChannel channel) throws IOException {
        ByteBuffer responseBuffer = ByteBuffer.allocate(6 + InetAddress.getLoopbackAddress().getAddress().length);
        responseBuffer.put(SOCKS_5);
        responseBuffer.put(SUCCESS);
        responseBuffer.put(RSV);
        responseBuffer.put(IPV4);
        responseBuffer.put(InetAddress.getLoopbackAddress().getAddress());

        responseBuffer.putShort((short) port); //!!!!!!!

        responseBuffer.flip();
        channel.write(responseBuffer);
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

    }

    @Override
    public void close() throws Exception {
        selector.close();
        serverChannel.close();
    }
}