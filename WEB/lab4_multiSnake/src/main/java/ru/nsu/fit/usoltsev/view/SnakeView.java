package ru.nsu.fit.usoltsev.view;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.Light;
import ru.nsu.fit.usoltsev.snakes.SnakesProto;

import java.util.List;

import static ru.nsu.fit.usoltsev.GameConstants.COLORS;
import static ru.nsu.fit.usoltsev.GameConstants.SQUARE_SIZE;

public class SnakeView {
    public void drawSnake(GraphicsContext gc, Light.Point snakeHead, List<Light.Point> snakeBody) {
        gc.setFill(snakeHead.getColor());
        gc.fillRoundRect(snakeHead.getX() * SQUARE_SIZE, snakeHead.getY() * SQUARE_SIZE, SQUARE_SIZE - 1, SQUARE_SIZE - 1, SQUARE_SIZE * 0.9, SQUARE_SIZE * 0.9);
        for (int i = 1; i < snakeBody.size(); i++) {
            gc.fillRoundRect(snakeBody.get(i).getX() * SQUARE_SIZE, snakeBody.get(i).getY() * SQUARE_SIZE, SQUARE_SIZE - 1, SQUARE_SIZE - 1, SQUARE_SIZE * 0.3, SQUARE_SIZE * 0.3);
        }
    }

    public static void drawSnake(GraphicsContext gc, SnakesProto.GameMessage.StateMsg msg) {
        for (var snake : msg.getState().getSnakesList()) {
            SnakesProto.GameState.Coord point = snake.getPointsList().get(0);
            gc.setFill(COLORS.get(snake.getPlayerId() % COLORS.size()));
            gc.fillRoundRect(point.getX() * SQUARE_SIZE, point.getY() * SQUARE_SIZE, SQUARE_SIZE - 1, SQUARE_SIZE - 1, SQUARE_SIZE * 0.9, SQUARE_SIZE * 0.9);
            for (int i = 1; i < snake.getPointsList().size(); i++) {
                point = snake.getPointsList().get(i);
                gc.fillRoundRect(point.getX() * SQUARE_SIZE, point.getY() * SQUARE_SIZE, SQUARE_SIZE - 1, SQUARE_SIZE - 1, SQUARE_SIZE * 0.3, SQUARE_SIZE * 0.3);
            }
        }
    }
}
