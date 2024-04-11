package com.example.the_magic_wheel.server.controllers;
import com.example.the_magic_wheel.Configuration;
import com.example.the_magic_wheel.server.views.App;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.event.ActionEvent;

public class ServerHallController extends ServerController{
    public ServerHallController(App app) {
        super(app);
    }
    @SuppressWarnings("exports")
    @FXML
    public Button getStartedButton;

    @FXML
    public void switchToMaximumPlayer(@SuppressWarnings("exports") ActionEvent event) {
        serverApp.getServerScenesManager().switchScene(Configuration.SERVER_MAXIMUM_PLAYER_FXML);
     }


}
