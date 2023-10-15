package ru.nsu.fit.usoltsev.network.gameMessageCreators;

import ru.nsu.fit.usoltsev.snakes.SnakesProto;

import static ru.nsu.fit.usoltsev.GameConfig.MSG_SEQ;

public class CopyOfMessage {
    public static SnakesProto.GameMessage makeMessageCopy(SnakesProto.GameMessage gameMessage) {
        SnakesProto.GameMessage message = SnakesProto.GameMessage.newBuilder()
                .mergeFrom(gameMessage)
                .setMsgSeq(MSG_SEQ.getAndIncrement())
                .build();
        return message;
    }
}
