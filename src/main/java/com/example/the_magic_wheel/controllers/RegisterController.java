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
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
    private void initialize() {
        // Add a focus listener to the nameTextField to remove error message when it receives focus
        nameTextField.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (newValue) { // If the text field gains focus
                    deleteErrMsgNode(); // Remove the error message
                }
            }
        });
    }

    @FXML
    private void switchToGreet() throws IOException {
        App.setRoot(Configuration.CLIENT_GREET_FXML);
    }

    @FXML 
    private void sendRegisterRequest() throws IOException {
        String nickname = nameTextField.getText();
        if (validate(nickname)) {
            App.getClient().sendRequest(new RegisterRequest(nickname));
            System.out.println("Client: sending register request...");
        }
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
    private void createErrMsgNode(String errorMessage) {
        HBox errMsgContainer = (HBox) App.lookup("#hbox_for_err_msg");
        Label errMsg = new Label(errorMessage);
        errMsg.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        errMsg.setTextFill(javafx.scene.paint.Color.RED);
        errMsg.setFont(new javafx.scene.text.Font("DejaVu Sans Mono Bold", 16));
        errMsgContainer.getChildren().add(errMsg);
    }
    

    @Override
    public void handleResponse(Response response) {
        // Handle the response here
        if (response instanceof RegisterSuccessResponse) {
            handleRegisterSuccess((RegisterSuccessResponse) response);
        } else if (response instanceof RegisterFailureResponse) {
            handleRegisterFailure((RegisterFailureResponse) response);
        } else {
            System.out.println("Called from RegisterController");
        }
    }

    private void handleRegisterSuccess(RegisterSuccessResponse response) {
        try {
            System.out.println("nickname: " + response.getUsername());
            System.out.println("order: " + String.valueOf(response.getOrder()));
            App.setRoot(Configuration.CLIENT_GAME_FXML);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleRegisterFailure(RegisterFailureResponse response) {
        // Example: Show an error message
        createErrMsgNode(response.getReason());
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

    private boolean validate(String nickname) {
        if (nickname != null && !nickname.isEmpty()) {
            return true;
        } else {
            createErrMsgNode("Nickname cannot be empty!");
            return false;
        }
    }
}