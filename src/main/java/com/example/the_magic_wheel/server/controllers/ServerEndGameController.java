package com.example.the_magic_wheel.server.controllers;

import com.example.the_magic_wheel.Configuration;
import com.example.the_magic_wheel.server.views.App;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;


public class ServerEndGameController extends ServerController{
    public ServerEndGameController(App app) {
        super(app);
    }

    @FXML
    public void closeServer(@SuppressWarnings("exports") ActionEvent event) {
        System. exit(0);
     }

     @FXML
    public void playAgain(@SuppressWarnings("exports") ActionEvent event) {
        serverApp.getServerScenesManager().switchScene(Configuration.SERVER_MAXIMUM_PLAYER_FXML);
        App.setPlayAgain(false);
        serverApp.playAgain();
     }
}
