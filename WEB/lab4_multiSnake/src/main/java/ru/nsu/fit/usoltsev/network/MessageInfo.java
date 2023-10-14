package ru.nsu.fit.usoltsev.network;

import ru.nsu.fit.usoltsev.snakes.SnakesProto;

import java.net.InetAddress;

public record MessageInfo(InetAddress ipAddr, int port, SnakesProto.GameMessage gameMessage) {
}
