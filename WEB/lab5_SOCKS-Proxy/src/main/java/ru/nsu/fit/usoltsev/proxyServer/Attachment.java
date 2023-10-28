package ru.nsu.fit.usoltsev.proxyServer;

import lombok.Getter;
import lombok.Setter;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;

@Getter
@Setter
public class Attachment {
    public static final int AUTH = 1;
    public static final int REQUEST = 2;

    private final int BUFFER_SIZE = 8192;

    /**
     * Buffer from host to proxy server
     */
    private ByteBuffer buffer;

    /**
     * Destination key
     */
    private SelectionKey dstKey;

    /**
     * Current status of connection
     */
    private int status;

    public Attachment() {
        this.buffer = ByteBuffer.allocate(BUFFER_SIZE);
        status = AUTH;
    }
}
