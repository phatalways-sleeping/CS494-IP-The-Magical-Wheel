package com.example.the_magic_wheel.severScenesManager.controllers;

import com.example.the_magic_wheel.sockets.Server.ServerApp;
import com.example.the_magic_wheel.Configuration;
import javafx.fxml.FXML;
import javafx.event.ActionEvent;


public class ServerEndGameController extends ServerController{
    public ServerEndGameController(ServerApp app) {
        super(app);
    }

    @FXML
    public void closeServer(@SuppressWarnings("exports") ActionEvent event) {
        System. exit(0);
     }

     @FXML
    public void playAgain(@SuppressWarnings("exports") ActionEvent event) {
        serverApp.getServerScenesManager().switchScene(Configuration.SERVER_MAXIMUM_PLAYER_FXML);
        ServerApp.setPlayAgain(false);
        serverApp.playAgain();
     }
}
