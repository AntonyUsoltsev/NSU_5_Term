package ru.nsu.fit.usoltsev.network.gameMessageCreators;

import lombok.extern.slf4j.Slf4j;
import ru.nsu.fit.usoltsev.snakes.SnakesProto;

@Slf4j
public class AnnouncementMsg {
    public static SnakesProto.GameMessage createAnnouncement(int width, int height, int foodCount, int TimeDelay, String gameName, int role) {

        SnakesProto.GameConfig gameConfig = SnakesProto.GameConfig.newBuilder()
                .setWidth(width)
                .setHeight(height)
                .setFoodStatic(foodCount)
                .setStateDelayMs(TimeDelay)
                .build();

        SnakesProto.GamePlayer gamePlayer = SnakesProto.GamePlayer.newBuilder()
                .setName("test1")
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

        SnakesProto.GameMessage message = SnakesProto.GameMessage.newBuilder()
                .setAnnouncement(am)
                .setMsgSeq(1)
                .build();
        log.info("AnnouncementMsg created");

        return message;

    }

}
