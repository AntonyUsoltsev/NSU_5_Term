package ru.nsu.fit.usoltsev.controller;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.Scene;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.Light;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;
import ru.nsu.fit.usoltsev.model.FoodModel;
import ru.nsu.fit.usoltsev.model.SnakeModel;
import ru.nsu.fit.usoltsev.view.BackgroundView;
import ru.nsu.fit.usoltsev.view.InfoView;

import java.util.ArrayList;
import java.util.List;

import static ru.nsu.fit.usoltsev.GameConfig.*;
import static ru.nsu.fit.usoltsev.GameConstants.*;

@Slf4j
public class GameController {
    private boolean gameOver;
    private int score;
    public GraphicsContext gc;

    public Scene scene;
    public BackgroundView backgroundView;
    public InfoView infoView;
    public FoodModel foodModel;
    public ArrayList<SnakeModel> snakeModelArrayList;
    private final List<Integer> freeSquares;

    public GameController(GraphicsContext gc, Scene scene) {
        this.gc = gc;
        this.scene = scene;
        freeSquares = new ArrayList<>(ROWS * COLUMNS);
        backgroundView = new BackgroundView();
        foodModel = new FoodModel(freeSquares);
        //snakeModel = new SnakeModel();
        infoView = new InfoView();
        snakeModelArrayList = new ArrayList<>();

        //new SnakeController(scene, snakeModel);
    }

    public void addNewSnake() {
        SnakeModel snakeModel = new SnakeModel();
        snakeModelArrayList.add(snakeModel);
        Light.Point generatePoint = findFreeSpace();
        if (generatePoint == null) {

        } else {
            snakeModel.setSnakeBody((int) generatePoint.getX(), (int) generatePoint.getY());
        }
    }

    public Light.Point findFreeSpace() {
        int i = 0;
        restart:
        for (; i < ROWS * COLUMNS; i++) {
            for (int k = 0; k < FREE_SQUARE_SIZE; k++) {
                for (int j = 0; j < FREE_SQUARE_SIZE; j++) {
                    if (!freeSquares.contains(i + j + k * COLUMNS)) {
                        continue restart;
                    }
                }
            }
            return new Light.Point(i % COLUMNS + 2, i / COLUMNS + 2, 0, null);
        }
        return null;
    }

    public void startGame() {
        addNewSnake();
        new SnakeController(scene, snakeModelArrayList.get(0));

        foodModel.generateFood(snakeModelArrayList, freeSquares);
        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(TIME_DELAY), e -> run()));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
        log.info("Timeline started");
    }

    public void run() {
        if (gameOver) {
            infoView.drawGameOver(gc);
            return;
        }

        backgroundView.drawBackground(gc);
        foodModel.drawFood(gc);

        for (var snake : snakeModelArrayList) {
            snake.snakeMovement();
            snake.drawSnake(gc);
            eatFood(snake);
        }


        gameOver = snakeCrush();

        infoView.drawScore(gc, score);
    }

    private void eatFood(SnakeModel snakeModel) {
        int[][] foods = foodModel.getFoodsCoords();
        int snakeX = (int) snakeModel.getSnakeHead().getX();
        int snakeY = (int) snakeModel.getSnakeHead().getY();

        if (foods[snakeX][snakeY] == FOOD) {
            score += FOOD_SCORE;
            foodModel.eraseOneFood(snakeX, snakeY, freeSquares);
            foodModel.generateOneFood(snakeModelArrayList, freeSquares);
            snakeModel.raiseUp();
        }
    }

    private boolean snakeCrush() {
        for (SnakeModel snake : snakeModelArrayList) {
            for (SnakeModel curSnake : snakeModelArrayList) {
                for (int i = 1; i < curSnake.getSnakeBody().size(); i++) {
                    if (snake.getSnakeHead().getX() == curSnake.getSnakeBody().get(i).getX() && snake.getSnakeHead().getY() == curSnake.getSnakeBody().get(i).getY()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

}
