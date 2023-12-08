package ru.nsu.fit.usoltsev.controller;

import javafx.scene.Scene;

import static ru.nsu.fit.usoltsev.GameConfig.ID;
import static ru.nsu.fit.usoltsev.GameConstants.*;

public class MasterSnakeController {
    GameController gameController;

    MasterSnakeController(Scene scene, GameController gameController) {
        this.gameController = gameController;
        control(scene);
    }

    public void control(Scene scene) {
        scene.setOnKeyPressed(event -> {
                    switch (event.getCode()) {
                        case D, RIGHT -> gameController.addNewState(RIGHT, ID);
                        case A, LEFT -> gameController.addNewState(LEFT, ID);
                        case S, DOWN -> gameController.addNewState(DOWN, ID);
                        case W, UP -> gameController.addNewState(UP, ID);
                    }
                }
        );
    }
}
