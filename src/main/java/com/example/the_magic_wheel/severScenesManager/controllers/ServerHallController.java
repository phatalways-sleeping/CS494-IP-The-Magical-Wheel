package com.example.the_magic_wheel.severScenesManager.controllers;
import com.example.the_magic_wheel.Configuration;
import com.example.the_magic_wheel.sockets.Server.ServerApp;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.event.ActionEvent;

public class ServerHallController extends ServerController{
    public ServerHallController(ServerApp app) {
        super(app);
    }
    @FXML
    public Button getStartedButton;
    
    @FXML
    public void switchToMaximumPlayer(ActionEvent event) {
        serverApp.getServerScenesManager().switchScene(Configuration.SERVER_MAXIMUM_PLAYER_FXML);
     }
    

}