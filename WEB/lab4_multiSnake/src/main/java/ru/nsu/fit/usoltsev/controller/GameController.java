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
import ru.nsu.fit.usoltsev.listeners.GameStateListener;
import ru.nsu.fit.usoltsev.listeners.HostAddListener;
import ru.nsu.fit.usoltsev.listeners.SteerListener;
import ru.nsu.fit.usoltsev.model.FoodModel;
import ru.nsu.fit.usoltsev.model.SnakeModel;
import ru.nsu.fit.usoltsev.network.Udp.UdpController;
import ru.nsu.fit.usoltsev.network.gameMessageCreators.RoleChangeMsg;
import ru.nsu.fit.usoltsev.network.gameMessageCreators.StateMsg;
import ru.nsu.fit.usoltsev.snakes.SnakesProto;
import ru.nsu.fit.usoltsev.view.BackgroundView;
import ru.nsu.fit.usoltsev.view.InfoView;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

import static ru.nsu.fit.usoltsev.GameConfig.*;
import static ru.nsu.fit.usoltsev.GameConstants.*;
import static ru.nsu.fit.usoltsev.network.NetworkUtils.*;

@Slf4j
public class GameController implements HostAddListener, SteerListener, GameStateListener {
    private final HashMap<Integer, HostInfo> players;  // id - host info
    private final HashMap<Integer, HostInfo> viewers;  // id - host info
    public GraphicsContext gc;

    public Scene scene;
    public BackgroundView backgroundView;
    public InfoView infoView;
    public FoodModel foodModel;
    public final HashMap<Integer, Integer> stateChanges;  // id - change
    private final ArrayList<Integer> freeSquares;
    private final UdpController udpController;
    private int curStateOrder = 0;
    private int maxScore = -1;

    public GameController(GraphicsContext gc, Scene scene, UdpController udpController) {
        this.gc = gc;
        this.scene = scene;
        this.udpController = udpController;

        freeSquares = new ArrayList<>(ROWS * COLUMNS);
        players = new HashMap<>();
        viewers = new HashMap<>();
        stateChanges = new HashMap<>();

        backgroundView = new BackgroundView();
        foodModel = new FoodModel(freeSquares);
        infoView = new InfoView();
    }

