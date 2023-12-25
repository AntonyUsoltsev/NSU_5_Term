package ru.nsu.fit.usoltsev.view;

import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.GraphicsContext;

import javafx.scene.paint.Color;

import static ru.nsu.fit.usoltsev.GameConfig.COLUMNS;
import static ru.nsu.fit.usoltsev.GameConfig.ROWS;
import static ru.nsu.fit.usoltsev.GameConstants.SQUARE_SIZE;
import static ru.nsu.fit.usoltsev.GameConstants.backgroundImage;

public class BackgroundView {
    public void drawBackground(GraphicsContext gc, Scene scene) {
        gc.drawImage(backgroundImage, 0, 0);
        for (int i = 0; i < COLUMNS; i++) {
            for (int j = 0; j < ROWS; j++) {
                if ((i + j) % 2 == 0) {
                    gc.setFill(Color.rgb(170, 200, 77));
                } else {
                    gc.setFill(Color.rgb(160, 194, 68));
                }
                gc.fillRect(i * SQUARE_SIZE, j * SQUARE_SIZE, SQUARE_SIZE, SQUARE_SIZE);
            }
        }

//        Button exitButton = new Button("Exit");
//        exitButton.setOnAction(event -> {
//            System.exit(0);
//        });
//        HBox hbox = new HBox(exitButton);
//        hbox.setLayoutX((COLUMNS - 1) * SQUARE_SIZE);
//        hbox.setLayoutY((ROWS - 1) * SQUARE_SIZE);
//        hbox.setMargin(exitButton, new javafx.geometry.Insets(0, 0, 10, 10));
//
//        Group root = (Group) scene.getRoot();
//        root.getChildren().add(hbox);
//        exitButton.setMinWidth(100);
    }
}
