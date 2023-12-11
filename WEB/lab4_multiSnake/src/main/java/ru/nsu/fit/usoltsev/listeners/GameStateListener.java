package ru.nsu.fit.usoltsev.listeners;

import ru.nsu.fit.usoltsev.snakes.SnakesProto;

public interface GameStateListener {
    void setNewState(SnakesProto.GameMessage.StateMsg msg);
}
