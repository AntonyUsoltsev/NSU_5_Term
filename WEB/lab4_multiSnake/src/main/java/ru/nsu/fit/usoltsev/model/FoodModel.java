package ru.nsu.fit.usoltsev.model;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.Light;
import javafx.scene.image.Image;
//import lombok.Getter;
import ru.nsu.fit.usoltsev.GameConstants;
import ru.nsu.fit.usoltsev.view.FoodView;

import java.util.List;

public class FoodModel implements GameConstants {

    public int getFoodX() {
        return foodX;
    }

    public int getFoodY() {
        return foodY;
    }

    private int foodX;


    private int foodY;
    private Image foodImage;

    FoodView foodView;

    public FoodModel(){
        foodView = new FoodView();
    }

    public void generateFood(List<Light.Point> snakeBody) {
        start:
        while (true) {
            foodX = (int) (Math.random() * ROWS);
            foodY = (int) (Math.random() * COLUMNS);
            for (Light.Point snake : snakeBody) {
                if (snake.getX() == foodX && snake.getY() == foodY) {
                    continue start;
                }
            }
            foodImage = new Image("ru/nsu/fit/usoltsev/apple.png");
            break;
        }
    }

    public void drawFood(GraphicsContext gc){
        foodView.drawFood(foodImage, foodX, foodY, gc);
    }


}
