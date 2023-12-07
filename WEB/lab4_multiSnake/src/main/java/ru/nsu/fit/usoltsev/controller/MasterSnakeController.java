package ru.nsu.fit.usoltsev.controller;

import javafx.scene.Scene;
import org.jetbrains.annotations.NotNull;
import ru.nsu.fit.usoltsev.model.SnakeModel;

public class MasterSnakeController {
    SnakeModel snakeModel;
    MasterSnakeController(Scene scene, @NotNull SnakeModel snakeModel) {
        this.snakeModel = snakeModel;
        control(scene);
    }

    public void control(@NotNull Scene scene) {
        scene.setOnKeyPressed(event -> snakeModel.changeDirection(event));
    }
}
