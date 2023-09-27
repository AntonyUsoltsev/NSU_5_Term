module ru.nsu.fit.usoltsev.lab4_multisnake {
    requires javafx.controls;
    requires javafx.fxml;

    requires com.almasb.fxgl.all;

    opens ru.nsu.fit.usoltsev to javafx.fxml;
    exports ru.nsu.fit.usoltsev;
}