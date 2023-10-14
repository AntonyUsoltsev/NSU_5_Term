package ru.nsu.fit.usoltsev.controller;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.Scene;
import javafx.scene.canvas.GraphicsContext;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;
import ru.nsu.fit.usoltsev.model.FoodModel;
import ru.nsu.fit.usoltsev.model.SnakeModel;
import ru.nsu.fit.usoltsev.view.BackgroundView;
import ru.nsu.fit.usoltsev.view.InfoView;

import static ru.nsu.fit.usoltsev.GameConstants.*;
import static ru.nsu.fit.usoltsev.GameConfig.*;
@Slf4j
public class GameControl {
    private boolean gameOver;
    private int score;
    public GraphicsContext gc;
    public BackgroundView backgroundView;
    public InfoView infoView;
    public FoodModel foodModel;
    public SnakeModel snakeModel;

    public GameControl(GraphicsContext gc, Scene scene) {
        this.gc = gc;
        backgroundView = new BackgroundView();
        foodModel = new FoodModel();
        snakeModel = new SnakeModel();
        infoView = new InfoView();
        new SnakeControl(scene, snakeModel);
    }

    public void startGame() {

        foodModel.generateFood(snakeModel.getSnakeBody());

        snakeModel.setSnakeBody();

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

        snakeModel.snakeMovement();
        snakeModel.drawSnake(gc);

        eatFood();

        gameOver = snakeModel.snakeCrush();

        infoView.drawScore(gc, score);
    }

    private void eatFood() {
        int[][] foods = foodModel.getFoodsCoords();
        int snakeX = (int) snakeModel.getSnakeHead().getX();
        int snakeY = (int) snakeModel.getSnakeHead().getY();

        if (foods[snakeX][snakeY] == FOOD) {
            score += FOOD_SCORE;
            foodModel.eraseOneFood(snakeX, snakeY);
            foodModel.generateOneFood(snakeModel.getSnakeBody());
            snakeModel.raiseUp();
        }
    }

}
