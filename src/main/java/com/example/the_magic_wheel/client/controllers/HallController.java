package com.example.the_magic_wheel.client.controllers;

import com.example.the_magic_wheel.Configuration;
import com.example.the_magic_wheel.client.views.App;
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
            app.getScenesManager().switchScene(Configuration.CLIENT_GAME_FXML);
            GameController gameController = (GameController) app.getScenesManager().getController(Configuration.CLIENT_GAME_FXML);
            gameController.setNickname(nickname);
            gameController.handleResponse((GameStartResponse) response);
            System.out.println("HallController: receive GameStartResponse");
        }
        else {
            System.out.println("HallController: undefined Reponse");
        }
    }


    public void setNickname(String username) {
        nickname = username;
        nicknameTextField.setStyle("-fx-font-family: 'DejaVu Sans';");
        nicknameTextField.setText("Your nickname: " + nickname);
    }
}
