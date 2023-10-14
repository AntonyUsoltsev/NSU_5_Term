package ru.nsu.fit.usoltsev.controller;

import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import ru.nsu.fit.usoltsev.GameConfig;
import ru.nsu.fit.usoltsev.listeners.NewGameListener;
import ru.nsu.fit.usoltsev.network.MessageInfo;
import ru.nsu.fit.usoltsev.network.Udp.UdpController;
import ru.nsu.fit.usoltsev.network.gameMessageCreators.JoinMsg;
import ru.nsu.fit.usoltsev.snakes.SnakesProto;

import java.net.InetAddress;
import java.net.SocketException;
import java.util.HashMap;
import java.util.concurrent.ThreadPoolExecutor;

import static ru.nsu.fit.usoltsev.GameConstants.*;
import static ru.nsu.fit.usoltsev.GameConfig.*;

@Slf4j
public class MenuController implements NewGameListener {
    private GraphicsContext gc;

    @Getter
    private final UdpController udpController;
    private final TextField width, height, foodCount, timeDelay, gameName, playerName, joinPlayerName;
    private int widthValue, heightValue, foodCountValue, timeValue, joinPlayerRole;
    private String gameNameValue, playerNameValue, joinPlayerNameValue, joinType = "";
    private String selectedGame;
    private final Button startButton, joinButton;
    private final ListView<String> existGamesView;
    private final HashMap<String, MessageInfo> gamesInfo;

