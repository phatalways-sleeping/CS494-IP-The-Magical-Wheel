package com.example.the_magic_wheel.sockets.Server;

import com.example.the_magic_wheel.severGameController.GameController;
//import com.example.the_magic_wheel.severScenesManager.ServerScenesManager;
import com.example.the_magic_wheel.protocols.Event;
import com.example.the_magic_wheel.severGameController.DatabaseController;
import com.example.the_magic_wheel.protocols.request.Request;
import com.example.the_magic_wheel.protocols.response.Response;
import com.example.the_magic_wheel.protocols.request.CloseConnectionRequest;
import com.example.the_magic_wheel.protocols.request.GuessRequest;
import com.example.the_magic_wheel.protocols.request.RegisterRequest;
import com.example.the_magic_wheel.protocols.response.GameStartResponse;
import com.example.the_magic_wheel.protocols.response.RegisterSuccessResponse;
import com.example.the_magic_wheel.protocols.response.ResultNotificationResponse;
import com.example.the_magic_wheel.protocols.response.Response;
import com.example.the_magic_wheel.severGameController.DatabaseController;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Iterator;

import java.nio.channels.SocketChannel;;

public class ServerApp extends Application implements GameMediator {
    public static void main(String[] args) {
        launch(args);
    }

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
    private final Server server;

    // GameController is the component that manages the game state
    // It contains the logic for the game and is responsible for updating the game
    // state
    // based on the requests from the clients
    // The ServerApp may call methods on the GameController to update the game state
    // when it is notified
    // with a request from Server
    private final GameController gameController;

    // DatabaseController is the component that manages the database of keywords and
    // hints
    // It is responsible for fetching the keywords and hints from the text file
    // It usually is called by the GameController to get the keywords and hints
    private final DatabaseController databaseController;
    //private ServerScenesManager serverScenesManager;

    public ServerApp() {
        final Server server = Server.spawn(new ServerConfiguration(8080,
                "localhost"));
        this.server = server;
        this.gameController = new GameController(this);
        this.databaseController = DatabaseController.getInstance();
        this.server.setMediator(this);
        //this.databaseController.setMediator(this);
    }

    public void setMaxconnection(int maxConnections) {
        this.gameController.setMaxConnections(maxConnections);
    }

    @Override
    public Response process(Request request, SocketChannel channel) throws Exception {
        // Syncronize the process method since this.process() is called by the multiple
        // threads spanwned by the ExecutionManager
        synchronized (this) {
            Response response = null;
            if (!guard((Event) request)) {
                return response;
            }
            response =  gameController.process(request);
            response.setSource(request.getDestination());
            response.setDestination(request.getSource()); // Send the response back to the client, not broadcast
            if (request instanceof RegisterRequest && (response instanceof RegisterSuccessResponse || response instanceof GameStartResponse))
                // Add new client to the list of clients
                server.getClients().put(request.getSource(), channel);

            // if (request instanceof RegisterRequest) {
            //     final String source = request.getSource();
            //     final String destination = request.getDestination();
            //     final RegisterRequest registerRequest = (RegisterRequest) request;
            //     final String username = registerRequest.getUsername();

            //     // Register the player by interacting with the game controller

            //     // Suppose the game controller returns the response
            //     response = new RegisterSuccessResponse(username, 1, registerRequest.getRequestedAt());
            //     response.setSource(destination);
            //     response.setDestination(source); // Send the response back to the client, not broadcast

            //     // Add new client to the list of clients
            //     server.getClients().put(source, channel);
            // } else if (request instanceof CloseConnectionRequest) {
            //     guard((Event) request);
            //     // final String source = request.getSource();
            //     // final String destination = request.getDestination();

            //     // Close the connection by interacting with the server

            //     // No response is needed for the CloseConnectionRequest
            // } else { // GuessRequest
            //     final String destination = request.getDestination();
            //     final GuessRequest guessRequest = (GuessRequest) request;

            //     // Interact with the game controller to process the guess request

            //     // Suppose the game controller returns the response
            //     response = ResultNotificationResponse.successfulGuessChar(guessRequest.getUsername(), 1,
            //             (short) 1, guessRequest.getRequestedAt());

            //     response.setSource(destination);
            // }
             return response;
        }
    }

    // The idea is to prevent the server from processing the request
    // that may lead to an invalid state
    // 1. If the game has not started, the server should not process the guess
    // 2. If the game has started, the server should not process the register
    // request
    // 3. ...
    private boolean guard(Event event) {
        // 1. If the game has not started, the server should not process the guess

        // 2. If the game has started, the server should not process the register
        // request

        // 3. The server should not process the request if the client is not in the list
        // of clients

        // 4. If the game has ended or in the waiting for players state, the server
        // should not process the guess

        // 5. Ignore duplicate requests from the same client to avoid DOS attack

        return true;
    }

    @Override
    public void start(@SuppressWarnings("exports") Stage stage) throws Exception {
        // Start the server
       // serverScenesManager = new ServerScenesManager(stage, this);

        final Thread serverThread = new Thread(server);
        serverThread.setDaemon(true);
        serverThread.start();

        Scene scene = new Scene(new Group(), 300, 250);
        stage.setTitle("Simple Window");
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public Iterator<SocketChannel> getClients() {
        return server.getClients().values().iterator();
    }

    @Override
    public Response notifyConnectionLost(SocketChannel channel) throws Exception {
        // server.getClients().remove(address);
        // channel.close();
        final Request request = new CloseConnectionRequest(null);
        return this.process(request, channel);
    }

    @Override
    public String getKeyWordString() {
        return databaseController.getKeyWordString();
    }
}
