package ru.nsu.fit.usoltsev.listeners;

import java.net.InetAddress;

public interface SnakeAddListener {
    boolean addNewSnake(String name, int playerID, int port, InetAddress ip, int role);
}
