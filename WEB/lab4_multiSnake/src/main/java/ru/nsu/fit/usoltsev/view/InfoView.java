package ru.nsu.fit.usoltsev.view;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import ru.nsu.fit.usoltsev.HostInfo;
import ru.nsu.fit.usoltsev.snakes.SnakesProto;

import java.util.HashMap;

import static ru.nsu.fit.usoltsev.GameConfig.*;
import static ru.nsu.fit.usoltsev.GameConstants.roles;

public class InfoView {
//    public void drawScore(GraphicsContext gc, int score) {
//        gc.setFill(Color.RED);
//        gc.setFont(new Font("Digital-7", 30));
//        gc.fillText("Score = " + score, 10, 30);
//    }

    public void drawGameOver(GraphicsContext gc, int maxScore) {
        gc.setFill(Color.RED);
        gc.setFont(new Font("Arial", (double) WIDTH / 10));
        gc.fillText("Game over", (double) WIDTH / 4, (double) HEIGHT / 2);
        gc.setFont(new Font("Arial", (double) WIDTH / 15));
        gc.fillText("Max score: " + maxScore, (double) WIDTH / 4, (double) HEIGHT * 2 / 3);
    }


    public void drawPlayersInfo(GraphicsContext gc, HashMap<Integer, HostInfo> players, HashMap<Integer, HostInfo> viewers) {
        int i = 0;
        gc.setFont(new Font("Arial", 22));
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

        gc.setFont(new Font("Arial", 22));
        for (var player : msg.getState().getPlayers().getPlayersList()) {
            gc.setFill(Color.WHITE);
            if (player.getId() == ID) {
                gc.setFill(Color.YELLOW);
            }
            gc.fillText(String.format("""
                    Player: %s,
                       score = %d,
                       role = %s
                    """, player.getName(), player.getScore(), roles.get(player.getRole().getNumber())), WIDTH + 10, 30 + (i * 80));
            i++;
        }
    }

    public void drawOnePlayerInfo(HostInfo player, GraphicsContext gc, int i) {
        gc.setFill(Color.WHITE);
        if (player.getID() == ID) {
            gc.setFill(Color.YELLOW);
        }
        gc.fillText(String.format("""
                Player: %s,
                   score = %d,
                   role = %s
                """, player.getName(), player.getScore(), roles.get(player.getRole())), WIDTH + 10, 30 + (i * 80));

    }
}
