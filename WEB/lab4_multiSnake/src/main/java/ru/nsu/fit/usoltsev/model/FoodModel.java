package ru.nsu.fit.usoltsev.model;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.Light;
import javafx.scene.image.Image;
import lombok.Getter;
import ru.nsu.fit.usoltsev.view.FoodView;

import static ru.nsu.fit.usoltsev.GameConfig.*;
import static ru.nsu.fit.usoltsev.GameConstants.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FoodModel {

    private final HashMap<Integer, Integer> foodsMap = new HashMap<>();

    //TODO: matrix for all game
    //TODO: Redraw field based on matrix ??

    @Getter
    private final int[][] foodsCoords = new int[ROWS][COLUMNS];
    private final List<Integer> freeSquares = new ArrayList<>(ROWS * COLUMNS);

    private final Image foodsImages = new Image("ru/nsu/fit/usoltsev/pictures/apple.png");
    private final FoodView foodView;

    public FoodModel() {
        foodView = new FoodView();
        for (int i = 0; i < ROWS * COLUMNS; i++) {
            freeSquares.add(i);
        }
    }

    public void generateFood(List<Light.Point> snakeBody) {
        for (int i = 0; i < FOOD_COUNT; i++) {
            generateOneFood(snakeBody);
        }
    }

    public void eraseOneFood(int foodX, int foodY) {
        foodsCoords[foodX][foodY] = 0;
        freeSquares.add(foodY * COLUMNS + foodX);
        foodsMap.remove(foodY * COLUMNS + foodX);
    }

    public void generateOneFood(List<Light.Point> snakeBody) {
        start:
        //TODO: fix loop when it is no free space
        while (true) {
            if (freeSquares.size() > snakeBody.size()) {
                Integer foodCoords = freeSquares.get((int) (Math.random() * freeSquares.size()));
                int foodX = foodCoords % COLUMNS;
                int foodY = foodCoords / COLUMNS;
                for (Light.Point snake : snakeBody) {
                    if (snake.getX() == foodX && snake.getY() == foodY) {
                        continue start;
                    }
                }
                freeSquares.remove(foodCoords);
                foodsCoords[foodX][foodY] = FOOD;
                foodsMap.put(foodCoords, FOOD);
            }
            break;
        }
    }

    public void drawFood(GraphicsContext gc) {
        for (var pair : foodsMap.entrySet()) {
            foodView.drawFood(foodsImages, pair.getKey() % COLUMNS,  pair.getKey() / COLUMNS, gc);
        }
    }
}
