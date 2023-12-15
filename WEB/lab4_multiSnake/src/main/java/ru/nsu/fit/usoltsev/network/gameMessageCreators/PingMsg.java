package ru.nsu.fit.usoltsev.network.gameMessageCreators;

import ru.nsu.fit.usoltsev.snakes.SnakesProto;

import static ru.nsu.fit.usoltsev.network.NetworkUtils.MSG_SEQ;

public class PingMsg {

    public static SnakesProto.GameMessage createPing(){
        SnakesProto.GameMessage.PingMsg pingMsg = SnakesProto.GameMessage.PingMsg.newBuilder()
                .build();

        SnakesProto.GameMessage gameMessage = SnakesProto.GameMessage.newBuilder()
                .setPing(pingMsg)
                .setMsgSeq(MSG_SEQ.getAndIncrement())
                .build();
        return gameMessage;
    }
}
