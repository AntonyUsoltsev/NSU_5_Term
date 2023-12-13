package ru.nsu.fit.usoltsev.model;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.Light;
import javafx.scene.paint.Color;
import java.util.ArrayList;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import ru.nsu.fit.usoltsev.view.SnakeView;

import static ru.nsu.fit.usoltsev.GameConfig.*;
import static ru.nsu.fit.usoltsev.GameConstants.*;

@Slf4j
public class SnakeModel {
    @Getter
    private final ArrayList<Light.Point> snakeBody = new ArrayList<>();
    @Getter
    private Light.Point snakeHead;
    @Getter
    private int curDirection = RIGHT;
    private final SnakeView snakeView;
    private final Color color;

    public SnakeModel(int index) {
        snakeView = new SnakeView();
        color = COLORS.get(index % COLORS.size());
    }

    public void setSnakeBody(int startX, int startY) {
        for (int i = 0; i < 2; i++) {
            snakeBody.add(new Light.Point(startX - i, startY, 0, color));
        }
        snakeHead = snakeBody.get(0);
    }
//    public void setSnakeHead(Light){
//
//    }

    public void changeDirection(int direction) {
        switch (direction) {
            case RIGHT -> curDirection = (curDirection != LEFT ? RIGHT : LEFT);
            case LEFT -> curDirection = (curDirection != RIGHT ? LEFT : RIGHT);
            case DOWN -> curDirection = (curDirection != UP ? DOWN : UP);
            case UP -> curDirection = (curDirection != DOWN ? UP : DOWN);
        }
    }

    public void snakeMovement() {
        for (int i = snakeBody.size() - 1; i >= 1; i--) {
            snakeBody.get(i).setX(snakeBody.get(i - 1).getX());
            snakeBody.get(i).setY(snakeBody.get(i - 1).getY());
        }

        switch (curDirection) {
            case RIGHT -> {
                if (snakeHead.getX() * SQUARE_SIZE >= WIDTH - SQUARE_SIZE) {
                    snakeHead.setX(0);
                } else {
                    snakeHead.setX(snakeHead.getX() + 1);
                }
            }
            case LEFT -> {
                if (snakeHead.getX() <= 0) {
                    snakeHead.setX(COLUMNS - 1);
                } else {
                    snakeHead.setX(snakeHead.getX() - 1);
                }
            }
            case UP -> {
                if (snakeHead.getY() <= 0) {
                    snakeHead.setY(ROWS - 1);
                } else {
                    snakeHead.setY(snakeHead.getY() - 1);
                }
            }
            case DOWN -> {
                if (snakeHead.getY() * SQUARE_SIZE >= HEIGHT - SQUARE_SIZE) {
                    snakeHead.setY(0);
                } else {
                    snakeHead.setY(snakeHead.getY() + 1);
                }
            }

        }

    }

    public void drawSnake(GraphicsContext gc) {
        snakeView.drawSnake(gc, snakeHead, snakeBody);
    }

    public void raiseUp(int x, int y) {
        snakeBody.add(new Light.Point(x, y, 0, color));
    }

    public void addPoint(double x, double y) {
        snakeBody.add(new Light.Point(x, y, 0, color));
        if (snakeBody.size() == 1){
            snakeHead = snakeBody.get(0);
        }
    }


}
