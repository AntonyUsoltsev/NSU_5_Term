package ru.nsu.fit.usoltsev.controller;

import javafx.scene.Scene;
import lombok.extern.slf4j.Slf4j;
import ru.nsu.fit.usoltsev.GameConfig;
import ru.nsu.fit.usoltsev.network.Udp.UdpController;
import ru.nsu.fit.usoltsev.network.gameMessageCreators.SteerMsg;

import static ru.nsu.fit.usoltsev.GameConstants.*;

@Slf4j
public class NormalSnakeController {

    UdpController udpController;

    NormalSnakeController(Scene scene, UdpController udpController) {
        this.udpController = udpController;
        control(scene);
    }

    public void control( Scene scene) {
        scene.setOnKeyPressed(event -> {
            try {
                switch (event.getCode()) {
                    case D, RIGHT -> udpController.setOutputMessage(GameConfig.MASTER_IP, GameConfig.MASTER_PORT, SteerMsg.createSteer(RIGHT));
                    case A, LEFT -> udpController.setOutputMessage(GameConfig.MASTER_IP, GameConfig.MASTER_PORT, SteerMsg.createSteer(LEFT));
                    case S, DOWN -> udpController.setOutputMessage(GameConfig.MASTER_IP, GameConfig.MASTER_PORT, SteerMsg.createSteer(DOWN));
                    case W, UP -> udpController.setOutputMessage(GameConfig.MASTER_IP, GameConfig.MASTER_PORT, SteerMsg.createSteer(UP));
                }
            } catch (InterruptedException ie) {
                log.warn("InterruptedException while sending direction", ie.getCause());
            }
        });
    }
}
