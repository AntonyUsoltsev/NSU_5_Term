package ru.nsu.fit.usoltsev;

import lombok.Getter;
import lombok.Setter;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;

@Getter

public class Attachment {
    public static final int AUTH = 1;
    public static final int REQUEST = 2;

    private final int BUFFER_SIZE = 4096;

    /**
     * Buffer from client to site
     */
    private final ByteBuffer inputBuffer;

    /**
     * Buffer from site to client
     */
    private final ByteBuffer outputBuffer;

    /**
     * Destination key
     */
    @Setter
    private SelectionKey dstKey;

    @Setter
    private int status;

    @Setter
    private InetAddress ipAddr;

    @Setter
    private int port;

    public Attachment() {
        this.inputBuffer = ByteBuffer.allocate(BUFFER_SIZE);
        this.outputBuffer = ByteBuffer.allocate(BUFFER_SIZE);
        status = AUTH;
    }
}
