package ru.nsu.fit.usoltsev.model;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.Light;
import lombok.Getter;
import ru.nsu.fit.usoltsev.HostInfo;
import ru.nsu.fit.usoltsev.snakes.SnakesProto;
import ru.nsu.fit.usoltsev.view.FoodView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import static ru.nsu.fit.usoltsev.GameConfig.*;

public class FoodModel {
    @Getter
    private final HashSet<Integer> foodsSet = new HashSet<>(); // coords of food
    private final FoodView foodView;

    public FoodModel(List<Integer> freeSquares) {
        foodView = new FoodView();
        for (int i = 0; i < ROWS * COLUMNS; i++) {
            freeSquares.add(i);
        }
    }

    public void generateFood(HashMap<Integer, HostInfo> snakes, List<Integer> freeSquares) {
        for (int i = 0; i < FOOD_COUNT; i++) {
            generateOneFood(snakes, freeSquares);
        }
    }

    public void eraseOneFood(int foodX, int foodY, List<Integer> freeSquares) {

        freeSquares.add(foodY * COLUMNS + foodX);
        foodsSet.remove(foodY * COLUMNS + foodX);
    }

    public void generateOneFood(HashMap<Integer, HostInfo> snakes, List<Integer> freeSquares) {
        int totalLen = 0;
        for (var snake : snakes.values()) {
            totalLen += snake.getModel().getSnakeBody().size();
        }
        start:
        //TODO: fix loop when it is no free space
        while (true) {
            if (freeSquares.size() > totalLen) {
                Integer foodCoords = freeSquares.get((int) (Math.random() * freeSquares.size()));
                int foodX = foodCoords % COLUMNS;
                int foodY = foodCoords / COLUMNS;
                for (var snake : snakes.values()) {
                    for (Light.Point snakeBody : snake.getModel().getSnakeBody()) {
                        if (snakeBody.getX() == foodX && snakeBody.getY() == foodY) {
                            continue start;
                        }
                    }
                }
                freeSquares.remove(foodCoords);
                foodsSet.add(foodCoords);
            }
            break;
        }
    }

    public void drawFood(GraphicsContext gc) {
        for (var key : foodsSet) {
            foodView.drawFood(key % COLUMNS, key / COLUMNS, gc);
        }
    }

    public void drawFood(GraphicsContext gc, SnakesProto.GameMessage.StateMsg msg) {
        for (var food : msg.getState().getFoodsList()) {
            foodView.drawFood(food.getX(), food.getY(), gc);
        }
    }

    public void crushSnake(ArrayList<Light.Point> snakeBody) {
        for (var point : snakeBody) {
            if (Math.random() > 0.5) {
                foodsSet.add((int) (point.getY() * COLUMNS + point.getX()));
            }
        }
    }
}
