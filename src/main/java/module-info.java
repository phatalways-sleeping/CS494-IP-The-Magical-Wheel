module com.example.the_magic_wheel {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.example.the_magic_wheel to javafx.fxml;
    exports com.example.the_magic_wheel;

    opens com.example.the_magic_wheel.controllers to javafx.fxml;
    exports com.example.the_magic_wheel.controllers;

    opens com.example.the_magic_wheel.protocols to javafx.fxml;
    exports com.example.the_magic_wheel.protocols;

    opens com.example.the_magic_wheel.sockets to javafx.fxml;
    exports com.example.the_magic_wheel.sockets;
}
