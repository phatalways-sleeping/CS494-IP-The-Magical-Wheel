package com.example.the_magic_wheel.sockets.Server;

import com.example.the_magic_wheel.protocols.Event;
import com.example.the_magic_wheel.protocols.request.CloseConnectionRequest;
import com.example.the_magic_wheel.protocols.request.GuessRequest;
import com.example.the_magic_wheel.protocols.request.RegisterRequest;
import com.example.the_magic_wheel.protocols.request.Request;
import com.example.the_magic_wheel.protocols.response.RegisterSuccessResponse;
import com.example.the_magic_wheel.protocols.response.Response;
import com.example.the_magic_wheel.protocols.response.ResultNotificationResponse;

import javafx.application.Application;
import javafx.stage.Stage;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

public class ServerApp extends Application implements GameMediator {
    // Server is the runnable component that listens for incoming connections
    // Therefore, it is wrapped in Thread to run in the background
    // Receiving connections is a blocking operation and should not be done on the
    // main thread
    // Servers receive requests from clients and call this.notify() to pass the
    // request to the mediator
    // The mediator is responsible for processing the request and returning a
    // response by
    // inserting it into the response queue
    // The server will iteratively checking the response queue and sending the
    // responses to the clients
    // if it finds any responses
    private Server server;

    // GameController is the component that manages the game state
    // It contains the logic for the game and is responsible for updating the game
    // state
    // based on the requests from the clients
    // The ServerApp may call methods on the GameController to update the game state
    // when it is notified
    // with a request from Server
    private Component gameController;

    // DatabaseController is the component that manages the database of keywords and
    // hints
    // It is responsible for fetching the keywords and hints from the text file
    // It usually is called by the GameController to get the keywords and hints
    @SuppressWarnings("unused")
    private Component databaseController;

    public ServerApp(Server server, Component gameController) {
        this.server = server;
        this.gameController = gameController;
        this.server.setMediator(this);
        this.gameController.setMediator(this);
    }

    @Override
    public Map<Integer, String> getPlayers() {
        return new TreeMap<>();
    }

    @Override
    public void addPlayer(String username) {
        getPlayers().put(getPlayers().size(), username);
    }

    @Override
    public void process(Request request) {
        guard((Event) request);
        Response response = null;
        if (request instanceof RegisterRequest) {
            final String source = request.getSource();
            final String destination = request.getDestination();
            final RegisterRequest registerRequest = (RegisterRequest) request;
            final String username = registerRequest.getUsername();

            // Register the player by interacting with the game controller

            // Suppose the game controller returns the response
            response = new RegisterSuccessResponse(username, 1, registerRequest.getRequestedAt());
            response.setSource(destination);
            response.setDestination(source); // Send the response back to the client, not broadcast
        } else if (request instanceof CloseConnectionRequest) {
            guard((Event) request);
            // final String source = request.getSource();
            // final String destination = request.getDestination();

            // Close the connection by interacting with the server

            // No response is needed for the CloseConnectionRequest
        } else { // GuessRequest
            final String destination = request.getDestination();
            final GuessRequest guessRequest = (GuessRequest) request;

            // Interact with the game controller to process the guess request

            // Suppose the game controller returns the response
            response = ResultNotificationResponse.successfulGuessChar(guessRequest.getUsername(), 1,
                    (short) 1, guessRequest.getRequestedAt());
            response.setSource(destination);
        }

        // Server App will send the response back to the client
        // by pushing it to the response queue
        if (Objects.nonNull(response)) {
            server.sendResponse(response);
        }
    }

    @SuppressWarnings("unused")
    private boolean gameHasStarted() {
        return false;
    }

    // The idea is to prevent the server from processing the request
    // that may lead to an invalid state
    // 1. If the game has not started, the server should not process the guess
    // 2. If the game has started, the server should not process the register request
    // 3. ...
    private void guard(Event event) {

    }

    @Override
    public void start(Stage stage) throws Exception {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'start'");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
