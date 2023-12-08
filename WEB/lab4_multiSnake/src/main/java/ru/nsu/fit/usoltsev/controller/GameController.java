package ru.nsu.fit.usoltsev.controller;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.Scene;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.Light;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;
import ru.nsu.fit.usoltsev.GameConfig;
import ru.nsu.fit.usoltsev.listeners.SnakeAddListener;
import ru.nsu.fit.usoltsev.listeners.StateChangeListener;
import ru.nsu.fit.usoltsev.model.FoodModel;
import ru.nsu.fit.usoltsev.model.SnakeModel;
import ru.nsu.fit.usoltsev.network.Udp.UdpController;
import ru.nsu.fit.usoltsev.network.gameMessageCreators.StateMsg;
import ru.nsu.fit.usoltsev.snakes.SnakesProto;
import ru.nsu.fit.usoltsev.view.BackgroundView;
import ru.nsu.fit.usoltsev.view.InfoView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ru.nsu.fit.usoltsev.GameConfig.*;
import static ru.nsu.fit.usoltsev.GameConstants.*;

@Slf4j
public class GameController implements SnakeAddListener, StateChangeListener {
    private boolean gameOver;
    private int score;
    public GraphicsContext gc;

    public Scene scene;
    public BackgroundView backgroundView;
    public InfoView infoView;
    public FoodModel foodModel;
    public final HashMap<Integer, SnakeModel> snakeModelMap;
    public final HashMap<Integer, Integer> stateChanges;
    private final List<Integer> freeSquares;
    private final UdpController udpController;

    public GameController(GraphicsContext gc, Scene scene, UdpController udpController) {
        this.gc = gc;
        this.scene = scene;
        this.udpController = udpController;
        freeSquares = new ArrayList<>(ROWS * COLUMNS);
        backgroundView = new BackgroundView();
        foodModel = new FoodModel(freeSquares);
        infoView = new InfoView();
        snakeModelMap = new HashMap<>();
        stateChanges = new HashMap<>();
    }

    @Override
    public boolean addNewSnake(int playerID) {
        synchronized (snakeModelMap){
            Light.Point generatePoint = findFreeSpace();
            if (generatePoint != null) {
                SnakeModel snakeModel = new SnakeModel();
                snakeModelMap.put(playerID, snakeModel);
                snakeModel.setSnakeBody((int) generatePoint.getX(), (int) generatePoint.getY());
                stateChanges.put(playerID, RIGHT);
                log.info(String.format("Add new snake, id = %d, ", playerID));
                log.info("Current snakes map = " + snakeModelMap);
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
                addNewSnake(ID);
                new MasterSnakeController(scene, this);
                foodModel.generateFood(snakeModelMap, freeSquares);
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

        synchronized (snakeModelMap) {
            synchronized (stateChanges) {
                for (Map.Entry<Integer, SnakeModel> entry : snakeModelMap.entrySet()) {
                    Integer key = entry.getKey();
                    SnakeModel curSnake = entry.getValue();
                    if (stateChanges.containsKey(key)) {
                        curSnake.changeDirection(stateChanges.get(key));
                        stateChanges.remove(key);
                    }
                    curSnake.snakeMovement();
                    curSnake.drawSnake(gc);
                    eatFood(curSnake);
                }
            }
        }

     //   gameOver = snakeCrush();
        infoView.drawScore(gc, score);

        SnakesProto.GameMessage message = StateMsg.createState();
        sendState(message);

    }

    public void sendState(SnakesProto.GameMessage message){
        synchronized (HOSTS_IP_PORT){

        }
    }

    public void normalRun() {
        backgroundView.drawBackground(gc);
        foodModel.drawFood(gc);
    }

    private void eatFood(SnakeModel snakeModel) {
        int[][] foods = foodModel.getFoodsCoords();
        int snakeX = (int) snakeModel.getSnakeHead().getX();
        int snakeY = (int) snakeModel.getSnakeHead().getY();

        if (foods[snakeX][snakeY] == FOOD) {
            score += FOOD_SCORE;
            foodModel.eraseOneFood(snakeX, snakeY, freeSquares);
            foodModel.generateOneFood(snakeModelMap, freeSquares);
            snakeModel.raiseUp();
        }
    }

    private boolean snakeCrush() {
        for (SnakeModel snake : snakeModelMap.values()) {
            for (SnakeModel curSnake : snakeModelMap.values()) {
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
