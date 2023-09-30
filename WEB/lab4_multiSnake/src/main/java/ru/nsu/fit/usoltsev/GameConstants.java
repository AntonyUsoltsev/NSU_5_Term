package ru.nsu.fit.usoltsev;

public class GameConstants {
    public int WIDTH = 800;
    public int HEIGHT = 800;
    public int ROWS = 20;
    public int COLUMNS = 20;
    public int SQUARE_SIZE = WIDTH / COLUMNS;
    public final int LEFT = 0, RIGHT = 1, UP = 2, DOWN = 3;
    public int FOOD_COUNT = 20;

    public void setConstants(int WIDTH, int HEIGHT, int ROWS, int COLUMNS, int FOOD_COUNT) {
        this.WIDTH = WIDTH;
        this.HEIGHT = HEIGHT;
        this.ROWS = ROWS;
        this.COLUMNS = COLUMNS;
        this.FOOD_COUNT = FOOD_COUNT;
    }
}
