package com.example.the_magic_wheel.controllers;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import com.example.the_magic_wheel.App;
import com.example.the_magic_wheel.Configuration;
import com.example.the_magic_wheel.protocols.request.RegisterRequest;
import com.example.the_magic_wheel.protocols.response.RegisterFailureResponse;
import com.example.the_magic_wheel.protocols.response.RegisterSuccessResponse;
import com.example.the_magic_wheel.protocols.response.Response;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

public class RegisterController implements Controller {

    @FXML
    private TextField nameTextField;

    @FXML
    private void switchToHall() throws IOException {
        App.setRoot(Configuration.CLIENT_GREET_FXML);
    }

    @FXML
    private void validateAndSwitchToGame() throws IOException {
        String nickname = nameTextField.getText();
        App.getClient().sendRequest(new RegisterRequest(nickname));
        System.out.println("Sending register request...");
    }

    @FXML
    private void deleteErrMsgNode() {
        HBox errMsgContainer = (HBox) App.lookup("#hbox_for_err_msg");
        Platform.runLater(() -> {
            if (errMsgContainer.getChildren().size() > 0)
                errMsgContainer.getChildren().remove(errMsgContainer.getChildren().size() - 1);
        });
    }

    @FXML
    private void createErrMsgNode() {
        HBox errMsgContainer = (HBox) App.lookup("#hbox_for_err_msg");
        Label errMsg = new Label("Invalid input!");
        errMsg.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        errMsg.setTextFill(javafx.scene.paint.Color.RED);
        errMsg.setFont(new javafx.scene.text.Font("DejaVu Sans Mono Bold", 16));
        errMsgContainer.getChildren().add(errMsg);
    }

    @Override
    public void handleResponse(Response response) {
        // Handle the response here
        System.out.println("Called from RegisterController");
        if (response instanceof RegisterSuccessResponse) {
            handleRegisterSuccess((RegisterSuccessResponse) response);
        } else if (response instanceof RegisterFailureResponse) {
            handleRegisterFailure((RegisterFailureResponse) response);
        }
    }

    private void handleRegisterSuccess(RegisterSuccessResponse response) {
        // Handle register success response here
        System.out.println("Register success! Username: " + response.getUsername() + ", Order: " + response.getOrder());
        // Example: Switch to another scene
        // App.setRoot(Configuration.CLIENT_GAME_FXML);
    }

    private void handleRegisterFailure(RegisterFailureResponse response) {
        // Handle register failure response here
        System.out.println("Register failed! Reason: " + response.getReason());
        // Example: Show an error message
        createErrMsgNode();
        // Example: Delete the error message after 2 seconds
        new Thread(() -> {
            try {
                Thread.sleep(2000);
                deleteErrMsgNode();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
}