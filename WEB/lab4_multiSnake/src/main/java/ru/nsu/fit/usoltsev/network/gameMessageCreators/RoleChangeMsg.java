package ru.nsu.fit.usoltsev.network.gameMessageCreators;

import ru.nsu.fit.usoltsev.snakes.SnakesProto;

import static ru.nsu.fit.usoltsev.GameConfig.ID;
import static ru.nsu.fit.usoltsev.network.NetworkUtils.MSG_SEQ;


public class RoleChangeMsg {
    public static SnakesProto.GameMessage createRoleChange(int receiverRole, int senderRole, int receiverID) {
        SnakesProto.GameMessage.RoleChangeMsg roleChangeMsg = SnakesProto.GameMessage.RoleChangeMsg.newBuilder()
                .setReceiverRole(SnakesProto.NodeRole.forNumber(receiverRole))
                .setSenderRole(SnakesProto.NodeRole.forNumber(senderRole))
                .build();

        SnakesProto.GameMessage gameMessage = SnakesProto.GameMessage.newBuilder()
                .setRoleChange(roleChangeMsg)
                .setMsgSeq(MSG_SEQ.getAndIncrement())
                .setSenderId(ID)
                .setReceiverId(receiverID)
                .build();

        return gameMessage;
    }
}
