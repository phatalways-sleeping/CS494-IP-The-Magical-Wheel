package com.example.the_magic_wheel.controllers;

import java.io.IOException;

import com.example.the_magic_wheel.App;
import com.example.the_magic_wheel.Client;
import com.example.the_magic_wheel.Configuration;
import com.example.the_magic_wheel.protocols.response.Response;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class GreetController implements Controller{

    public GreetController(App app) {
        this.app = app;
    }

    @FXML
    private Button getStartedButton;
    private App app;

    @FXML
    void switchToRegister(ActionEvent event) throws IOException {
        app.getScenesManager().switchScene(Configuration.CLIENT_REGISTER_FXML);
    }

    @Override
    public void handleResponse(Response response) {
        System.out.println("Called from GreetController");
    }

}
