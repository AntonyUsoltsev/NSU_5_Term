package ru.nsu.fit.usoltsev.network.gameMessageCreators;

import ru.nsu.fit.usoltsev.snakes.SnakesProto;

import static ru.nsu.fit.usoltsev.GameConfig.ID;

public class ErrorMsg {
    public static SnakesProto.GameMessage createError(long msgSeq, int receiverId, String message){
        SnakesProto.GameMessage.ErrorMsg errMsg = SnakesProto.GameMessage.ErrorMsg.newBuilder().setErrorMessage(message)
                .build();
        SnakesProto.GameMessage gameMessage = SnakesProto.GameMessage.newBuilder()
                .setMsgSeq(msgSeq)
                .setError(errMsg)
                .setSenderId(ID)
                .setReceiverId(receiverId)
                .build();
        return gameMessage;
    }
}