    public MenuController(AnchorPane root, ThreadPoolExecutor executor) throws SocketException {
        width = (TextField) root.lookup("#width");
        height = (TextField) root.lookup("#height");
        foodCount = (TextField) root.lookup("#foodCount");
        timeDelay = (TextField) root.lookup("#timeDelay");
        gameName = (TextField) root.lookup("#gameName");
        playerName = (TextField) root.lookup("#playerName");
        joinPlayerName = (TextField) root.lookup("#joinPlayerName");

        startButton = (Button) root.lookup("#startButton");
        joinButton = (Button) root.lookup("#joinButton");

        RadioButton joinViewerButton = (RadioButton) root.lookup("#joinViewerButton");
        RadioButton joinPlayerButton = (RadioButton) root.lookup("#joinPlayerButton");

        ToggleGroup toggleGroup = new ToggleGroup();
        joinViewerButton.setToggleGroup(toggleGroup);
        joinPlayerButton.setToggleGroup(toggleGroup);

        toggleGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                joinType = "";
            } else {
                joinType = ((RadioButton) newValue).getText();
            }
        });

        udpController = new UdpController(executor);
        existGamesView = (ListView<String>) root.lookup("#existGames");

        gamesInfo = new HashMap<>();

    }

    private boolean isFieldEmpty(TextField textField, String item) {
        if (textField.getText().isEmpty()) {
            textField.setPromptText("Insert " + item);
            return true;
        }
        return false;
    }

    private boolean isValueInvalidate(TextField textField, int value) {
        if (value <= 0) {
            textField.clear();
            textField.setPromptText("Must be > 0");
            return true;
        }
        return false;
    }

    private int tryParse(TextField textField) {
        try {
            return Integer.parseInt(textField.getText());
        } catch (NumberFormatException ne) {
            textField.clear();
            textField.setPromptText("Must be integer");
            return -1;
        }

    }

    private boolean checkNewGameConfig() {
        if (isFieldEmpty(width, "width") ||
                isFieldEmpty(height, "height") ||
                isFieldEmpty(foodCount, "food count") ||
                isFieldEmpty(timeDelay, "time delay") ||
                isFieldEmpty(gameName, "game name") ||
                isFieldEmpty(playerName, "player name")) {
            return false;
        } else {
            widthValue = tryParse(width);
            heightValue = tryParse(height);
            timeValue = tryParse(timeDelay);
            foodCountValue = tryParse(foodCount);

            gameNameValue = gameName.getText();
            playerNameValue = playerName.getText();

            if (isValueInvalidate(width, widthValue) ||
                    isValueInvalidate(height, heightValue) ||
                    isValueInvalidate(foodCount, foodCountValue) ||
                    isValueInvalidate(timeDelay, timeValue)) {
                return false;
            }
        }
        return true;
    }

    private boolean checkJoinConfig() {
        if (selectedGame != null &&
                !isFieldEmpty(joinPlayerName, "player name") &&
                !joinType.isEmpty()) {
            joinPlayerNameValue = joinPlayerName.getText();
            switch (joinType) {
                case "Player" -> joinPlayerRole = NORMAL;
                case "Viewer" -> joinPlayerRole = VIEWER;
            }
            return true;
        }
        return false;


    }

    private void setWindowProperties(Scene scene, Stage stage) {
        Group group = new Group();
        Canvas canvas = new Canvas(WIDTH, HEIGHT);
        group.getChildren().add(canvas);
        scene.setRoot(group);
        stage.setX(Screen.getPrimary().getVisualBounds().getWidth() / 2 - (double) (WIDTH) / 2);
        stage.setY(Screen.getPrimary().getVisualBounds().getHeight() / 2 - (double) (HEIGHT) / 2);
        stage.sizeToScene();
        gc = canvas.getGraphicsContext2D();
    }

    @Override
    public void addNewGame(InetAddress ip, int port, SnakesProto.GameMessage gameMessage) {
        String gameName = gameMessage.getAnnouncement().getGames(0).getGameName();
        if (!gamesInfo.containsKey(gameName)) {
            log.info("map new game");
            MessageInfo messageInfo = new MessageInfo(ip, port, gameMessage);
            gamesInfo.put(gameName, messageInfo);
            existGamesView.getItems().add(gameName);
        }
    }


    public void newMenu(Stage stage, Scene scene) {

        startButton.setOnAction(event -> {
            if (checkNewGameConfig()) {

                udpController.startSendRecv();

                GameConfig.setConstants(widthValue, heightValue, foodCountValue, timeValue, gameNameValue, playerNameValue, MASTER);
                setWindowProperties(scene, stage);

                // SnakesProto.GameMessage gameMessage = AnnouncementMsg.createAnnouncement(widthValue, heightValue, foodCountValue, timeValue, gameNameValue, MASTER);
                udpController.startAnnouncement();
                // udpController.setOutputMessage(MULTICAST_IP, MULTICAST_PORT, gameMessage);

                GameControl gameControl = new GameControl(gc, scene);
                gameControl.startGame();


            }
        });

        existGamesView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                selectedGame = newValue;
            }
        });

        joinButton.setOnAction(event -> {
            if (checkJoinConfig()) {
                // TODO: send JoinMsg firstly
                try {
                    udpController.startSendRecv();

                    MessageInfo messageInfo = gamesInfo.get(selectedGame);
                    SnakesProto.GameAnnouncement gameInfo = messageInfo.gameMessage().getAnnouncement().getGames(0);

                    SnakesProto.GameMessage joinMsg = JoinMsg.createJoin(joinPlayerNameValue, gameInfo.getGameName(), joinPlayerRole);
                    udpController.setOutputMessage(messageInfo.ipAddr(), messageInfo.port(), joinMsg);

                   // System.out.println(messageInfo.ipAddr().toString() + " " +  messageInfo.port());

                    heightValue = gameInfo.getConfig().getHeight();
                    widthValue = gameInfo.getConfig().getWidth();
                    foodCountValue = gameInfo.getConfig().getFoodStatic();
                    timeValue = gameInfo.getConfig().getStateDelayMs();

                    // log.info("height "+ heightValue + ",width " + widthValue + ",food count " + foodCountValue + ",time delay " + timeValue);

                    GameConfig.setConstants(widthValue, heightValue, foodCountValue, timeValue, gameNameValue, joinPlayerNameValue, NORMAL);

                    setWindowProperties(scene, stage);


                    GameControl gameControl = new GameControl(gc, scene);

                    gameControl.startGame();

                } catch (InterruptedException ex) {
                    ex.printStackTrace(System.err);
                    System.exit(1);
                }
            } else {
                System.out.println("Choose game");
            }

        });
    }


}

