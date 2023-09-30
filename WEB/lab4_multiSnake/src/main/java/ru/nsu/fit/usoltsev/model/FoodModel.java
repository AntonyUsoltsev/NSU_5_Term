package ru.nsu.fit.usoltsev.model;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.Light;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import ru.nsu.fit.usoltsev.GameConstants;
import ru.nsu.fit.usoltsev.view.FoodView;

import java.util.ArrayList;
import java.util.List;

public class FoodModel extends  GameConstants {

    private final Point2D[] foods = new Point2D[FOOD_COUNT];
    private final Image[] foodsImages = new Image[FOOD_COUNT];
    private final FoodView foodView;

    private final String[] foodImageNames = {"ru/nsu/fit/usoltsev/apple.png", "ru/nsu/fit/usoltsev/watermelon.png",
                                            "ru/nsu/fit/usoltsev/orange.png","ru/nsu/fit/usoltsev/strawberry.png"};

    public FoodModel() {
        foodView = new FoodView();
        for (int i = 0; i < FOOD_COUNT; i++) {
            foodsImages[i] = new Image("ru/nsu/fit/usoltsev/apple.png");
        }
    }

    public Point2D[] getFoods() {
        return foods;
    }

    public void generateFood(List<Light.Point> snakeBody) {
        for (int i = 0; i < FOOD_COUNT; i++) {
            generateOneFood(snakeBody, i);
        }
    }

    public void generateOneFood(List<Light.Point> snakeBody, int i) {
        start:
        while (true) {
            foods[i] = new Point2D((int) (Math.random() * ROWS), (int) (Math.random() * COLUMNS));
            for (Light.Point snake : snakeBody) {
                if (snake.getX() == foods[i].getX() && snake.getY() == foods[i].getY()) {
                    continue start;
                }
            }
           // foodsImages[i] = new Image(foodImageNames[(int) (Math.random() * 10) % 4]);
            break;
        }
    }

    public void drawFood(GraphicsContext gc) {
        for (int i = 0; i < FOOD_COUNT; i++) {
            foodView.drawFood(foodsImages[i], foods[i].getX(), foods[i].getY(), gc);
        }
    }
}
