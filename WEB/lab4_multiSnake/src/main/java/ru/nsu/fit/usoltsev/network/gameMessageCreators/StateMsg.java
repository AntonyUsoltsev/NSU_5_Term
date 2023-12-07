package ru.nsu.fit.usoltsev.network.gameMessageCreators;

import ru.nsu.fit.usoltsev.GameConfig;
import ru.nsu.fit.usoltsev.snakes.SnakesProto;

import static ru.nsu.fit.usoltsev.GameConfig.ID;

public class StateMsg {

    public static SnakesProto.GameMessage createState(){

//        SnakesProto.GameState state = SnakesProto.GameState.newBuilder()
//                .setSnakes()
//                .build();
//
//
//        SnakesProto.GameMessage.StateMsg stateMsg = SnakesProto.GameMessage.StateMsg.newBuilder()
//                .setState(state)
//                .build();
//        SnakesProto.GameMessage gameMessage = SnakesProto.GameMessage.newBuilder()
//                .setMsgSeq(GameConfig.MSG_SEQ.getAndIncrement())
//                .setState(stateMsg)
//                .setSenderId(ID)
////                .setReceiverId(receiverId)
//                .build();
        return null;
    }
}
