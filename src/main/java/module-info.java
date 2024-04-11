module com.example.the_magic_wheel {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.base;

    // PROJECT

    opens com.example.the_magic_wheel to javafx.fxml;

    exports com.example.the_magic_wheel;

    // CLIENT

    opens com.example.the_magic_wheel.client.controllers to javafx.fxml;

    exports com.example.the_magic_wheel.client.controllers;

    opens com.example.the_magic_wheel.client.sockets to javafx.fxml;

    exports com.example.the_magic_wheel.client.sockets;

    opens com.example.the_magic_wheel.client.views to javafx.fxml;

    exports com.example.the_magic_wheel.client.views;

    // PROTOCOLS

    opens com.example.the_magic_wheel.protocols.interfaces to javafx.fxml;

    exports com.example.the_magic_wheel.protocols.interfaces;

    opens com.example.the_magic_wheel.protocols.request to javafx.fxml;

    exports com.example.the_magic_wheel.protocols.request;

    opens com.example.the_magic_wheel.protocols.response to javafx.fxml;

    exports com.example.the_magic_wheel.protocols.response;

    // SERVER

    opens com.example.the_magic_wheel.server.views to javafx.fxml;

    exports com.example.the_magic_wheel.server.views;

    opens com.example.the_magic_wheel.server.controllers to javafx.fxml;

    exports com.example.the_magic_wheel.server.controllers;

    opens com.example.the_magic_wheel.server.sockets to javafx.fxml;

    exports com.example.the_magic_wheel.server.sockets;

    opens com.example.the_magic_wheel.server.sockets.defense to javafx.fxml;

    exports com.example.the_magic_wheel.server.sockets.defense;

    opens com.example.the_magic_wheel.server.sockets.manager to javafx.fxml;

    exports com.example.the_magic_wheel.server.sockets.manager;

    opens com.example.the_magic_wheel.server.utils to javafx.fxml;

    exports com.example.the_magic_wheel.server.utils;
}
