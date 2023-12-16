package ru.nsu.fit.usoltsev.network.gameMessageCreators;

import ru.nsu.fit.usoltsev.HostInfo;
import ru.nsu.fit.usoltsev.snakes.SnakesProto;

import java.util.HashMap;
import java.util.HashSet;

import static ru.nsu.fit.usoltsev.GameConfig.COLUMNS;
import static ru.nsu.fit.usoltsev.GameConfig.ID;
import static ru.nsu.fit.usoltsev.network.NetworkUtils.MSG_SEQ;
import static ru.nsu.fit.usoltsev.network.NetworkUtils.STATE_SEQ;

public class StateMsg {

    public static SnakesProto.GameMessage.Builder createState(HashMap<Integer, HostInfo> hosts, HashMap<Integer, HostInfo> viewers, HashSet<Integer> foods) {

        SnakesProto.GameState.Builder state = SnakesProto.GameState.newBuilder()
                .setStateOrder(STATE_SEQ.getAndIncrement());
        boolean flag = false;

        for (Integer oneFood : foods) {
            if (oneFood < 0) {
                flag = true;
            }
            SnakesProto.GameState.Coord coord = SnakesProto.GameState.Coord.newBuilder()
                    .setX(oneFood % COLUMNS)
                    .setY(oneFood / COLUMNS)
                    .build();
            state.addFoods(coord);
        }


        SnakesProto.GamePlayers.Builder gamePlayers = SnakesProto.GamePlayers.newBuilder();
        for (var host : hosts.values()) {
            SnakesProto.GameState.Snake.Builder snake = SnakesProto.GameState.Snake.newBuilder()
                    .setState(SnakesProto.GameState.Snake.SnakeState.ALIVE)
                    .setHeadDirection(SnakesProto.Direction.forNumber(host.getDirection()))
                    .setPlayerId(host.getID());
            for (int i = 0; i < host.getModel().getSnakeBody().size(); i++) {

                int x = (int) host.getModel().getSnakeBody().get(i).getX();
                int y = (int) host.getModel().getSnakeBody().get(i).getY();

                SnakesProto.GameState.Coord coord = SnakesProto.GameState.Coord.newBuilder()
                        .setX((int) host.getModel().getSnakeBody().get(i).getX())
                        .setY((int) host.getModel().getSnakeBody().get(i).getY())
                        .build();

                snake.addPoints(coord);

                if (x < 0 || y < 0) {
                    flag = true;
                }
            }
            state.addSnakes(snake.build());

            gamePlayers.addPlayers(onePlayerInfo(host));

        }
        for (var viewer : viewers.values()) {
            gamePlayers.addPlayers(onePlayerInfo(viewer));
        }

        state.setPlayers(gamePlayers.build());

        SnakesProto.GameMessage.StateMsg stateMsg = SnakesProto.GameMessage.StateMsg.newBuilder()
                .setState(state.build())
                .build();

        SnakesProto.GameMessage.Builder gameMessage = SnakesProto.GameMessage.newBuilder()
                .setMsgSeq(MSG_SEQ.getAndIncrement())
                .setState(stateMsg)
                .setSenderId(ID);

        if (flag) {
            System.out.println(gameMessage);
        }

        return gameMessage;
    }

    private static SnakesProto.GamePlayer onePlayerInfo(HostInfo host) {
        return SnakesProto.GamePlayer.newBuilder()
                .setName(host.getName())
                .setId(host.getID())
                .setIpAddress(String.valueOf(host.getIp()))
                .setPort(host.getPort())
                .setRole(SnakesProto.NodeRole.forNumber(host.getRole()))
                .setType(SnakesProto.PlayerType.HUMAN)
                .setScore(host.getScore())
                .build();
    }

}
