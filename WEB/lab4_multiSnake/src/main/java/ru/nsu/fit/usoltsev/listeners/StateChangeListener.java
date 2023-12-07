package ru.nsu.fit.usoltsev.listeners;

public interface StateChangeListener {
    void addNewState(int direction, int senderID);
}