    public void startGame() {
        switch (ROLE) {
            case MASTER -> {
                log.info(String.format("New master: name:%s, id: %d, port: %d, ip: %s, role: %d", PLAYER_NAME, ID, MASTER_PORT, MASTER_IP, ROLE));
                addNewSnake(PLAYER_NAME, ID, MASTER_PORT, MASTER_IP, ROLE);
                new MasterSnakeController(scene, this);
                foodModel.generateFood(players, freeSquares);
                Timeline timeline = new Timeline(new KeyFrame(Duration.millis(TIME_DELAY), e -> masterRun()));
                timeline.setCycleCount(Animation.INDEFINITE);
                timeline.play();
                log.info("Master timeline started");
            }
            case VIEWER -> {
                Timeline timeline = new Timeline(new KeyFrame(Duration.millis(TIME_DELAY), e -> normalRun()));
                timeline.setCycleCount(Animation.INDEFINITE);
                timeline.play();
                log.info("Viewer timeline started");
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
        synchronized (players) {
            Iterator<HostInfo> iterator = players.values().iterator();
            while (iterator.hasNext()) {
                HostInfo host = iterator.next();
                if (host.isGameOver()) {
                    foodModel.crushSnake(host.getModel().getSnakeBody());
                    host.getModel().getSnakeBody().clear();
                    changeRole(NORMAL, VIEWER, host);
                    iterator.remove();
                    System.out.println("players = " + players + " viewers = " + viewers);
                }
            }
        }

        backgroundView.drawBackground(gc);
        foodModel.drawFood(gc);

        synchronized (players) {
            synchronized (stateChanges) {
                for (Map.Entry<Integer, HostInfo> entry : players.entrySet()) {
                    Integer id = entry.getKey();
                    HostInfo curHost = entry.getValue();
                    SnakeModel curSnake = curHost.getModel();
                    if (stateChanges.containsKey(id)) {
                        curSnake.changeDirection(stateChanges.get(id));
                        stateChanges.remove(id);
                        players.get(id).setDirection(curSnake.getCurDirection());
                    }

                    if (eatFood(curSnake)) {
                        curHost.addScore(FOOD_SCORE);
                    }
                    curSnake.snakeMovement();
                    curSnake.drawSnake(gc);

                }
            }
        }
        checkSnakeCrush();
        if (players.containsKey(ID)) {
            maxScore = players.get(ID).getScore();
            infoView.drawScore(gc, maxScore);
            sendState();
        } else {
            infoView.drawGameOver(gc, maxScore);
        }


    }

    public void changeRole(int oldRole, int newRole, HostInfo host) {
        if (oldRole == NORMAL && newRole == VIEWER) {
            host.setRole(VIEWER);
            host.setModel(null);
            viewers.put(host.getID(), host);
            SnakesProto.GameMessage message = RoleChangeMsg.createRoleChange(VIEWER, MASTER, host.getID());
//            udpController.setOutputMessage(host.getIp(), host.getPort(), message);
        }
    }

    @Override
    public boolean addNewViewer(String name, int playerID, int port, InetAddress ip, int role) {
        HostInfo hostInfo = new HostInfo(name, playerID, port, ip, role, null, 0, false, RIGHT);
        viewers.put(playerID, hostInfo);
        System.out.println("viewers = " + viewers);
        return true;
    }

    @Override
    public boolean addNewSnake(String name, int playerID, int port, InetAddress ip, int role) {
        synchronized (players) {
            Light.Point generatePoint = findFreeSpace();
            if (generatePoint != null) {
                SnakeModel snakeModel = new SnakeModel(playerID);
                snakeModel.setSnakeBody((int) generatePoint.getX(), (int) generatePoint.getY());
                HostInfo hostInfo = new HostInfo(name, playerID, port, ip, role, snakeModel, 0, false, RIGHT);
                players.put(playerID, hostInfo);
                stateChanges.put(playerID, RIGHT);
                log.info("Add new snake " + hostInfo);
                log.info("Current snakes map = " + players);
                return true;
            } else {
                return false;
            }
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

    @Override
    public void setNewSteer(int direction, int senderID) {
        synchronized (stateChanges) {
            stateChanges.put(senderID, direction);
        }
    }

    public void sendState() {
        synchronized (players) {
            if (players.size() > 1 || !viewers.isEmpty()) {
                SnakesProto.GameMessage.Builder message = StateMsg.createState(players, viewers, foodModel.getFoodsSet());
                try {
                    for (var host : players.values()) {
                        if (host.getID() != ID) {
                            udpController.setOutputMessage(host.getIp(), host.getPort(), message.setReceiverId(host.getID()).build());
                            log.info("Send state to normal, id: " + host.getID());
                        }
                    }
                    for (var host : viewers.values()) {
                        udpController.setOutputMessage(host.getIp(), host.getPort(), message.setReceiverId(host.getID()).build());
                        log.info("Send state to viewer, id: " + host.getID());
                    }

                } catch (InterruptedException e) {
                    log.warn("Cannot send game state message", e);
                }
            }
        }
    }

    @Override
    public void setNewState(SnakesProto.GameMessage.StateMsg msg) {
        synchronized (players) {
            if (msg.getState().getStateOrder() <= curStateOrder) {
                return;
            }
            curStateOrder = msg.getState().getStateOrder();
            players.clear();
            viewers.clear();
            for (var player : msg.getState().getPlayers().getPlayersList()) {
                try {

                    String ipAddr = player.getIpAddress().substring(1);
                    InetAddress ip = InetAddress.getByName("192.168.1.172");
                    if (ipAddr.matches(IP_REGEX)) {
                        byte[] ipBytes = new byte[4];
                        String[] parts = ipAddr.split("\\.");
                        for (int i = 0; i < 4; i++) {
                            ipBytes[i] = (byte) Integer.parseInt(parts[i]);
                        }
                        ip = InetAddress.getByAddress(ipBytes);
                    }
                    HostInfo host = new HostInfo(player.getName(), player.getId(), player.getPort(),
                            ip, player.getRole().getNumber(), null,
                            player.getScore(), false, 0);

                    if (player.getRole().getNumber() == VIEWER) {
                        viewers.put(player.getId(), host);
                    } else {
                        host.setModel(new SnakeModel(player.getId()));
                        players.put(player.getId(), host);
                    }
                } catch (UnknownHostException e) {
                    log.warn("Exception while parsing game state, ip = " + player.getIpAddress(), e);
                }
            }
            for (var snake : msg.getState().getSnakesList()) {
                HostInfo curHost = players.get(snake.getPlayerId());
                curHost.setDirection(snake.getHeadDirection().getNumber());
                curHost.getModel().getSnakeBody().clear();
                for (var point : snake.getPointsList()) {
                    curHost.getModel().addPoint(point.getX(), point.getY());
                }
            }
            foodModel.getFoodsSet().clear();
            for (var food : msg.getState().getFoodsList()) {
                foodModel.getFoodsSet().add(food.getY() * COLUMNS + food.getX());
            }
        }
    }

    public void normalRun() {
        backgroundView.drawBackground(gc);
        foodModel.drawFood(gc);
        synchronized (players) {
            for (var host : players.values()) {
                host.getModel().drawSnake(gc);
            }
        }
        if (players.containsKey(ID)) {
            maxScore = players.get(ID).getScore();
            infoView.drawScore(gc, maxScore);
        } else if (maxScore != -1) {
            infoView.drawGameOver(gc, maxScore);
        }

    }

    private boolean eatFood(SnakeModel snakeModel) {
        HashSet<Integer> foods = foodModel.getFoodsSet();
        int snakeX = (int) snakeModel.getSnakeHead().getX();
        int snakeY = (int) snakeModel.getSnakeHead().getY();

        if (foods.contains(snakeY * COLUMNS + snakeX)) {
            foodModel.eraseOneFood(snakeX, snakeY, freeSquares);
            foodModel.generateOneFood(players, freeSquares);
            snakeModel.raiseUp((int) snakeModel.getSnakeBody().get(1).getX(),
                    (int) snakeModel.getSnakeBody().get(1).getY());
            return true;
        }
        return false;
    }

    private void checkSnakeCrush() {
        synchronized (players) {
            for (HostInfo snake : players.values()) {
                for (HostInfo curSnake : players.values()) {

                    if (snake.getID() != curSnake.getID()
                            && snake.getModel().getSnakeHead().getX() == curSnake.getModel().getSnakeHead().getX()
                            && snake.getModel().getSnakeHead().getY() == curSnake.getModel().getSnakeHead().getY()) {
                        snake.setGameOver(true);
                        curSnake.setGameOver(true);
                    }
                    for (int i = 1; i < curSnake.getModel().getSnakeBody().size(); i++) {
                        if (snake.getModel().getSnakeHead().getX() == curSnake.getModel().getSnakeBody().get(i).getX()
                                && snake.getModel().getSnakeHead().getY() == curSnake.getModel().getSnakeBody().get(i).getY()) {
                            snake.setGameOver(true);
                            curSnake.addScore(1);
                        }
                    }
                }
            }
        }
    }
}
