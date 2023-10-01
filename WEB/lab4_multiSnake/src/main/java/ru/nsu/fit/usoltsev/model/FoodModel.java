package ru.nsu.fit.usoltsev.model;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.Light;
import javafx.scene.image.Image;
import ru.nsu.fit.usoltsev.view.FoodView;
import ru.nsu.fit.usoltsev.view.InfoView;


import static ru.nsu.fit.usoltsev.GameConstants.*;

import java.util.HashMap;
import java.util.List;

public class FoodModel {

    private final HashMap<Integer, Integer> foods = new HashMap<>();
    private final int[][] foodsCoords = new int[ROWS][COLUMNS];
    private final Image foodsImages =  new Image("ru/nsu/fit/usoltsev/apple.png");
    private final FoodView foodView;

    private final String[] foodImageNames = {"ru/nsu/fit/usoltsev/apple.png", "ru/nsu/fit/usoltsev/watermelon.png",
            "ru/nsu/fit/usoltsev/orange.png", "ru/nsu/fit/usoltsev/strawberry.png"};

    public FoodModel() {
        foodView = new FoodView();
//        for (int i = 0; i < FOOD_COUNT; i++) {
//            foodsImages[i] = new Image("ru/nsu/fit/usoltsev/apple.png");
//        }
    }

//    public Point2D[] getFoods() {
//        return foods;
//    }

    public int[][] getFoodsCoords() {
        return foodsCoords;
    }

    public void generateFood(List<Light.Point> snakeBody) {
        for (int i = 0; i < FOOD_COUNT; i++) {
            generateOneFood(snakeBody);
        }
    }

    public void eraseOneFood(int foodX, int foodY){
        foodsCoords[foodX][foodY] = 0;
        foods.remove(foodY * COLUMNS + foodX);
    }

    public void generateOneFood(List<Light.Point> snakeBody) {
        start:
        while (true) {
            int foodX = (int) (Math.random() * ROWS);
            int foodY = (int) (Math.random() * COLUMNS);
            for (Light.Point snake : snakeBody) {
                if (snake.getX() == foodX && snake.getY() == foodY  || foodsCoords[foodX][foodY] == 1) {
                    continue start;
                }
            }
            foodsCoords[foodX][foodY] = 1;
            foods.put(foodY * COLUMNS + foodX, 1);
            // foodsImages[i] = new Image(foodImageNames[(int) (Math.random() * 10) % 4]);
            break;
        }
    }

    public void drawFood(GraphicsContext gc) {
        for (var pair : foods.entrySet()) {
            foodView.drawFood(foodsImages, pair.getKey() % COLUMNS, pair.getKey() / COLUMNS, gc);
        }
    }

//    public void drawOneFood(GraphicsContext gc, int foodX, int foodY){
//        foodView.drawFood(foodsImages, foodX, foodY, gc);
//    }
}
