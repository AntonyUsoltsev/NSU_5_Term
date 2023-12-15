package ru.nsu.fit.usoltsev.listeners;

public interface RoleChangeListener {
    void normalToDeputy();
    void normalToViewer();
    void deputyToMaster();

}
