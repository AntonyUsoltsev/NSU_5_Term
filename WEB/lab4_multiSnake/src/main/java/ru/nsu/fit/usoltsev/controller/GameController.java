package ru.nsu.fit.usoltsev.controller;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.Scene;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.Light;
import javafx.util.Duration;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import ru.nsu.fit.usoltsev.HostInfo;
import ru.nsu.fit.usoltsev.listeners.*;
import ru.nsu.fit.usoltsev.model.FoodModel;
import ru.nsu.fit.usoltsev.model.SnakeModel;
import ru.nsu.fit.usoltsev.network.NetworkUtils;
import ru.nsu.fit.usoltsev.network.Udp.UdpController;
import ru.nsu.fit.usoltsev.network.gameMessageCreators.RoleChangeMsg;
import ru.nsu.fit.usoltsev.network.gameMessageCreators.StateMsg;
import ru.nsu.fit.usoltsev.snakes.SnakesProto;
import ru.nsu.fit.usoltsev.view.BackgroundView;
import ru.nsu.fit.usoltsev.view.FoodView;
import ru.nsu.fit.usoltsev.view.InfoView;
import ru.nsu.fit.usoltsev.view.SnakeView;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

import static ru.nsu.fit.usoltsev.GameConfig.*;
import static ru.nsu.fit.usoltsev.GameConstants.*;
import static ru.nsu.fit.usoltsev.network.NetworkUtils.*;

@Slf4j
public class GameController implements HostAddListener, SteerListener, GameStateListener, RoleChangeListener, DisconnectListener {
    @Getter
    private final HashMap<Integer, HostInfo> players;  // id - host info
    @Getter
    private final HashMap<Integer, HostInfo> viewers;  // id - host info
    private final HashMap<Integer, Integer> stateChanges;  // id - change
    private final HashMap<String, Integer> ipPortId; // ip:port - id
    private final ArrayList<Integer> freeSquares;
    private final BackgroundView backgroundView;
    private final UdpController udpController;
    private final FoodModel foodModel;
    private final InfoView infoView;
    private final GraphicsContext gc;
    private final Scene scene;
    private int curStateOrder = 0;
    private int maxScore = -1;
    private int deputyPretend = -1;
    private boolean deputyChosen = false;
    private volatile SnakesProto.GameMessage.StateMsg lastMessage;
    private Timeline timeline;

