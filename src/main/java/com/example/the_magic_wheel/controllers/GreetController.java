package com.example.the_magic_wheel.controllers;

import java.io.IOException;

import com.example.the_magic_wheel.App;
import com.example.the_magic_wheel.Configuration;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class GreetController {

    @FXML
    private Button getStartedButton;

    @FXML
    void switchToRegister(ActionEvent event) throws IOException {
        App.setRoot(Configuration.CLIENT_REGISTER_FXML);
    }

}
