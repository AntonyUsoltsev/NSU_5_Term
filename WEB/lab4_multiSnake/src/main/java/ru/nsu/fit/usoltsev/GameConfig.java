package ru.nsu.fit.usoltsev;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class GameConfig {


    public static int WIDTH, HEIGHT;
    public static int ROWS, COLUMNS;
    public static int SQUARE_SIZE;
    public static int FOOD_COUNT;
    public static int TIME_DELAY;
    public static int ROLE;

    public static void setConstants(int WIDTH, int HEIGHT, int ROWS, int COLUMNS, int FOOD_COUNT, int TIME_DELAY, int ROLE) {
        GameConfig.WIDTH = WIDTH;
        GameConfig.HEIGHT = HEIGHT;
        GameConfig.ROWS = ROWS;
        GameConfig.COLUMNS = COLUMNS;
        GameConfig.FOOD_COUNT = FOOD_COUNT;
        GameConfig.SQUARE_SIZE = WIDTH / COLUMNS;
        GameConfig.TIME_DELAY = TIME_DELAY;
        GameConfig.ROLE = ROLE;
    }

    private GameConfig() {
    }
}
