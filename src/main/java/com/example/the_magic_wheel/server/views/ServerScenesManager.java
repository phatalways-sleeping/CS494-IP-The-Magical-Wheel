package com.example.the_magic_wheel.server.views;

import java.io.IOException;
import java.util.HashMap;

import com.example.the_magic_wheel.Configuration;
import com.example.the_magic_wheel.server.controllers.ServerController;
import com.example.the_magic_wheel.server.controllers.ServerEndGameController;
import com.example.the_magic_wheel.server.controllers.ServerGameIsRunningController;
import com.example.the_magic_wheel.server.controllers.ServerHallController;
import com.example.the_magic_wheel.server.controllers.ServerMaximumPlayerController;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ServerScenesManager  {
    private App app;
    private Scene scene;
    private String currentFxml;
    private HashMap<String, Parent> scenesMap = new HashMap<>();
    private HashMap<String, ServerController> controllersMap = new HashMap<>();
    private Thread serverThread;

    public ServerScenesManager(@SuppressWarnings("exports") Stage stage, App app, Thread serverThread ) throws IOException {
        this.app = app;
        this.serverThread = serverThread;
        addNewScene(Configuration.SERVER_HALL_FXML, new ServerHallController(app));
        addNewScene(Configuration.SERVER_MAXIMUM_PLAYER_FXML, new ServerMaximumPlayerController(app));
        addNewScene(Configuration.SERVER_GAME_RUNNING_FXML, new ServerGameIsRunningController(app));
        addNewScene(Configuration.SERVER_END_GAME_FXML, new ServerEndGameController(app));

        scene = new Scene(scenesMap.get( Configuration.SERVER_HALL_FXML), Configuration.WIDTH, Configuration.HEIGHT);
        currentFxml =  Configuration.SERVER_HALL_FXML;

        stage.setTitle("Magical Wheel Server");
        stage.setScene(scene);
        stage.show();
    }

    public void startServer() {
        serverThread.start();
    }
    private void addNewScene(String fxml, ServerController serverController) throws IOException {
         FXMLLoader loader = new FXMLLoader(Configuration.class.getResource(fxml + ".fxml"));
         loader.setController(serverController);
         scenesMap.put(fxml, loader.load());
         controllersMap.put(fxml, serverController);
    }

    public void switchScene(String sceneName) {
       // System.err.println("Switching to " + sceneName);
        Parent p = scenesMap.get(sceneName);
        currentFxml = sceneName;
        if (p != null)
            scene.setRoot(p);
    }

    public ServerController getController(String sceneName) {
        return controllersMap.get(sceneName);
    }

    public ServerController getCurrentController() {
        return controllersMap.get(currentFxml);
    }

    public void exitGame() {
        Platform.exit();
        System.exit(0);
    }

    public void resetController() throws IOException {
        scenesMap.clear();
        controllersMap.clear();
        addNewScene( Configuration.SERVER_HALL_FXML, new ServerHallController(app));
        addNewScene(Configuration.SERVER_MAXIMUM_PLAYER_FXML, new ServerMaximumPlayerController(app));
        addNewScene(Configuration.SERVER_GAME_RUNNING_FXML, new ServerGameIsRunningController(app));
        addNewScene(Configuration.SERVER_END_GAME_FXML, new ServerEndGameController(app));
    }
}