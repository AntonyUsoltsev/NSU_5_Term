package ru.nsu.fit.usoltsev;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import ru.nsu.fit.usoltsev.controller.GameControl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;


public class Main extends Application {

    public GraphicsContext gc;

    @Override
    public void start(Stage stage) throws IOException {
//        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
//        Scene scene = new Scene(fxmlLoader.load(), 320, 240);

        stage.setTitle("Snake");
        Image icon = new Image("ru/nsu/fit/usoltsev/snakeIcon.png");
        stage.getIcons().add(icon);
        AnchorPane root = FXMLLoader.load(Objects.requireNonNull(Main.class.getResource("snakeMenu.fxml")));
        GameConstants gameConstants = new GameConstants();

        TextField widhtValue = (TextField) root.lookup("#widhtValue");
        TextField rowsValue = (TextField) root.lookup("#rowsValue");
        TextField foodCount = (TextField) root.lookup("#foodCount");
        Button okButton = (Button) root.lookup("#okButton");
        Scene scene = new Scene(root);
        okButton.setOnAction(event -> {
            if (widhtValue.getText().equals("")) {
                widhtValue.setPromptText("INSERT HEIGHT!");
            } else if (rowsValue.getText().equals("")) {
                rowsValue.setPromptText("INSERT ROWS!");
            } else if (foodCount.getText().equals("")) {
                foodCount.setPromptText("INSERT FOOD COUNT!");
            } else {
                gameConstants.setConstants(Integer.parseInt(widhtValue.getText()), Integer.parseInt(widhtValue.getText()),
                        Integer.parseInt(rowsValue.getText()), Integer.parseInt(rowsValue.getText()), Integer.parseInt(foodCount.getText()));

                root.getChildren().clear();
                Canvas canvas = new Canvas(Integer.parseInt(widhtValue.getText()), Integer.parseInt(widhtValue.getText()));

                root.getChildren().add(canvas);

                gc = canvas.getGraphicsContext2D();
                GameControl gameControl = new GameControl(gc, scene);
                gameControl.startGame();
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