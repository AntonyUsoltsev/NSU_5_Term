package ru.nsu.fit.usoltsev;

public final class GameConstants {
    public static int WIDTH;
    public static int HEIGHT;
    public static int ROWS;
    public static int COLUMNS;
    public static int SQUARE_SIZE;
    public static int FOOD_COUNT;
    public static final int LEFT = 0, RIGHT = 1, UP = 2, DOWN = 3;

    public static void setConstants(int WIDTH, int HEIGHT, int ROWS, int COLUMNS, int FOOD_COUNT) {
        GameConstants.WIDTH = WIDTH;
        GameConstants.HEIGHT = HEIGHT;
        GameConstants.ROWS = ROWS;
        GameConstants.COLUMNS = COLUMNS;
        GameConstants.FOOD_COUNT = FOOD_COUNT;
        GameConstants.SQUARE_SIZE = WIDTH / COLUMNS;
    }

    private GameConstants(){
    }
}
