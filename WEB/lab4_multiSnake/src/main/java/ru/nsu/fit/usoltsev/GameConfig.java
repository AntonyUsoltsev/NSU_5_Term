package ru.nsu.fit.usoltsev;

import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static ru.nsu.fit.usoltsev.GameConstants.SQUARE_SIZE;

@Slf4j
public final class GameConfig {
    public static int WIDTH, HEIGHT;
    public static int ROWS, COLUMNS;
    public static int FOOD_COUNT;
    public static int TIME_DELAY;
    public static String GAME_NAME;
    public static String PLAYER_NAME;

    public static int ID;
    public static volatile int ROLE;



    public static void setConstants(int WIDTH, int HEIGHT, int FOOD_COUNT, int TIME_DELAY, String GAME_NAME, String PLAYER_NAME, int ROLE, int ID) {
        GameConfig.WIDTH = WIDTH * SQUARE_SIZE;
        GameConfig.HEIGHT = HEIGHT * SQUARE_SIZE;
        GameConfig.ROWS = HEIGHT;
        GameConfig.COLUMNS = WIDTH;
        GameConfig.FOOD_COUNT = FOOD_COUNT;
        GameConfig.TIME_DELAY = TIME_DELAY;
        GameConfig.GAME_NAME = GAME_NAME;
        GameConfig.PLAYER_NAME = PLAYER_NAME;
        GameConfig.ROLE = ROLE;
        GameConfig.ID = ID;
    }

    private GameConfig() {
    }
}
