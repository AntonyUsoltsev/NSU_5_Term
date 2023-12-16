package ru.nsu.fit.usoltsev.view;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import ru.nsu.fit.usoltsev.snakes.SnakesProto;

import static ru.nsu.fit.usoltsev.GameConstants.SQUARE_SIZE;

public class FoodView {
    private static final Image foodsImages = new Image("ru/nsu/fit/usoltsev/pictures/apple.png");

    public void drawFood(double foodX, double foodY, GraphicsContext gc) {
        gc.drawImage(foodsImages, foodX * SQUARE_SIZE, foodY * SQUARE_SIZE, SQUARE_SIZE, SQUARE_SIZE);
    }

    public static void drawFood(GraphicsContext gc, SnakesProto.GameMessage.StateMsg msg) {
        for (var food : msg.getState().getFoodsList()) {
            gc.drawImage(foodsImages, food.getX() * SQUARE_SIZE, food.getY() * SQUARE_SIZE, SQUARE_SIZE, SQUARE_SIZE);
        }
    }

}
