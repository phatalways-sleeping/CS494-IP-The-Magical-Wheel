package com.example.the_magic_wheel.controllers;

import java.io.IOException;

import com.example.the_magic_wheel.App;
import com.example.the_magic_wheel.Configuration;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public class RegisterController {

    @FXML
    private void switchToHall() throws IOException {
        App.setRoot(Configuration.CLIENT_HALL_FXML);
    }

    @FXML
    private void validateAndSwitchToGame() throws IOException {
        // 1. Send request to server to validate the user input
        // App.send(new RegisterRequest());
        // 2. If the user input is valid, switch to the game screen
        // App.setRoot(Configuration.CLIENT_GAME_FXML);

        // 3. If the user input is invalid, show an error message
        createErrMsgNode();
        // wait for 2 seconds to delete the error message
        new Thread(() -> {
            try {
                Thread.sleep(2000);
                deleteErrMsgNode();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void deleteErrMsgNode() {
        // Get the id of the HBox for error message
        HBox errMsgContainer = (HBox) App.lookup("#hbox_for_err_msg");
        // Remove the error message from the HBox
        Platform.runLater(() -> {
            if (errMsgContainer.getChildren().size() > 0)
                errMsgContainer.getChildren().remove(errMsgContainer.getChildren().size() - 1);
        });
    }

    private void createErrMsgNode() {
        // Get the id of the HBox for error message
        HBox errMsgContainer = (HBox) App.lookup("#hbox_for_err_msg");
        // Insert the error message into the HBox
        Label errMsg = new Label("Invalid input!");
        errMsg.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        errMsg.setTextFill(javafx.scene.paint.Color.RED);
        errMsg.setFont(new javafx.scene.text.Font("DejaVu Sans Mono Bold", 16));
        errMsgContainer.getChildren().add(errMsg);
    }
}
