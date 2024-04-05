package com.example.the_magic_wheel.controllers;

import java.io.IOException;

import com.example.the_magic_wheel.App;
import com.example.the_magic_wheel.Configuration;
import com.example.the_magic_wheel.protocols.response.Response;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class GreetController implements Controller{

    @FXML
    private Button getStartedButton;

    @FXML
    void switchToRegister(ActionEvent event) throws IOException {
        App.setRoot(Configuration.CLIENT_REGISTER_FXML);
    }

    @Override
    public void handleResponse(Response response) {
        // TODO Auto-generated method stub
        System.out.println("Called from GreetController");
    }

}
