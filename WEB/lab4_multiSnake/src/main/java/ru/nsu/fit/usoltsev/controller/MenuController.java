package ru.nsu.fit.usoltsev.controller;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.Group;
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
import lombok.extern.slf4j.Slf4j;
import ru.nsu.fit.usoltsev.GameConfig;
import ru.nsu.fit.usoltsev.listeners.NewGameListener;
import ru.nsu.fit.usoltsev.network.UdpController;
import ru.nsu.fit.usoltsev.snakes.SnakesProto;

import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ThreadPoolExecutor;

import static ru.nsu.fit.usoltsev.GameConstants.*;
@Slf4j
public class MenuController implements NewGameListener {
    private GraphicsContext gc;
    private final TextField width, height, foodCount, timeDelay;
    private final Button startButton, joinButton;
    private final ListView<String> existGames;
    private String selectedGame;
    private final HashMap<String, SnakesProto.GameAnnouncement> gamesInfo;


    public MenuController(AnchorPane root) {
        width = (TextField) root.lookup("#width");
        height = (TextField) root.lookup("#height");
        foodCount = (TextField) root.lookup("#foodCount");
        timeDelay = (TextField) root.lookup("#timeDelay");
        startButton = (Button) root.lookup("#startButton");
        joinButton = (Button) root.lookup("#joinButton");
        existGames = (ListView<String>) root.lookup("#existGames");

        gamesInfo = new HashMap<>();

    }


    public void newMenu(Stage stage, Scene scene, ThreadPoolExecutor executor) {

        startButton.setOnAction(event -> {
            if (width.getText().isEmpty()) {
                width.setPromptText("Insert width");
            } else if (height.getText().isEmpty()) {
                height.setPromptText("Insert height");
            } else if (foodCount.getText().isEmpty()) {
                foodCount.setPromptText("Insert food count!");
            } else if (timeDelay.getText().isEmpty()) {
                timeDelay.setPromptText("Insert time delay!");
            } else {
                try {
                    int widthValue = Integer.parseInt(width.getText());
                    int heightValue = Integer.parseInt(height.getText());
                    int timeValue = Integer.parseInt(timeDelay.getText());
                    int foodCountValue = Integer.parseInt(foodCount.getText());

                    if (widthValue <= 0) {
                        width.clear();
                        width.setPromptText("Must be > 0");
                    } else if (heightValue <= 0) {
                        height.clear();
                        height.setPromptText("Must be > 0");
                    } else if (foodCountValue <= 0) {
                        foodCount.clear();
                        foodCount.setPromptText("Must be > 0");
                    } else if (timeValue <= 0) {
                        timeDelay.clear();
                        timeDelay.setPromptText("Must be > 0");
                    } else {

                        UdpController udpController = new UdpController();
                        executor.submit(udpController);

                        //NEED TO SEND CURRENT GAME

                        GameConfig.setConstants(widthValue, heightValue, foodCountValue, timeValue, MASTER);
                        Group group = new Group();
                        Canvas canvas = new Canvas(widthValue * SQUARE_SIZE, heightValue * SQUARE_SIZE);
                        group.getChildren().add(canvas);
                        scene.setRoot(group);
                        stage.setX(Screen.getPrimary().getVisualBounds().getWidth() / 2 - (double) (widthValue * SQUARE_SIZE) / 2);
                        stage.setY(Screen.getPrimary().getVisualBounds().getHeight() / 2 - (double) (heightValue * SQUARE_SIZE) / 2);
                        stage.sizeToScene();
                        System.out.println("ok1");
                        udpController.setNewGame(widthValue, heightValue, foodCountValue, timeValue, MASTER);
                        System.out.println("ok");
                        gc = canvas.getGraphicsContext2D();

                        GameControl gameControl = new GameControl(gc, scene);
                        gameControl.startGame();
                    }

                } catch (NumberFormatException | SocketException ex) {
                    ex.printStackTrace(System.err);
                    System.exit(1);
                }
            }
        });

        existGames.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                selectedGame = newValue;
            }
        });

        joinButton.setOnAction(event -> {
            if (selectedGame != null) {
                try {
                    SnakesProto.GameAnnouncement gameInfo = gamesInfo.get(selectedGame);
                    int heightValue = gameInfo.getConfig().getHeight();
                    int widthValue = gameInfo.getConfig().getWidth();
                    int foodCountValue = gameInfo.getConfig().getFoodStatic();
                    int timeValue = gameInfo.getConfig().getStateDelayMs();
                    UdpController udpController = new UdpController();
                    executor.submit(udpController);

                    GameConfig.setConstants(widthValue, heightValue, foodCountValue, timeValue, NORMAL);
                    Group group = new Group();
                    Canvas canvas = new Canvas(widthValue * SQUARE_SIZE, heightValue * SQUARE_SIZE);
                    group.getChildren().add(canvas);
                    scene.setRoot(group);
                    stage.setX(Screen.getPrimary().getVisualBounds().getWidth() / 2 - (double) (widthValue * SQUARE_SIZE) / 2);
                    stage.setY(Screen.getPrimary().getVisualBounds().getHeight() / 2 - (double) (heightValue * SQUARE_SIZE) / 2);
                    stage.sizeToScene();

                    gc = canvas.getGraphicsContext2D();

                    GameControl gameControl = new GameControl(gc, scene);

                    gameControl.startGame();
                } catch (SocketException ex) {
                    ex.printStackTrace(System.err);
                    System.exit(1);
                }
            } else {
                System.out.println("Choose game");
            }

        });
    }


    @Override
    public void addNewGame(SnakesProto.GameAnnouncement gameInfo) {
        if (!gamesInfo.containsKey(gameInfo.getGameName())) {
            log.info("map new game");
            gamesInfo.put(gameInfo.getGameName(), gameInfo);
            existGames.getItems().add(gameInfo.getGameName());
        }
    }

}

