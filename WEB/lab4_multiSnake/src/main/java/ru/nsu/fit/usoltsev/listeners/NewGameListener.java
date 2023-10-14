package ru.nsu.fit.usoltsev.listeners;

import ru.nsu.fit.usoltsev.snakes.SnakesProto;

import java.net.InetAddress;

public interface NewGameListener {
    void addNewGame(InetAddress ip, int port, SnakesProto.GameMessage gameMessage);
}
