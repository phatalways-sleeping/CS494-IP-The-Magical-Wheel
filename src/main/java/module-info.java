module com.example.the_magic_wheel {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    opens com.example.the_magic_wheel to javafx.fxml;

    exports com.example.the_magic_wheel;

    opens com.example.the_magic_wheel.controllers to javafx.fxml;

    exports com.example.the_magic_wheel.controllers;

    opens com.example.the_magic_wheel.protocols to javafx.fxml;

    exports com.example.the_magic_wheel.protocols;

    opens com.example.the_magic_wheel.protocols.request to javafx.fxml;

    exports com.example.the_magic_wheel.protocols.request;

    opens com.example.the_magic_wheel.protocols.response to javafx.fxml;

    exports com.example.the_magic_wheel.protocols.response;

    opens com.example.the_magic_wheel.sockets to javafx.fxml;

    exports com.example.the_magic_wheel.sockets;

    opens com.example.the_magic_wheel.sockets.Server to javafx.fxml;

    exports com.example.the_magic_wheel.sockets.Server;

    opens com.example.the_magic_wheel.sockets.Server.manager to javafx.fxml;

    exports com.example.the_magic_wheel.sockets.Server.manager;

    opens com.example.the_magic_wheel.sockets.Server.defense to javafx.fxml;

    exports com.example.the_magic_wheel.sockets.Server.defense;
}
