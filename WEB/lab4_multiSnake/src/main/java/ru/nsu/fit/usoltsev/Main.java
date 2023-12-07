package ru.nsu.fit.usoltsev;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import ru.nsu.fit.usoltsev.controller.MenuController;
import ru.nsu.fit.usoltsev.network.MulticastInputController;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class Main extends Application {
    @Override
    public void start(Stage stage) {
        try {
            stage.setTitle("Snake");
            Image icon = new Image("ru/nsu/fit/usoltsev/pictures/snakeIcon.png");
            stage.getIcons().add(icon);

            AnchorPane root = FXMLLoader.load(Objects.requireNonNull(Main.class.getResource("snakeMenu.fxml")));
            Scene scene = new Scene(root);

            ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(50);

            MenuController menuController = new MenuController(root, executor);
            MulticastInputController multicastInputController = new MulticastInputController();
            multicastInputController.setNewGameListener(menuController);

            executor.submit(multicastInputController);

            menuController.newMenu(stage, scene);

            stage.setScene(scene);
            stage.setResizable(false);
            stage.setOnCloseRequest(event -> {
                System.out.println(executor.getActiveCount());
                System.out.println(executor.shutdownNow());
                System.exit(0);
            });
            stage.show();
        } catch (IOException ioException) {
            stage.close();
        }
    }

    public static void main(String[] args) {
        launch();
    }
}