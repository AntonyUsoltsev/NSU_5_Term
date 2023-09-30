package ru.nsu.fit.usoltsev.view;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.Light;
import javafx.scene.paint.Color;
import ru.nsu.fit.usoltsev.GameConstants;

import java.util.List;

public class SnakeView extends GameConstants {
    public void drawSnake( GraphicsContext gc, Light.Point snakeHead, List<Light.Point> snakeBody) {
        gc.setFill(Color.BLUE);
        gc.fillRoundRect(snakeHead.getX() * SQUARE_SIZE, snakeHead.getY() * SQUARE_SIZE, SQUARE_SIZE - 1, SQUARE_SIZE - 1, SQUARE_SIZE * 0.7, SQUARE_SIZE * 0.7);
        for (int i = 1; i < snakeBody.size(); i++) {
            gc.fillRoundRect(snakeBody.get(i).getX() * SQUARE_SIZE, snakeBody.get(i).getY() * SQUARE_SIZE, SQUARE_SIZE - 1, SQUARE_SIZE - 1, SQUARE_SIZE * 0.25, SQUARE_SIZE * 0.25);
        }
    }
}
