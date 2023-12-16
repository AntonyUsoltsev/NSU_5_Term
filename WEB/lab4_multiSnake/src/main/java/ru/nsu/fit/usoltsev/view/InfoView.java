package ru.nsu.fit.usoltsev.view;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import ru.nsu.fit.usoltsev.HostInfo;
import ru.nsu.fit.usoltsev.snakes.SnakesProto;

import java.util.HashMap;

import static ru.nsu.fit.usoltsev.GameConfig.*;
import static ru.nsu.fit.usoltsev.GameConstants.SQUARE_SIZE;
import static ru.nsu.fit.usoltsev.GameConstants.roles;

public class InfoView {

    private static final double scoreTextSize = (double) WIDTH / 35;
    private static final double textScale = (double) WIDTH / SQUARE_SIZE;
    private static final double xStartScoreText = (double) WIDTH + 10;


    public void drawGameOver(GraphicsContext gc, int maxScore) {
        gc.setFill(Color.RED);
        gc.setFont(new Font("Arial", (double) WIDTH / 10));
        gc.fillText("Game over", (double) WIDTH / 4, (double) HEIGHT / 2);
        gc.setFont(new Font("Arial", (double) WIDTH / 15));
        gc.fillText("Max score: " + maxScore, (double) WIDTH / 4, (double) HEIGHT * 2 / 3);
    }


    public void drawPlayersInfo(GraphicsContext gc, HashMap<Integer, HostInfo> players, HashMap<Integer, HostInfo> viewers) {
        int i = 0;
        gc.setFont(new Font("Arial", scoreTextSize));
        for (var player : players.values()) {
            drawOnePlayerInfo(player, gc, i);
            i++;
        }
        for (var viewer : viewers.values()) {
            drawOnePlayerInfo(viewer, gc, i);
            i++;
        }
    }

    public void drawPlayersInfo(GraphicsContext gc, SnakesProto.GameMessage.StateMsg msg) {
        int i = 0;
        gc.setFont(new Font("Arial", scoreTextSize));
        for (var player : msg.getState().getPlayers().getPlayersList()) {
            gc.setFill(Color.WHITE);
            if (player.getId() == ID) {
                gc.setFill(Color.YELLOW);
            }
            gc.fillText(String.format("""
                            %s(%s) - score = %d
                            """, player.getName(), roles.get(player.getRole().getNumber()), player.getScore()),
                    xStartScoreText, textScale + (i * 30));
            i++;
        }
    }

    public void drawOnePlayerInfo(HostInfo player, GraphicsContext gc, int i) {
        gc.setFill(Color.WHITE);
        if (player.getID() == ID) {
            gc.setFill(Color.YELLOW);
        }
        gc.fillText(String.format("""
                        %s(%s) - score = %d
                        """, player.getName(), roles.get(player.getRole()), player.getScore()),
                xStartScoreText, textScale + (i * 30));

    }
}
