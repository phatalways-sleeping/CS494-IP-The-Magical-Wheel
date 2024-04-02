package com.example.the_magic_wheel.controllers;

import java.io.IOException;

import com.example.the_magic_wheel.App;
import com.example.the_magic_wheel.Configuration;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

public class GameController {

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
        App.setRoot(Configuration.CLIENT_RANKING_FXML);
    }
    

}
