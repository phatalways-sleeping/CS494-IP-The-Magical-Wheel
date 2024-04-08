package com.example.the_magic_wheel;

import java.io.IOException;
import java.util.HashMap;

import com.example.the_magic_wheel.controllers.Controller;
import com.example.the_magic_wheel.controllers.GreetController;
import com.example.the_magic_wheel.controllers.HallController;
import com.example.the_magic_wheel.controllers.RegisterController;
import com.example.the_magic_wheel.controllers.GameController;
import com.example.the_magic_wheel.controllers.RankController;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ScenesManager {

    private Scene scene;
    private String currentFxml;
    private HashMap<String, Parent> scenesMap = new HashMap<>();
    private HashMap<String, Controller> controllersMap = new HashMap<>();

    ScenesManager(Stage stage, App app) throws IOException {
        addNewScene(Configuration.CLIENT_GREET_FXML, new GreetController(app));
        addNewScene(Configuration.CLIENT_REGISTER_FXML, new RegisterController(app));
        addNewScene(Configuration.CLIENT_HALL_FXML, new HallController(app));
        addNewScene(Configuration.CLIENT_GAME_FXML, new GameController(app));
        addNewScene(Configuration.CLIENT_RANKING_FXML, new RankController(app));

        scene = new Scene(scenesMap.get(Configuration.CLIENT_GREET_FXML), Configuration.WIDTH, Configuration.HEIGHT);
        currentFxml = Configuration.CLIENT_GREET_FXML;

        stage.setTitle("Magical Wheel");
        stage.setScene(scene);
        stage.show();
    }

    private void addNewScene(String fxml, Controller controller) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml + ".fxml"));
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
}