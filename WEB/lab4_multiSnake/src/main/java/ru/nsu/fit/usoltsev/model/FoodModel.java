package ru.nsu.fit.usoltsev.model;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.Light;
import javafx.scene.image.Image;
import lombok.Getter;
import ru.nsu.fit.usoltsev.view.FoodView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static ru.nsu.fit.usoltsev.GameConfig.*;
import static ru.nsu.fit.usoltsev.GameConstants.FOOD;

public class FoodModel {

    private final HashMap<Integer, Integer> foodsMap = new HashMap<>();

    //TODO: matrix for all game

    @Getter
    private final int[][] foodsCoords = new int[COLUMNS][ROWS];

    private final Image foodsImages = new Image("ru/nsu/fit/usoltsev/pictures/apple.png");
    private final FoodView foodView;

    public FoodModel(List<Integer> freeSquares) {
        foodView = new FoodView();
        for (int i = 0; i < ROWS * COLUMNS; i++) {
            freeSquares.add(i);
        }
    }

    public void generateFood(ArrayList<SnakeModel> snakeModels, List<Integer> freeSquares) {
        for (int i = 0; i < FOOD_COUNT; i++) {
            generateOneFood(snakeModels, freeSquares);
        }
    }

    public void eraseOneFood(int foodX, int foodY, List<Integer> freeSquares) {
        foodsCoords[foodX][foodY] = 0;
        freeSquares.add(foodY * COLUMNS + foodX);
        foodsMap.remove(foodY * COLUMNS + foodX);
    }

    public void generateOneFood(ArrayList<SnakeModel> snakeModels, List<Integer> freeSquares) {
        int totalLen = 0;
        for (var snake : snakeModels) {
            totalLen += snake.getSnakeBody().size();
        }
        start:
        //TODO: fix loop when it is no free space
        while (true) {
            if (freeSquares.size() > totalLen) {
                Integer foodCoords = freeSquares.get((int) (Math.random() * freeSquares.size()));
                int foodX = foodCoords % COLUMNS;
                int foodY = foodCoords / COLUMNS;
                for (var snake : snakeModels) {
                    for (Light.Point snakeBody : snake.getSnakeBody()) {
                        if (snakeBody.getX() == foodX && snakeBody.getY() == foodY) {
                            continue start;
                        }
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
            foodView.drawFood(foodsImages, pair.getKey() % COLUMNS, pair.getKey() / COLUMNS, gc);
        }
    }
}
