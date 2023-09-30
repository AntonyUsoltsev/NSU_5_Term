package ru.nsu.fit.usoltsev;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;

import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.Light;
import javafx.scene.image.Image;

import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.ArrayList;

import java.util.List;

public class Main extends Application {

    public static final int WIDTH = 800;
    public static final int HEIGHT = 800;
    public static final int ROWS = 20;
    public static final int COLUMNS = 20;
    public static final int SQUARE_SIZE = WIDTH / COLUMNS;

    private static final int LEFT = 0, RIGHT = 1, UP = 2, DOWN = 3;
    private int curDirection = RIGHT;
    private final List<Light.Point> snakeBody = new ArrayList<>();
    private Light.Point snakeHead;
    private int foodX;
    private int foodY;
    private Image foodImage;
    public GraphicsContext gc;

    private boolean gameOver;
    private int score = 0;

    @Override
    public void start(Stage stage) throws IOException {
//        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
//        Scene scene = new Scene(fxmlLoader.load(), 320, 240);
//        stage.setTitle("Hello!");
//        stage.setScene(scene);
//        stage.show();

        stage.setTitle("Snake");
        Group root = new Group();
        Canvas canvas = new Canvas(WIDTH, HEIGHT);
        root.getChildren().add(canvas);
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();

        gc = canvas.getGraphicsContext2D();
        setSnakeBody();
        generateFood();

        scene.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case RIGHT -> {
                    curDirection = (curDirection != LEFT ? RIGHT : LEFT);
//                    if (curDirection != LEFT) {
//                        curDirection = RIGHT;
//                    }
                }
                case LEFT -> {
                    if (curDirection != RIGHT) {
                        curDirection = LEFT;
                    }
                }
                case DOWN -> {
                    if (curDirection != UP) {
                        curDirection = DOWN;
                    }
                }
                case UP -> {
                    if (curDirection != DOWN) {
                        curDirection = UP;
                    }
                }
            }
        });

        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(130), e -> run()));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    private void run() {
        if (gameOver) {
            gc.setFill(Color.RED);
            gc.setFont(new Font("Arial", 70));
            gc.fillText("Game over", WIDTH / 3.5, (double) HEIGHT / 2);
            return;
        }
        drawBackground();
        drawFood();
        drawSnake();

        for (int i = snakeBody.size() - 1; i >= 1; i--) {
            snakeBody.get(i).setX(snakeBody.get(i - 1).getX());
            snakeBody.get(i).setY(snakeBody.get(i - 1).getY());
        }

        switch (curDirection) {
            case RIGHT -> snakeHead.setX(snakeHead.getX() + 1);
            case LEFT -> snakeHead.setX(snakeHead.getX() - 1);
            case UP -> snakeHead.setY(snakeHead.getY() - 1);
            case DOWN -> snakeHead.setY(snakeHead.getY() + 1);
        }

        gameOver();
        eatFood();
        drawScore();
    }

    private void drawBackground() {
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLUMNS; j++) {
                if ((i + j) % 2 == 0) {
                    gc.setFill(Color.rgb(170, 200, 77));
                } else {
                    gc.setFill(Color.rgb(160, 194, 68));
                }
                gc.fillRect(i * SQUARE_SIZE, j * SQUARE_SIZE, SQUARE_SIZE, SQUARE_SIZE);
            }
        }
    }

    private void generateFood() {
        start:
        while (true) {
            foodX = (int) (Math.random() * ROWS);
            foodY = (int) (Math.random() * COLUMNS);
            for (Light.Point snake : snakeBody) {
                if (snake.getX() == foodX && snake.getY() == foodY) {
                    continue start;
                }
            }
            foodImage = new Image("D:\\Antony\\NSU_Education\\5_Term\\WEB\\lab4_multiSnake\\src\\main\\resources\\ru\\nsu\\fit\\usoltsev\\apple.png");
            break;
        }
    }

    private void drawFood() {
        gc.drawImage(foodImage, foodX * SQUARE_SIZE, foodY * SQUARE_SIZE, SQUARE_SIZE, SQUARE_SIZE);
    }

    private void drawSnake() {
        gc.setFill(Color.BLUE);
        gc.fillRoundRect(snakeHead.getX() * SQUARE_SIZE, snakeHead.getY() * SQUARE_SIZE, SQUARE_SIZE - 1, SQUARE_SIZE - 1, 35, 35);
        for (int i = 1; i < snakeBody.size(); i++) {
            gc.fillRoundRect(snakeBody.get(i).getX() * SQUARE_SIZE, snakeBody.get(i).getY() * SQUARE_SIZE, SQUARE_SIZE - 1, SQUARE_SIZE - 1, 20, 20);
        }
    }

    private void setSnakeBody() {
        for (int i = 0; i < 3; i++) {
            snakeBody.add(new Light.Point(5, (double) ROWS / 2, 0, Color.BLUE));
        }
        snakeHead = snakeBody.get(0);
    }

    private void gameOver() {
        if (snakeHead.getX() < 0 || snakeHead.getY() < 0 || snakeHead.getX() * SQUARE_SIZE >= WIDTH || snakeHead.getX() * SQUARE_SIZE >= HEIGHT) {
            gameOver = true;
        }
        for (int i = 1; i < snakeBody.size(); i++) {
            if (snakeHead.getX() == snakeBody.get(i).getX() && snakeHead.getY() == snakeBody.get(i).getY()) {
                gameOver = true;
                break;
            }
        }
    }

    private void eatFood() {
        if (snakeHead.getX() == foodX && snakeHead.getY() == foodY) {
            snakeBody.add(new Light.Point(-1, -1, 0, Color.BLUE));
            score += 10;
            generateFood();
        }
    }

    private void drawScore() {
        gc.setFill(Color.RED);
        gc.setFont(new Font("Digital-7", 30));
        gc.fillText("Score = " + score, 10, 30);
    }

    public static void main(String[] args) {
        launch();
    }
}