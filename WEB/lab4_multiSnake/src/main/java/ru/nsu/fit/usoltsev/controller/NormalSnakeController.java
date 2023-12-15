package ru.nsu.fit.usoltsev.controller;

import javafx.scene.Scene;
import lombok.extern.slf4j.Slf4j;
import ru.nsu.fit.usoltsev.network.Udp.UdpController;
import ru.nsu.fit.usoltsev.network.gameMessageCreators.SteerMsg;

import static ru.nsu.fit.usoltsev.GameConstants.*;
import static ru.nsu.fit.usoltsev.network.NetworkUtils.MASTER_IP;
import static ru.nsu.fit.usoltsev.network.NetworkUtils.MASTER_PORT;


@Slf4j
public class NormalSnakeController {

    UdpController udpController;

    NormalSnakeController(Scene scene, UdpController udpController) {
        this.udpController = udpController;
        control(scene);
    }

    public void control(Scene scene) {
        scene.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case D, RIGHT -> udpController.setOutputMessage(MASTER_IP, MASTER_PORT, SteerMsg.createSteer(RIGHT));
                case A, LEFT -> udpController.setOutputMessage(MASTER_IP, MASTER_PORT, SteerMsg.createSteer(LEFT));
                case S, DOWN -> udpController.setOutputMessage(MASTER_IP, MASTER_PORT, SteerMsg.createSteer(DOWN));
                case W, UP -> udpController.setOutputMessage(MASTER_IP, MASTER_PORT, SteerMsg.createSteer(UP));
            }
        });
    }
}
