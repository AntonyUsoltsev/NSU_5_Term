package ru.nsu.fit.usoltsev;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import ru.nsu.fit.usoltsev.controller.MenuController;
import ru.nsu.fit.usoltsev.network.NetworkController;

import java.io.IOException;
import java.util.Objects;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {

        stage.setTitle("Snake");
        Image icon = new Image("ru/nsu/fit/usoltsev/pictures/snakeIcon.png");
        stage.getIcons().add(icon);

        AnchorPane root = FXMLLoader.load(Objects.requireNonNull(Main.class.getResource("snakeMenu.fxml")));
        Scene scene = new Scene(root);

        MenuController menuController = new MenuController(root);
        NetworkController networkController = new NetworkController();
        networkController.setNewGameListener(menuController);

        networkController.run();

        menuController.newMenu(root, stage, scene);


//        SnakesProto.GameAnnouncement gameAnnouncement = SnakesProto.GameAnnouncement.newBuilder().set
//        SnakesProto.GameMessage.AnnouncementMsg announcementMsg = SnakesProto.GameMessage.AnnouncementMsg.newBuilder()
//                .setGames()
//
//        SnakesProto.GameMessage message = SnakesProto.GameMessage.newBuilder();
//
//
//        SnakesProto.GameMessage gameMessage = SnakesProto.GameMessage.parseFrom();
//        gameMessage.getTypeCase().getNumber();
//        gameMessage.getAnnouncement().getGames()

        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}