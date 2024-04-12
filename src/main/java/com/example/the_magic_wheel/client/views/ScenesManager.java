package com.example.the_magic_wheel.client.views;

import com.example.the_magic_wheel.Configuration;
import com.example.the_magic_wheel.client.controllers.*;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;

public class ScenesManager {

    private App app;
    private Scene scene;
    private String currentFxml;
    private HashMap<String, Parent> scenesMap = new HashMap<>();
    private HashMap<String, Controller> controllersMap = new HashMap<>();

    ScenesManager(Stage stage, App app) throws IOException {
        this.app = app;
        addNewScene(Configuration.CLIENT_GREET_FXML, new GreetController(app));
        addNewScene(Configuration.CLIENT_REGISTER_FXML, new RegisterController(app));
        addNewScene(Configuration.CLIENT_HALL_FXML, new HallController(app));
        addNewScene(Configuration.CLIENT_GAME_FXML, new GameController(app));
        addNewScene(Configuration.CLIENT_RANKING_FXML, new RankController(app));

        scene = new Scene(scenesMap.get(Configuration.CLIENT_GREET_FXML), Configuration.WIDTH, Configuration.HEIGHT);
        currentFxml = Configuration.CLIENT_GREET_FXML;

        stage.setTitle("The Magical Wheel");
        stage.setScene(scene);

        stage.setResizable(false);

        stage.show();
    }

    private void addNewScene(String fxml, Controller controller) throws IOException {
        FXMLLoader loader = new FXMLLoader(Configuration.class.getResource(fxml + ".fxml"));
        loader.setController(controller);
        scenesMap.put(fxml, loader.load());
        controllersMap.put(fxml, controller);
    }

    public void switchScene(String sceneName) {
        Parent p = scenesMap.get(sceneName);
        currentFxml = sceneName;
        if (p != null) scene.setRoot(p);
    }

    public Controller getController(String sceneName) {
        return controllersMap.get(sceneName);
    }

    public Controller getCurrentController() {
        return controllersMap.get(currentFxml);
    }

    public void exitGame() {
        Platform.exit();
        System.exit(0);
    }

    public void resetController() throws IOException {
        scenesMap.clear();
        controllersMap.clear();


        addNewScene(Configuration.CLIENT_GREET_FXML, new GreetController(app));
        addNewScene(Configuration.CLIENT_REGISTER_FXML, new RegisterController(app));
        addNewScene(Configuration.CLIENT_HALL_FXML, new HallController(app));
        addNewScene(Configuration.CLIENT_GAME_FXML, new GameController(app));
        addNewScene(Configuration.CLIENT_RANKING_FXML, new RankController(app));
    }

    public void showAlert() {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Connection Error");
            alert.setHeaderText(null);
            alert.setContentText("Lost connection to server");

            Stage stage = (Stage) scene.getWindow();
            alert.initOwner(stage);

            // Show the alert in the middle of the scene
            alert.setX(stage.getX() + stage.getWidth() / 2 - alert.getWidth() / 2);
            alert.setY(stage.getY() + stage.getHeight() / 2 - alert.getHeight() / 2);


            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    exitGame();
                }
            });
        });

    }

}