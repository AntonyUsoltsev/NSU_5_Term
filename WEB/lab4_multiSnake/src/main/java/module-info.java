module ru.nsu.fit.usoltsev.lab4_multisnake {
    requires javafx.controls;
    requires javafx.fxml;

//    requires org.controlsfx.controls;
//    requires org.kordamp.bootstrapfx.core;
//    requires com.google.protobuf;
//    requires org.slf4j;
//    requires lombok;
    opens ru.nsu.fit.usoltsev to javafx.fxml;
    exports ru.nsu.fit.usoltsev;
}