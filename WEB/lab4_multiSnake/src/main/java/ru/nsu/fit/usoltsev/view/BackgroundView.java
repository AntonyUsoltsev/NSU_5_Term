package ru.nsu.fit.usoltsev.view;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import ru.nsu.fit.usoltsev.HostInfo;

import static ru.nsu.fit.usoltsev.GameConfig.*;
import static ru.nsu.fit.usoltsev.GameConstants.SQUARE_SIZE;
import static ru.nsu.fit.usoltsev.GameConstants.backgroundImage;

public class BackgroundView {
    public void drawBackground(GraphicsContext gc) {
        gc.drawImage(backgroundImage,0,0);
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
    }
}
