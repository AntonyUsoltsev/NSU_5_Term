package ru.nsu.fit.usoltsev.model;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.Light;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;

import static ru.nsu.fit.usoltsev.GameConfig.*;
import static ru.nsu.fit.usoltsev.GameConstants.*;


import lombok.Getter;
import ru.nsu.fit.usoltsev.view.SnakeView;

import java.util.ArrayList;
import java.util.List;

public class SnakeModel {

    @Getter
    private final List<Light.Point> snakeBody = new ArrayList<>();

    @Getter
    private Light.Point snakeHead;
    private int curDirection = RIGHT;

    private final SnakeView snakeView;

    public SnakeModel() {
        snakeView = new SnakeView();
    }

    public void setSnakeBody() {
        for (int i = 0; i < 3; i++) {
            snakeBody.add(new Light.Point(5, ROWS / 2, 0, Color.BLUE));
        }
        snakeHead = snakeBody.get(0);
    }

    public void changeDirection(KeyEvent keyCode) {
        switch (keyCode.getCode()) {
            case D, RIGHT -> curDirection = (curDirection != LEFT ? RIGHT : LEFT);
            case A, LEFT -> curDirection = (curDirection != RIGHT ? LEFT : RIGHT);
            case S, DOWN -> curDirection = (curDirection != UP ? DOWN : UP);
            case W, UP -> curDirection = (curDirection != DOWN ? UP : DOWN);
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

    public boolean snakeCrush() {
        for (int i = 1; i < snakeBody.size(); i++) {
            if (snakeHead.getX() == snakeBody.get(i).getX() && snakeHead.getY() == snakeBody.get(i).getY()) {
                return true;
            }
        }
        return false;
    }

    public void raiseUp() {
        snakeBody.add(new Light.Point(-1, -1, 0, Color.BLUE));
    }


}
