package ru.nsu.fit.usoltsev.controller;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.Light;
import javafx.util.Duration;
import static ru.nsu.fit.usoltsev.GameConstants.*;
import ru.nsu.fit.usoltsev.model.FoodModel;
import ru.nsu.fit.usoltsev.model.SnakeModel;
import ru.nsu.fit.usoltsev.view.BackgroundView;
import ru.nsu.fit.usoltsev.view.InfoView;

public class GameControl  {
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
        SnakeControl snakeControl = new SnakeControl(scene, snakeModel);
    }

    public void startGame() {
        foodModel.generateFood(snakeModel.getSnakeBody());

        snakeModel.setSnakeBody();

        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(130), e -> run()));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
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
        gameOver = snakeModel.snakeCrush();

        eatFood();

        infoView.drawScore(gc, score);
    }

    private void eatFood() {
        Point2D[] foods = foodModel.getFoods();
        for (int i = 0; i < FOOD_COUNT; i++) {

            if (snakeModel.getSnakeHead().getX() == foods[i].getX() && snakeModel.getSnakeHead().getY() == foods[i].getY()) {
                snakeModel.raiseUp();
                score += 10;
                foodModel.generateOneFood(snakeModel.getSnakeBody(), i);
            }
        }
    }

}
