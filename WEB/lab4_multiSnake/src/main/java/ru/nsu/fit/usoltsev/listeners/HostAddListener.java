package ru.nsu.fit.usoltsev.listeners;

import java.net.InetAddress;

public interface HostAddListener {
    boolean addNewSnake(String name, int playerID, int port, InetAddress ip, int role);

    boolean addNewViewer(String name, int playerID, int port, InetAddress ip, int role);
}
