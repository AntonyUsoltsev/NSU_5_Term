package ru.nsu.fit.usoltsev.listeners;

import ru.nsu.fit.usoltsev.snakes.SnakesProto;

public interface NewGameListener {
    void addNewGame( SnakesProto.GameAnnouncement gameInfo);
}
