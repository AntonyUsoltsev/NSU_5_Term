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
        Image icon = new Image("ru/nsu/fit/usoltsev/pictures/snakeIcon.png");
        stage.getIcons().add(icon);

        AnchorPane root = FXMLLoader.load(Objects.requireNonNull(Main.class.getResource("snakeMenu.fxml")));
        Scene scene = new Scene(root);

        TextField widthValue = (TextField) root.lookup("#widthValue");
        TextField rowsValue = (TextField) root.lookup("#rowsValue");
        TextField foodCountValue = (TextField) root.lookup("#foodCount");
        TextField timeDelay = (TextField) root.lookup("#timeDelay");
        Button okButton = (Button) root.lookup("#okButton");

        okButton.setOnAction(event -> {
            if (widthValue.getText().isEmpty()) {
                widthValue.setPromptText("Insert height");
            } else if (rowsValue.getText().isEmpty()) {
                rowsValue.setPromptText("Insert rows count");
            } else if (foodCountValue.getText().isEmpty()) {
                foodCountValue.setPromptText("Insert food count!");
            } else if (timeDelay.getText().isEmpty()) {
                timeDelay.setPromptText("Insert time delay!");
            } else {
                try {
                    int width = Integer.parseInt(widthValue.getText());
                    int height = width;
                    int rows = Integer.parseInt(rowsValue.getText());
                    int columns = rows;
                    int time = Integer.parseInt(timeDelay.getText());

                    if (width % rows != 0) {
                        rowsValue.clear();
                        rowsValue.setPromptText("Must be multiple of width");
                    } else if (width <= 0) {
                        widthValue.clear();
                        widthValue.setPromptText("Must be > 0");
                    }else if (rows <= 0) {
                        rowsValue.clear();
                        rowsValue.setPromptText("Must be > 0");
                    }else if (time <= 0) {
                        timeDelay.clear();
                        timeDelay.setPromptText("Must be > 0");
                    } else {
                        int foodCount = Integer.parseInt(foodCountValue.getText());
                        GameConstants.setConstants(width, height, rows, columns, foodCount, time);

                        root.getChildren().clear();
                        Canvas canvas = new Canvas(width, height);
                        root.getChildren().add(canvas);

                        stage.setX(Screen.getPrimary().getVisualBounds().getWidth() / 2 - (double) width / 2);
                        stage.setY(Screen.getPrimary().getVisualBounds().getHeight() / 2 - (double) height / 2);
                        stage.sizeToScene();

                        gc = canvas.getGraphicsContext2D();

                        GameControl gameControl = new GameControl(gc, scene);
                        gameControl.startGame();
                    }

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