package ru.nsu.fit.usoltsev;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import ru.nsu.fit.usoltsev.controller.GameControl;

import java.io.IOException;
import java.util.Objects;

public class Main extends Application {
    public GraphicsContext gc;

    @Override
    public void start(Stage stage) throws IOException {

        stage.setTitle("Snake");
        Image icon = new Image("ru/nsu/fit/usoltsev/snakeIcon.png");
        stage.getIcons().add(icon);

        AnchorPane root = FXMLLoader.load(Objects.requireNonNull(Main.class.getResource("snakeMenu.fxml")));
        Scene scene = new Scene(root);

        TextField widthValue = (TextField) root.lookup("#widthValue");
        TextField rowsValue = (TextField) root.lookup("#rowsValue");
        TextField foodCountValue = (TextField) root.lookup("#foodCount");
        Button okButton = (Button) root.lookup("#okButton");

        okButton.setOnAction(event -> {
            if (widthValue.getText().isEmpty()) {
                widthValue.setPromptText("INSERT HEIGHT!");
            } else if (rowsValue.getText().isEmpty()) {
                rowsValue.setPromptText("INSERT ROWS!");
            } else if (foodCountValue.getText().isEmpty()) {
                foodCountValue.setPromptText("INSERT FOOD COUNT!");
            } else {
                try {
                    int width = Integer.parseInt(widthValue.getText());
                    int height = width;
                    int rows = Integer.parseInt(rowsValue.getText());
                    int columns = rows;
                    int foodCount = Integer.parseInt(foodCountValue.getText());
                    GameConstants.setConstants(width, height, rows, columns, foodCount);

                    root.getChildren().clear();
                    Canvas canvas = new Canvas(width, height);
                    root.getChildren().add(canvas);
                    stage.setX(Screen.getPrimary().getVisualBounds().getWidth() / 2 - (double) width / 2);
                    stage.setY(Screen.getPrimary().getVisualBounds().getHeight() / 2 - (double) height / 2);
                    stage.sizeToScene();
                    gc = canvas.getGraphicsContext2D();
                    GameControl gameControl = new GameControl(gc, scene);
                    gameControl.startGame();

                } catch (NumberFormatException ex) {
                    ex.printStackTrace(System.err);
                    System.exit(1);
                }
            }
        });

        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}