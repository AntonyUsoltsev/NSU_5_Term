package ru.nsu.fit.usoltsev;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class GameConstants {
    /**
     * Directions
     */
    public static final int
            UP = 1,
            DOWN = 2,
            LEFT = 3,
            RIGHT = 4;

    /**
     * Roles
     */
    public static final int
            NORMAL = 0,
            MASTER = 1,
            DEPUTY = 2,
            VIEWER = 3;
    public static final int FOOD = 1;
    public static final int FOOD_SCORE = 1;
    public static final int SQUARE_SIZE = 30;
    public static final int FREE_SQUARE_SIZE = 5;
    public static final int MULTICAST_PORT = 9192;
    public static final InetAddress MULTICAST_IP;

    static {
        try {
            MULTICAST_IP = InetAddress.getByName("239.192.0.4");
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

}
