package ru.nsu.fit.usoltsev.network;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class NetworkUtils {
    public static final AtomicInteger ID_JOIN = new AtomicInteger(2);
    public static final AtomicLong MSG_SEQ = new AtomicLong(1);
    public static final AtomicInteger STATE_SEQ = new AtomicInteger(1);
    public static final CountDownLatch countDownLatch = new CountDownLatch(1);
    public static InetAddress MASTER_IP;
    public static int MASTER_PORT;
    public static final String IP_REGEX = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";

    public static InetAddress parseIp(String ipString) throws UnknownHostException {
        if (ipString.indexOf("/") == 0) {
            return InetAddress.getByName(ipString.substring(1));
        } else {
            return InetAddress.getByName(ipString);
        }
    }
}
