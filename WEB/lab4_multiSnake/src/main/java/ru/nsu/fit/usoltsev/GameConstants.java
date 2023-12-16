package ru.nsu.fit.usoltsev;

import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;

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

    public static final HashMap<Integer, String> roles = new HashMap<>();
    public static final int FOOD_SCORE = 1;
    public static int SQUARE_SIZE = 20;
    public static final int FREE_SQUARE_SIZE = 5;
    public static final int MULTICAST_PORT = 9192;
    public static final InetAddress MULTICAST_IP;
    public static final ArrayList<Color> COLORS = new ArrayList<>();

    public static Image backgroundImage = new Image("ru/nsu/fit/usoltsev/pictures/menuBackground.png");

    static {
        COLORS.add(Color.RED);
        COLORS.add(Color.BLUE);
        COLORS.add(Color.MAGENTA);
        COLORS.add(Color.YELLOW);
        COLORS.add(Color.AQUAMARINE);
        COLORS.add(Color.BLACK);
        COLORS.add(Color.GREEN);
        COLORS.add(Color.LIGHTSALMON);
        COLORS.add(Color.LIME);
        COLORS.add(Color.DARKBLUE);
        COLORS.add(Color.ORANGE);

        roles.put(0, "Normal");
        roles.put(1, "Master");
        roles.put(2, "Deputy");
        roles.put(3, "Viewer");

        try {
            MULTICAST_IP = InetAddress.getByName("239.192.0.4");
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }


    public static void updateSquareSize(int widthValue, int heightValue) {
        SQUARE_SIZE = 600 / (Math.max(widthValue, heightValue));
    }
}
