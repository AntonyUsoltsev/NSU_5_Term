package ru.nsu.fit.usoltsev.controller;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.Scene;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.Light;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;
import ru.nsu.fit.usoltsev.HostInfo;
import ru.nsu.fit.usoltsev.listeners.SnakeAddListener;
import ru.nsu.fit.usoltsev.listeners.StateChangeListener;
import ru.nsu.fit.usoltsev.model.FoodModel;
import ru.nsu.fit.usoltsev.model.SnakeModel;
import ru.nsu.fit.usoltsev.network.Udp.UdpController;
import ru.nsu.fit.usoltsev.network.gameMessageCreators.StateMsg;
import ru.nsu.fit.usoltsev.snakes.SnakesProto;
import ru.nsu.fit.usoltsev.view.BackgroundView;
import ru.nsu.fit.usoltsev.view.InfoView;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static ru.nsu.fit.usoltsev.GameConfig.*;
import static ru.nsu.fit.usoltsev.GameConstants.*;

@Slf4j
public class GameController implements SnakeAddListener, StateChangeListener {
    private boolean gameOver;
    private final HashMap<Integer, HostInfo> hosts; // id - host info
    //   private final HashMap<Integer, Integer> scores;  // id - score
    public GraphicsContext gc;

    public Scene scene;
    public BackgroundView backgroundView;
    public InfoView infoView;
    public FoodModel foodModel;
    // public final HashMap<Integer, SnakeModel> snakeModelMap;  // id - model
    public final HashMap<Integer, Integer> stateChanges;    // id - change
    private final ArrayList<Integer> freeSquares;
    private final UdpController udpController;

    public GameController(GraphicsContext gc, Scene scene, UdpController udpController) {
        this.gc = gc;
        this.scene = scene;
        this.udpController = udpController;
        freeSquares = new ArrayList<>(ROWS * COLUMNS);
        hosts = new HashMap<>();
        backgroundView = new BackgroundView();
        foodModel = new FoodModel(freeSquares);
        infoView = new InfoView();
        stateChanges = new HashMap<>();
    }

    @Override
    public boolean addNewSnake(String name, int playerID, int port, InetAddress ip, int role) {
        synchronized (hosts) {
            Light.Point generatePoint = findFreeSpace();
            if (generatePoint != null) {
                SnakeModel snakeModel = new SnakeModel(playerID);
                snakeModel.setSnakeBody((int) generatePoint.getX(), (int) generatePoint.getY());
                HostInfo hostInfo = new HostInfo(name, playerID, port, ip, role, snakeModel, 0, false, RIGHT);
                hosts.put(playerID, hostInfo);
                stateChanges.put(playerID, RIGHT);
                log.info(String.format("Add new snake - name:%s, id: %d, port: %d, ip: %s, role: %d", name, playerID, port, ip, role));
                log.info("Current snakes map = " + hosts);
                return true;
            } else {
                return false;
            }
        }

    }

    @Override
    public void addNewState(int direction, int senderID) {
        synchronized (stateChanges) {
            stateChanges.put(senderID, direction);

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
        switch (ROLE) {
            case MASTER -> {
                log.info(String.format("New master: name:%s, id: %d, port: %d, ip: %s, role: %d", PLAYER_NAME, ID, MASTER_PORT, MASTER_IP, ROLE));
                addNewSnake(PLAYER_NAME, ID, MASTER_PORT, MASTER_IP, ROLE);
                new MasterSnakeController(scene, this);
                foodModel.generateFood(hosts, freeSquares);
                Timeline timeline = new Timeline(new KeyFrame(Duration.millis(TIME_DELAY), e -> masterRun()));
                timeline.setCycleCount(Animation.INDEFINITE);
                timeline.play();
                log.info("Master timeline started");
            }
            case VIEWER -> {

            }
            case NORMAL -> {
                new NormalSnakeController(scene, udpController);
                Timeline timeline = new Timeline(new KeyFrame(Duration.millis(TIME_DELAY), e -> normalRun()));
                timeline.setCycleCount(Animation.INDEFINITE);
                timeline.play();
                log.info("Normal timeline started");
            }
            case DEPUTY -> {

            }
        }
    }

    public void masterRun() {
//        if (gameOver) {
//            infoView.drawGameOver(gc);
//            return;
//        }

        backgroundView.drawBackground(gc);
        foodModel.drawFood(gc);

        synchronized (hosts) {
            synchronized (stateChanges) {
                for (Map.Entry<Integer, HostInfo> entry : hosts.entrySet()) {
                    Integer id = entry.getKey();
                    HostInfo curHost = entry.getValue();
                    SnakeModel curSnake = curHost.getModel();
                    if (stateChanges.containsKey(id)) {
                        curSnake.changeDirection(stateChanges.get(id));
                        stateChanges.remove(id);
                        hosts.get(id).setDirection(curSnake.getCurDirection());
                    }
                    curSnake.snakeMovement();
                    curSnake.drawSnake(gc);
                    if (eatFood(curSnake)) {
                        curHost.addScore(FOOD_SCORE);
                    }
                }
            }
        }

        //   gameOver = snakeCrush();
        infoView.drawScore(gc, hosts.get(ID).getScore());

        SnakesProto.GameMessage.Builder message = StateMsg.createState(hosts, foodModel.getFoodsMap());
        sendState(message);

    }

    public void sendState(SnakesProto.GameMessage.Builder message) {
        synchronized (hosts) {
            try {
                for (var host : hosts.values()) {
                    udpController.setOutputMessage(host.getIp(), host.getPort(), message.setReceiverId(host.getID()).build());
                }
            } catch (InterruptedException e) {
                log.warn("Cannot send game state message", e);
            }
        }
    }

    public void normalRun() {
        backgroundView.drawBackground(gc);
        foodModel.drawFood(gc);
    }

    private boolean eatFood(SnakeModel snakeModel) {
        int[][] foods = foodModel.getFoodsCoords();
        int snakeX = (int) snakeModel.getSnakeHead().getX();
        int snakeY = (int) snakeModel.getSnakeHead().getY();

        if (foods[snakeX][snakeY] == FOOD) {
            foodModel.eraseOneFood(snakeX, snakeY, freeSquares);
            foodModel.generateOneFood(hosts, freeSquares);
            snakeModel.raiseUp();
            return true;
        }
        return false;
    }

    private boolean snakeCrush() {
        for (HostInfo snake : hosts.values()) {
            for (HostInfo curSnake : hosts.values()) {
                for (int i = 1; i < curSnake.getModel().getSnakeBody().size(); i++) {
                    if (snake.getModel().getSnakeHead().getX() == curSnake.getModel().getSnakeBody().get(i).getX()
                            && snake.getModel().getSnakeHead().getY() == curSnake.getModel().getSnakeBody().get(i).getY()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

}
