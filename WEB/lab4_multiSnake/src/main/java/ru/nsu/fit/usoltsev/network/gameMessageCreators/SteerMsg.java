package ru.nsu.fit.usoltsev.network.gameMessageCreators;

import ru.nsu.fit.usoltsev.snakes.SnakesProto;

import static ru.nsu.fit.usoltsev.GameConfig.ID;
import static ru.nsu.fit.usoltsev.network.NetworkUtils.MSG_SEQ;


public class SteerMsg {

    public static SnakesProto.GameMessage createSteer(int newDirection) {
        SnakesProto.GameMessage.SteerMsg steerMsg = SnakesProto.GameMessage.SteerMsg.newBuilder()
                .setDirection(SnakesProto.Direction.forNumber(newDirection))
                .build();
        return SnakesProto.GameMessage.newBuilder()
                .setMsgSeq(MSG_SEQ.getAndIncrement())
                .setSteer(steerMsg)
                .setSenderId(ID)
                .build();
    }

}
