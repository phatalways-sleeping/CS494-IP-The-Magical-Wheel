package com.example.the_magic_wheel.client.controllers;

import com.example.the_magic_wheel.Configuration;
import com.example.the_magic_wheel.client.views.App;
import com.example.the_magic_wheel.protocols.response.Response;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

import java.io.IOException;

public class GreetController extends Controller{

    public GreetController(App app) {
        super(app);
    }

    @FXML
    private Button getStartedButton;

    @FXML
    void switchToRegister(ActionEvent event) throws IOException {
        app.getScenesManager().switchScene(Configuration.CLIENT_REGISTER_FXML);
    }

    @Override
    public void handleResponse(Response response) {
        System.out.println("GreetController: handleResponse is invoked");
    }

}
