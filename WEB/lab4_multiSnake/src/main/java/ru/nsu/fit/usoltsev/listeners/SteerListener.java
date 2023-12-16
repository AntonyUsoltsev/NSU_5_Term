package ru.nsu.fit.usoltsev.listeners;

public interface SteerListener {
    void setNewSteer(int direction, int senderID);
    void setNewSteer(int direction, String ipPortInfo);
}
