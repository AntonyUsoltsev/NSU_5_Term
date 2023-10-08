package ru.nsu.fit.usoltsev.controller;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;
import ru.nsu.fit.usoltsev.GameConfig;
import ru.nsu.fit.usoltsev.listeners.NewGameListener;

import java.util.ArrayList;

import static ru.nsu.fit.usoltsev.GameConstants.*;


public class MenuController implements NewGameListener {
    private GraphicsContext gc;
    private final TextField width, rows, foodCount, timeDelay;
    private final Button startButton;
    private final ListView<String> existGames;

    ArrayList<String> games;

    public MenuController(AnchorPane root) {
        width = (TextField) root.lookup("#widthValue");
        rows = (TextField) root.lookup("#rowsValue");
        foodCount = (TextField) root.lookup("#foodCount");
        timeDelay = (TextField) root.lookup("#timeDelay");
        startButton = (Button) root.lookup("#startButton");
        existGames = (ListView<String>) root.lookup("#existGames");
        games = new ArrayList<>();
    }


    public void newMenu(AnchorPane root, Stage stage, Scene scene) {


        Timeline existGamesTimeline = new Timeline(new KeyFrame(Duration.millis(1000), e -> addNewGameToView()));
        existGamesTimeline.setCycleCount(Animation.INDEFINITE);
        existGamesTimeline.play();


        startButton.setOnAction(event -> {
            if (width.getText().isEmpty()) {
                width.setPromptText("Insert height");
            } else if (rows.getText().isEmpty()) {
                rows.setPromptText("Insert rows count");
            } else if (foodCount.getText().isEmpty()) {
                foodCount.setPromptText("Insert food count!");
            } else if (timeDelay.getText().isEmpty()) {
                timeDelay.setPromptText("Insert time delay!");
            } else {
                try {
                    int widthValue = Integer.parseInt(width.getText());
                    int heightValue = widthValue;
                    int rowsValue = Integer.parseInt(rows.getText());
                    int columnsValue = rowsValue;
                    int timeValue = Integer.parseInt(timeDelay.getText());

                    if (widthValue % rowsValue != 0) {
                        rows.clear();
                        rows.setPromptText("Must be multiple of width");
                    } else if (widthValue <= 0) {
                        width.clear();
                        width.setPromptText("Must be > 0");
                    } else if (rowsValue <= 0) {
                        rows.clear();
                        rows.setPromptText("Must be > 0");
                    } else if (timeValue <= 0) {
                        timeDelay.clear();
                        timeDelay.setPromptText("Must be > 0");
                    } else {
                        int foodCountValue = Integer.parseInt(foodCount.getText());
                        GameConfig.setConstants(widthValue, heightValue, rowsValue, columnsValue, foodCountValue, timeValue, MASTER);
                        root.getChildren().clear();
                        Canvas canvas = new Canvas(widthValue, heightValue);
                        root.getChildren().add(canvas);

                        stage.setX(Screen.getPrimary().getVisualBounds().getWidth() / 2 - (double) widthValue / 2);
                        stage.setY(Screen.getPrimary().getVisualBounds().getHeight() / 2 - (double) heightValue / 2);
                        stage.sizeToScene();

                        gc = canvas.getGraphicsContext2D();

                        existGamesTimeline.stop();
                        GameControl gameControl = new GameControl(gc, scene);
                        gameControl.startGame();
                    }

                } catch (NumberFormatException ex) {
                    ex.printStackTrace(System.err);
                    System.exit(1);
                }
            }
        });
    }


    @Override
    public void addNewGame(String gameName) {
        games.add(gameName);
    }

    private void addNewGameToView(){
        existGames.getItems().addAll(games);
    }
}
