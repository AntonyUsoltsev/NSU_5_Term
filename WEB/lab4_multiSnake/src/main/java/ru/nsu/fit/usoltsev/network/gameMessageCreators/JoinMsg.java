package ru.nsu.fit.usoltsev.network.gameMessageCreators;

import lombok.extern.slf4j.Slf4j;
import ru.nsu.fit.usoltsev.snakes.SnakesProto;

import static ru.nsu.fit.usoltsev.GameConfig.*;

@Slf4j
public class JoinMsg {
    public static SnakesProto.GameMessage createJoin(String playerName, String gameName, int role) {
        SnakesProto.GameMessage.JoinMsg joinMsg = SnakesProto.GameMessage.JoinMsg.newBuilder()
                .setPlayerName(playerName)
                .setGameName(gameName)
                .setRequestedRole(SnakesProto.NodeRole.forNumber(role))
                .build();

        SnakesProto.GameMessage gameMessage = SnakesProto.GameMessage.newBuilder()
                .setJoin(joinMsg)
                .setMsgSeq(MSG_SEQ.getAndIncrement())
                .build();
        log.info("Join message created");
        return gameMessage;
    }
}
