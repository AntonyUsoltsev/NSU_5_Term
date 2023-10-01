package ru.nsu.fit.usoltsev.controller;

import javafx.scene.Scene;
import ru.nsu.fit.usoltsev.model.SnakeModel;

public class SnakeControl  {

    SnakeModel snakeModel;
    SnakeControl(Scene scene, SnakeModel snakeModel) {
        this.snakeModel = snakeModel;
        control(scene);
    }

    public void control(Scene scene) {
        scene.setOnKeyPressed(event -> snakeModel.changeDirection(event));
    }
}
