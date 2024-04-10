package com.example.the_magic_wheel.controllers;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import com.example.the_magic_wheel.App;
import com.example.the_magic_wheel.Client;
import com.example.the_magic_wheel.Configuration;
import com.example.the_magic_wheel.protocols.request.RegisterRequest;
import com.example.the_magic_wheel.protocols.response.GameEndResponse;
import com.example.the_magic_wheel.protocols.response.GameStartResponse;
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

public class RegisterController extends Controller {

    public RegisterController(App app) {
        super(app);
    }

    @FXML
    private TextField nameTextField;

    @FXML
    private HBox errorMsgHBox;

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
        app.getScenesManager().switchScene(Configuration.CLIENT_GREET_FXML);
    }

    @FXML 
    private void sendRegisterRequest() throws IOException {
        String nickname = nameTextField.getText();
        if (validate(nickname)) {
            app.getClient().sendRequest(new RegisterRequest(nickname));
            System.out.println("Client: sending register request...");
        }
    }
    

    @FXML
    private void deleteErrMsgNode() {
        Platform.runLater(() -> {
            if (errorMsgHBox.getChildren().size() > 0)
            errorMsgHBox.getChildren().remove(errorMsgHBox.getChildren().size() - 1);
        });
    }

    @FXML
    private void createErrMsgNode(String errorMessage) {
        Label errMsg = new Label(errorMessage);
        errMsg.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        errMsg.setTextFill(javafx.scene.paint.Color.RED);
        errMsg.setFont(new javafx.scene.text.Font("DejaVu Sans Mono Bold", 16));
        errorMsgHBox.getChildren().add(errMsg);
    }
    

    @Override
    public void handleResponse(Response response) {
        // Handle the response here
        if (response instanceof RegisterSuccessResponse) {
            handleRegisterSuccess((RegisterSuccessResponse) response);
        } else if (response instanceof RegisterFailureResponse) {
            handleRegisterFailure((RegisterFailureResponse) response);
        } else if (response instanceof GameStartResponse) {
            handleGameStart((GameStartResponse) response);
        } else {
            System.out.println("RegisterController: undefined response");
        }
    }

    private void handleGameStart(GameStartResponse response) {
        // System.out.println("Called from HallController: inside");
        app.getScenesManager().switchScene(Configuration.CLIENT_GAME_FXML);
        GameController gameController = (GameController) app.getScenesManager().getController(Configuration.CLIENT_GAME_FXML);
        gameController.setNickname(nameTextField.getText());
        gameController.handleResponse((GameStartResponse) response);
    }

    private void handleRegisterSuccess(RegisterSuccessResponse response) {
        System.out.println("nickname: " + response.getUsername());
        System.out.println("order: " + String.valueOf(response.getOrder()));
        app.getScenesManager().switchScene(Configuration.CLIENT_HALL_FXML);
        HallController hallController = (HallController) app.getScenesManager().getController(Configuration.CLIENT_HALL_FXML);
        hallController.setNickname(response.getUsername());
    }

    private void handleRegisterFailure(RegisterFailureResponse response) {
        createErrMsgNode(response.getReason());
        // Delete the error message after 2 seconds
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