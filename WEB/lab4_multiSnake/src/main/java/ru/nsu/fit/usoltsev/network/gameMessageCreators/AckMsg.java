package ru.nsu.fit.usoltsev.network.gameMessageCreators;

import ru.nsu.fit.usoltsev.snakes.SnakesProto;

import static ru.nsu.fit.usoltsev.GameConfig.ID;

public class AckMsg {
    public static SnakesProto.GameMessage createAck(long msgSeq, int senderId){
        SnakesProto.GameMessage.AckMsg ackMsg = SnakesProto.GameMessage.AckMsg.newBuilder()
                .build();
        SnakesProto.GameMessage gameMessage = SnakesProto.GameMessage.newBuilder()
                .setMsgSeq(msgSeq)
                .setAck(ackMsg)
                .setSenderId(senderId)
                .setReceiverId(ID)
                .build();
        return gameMessage;
    }
}
