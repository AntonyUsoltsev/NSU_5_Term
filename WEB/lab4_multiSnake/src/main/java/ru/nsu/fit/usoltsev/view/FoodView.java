package ru.nsu.fit.usoltsev.view;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import static ru.nsu.fit.usoltsev.GameConstants.*;

public class FoodView {

    public void drawFood(Image foodImage, double foodX, double foodY, GraphicsContext gc) {
        gc.drawImage(foodImage, foodX * SQUARE_SIZE, foodY * SQUARE_SIZE, SQUARE_SIZE, SQUARE_SIZE);
    }
}
