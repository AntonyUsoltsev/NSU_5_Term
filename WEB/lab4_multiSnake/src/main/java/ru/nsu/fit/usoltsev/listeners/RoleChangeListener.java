package ru.nsu.fit.usoltsev.listeners;

import ru.nsu.fit.usoltsev.snakes.SnakesProto;

public interface RoleChangeListener {
    void setRoleChange(SnakesProto.GameMessage.RoleChangeMsg msg);

}
