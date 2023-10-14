package ru.nsu.fit.usoltsev.controller;

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
import lombok.extern.slf4j.Slf4j;
import ru.nsu.fit.usoltsev.GameConfig;
import ru.nsu.fit.usoltsev.listeners.NewGameListener;
import ru.nsu.fit.usoltsev.network.UdpController;
import ru.nsu.fit.usoltsev.network.gameMessageCreators.AnnouncementMsg;
import ru.nsu.fit.usoltsev.snakes.SnakesProto;

import java.net.SocketException;
import java.util.HashMap;
import java.util.concurrent.ThreadPoolExecutor;

import static ru.nsu.fit.usoltsev.GameConstants.*;

@Slf4j
public class MenuController implements NewGameListener {
    private GraphicsContext gc;
    private final TextField width, height, foodCount, timeDelay, gameName;
    private int widthValue, heightValue, foodCountValue, timeValue;
    private String gameNameValue;
    private final Button startButton, joinButton;
    private final ListView<String> existGamesView;
    private String selectedGame;
    private final HashMap<String, SnakesProto.GameAnnouncement> gamesInfo;

    public MenuController(AnchorPane root) {
        width = (TextField) root.lookup("#width");
        height = (TextField) root.lookup("#height");
        foodCount = (TextField) root.lookup("#foodCount");
        timeDelay = (TextField) root.lookup("#timeDelay");
        gameName = (TextField) root.lookup("#gameName");
        startButton = (Button) root.lookup("#startButton");
        joinButton = (Button) root.lookup("#joinButton");
        existGamesView = (ListView<String>) root.lookup("#existGames");

        gamesInfo = new HashMap<>();

    }

    private boolean checkInputConfig() {
        if (width.getText().isEmpty()) {
            width.setPromptText("Insert width");
            return false;
        } else if (height.getText().isEmpty()) {
            height.setPromptText("Insert height");
            return false;
        } else if (foodCount.getText().isEmpty()) {
            foodCount.setPromptText("Insert food count!");
            return false;
        } else if (timeDelay.getText().isEmpty()) {
            timeDelay.setPromptText("Insert time delay!");
            return false;
        } else if (gameName.getText().isEmpty()) {
            gameName.setPromptText("Insert game name!");
            return false;
        } else {
            try {
                widthValue = Integer.parseInt(width.getText());
                heightValue = Integer.parseInt(height.getText());
                timeValue = Integer.parseInt(timeDelay.getText());
                foodCountValue = Integer.parseInt(foodCount.getText());
                gameNameValue = gameName.getText();

                if (widthValue <= 0) {
                    width.clear();
                    width.setPromptText("Must be > 0");
                    return false;
                } else if (heightValue <= 0) {
                    height.clear();
                    height.setPromptText("Must be > 0");
                    return false;
                } else if (foodCountValue <= 0) {
                    foodCount.clear();
                    foodCount.setPromptText("Must be > 0");
                    return false;
                } else if (timeValue <= 0) {
                    timeDelay.clear();
                    return false;
                }
            } catch (NumberFormatException ne) {
                ne.printStackTrace(System.err);
                System.exit(1);
                return false;
            }
        }
        return true;
    }

    private void setWindowProperties(Scene scene, Stage stage) {
        Group group = new Group();
        Canvas canvas = new Canvas(widthValue * SQUARE_SIZE, heightValue * SQUARE_SIZE);
        group.getChildren().add(canvas);
        scene.setRoot(group);
        stage.setX(Screen.getPrimary().getVisualBounds().getWidth() / 2 - (double) (widthValue * SQUARE_SIZE) / 2);
        stage.setY(Screen.getPrimary().getVisualBounds().getHeight() / 2 - (double) (heightValue * SQUARE_SIZE) / 2);
        stage.sizeToScene();
        gc = canvas.getGraphicsContext2D();
    }

    public void newMenu(Stage stage, Scene scene) {

        startButton.setOnAction(event -> {
            if (checkInputConfig()) {
                try {
                    UdpController udpController = new UdpController();

                    GameConfig.setConstants(widthValue, heightValue, foodCountValue, timeValue, gameNameValue, MASTER);
                    setWindowProperties(scene, stage);

                    SnakesProto.GameMessage gameMessage = AnnouncementMsg.createAnnouncement(widthValue, heightValue, foodCountValue, timeValue, gameNameValue, MASTER);

                    udpController.setOutputMessage(MULTICAST_IP, MULTICAST_PORT, gameMessage);

                    GameControl gameControl = new GameControl(gc, scene);
                    gameControl.startGame();


                } catch (NumberFormatException | SocketException | InterruptedException ex) {
                    System.err.println(ex.getCause() + ex.getMessage());
                    System.exit(1);
                }
            }
        });

        existGamesView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                selectedGame = newValue;
            }
        });

        joinButton.setOnAction(event -> {
            if (selectedGame != null) {

                // TODO: send JoinMsg firstly
                try {

                    UdpController udpController = new UdpController();
                    udpController.sendJoinMsg();

                    SnakesProto.GameAnnouncement gameInfo = gamesInfo.get(selectedGame);
                    heightValue = gameInfo.getConfig().getHeight();
                    widthValue = gameInfo.getConfig().getWidth();
                    foodCountValue = gameInfo.getConfig().getFoodStatic();
                    timeValue = gameInfo.getConfig().getStateDelayMs();

                    setWindowProperties(scene, stage);


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
            existGamesView.getItems().add(gameInfo.getGameName());
        }
    }

}

