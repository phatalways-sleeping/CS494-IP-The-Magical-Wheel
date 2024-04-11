package com.example.the_magic_wheel.severScenesManager.controllers;
import com.example.the_magic_wheel.sockets.Server.ServerApp;

import java.io.IOException;

import com.example.the_magic_wheel.Configuration;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

public class ServerMaximumPlayerController extends ServerController{
    public ServerMaximumPlayerController(ServerApp app) {
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
    private void switchToServerHall() throws IOException {
        serverApp.getServerScenesManager().switchScene(Configuration.SERVER_HALL_FXML);
    }

    @FXML 
    private void startSever() throws IOException {
        String maximumPlayer = nameTextField.getText();
        int maximumPlayerInNumber = getMaximumPlayer(maximumPlayer);
        if (maximumPlayerInNumber != -1) {
            serverApp.getServerScenesManager().switchScene(Configuration.SERVER_GAME_RUNNING_FXML);
            serverApp.setMaxconnection(maximumPlayerInNumber);
            if (ServerApp.gameIsStarted() == false)
                {
                    serverApp.getServerScenesManager().startServer();
                    ServerApp.setGameStart();
                }
            Thread checkEndGameThread = new Thread(() -> {
                while(true) {
                    if (ServerApp.isEndGame()) {
                        Platform.runLater(() -> {
                            serverApp.getServerScenesManager().switchScene(Configuration.SERVER_END_GAME_FXML);
                        });
                        break;
                    }
                    try {
                        // Adjust the sleep time according to your requirements
                        Thread.sleep(1000); // Sleep for 1 second before checking again
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
            checkEndGameThread.setDaemon(true);
            checkEndGameThread.start();
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

    private int getMaximumPlayer(String maximumPlayer) {
        if (maximumPlayer == null || maximumPlayer.isEmpty()) {
            createErrMsgNode("The number of players cannot be empty!");
            return -1;
        }
        int maximumPlayerInNumber = 0;
        for (int i = 0; i < maximumPlayer.length(); i++) {
            if (!Character.isDigit(maximumPlayer.charAt(i))) {
                createErrMsgNode("The number of players must be a number!");
                return -1;
            }
            maximumPlayerInNumber = maximumPlayerInNumber * 10 + (maximumPlayer.charAt(i) - '0');
        }
        if (maximumPlayerInNumber < 2 || maximumPlayerInNumber > 10) {
            createErrMsgNode("The number of players must be between 2 and 10!");
            return -1;
        }
       return maximumPlayerInNumber;
    }
    


}
