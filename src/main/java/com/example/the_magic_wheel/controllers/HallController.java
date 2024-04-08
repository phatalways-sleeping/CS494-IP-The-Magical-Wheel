package com.example.the_magic_wheel.controllers;

import java.io.IOException;

import com.example.the_magic_wheel.App;
import com.example.the_magic_wheel.Client;
import com.example.the_magic_wheel.Configuration;
import com.example.the_magic_wheel.protocols.response.GameStartResponse;
import com.example.the_magic_wheel.protocols.response.Response;

import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;

public class HallController extends Controller {

    public HallController(App app) {
        super(app);
    }

    private String nickname;

    @FXML
    private ImageView imageView;

    @FXML
    private Text nicknameTextField;    

    @Override
    public void handleResponse(Response response) {
        if (response instanceof GameStartResponse) {
            // System.out.println("Called from HallController: inside");
            app.getScenesManager().switchScene(Configuration.CLIENT_GAME_FXML);
            GameController gameController = (GameController) app.getScenesManager().getController(Configuration.CLIENT_GAME_FXML);
            gameController.setNickname(nickname);
            gameController.handleResponse((GameStartResponse) response);
        }
        else {
            System.out.println("Called from HallController: Undefined Reponse");
        }
    }


    public void setNickname(String username) {
        nickname = username;
        nicknameTextField.setText("Your nickname: " + nickname);
    }


}
