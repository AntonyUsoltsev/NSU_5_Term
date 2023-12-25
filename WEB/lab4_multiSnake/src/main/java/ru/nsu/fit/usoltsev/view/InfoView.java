package ru.nsu.fit.usoltsev.view;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import ru.nsu.fit.usoltsev.HostInfo;
import ru.nsu.fit.usoltsev.snakes.SnakesProto;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

import static ru.nsu.fit.usoltsev.GameConfig.*;
import static ru.nsu.fit.usoltsev.GameConstants.*;

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


    public void drawPlayersInfo(GraphicsContext gc, HashMap<Integer, HostInfo> players) {
        int i = 0;
        ArrayList<HostInfo> playersList = new java.util.ArrayList<>(players.values().stream().toList());
        playersList.sort(Comparator.comparingInt(HostInfo::getScore).reversed());
        gc.setFont(new Font("Arial", scoreTextSize));
        for (var player : playersList) {
            if (player.getStatus() == ZOMBIE) {
                continue;
            }
            drawOnePlayerInfo(player, gc, i);
            i++;
        }
    }

    public void drawPlayersInfo(GraphicsContext gc, SnakesProto.GameMessage.StateMsg msg) {
        int i = 0;
        gc.setFont(new Font("Arial", scoreTextSize));
        ArrayList<SnakesProto.GamePlayer> players = new ArrayList<>(msg.getState().getPlayers().getPlayersList());
        players.sort(Comparator.comparingInt(SnakesProto.GamePlayer::getScore).reversed());
        for (var player : players) {
            if (msg.getState().getSnakesList().stream().anyMatch(snake -> snake.getPlayerId() == player.getId()
                    && snake.getState().getNumber() != ZOMBIE) ) {

                gc.setFill(Color.WHITE);
//                System.out.println(player.getId());
                if (player.getId() == ID) {
                    gc.setFill(Color.YELLOW);
                    ROLE = player.getRole().getNumber();
                }
                String name;
                if (player.getName().length() > 10) {
                    name = player.getName().substring(0, 2) + "..." + player.getName().substring(player.getName().length() - 2);
                } else {
                    name = player.getName();
                }
                gc.fillText(String.format("""
                                %s(%s) - score = %d
                                """, name, roles.get(player.getRole().getNumber()), player.getScore()),
                        xStartScoreText, textScale + (i * 30));
                i++;
            }
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
