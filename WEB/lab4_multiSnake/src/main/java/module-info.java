module ru.nsu.fit.usoltsev.lab4_multisnake {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.protobuf;
    requires org.slf4j;
    requires lombok;

    opens ru.nsu.fit.usoltsev to javafx.fxml;
    opens ru.nsu.fit.usoltsev.snakes to com.google.protobuf;
    exports ru.nsu.fit.usoltsev;
}