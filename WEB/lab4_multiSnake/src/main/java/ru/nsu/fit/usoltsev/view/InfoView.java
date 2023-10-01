package ru.nsu.fit.usoltsev.view;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import static ru.nsu.fit.usoltsev.GameConstants.*;

public class InfoView {
    public void drawScore(GraphicsContext gc, int score) {
        gc.setFill(Color.RED);
        gc.setFont(new Font("Digital-7", 30));
        gc.fillText("Score = " + score, 10, 30);
    }

    public void drawGameOver(GraphicsContext gc) {
        gc.setFill(Color.RED);
        gc.setFont(new Font("Arial", (double) WIDTH / 10));
        gc.fillText("Game over", (double) WIDTH / 4, (double) HEIGHT / 2);
    }
}
