package ru.nsu.fit.usoltsev;

import lombok.extern.slf4j.Slf4j;

import static ru.nsu.fit.usoltsev.GameConstants.*;

@Slf4j
public final class GameConfig {


    public static int WIDTH, HEIGHT;
    public static int ROWS, COLUMNS;
    //public static int SQUARE_SIZE;
    public static int FOOD_COUNT;
    public static int TIME_DELAY;

    public static String GAME_NAME;
    public static int ROLE;

    public static void setConstants(int WIDTH, int HEIGHT, int FOOD_COUNT, int TIME_DELAY, String GAME_NAME, int ROLE) {
        GameConfig.WIDTH = WIDTH * SQUARE_SIZE;
        GameConfig.HEIGHT = HEIGHT * SQUARE_SIZE;
        GameConfig.ROWS = HEIGHT;
        GameConfig.COLUMNS = WIDTH;
        GameConfig.FOOD_COUNT = FOOD_COUNT;
        GameConfig.TIME_DELAY = TIME_DELAY;
        GameConfig.GAME_NAME = GAME_NAME;
        GameConfig.ROLE = ROLE;
    }

    private GameConfig() {
    }
}
