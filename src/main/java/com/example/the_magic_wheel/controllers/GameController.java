package com.example.the_magic_wheel.controllers;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import com.example.the_magic_wheel.App;
import com.example.the_magic_wheel.Configuration;
import com.example.the_magic_wheel.protocols.request.RegisterRequest;
import com.example.the_magic_wheel.protocols.response.Response;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

public class GameController implements Controller  {

    @FXML
    private TextField guessTextField;

    @FXML
    private Text hintText;

    @FXML
    private Text keywordText;

    @FXML
    private TextField keywordTextField;

    @FXML
    private Button submitButton;

    @FXML
    private Text turnText;

    @FXML
    void switchToRanking(ActionEvent event) throws IOException {
        // App.setRoot(Configuration.CLIENT_RANKING_FXML);
        App.getClient().sendRequest(new RegisterRequest("phuc"));
        System.out.println("sendddddddddddddd");
    }
    
    @FXML
    void submitAnswer(ActionEvent event) throws IOException {
        
    }

    public void updateTurn() {
        Integer n = Integer.valueOf(turnText.getText()) + 1;
        turnText.setText(String.valueOf(n));
    }

    public void handleResponse() {

    }

    @Override
    public void handleResponse(Response response) {
        // TODO Auto-generated method stub
        System.out.println("Called from GameController");
    }

}
