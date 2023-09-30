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
import ru.nsu.fit.usoltsev.controller.GameControl;

import java.io.IOException;
import java.util.ArrayList;

import java.util.List;


public class Main extends Application implements GameConstants {

    public GraphicsContext gc;

    @Override
    public void start(Stage stage) throws IOException {
//        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
//        Scene scene = new Scene(fxmlLoader.load(), 320, 240);

        stage.setTitle("Snake");
        Image icon = new Image("ru/nsu/fit/usoltsev/snakeIcon.png");
        stage.getIcons().add(icon);
        Group root = new Group();
        Canvas canvas = new Canvas(WIDTH, HEIGHT);
        root.getChildren().add(canvas);
        Scene scene = new Scene(root);

        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
        gc = canvas.getGraphicsContext2D();
        GameControl gameControl = new GameControl(gc, scene);
        gameControl.startGame();
    }
    public static void main(String[] args) {
        launch();
    }
}