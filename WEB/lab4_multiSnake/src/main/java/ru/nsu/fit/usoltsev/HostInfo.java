package ru.nsu.fit.usoltsev;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import ru.nsu.fit.usoltsev.model.SnakeModel;

import java.net.InetAddress;

@Getter
@Setter
@AllArgsConstructor
public class HostInfo {
    private String name;
    private int ID;
    private int port;
    private InetAddress ip;
    private int role;
    private SnakeModel model;
    private int score;
    private boolean gameOver;
    private int direction;
    private int status;

    public void addScore(int delta) {
        score += delta;
    }

    @Override
    public String toString() {
        return String.format("Name:%s, id: %d, port: %d, ip: %s, role: %d, status: %d", name, ID, port, ip, role, status);
    }
}
