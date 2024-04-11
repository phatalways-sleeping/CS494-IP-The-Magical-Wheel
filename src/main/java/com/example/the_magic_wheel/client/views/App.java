package com.example.the_magic_wheel.client.views;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.example.the_magic_wheel.client.sockets.Client;
import com.example.the_magic_wheel.protocols.request.Request;
import com.example.the_magic_wheel.protocols.response.Response;

/**
 * JavaFX App
 */
public class App extends Application {
    public static void main(String[] args) {
        launch();
    }

    private ScenesManager scenesManager;
    private Client client;

    private volatile boolean isClientRunning = true;

    public App() {
        // Connect to server
        BlockingQueue<Request> requests = new LinkedBlockingQueue<>();
        BlockingQueue<Response> responses = new LinkedBlockingQueue<>();

        // Initialize the client with host, port, and the blocking queues
        client = new Client("localhost", 8080, requests, responses);
    }

    @Override
    public void start(@SuppressWarnings("exports") Stage stage) throws IOException {
        scenesManager = new ScenesManager(stage, this);

        Thread clientThread = new Thread(() -> {
            client.run();
            isClientRunning = false;
            scenesManager.showAlert();
        });
        clientThread.setDaemon(true); // Set it as daemon thread
        clientThread.start();

        Thread responseListenerThread = new Thread(() -> listenForResponses());
        responseListenerThread.setDaemon(true); // Set it as daemon thread
        responseListenerThread.start();
    }

    private void listenForResponses() {
        try {
            while (isClientRunning) {
                Response response = client.receiveResponse(); // Blocks until a response is available
                if (response != null) {
                    Platform.runLater(() -> {
                        scenesManager.getCurrentController().handleResponse(response);
                    });
                    Thread.sleep(100);
                }

            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            e.printStackTrace();
        }
    }

    public ScenesManager getScenesManager() {
        return scenesManager;
    }

    public Client getClient() {
        return client;
    }
}