    public GameController(GraphicsContext gc, Scene scene, UdpController udpController) {
        this.gc = gc;
        this.scene = scene;
        this.udpController = udpController;

        freeSquares = new ArrayList<>(ROWS * COLUMNS);
        players = new HashMap<>();
        viewers = new HashMap<>();
        stateChanges = new HashMap<>();
        ipPortId = new HashMap<>();

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
                timeline = new Timeline(new KeyFrame(Duration.millis(TIME_DELAY), e -> masterRun()));
                timeline.setCycleCount(Animation.INDEFINITE);
                timeline.play();
                log.info("Master timeline started");
            }
            case VIEWER -> {
                timeline = new Timeline(new KeyFrame(Duration.millis(TIME_DELAY), e -> normalRun()));
                timeline.setCycleCount(Animation.INDEFINITE);
                timeline.play();
                log.info("Viewer timeline started");
            }
            case NORMAL -> {
                new NormalSnakeController(scene, udpController);
                timeline = new Timeline(new KeyFrame(Duration.millis(TIME_DELAY), e -> normalRun()));
                timeline.setCycleCount(Animation.INDEFINITE);
                timeline.play();
                log.info("Normal timeline started");
            }
            case DEPUTY -> {
                log.info("Deputy start game?");
            }
        }
    }
    private void masterRun() {
        chooseDeputy();
        removeAfterCrush();
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
        infoView.drawPlayersInfo(gc, players, viewers);

        if (players.containsKey(ID)) {
            maxScore = players.get(ID).getScore();
        } else {
            infoView.drawGameOver(gc, maxScore);
        }
//        log.info("Send state");
        sendState();
    }
    public void chooseDeputy() {
        if (!deputyChosen && players.size() >= 2) {
            int id = -1;
            for (var player : players.values()) {
                if (player.getID() == ID || player.getStatus() == ZOMBIE) {
                    continue;
                }
                id = player.getID();
            }
            if (id != -1) {
                sendChangeRole(NORMAL, DEPUTY, players.get(id));
                deputyPretend = id;
                deputyChosen = true;
            }
        }
    }
    private void removeAfterCrush() {
        synchronized (players) {
            Iterator<HostInfo> iterator = players.values().iterator();
            while (iterator.hasNext()) {
                HostInfo host = iterator.next();
                if (host.isGameOver()) {
                    foodModel.crushSnake(host.getModel().getSnakeBody());
                    host.getModel().getSnakeBody().clear();
                    if (host.getStatus() != ZOMBIE) {
                        sendChangeRole(host.getRole(), VIEWER, host);
                    }
                    if (host.getID() == deputyPretend) {
                        deputyChosen = false;
                    }
                    iterator.remove();
                    FOOD_COUNT--;
                    System.out.println("players = " + players + " viewers = " + viewers);
                }
            }
        }
    }
    private boolean eatFood(SnakeModel snakeModel) {
        HashSet<Integer> foods = foodModel.getFoodsSet();
        int snakeX = (int) snakeModel.getSnakeHead().getX();
        int snakeY = (int) snakeModel.getSnakeHead().getY();

        if (foods.contains(snakeY * COLUMNS + snakeX)) {
            foodModel.eraseOneFood(snakeX, snakeY, freeSquares);
            foodModel.regenerateFood(players, freeSquares);
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
                        log.info("Crush:" + snake.getID() + " each other " + curSnake.getID());

                    }
                    for (int i = 1; i < curSnake.getModel().getSnakeBody().size(); i++) {
                        if (snake.getModel().getSnakeHead().getX() == curSnake.getModel().getSnakeBody().get(i).getX()
                                && snake.getModel().getSnakeHead().getY() == curSnake.getModel().getSnakeBody().get(i).getY()) {
                            snake.setGameOver(true);
                            curSnake.addScore(1);
                            log.info("Crush:" + snake.getID() + " into " + curSnake.getID());
                        }
                    }
                }
            }
        }
    }

    @Override
    public void setRoleChange(SnakesProto.GameMessage.RoleChangeMsg msg) {
        log.info("Role changed from " + roles.get(ROLE) + " to " + roles.get(msg.getReceiverRole().getNumber()) + " in id " + ID);
        ROLE = msg.getReceiverRole().getNumber();
    }

    public void sendChangeRole(int oldRole, int newRole, HostInfo host) {
        try {
            if ((oldRole == NORMAL || oldRole == DEPUTY) && newRole == VIEWER) {
                host.setRole(VIEWER);
                host.setModel(null);
                viewers.put(host.getID(), host);
                log.info("Change role from " + oldRole + " to Viewer on " + host);
                SnakesProto.GameMessage message = RoleChangeMsg.createRoleChange(VIEWER, MASTER, host.getID());
                udpController.setOutputMessage(host.getIp(), host.getPort(), message);
            } else if (oldRole == NORMAL && newRole == DEPUTY) {
                host.setRole(DEPUTY);
                log.info("Change role from Normal to Deputy on " + host);
                SnakesProto.GameMessage message = RoleChangeMsg.createRoleChange(DEPUTY, MASTER, host.getID());
                udpController.setOutputMessage(host.getIp(), host.getPort(), message);
            } else if (oldRole == MASTER && newRole == VIEWER) {
//                host.setRole(VIEWER);
                host.setModel(null);
                host.setStatus(ZOMBIE);
                viewers.put(host.getID(), host);
                scene.setOnKeyPressed(null);
                log.info("Change role from Master to Viewer on " + host);
            } else {
                log.warn("Unknown role change: oldRole " + oldRole + " newRole " + newRole);
            }
        } catch (Exception e) {
            log.warn("Failed to set roleChange message", e);
        }
    }

    @Override
    public boolean addNewViewer(String name, int playerID, int port, InetAddress ip, int role) {
        HostInfo hostInfo = new HostInfo(name, playerID, port, ip, role, null, 0, false, RIGHT, ALIVE);
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
                HostInfo hostInfo = new HostInfo(name, playerID, port, ip, role, snakeModel, 0, false, RIGHT, ALIVE);
                players.put(playerID, hostInfo);
                ipPortId.put(ip + ":" + port, playerID);
                stateChanges.put(playerID, RIGHT);
                log.info("Add new snake " + hostInfo);
                log.info("Current snakes map = " + players);
                FOOD_COUNT++;
                return true;
            } else {
                return false;
            }
        }
    }

    private Light.Point findFreeSpace() {
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

    @Override
    public void setNewSteer(int direction, String ipPortInfo) {
        int id = ipPortId.get(ipPortInfo);
        synchronized (stateChanges) {
            stateChanges.put(id, direction);
        }
    }

    private void sendState() {
        synchronized (players) {
            if (!players.isEmpty() || !viewers.isEmpty()) {
                SnakesProto.GameMessage.Builder message = StateMsg.createState(players, viewers, foodModel.getFoodsSet());
                for (var host : players.values()) {
                    if (host.getID() != ID && host.getStatus() == ALIVE) {
                        udpController.setOutputMessage(host.getIp(), host.getPort(), message.setReceiverId(host.getID()).build());
                    }
                }
                viewers.entrySet()
                        .removeIf(entry -> entry.getValue().getStatus() == ZOMBIE && entry.getValue().getRole() != MASTER);
                for (var host : viewers.values()) {
                    if (host.getID() != ID) {
                        udpController.setOutputMessage(host.getIp(), host.getPort(), message.setReceiverId(host.getID()).build());
                    }
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
            lastMessage = msg;
        }
    }

    private void normalRun() {
        if (ROLE == VIEWER) {
            scene.setOnKeyPressed(null);
        }
        backgroundView.drawBackground(gc);
        if (lastMessage != null) {
            FoodView.drawFood(gc, lastMessage);
            SnakeView.drawSnake(gc, lastMessage);
            infoView.drawPlayersInfo(gc, lastMessage);
        }

    }

    private void restoreStateFromMessage() {
        log.info("Restore State");
        for (var player : lastMessage.getState().getPlayers().getPlayersList()) {
            HostInfo host = new HostInfo(player.getName(), player.getId(), player.getPort(),
                    null, player.getRole().getNumber(), null,
                    player.getScore(), false, 0, ALIVE);
            InetAddress ip = null;
            try {
                ip = InetAddress.getByName("192.168.1.172");
                if (player.hasIpAddress()) {
                    ip = NetworkUtils.parseIp(player.getIpAddress().toString());
                    ipPortId.put(player.getIpAddress() + ":" + player.getPort(), player.getId());
                }
                if (player.getRole() == SnakesProto.NodeRole.MASTER) {
                    ipPortId.put(MASTER_IP.toString() + ":" + MASTER_PORT, player.getId());
                    host.setStatus(ZOMBIE);
                }
            } catch (UnknownHostException e) {
                log.warn("Exception while parsing game state, ip = " + player.getIpAddress(), e);
            }
            host.setIp(ip);
            if (player.getRole().getNumber() == VIEWER) {
                viewers.put(player.getId(), host);
            } else {
                host.setModel(new SnakeModel(player.getId()));
                players.put(player.getId(), host);
            }
            if (host.getID() == ID) {
                ROLE = host.getRole();
            }
        }
        for (var snake : lastMessage.getState().getSnakesList()) {
            HostInfo curHost = players.get(snake.getPlayerId());
            curHost.setDirection(snake.getHeadDirection().getNumber());
            curHost.setStatus(snake.getState().getNumber());
            curHost.getModel().getSnakeBody().clear();
            for (var point : snake.getPointsList()) {
                curHost.getModel().addPoint(point.getX(), point.getY());
            }
        }
        foodModel.getFoodsSet().clear();
        for (var food : lastMessage.getState().getFoodsList()) {
            foodModel.getFoodsSet().add(food.getY() * COLUMNS + food.getX());
        }

        System.out.println("After resotore:" + players + viewers);
    }

    @Override
    public void disconnectPlayer(String inetInfo) {
        if (ROLE != MASTER) {
            restoreStateFromMessage();
        }
        System.out.println(ipPortId);
        System.out.println(inetInfo);
        System.out.println(ipPortId.containsKey(inetInfo));
        int id = ipPortId.get(inetInfo);
        synchronized (players) {
            System.out.println(players.containsKey(id));
            if (players.containsKey(id)) {
                HostInfo player = players.get(id);
//                System.out.println(player);
                if (player.getRole() == DEPUTY) {
                    log.info("AFK was DEPUTY");
                    player.setRole(NORMAL);
                    player.setStatus(ZOMBIE);
                    deputyChosen = false;
                    deputyPretend = -1;
                }
                if (player.getRole() == MASTER && players.get(ID).getRole() == DEPUTY) {
                    if (player.getStatus() == ZOMBIE) {
                        System.out.println("Master is zombie");
                        players.remove(id);
                        viewers.put(id, player);
                    }
                    else{
                        player.setStatus(ZOMBIE);
                    }
                    log.info("AFK was MASTER, Noticed DEPUTY");
                    deputyToMaster();
                }
                if (player.getRole() == MASTER && (ROLE == NORMAL || ROLE == VIEWER)) {
                    log.info("AFK was MASTER, noticed NORMAL");
                    player.setStatus(ZOMBIE);
                    for (var statePlayer : lastMessage.getState().getPlayers().getPlayersList()) {
                        try {
                            if (statePlayer.getRole() == SnakesProto.NodeRole.DEPUTY) {
                                MASTER_IP = NetworkUtils.parseIp(statePlayer.getIpAddress());
                                MASTER_PORT = statePlayer.getPort();
                                break;
                            }
                        } catch (UnknownHostException e) {
                            log.error("Failed to parse IP in disconnectPlayer", e);
                        }
                    }
                }
                log.info("Set status Zombie to player " + id);
                return;
            }
        }
        synchronized (viewers) {
            if (viewers.containsKey(id)) {
                viewers.get(id).setStatus(ZOMBIE);
                log.info("Set status Zombie to viewer " + id);
                return;
            }
        }
    }

    public void deputyToMaster(){
        scene.setOnKeyPressed(null);
        new MasterSnakeController(scene, this);
        ROLE = MASTER;
        players.get(ID).setRole(MASTER);
        udpController.stopPing();
        udpController.startAnnouncement();
        deputyPretend = -1;
        deputyChosen = false;
        STATE_SEQ.set(lastMessage.getState().getStateOrder());
        int maxKey = Integer.MIN_VALUE;
        for (int key : players.keySet()) {
            if (key > maxKey) {
                maxKey = key;
            }
        }
        ID_JOIN.set(maxKey);
        timeline.stop();
        log.info("Deputy became MASTER");
        timeline = new Timeline(new KeyFrame(Duration.millis(TIME_DELAY), e -> masterRun()));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

}
