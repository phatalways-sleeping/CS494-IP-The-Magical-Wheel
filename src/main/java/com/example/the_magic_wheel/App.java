package com.example.the_magic_wheel;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.example.the_magic_wheel.controllers.Controller;
import com.example.the_magic_wheel.controllers.GameController;
import com.example.the_magic_wheel.protocols.request.Request;
import com.example.the_magic_wheel.protocols.response.GameEndResponse;
import com.example.the_magic_wheel.protocols.response.GameStartResponse;
import com.example.the_magic_wheel.protocols.response.RegisterFailureResponse;
import com.example.the_magic_wheel.protocols.response.RegisterSuccessResponse;
import com.example.the_magic_wheel.protocols.response.Response;
import com.example.the_magic_wheel.protocols.response.ResultNotificationResponse;

/**
 * JavaFX App
 */
public class App extends Application {

    private static Scene scene;
    private static Controller controller;
    private static Client client;

    @Override
    public void start(Stage stage) throws IOException {
        scene = new Scene(loadFXML(Configuration.CLIENT_GREET_FXML), Configuration.WIDTH, Configuration.HEIGHT);
        stage.setScene(scene);
        stage.show();

        
        // Connect to server
        BlockingQueue<Request> requests = new LinkedBlockingQueue<>();
        BlockingQueue<Response> responses = new LinkedBlockingQueue<>();

        // Initialize the client with host, port, and the blocking queues
        client = new Client("localhost", 8080, requests, responses);

        // Start the client in a separate thread
        Thread clientThread = new Thread(client);
        clientThread.setDaemon(true); // Set it as daemon thread
        clientThread.start();


        Thread responseListenerThread = new Thread(() -> listenForResponses());
        responseListenerThread.setDaemon(true); // Set it as daemon thread
        responseListenerThread.start();
    }

    public static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        Parent root = fxmlLoader.load();
        // Get the controller associated with the loaded FXML
        controller = fxmlLoader.getController();
        return root;
    }

    public static void main(String[] args) {
        launch();
    }

    @SuppressWarnings("exports")
    public static Node lookup(String string) {
        return scene.lookup(string);
    }

    public static Client getClient() {
        return client;
    }

    public static Controller getCurrentController() {
        return controller;
    }

    private void listenForResponses() {
        try {
            while (true) {
                Response response = getClient().receiveResponse(); // Blocks until a response is available
                if (response != null) {
                    Platform.runLater(() -> {
                        getCurrentController().handleResponse(response);
                    });
                    Thread.sleep(100);
                }
                
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}