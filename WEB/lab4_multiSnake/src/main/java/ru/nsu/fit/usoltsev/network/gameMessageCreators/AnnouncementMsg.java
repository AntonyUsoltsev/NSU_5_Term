package ru.nsu.fit.usoltsev.network.gameMessageCreators;

import lombok.extern.slf4j.Slf4j;
import ru.nsu.fit.usoltsev.HostInfo;
import ru.nsu.fit.usoltsev.snakes.SnakesProto;

import java.util.HashMap;

import static ru.nsu.fit.usoltsev.network.NetworkUtils.MSG_SEQ;

@Slf4j
public class AnnouncementMsg {
    public static SnakesProto.GameMessage createAnnouncement(int width, int height, int foodCount, int TimeDelay,
                                                             String gameName, HashMap<Integer, HostInfo> hosts, HashMap<Integer, HostInfo> viewers) {
        SnakesProto.GameConfig gameConfig = SnakesProto.GameConfig.newBuilder()
                .setWidth(width)
                .setHeight(height)
                .setFoodStatic(foodCount)
                .setStateDelayMs(TimeDelay)
                .build();
        SnakesProto.GamePlayers.Builder gamePlayers = SnakesProto.GamePlayers.newBuilder();
        for (var host : hosts.values()) {
            gamePlayers.addPlayers(onePlayerInfo(host));
        }
        for(var viewer: viewers.values()){
            gamePlayers.addPlayers(onePlayerInfo(viewer));
        }
        SnakesProto.GameAnnouncement gameAnnouncement = SnakesProto.GameAnnouncement.newBuilder()
                .setConfig(gameConfig)
                .setPlayers(gamePlayers.build())
                .setCanJoin(true)
                .setGameName(gameName)
                .build();

        SnakesProto.GameMessage.AnnouncementMsg am = SnakesProto.GameMessage.AnnouncementMsg
                .newBuilder()
                .addGames(gameAnnouncement)
                .build();

        return SnakesProto.GameMessage.newBuilder()
                .setAnnouncement(am)
                .setMsgSeq(MSG_SEQ.getAndIncrement())
                .build();
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


    public static SnakesProto.GameMessage createAnnouncement(int width, int height, int foodCount, int TimeDelay, String gameName, String playerName, int role) {
        SnakesProto.GameConfig gameConfig = SnakesProto.GameConfig.newBuilder()
                .setWidth(width)
                .setHeight(height)
                .setFoodStatic(foodCount)
                .setStateDelayMs(TimeDelay)
                .build();

        SnakesProto.GamePlayer gamePlayer = SnakesProto.GamePlayer.newBuilder()
                .setName(playerName)
                .setId(1)
                .setRole(SnakesProto.NodeRole.forNumber(role))
                .setScore(0)
                .build();

        SnakesProto.GamePlayers gamePlayers = SnakesProto.GamePlayers.newBuilder()
                .addPlayers(gamePlayer)
                .build();

        SnakesProto.GameAnnouncement gameAnnouncement = SnakesProto.GameAnnouncement.newBuilder()
                .setConfig(gameConfig)
                .setPlayers(gamePlayers)
                .setCanJoin(true)
                .setGameName(gameName)
                .build();

        SnakesProto.GameMessage.AnnouncementMsg am = SnakesProto.GameMessage.AnnouncementMsg
                .newBuilder()
                .addGames(gameAnnouncement)
                .build();

        return SnakesProto.GameMessage.newBuilder()
                .setAnnouncement(am)
                .setMsgSeq(MSG_SEQ.getAndIncrement())
                .build();
    }
}
