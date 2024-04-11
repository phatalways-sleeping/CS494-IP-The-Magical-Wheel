package com.example.the_magic_wheel.sockets.Server;

import com.example.the_magic_wheel.severGameController.GameController;
import com.example.the_magic_wheel.ServerScenesManager;
import com.example.the_magic_wheel.protocols.Event;
import com.example.the_magic_wheel.severGameController.DatabaseController;
import com.example.the_magic_wheel.protocols.request.Request;
import com.example.the_magic_wheel.protocols.response.Response;
import com.example.the_magic_wheel.protocols.response.ResultNotificationResponse;
import com.example.the_magic_wheel.protocols.request.CloseConnectionRequest;
import com.example.the_magic_wheel.protocols.request.RegisterRequest;
import com.example.the_magic_wheel.protocols.response.GameEndResponse;
import com.example.the_magic_wheel.protocols.response.GameStartResponse;
import com.example.the_magic_wheel.protocols.response.RegisterSuccessResponse;
import javafx.application.Application;
import javafx.stage.Stage;

import java.util.Iterator;
import java.io.IOException;
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
    static boolean isEndGame = false;
    static boolean gameIsStarted = false;
    static int maxConnections;
    private final Server server;

    // GameController is the component that manages the game state
    // It contains the logic for the game and is responsible for updating the game
    // state
    // based on the requests from the clients
    // The ServerApp may call methods on the GameController to update the game state
    // when it is notified
    // with a request from Server
    private GameController gameController;

    // DatabaseController is the component that manages the database of keywords and
    // hints
    // It is responsible for fetching the keywords and hints from the text file
    // It usually is called by the GameController to get the keywords and hints
    private final DatabaseController databaseController;
    private ServerScenesManager serverScenesManager;

    public ServerApp() {
        final Server server = Server.spawn(new ServerConfiguration(8080,
                "localhost"));
        this.server = server;
        this.gameController = new GameController((GameMediator) this);
        this.databaseController = DatabaseController.getInstance();
        this.server.setMediator(this);
    }

    public void setMaxconnection(int maxConnections) {
        this.gameController.setMaxConnections(maxConnections);
        ServerApp.maxConnections = maxConnections;
    }

    public ServerScenesManager getServerScenesManager() {
        return serverScenesManager;
    }

    public static boolean isEndGame() {
        return isEndGame;
    }

    public static void setPlayAgain(boolean isEndGame) {
        ServerApp.isEndGame = isEndGame;
    }
    public static boolean gameIsStarted() {
        return gameIsStarted;
    }
    public static void setGameStart() {
        ServerApp.gameIsStarted = true;
    }

    public void playAgain() {
        gameController = new GameController((GameMediator) this);
        gameController.setMaxConnections(ServerApp.maxConnections);
    }

    @Override
    public Response process(Request request, SocketChannel channel) throws IOException {
        // Syncronize the process method since this.process() is called by the multiple
        // threads spanwned by the ExecutionManager
        synchronized (this) {
            System.out.println("Mediator: Processing request " + request.toString());
            Response response = null;
            // if (guard((Event) request) == true) {
            //     return response;
            // }
            if (request instanceof CloseConnectionRequest) {
                server.getClients().remove(request.getSource());
                channel.close();
                System.out.println(
                        "Mediator: Client " + request.getSource() + " has been removed from the list of clients");
            }
            response = gameController.process(request);
            if (response == null)
            {
                return response;
            }
            response.setSource(request.getDestination());
            response.setDestination(request.getSource());
            if (request instanceof RegisterRequest
                    && (response instanceof RegisterSuccessResponse || response instanceof GameStartResponse))
                // // Add new client to the list of clients
                server.getClients().put(request.getSource(), channel);
            if (response instanceof GameStartResponse || response instanceof GameEndResponse
                    || response instanceof ResultNotificationResponse) {
                response.setDestination(null);
            }
            if (response instanceof GameEndResponse)
                isEndGame = true;
            System.out.println("Mediator: Returning response " + response.getClass());
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
        return isEndGame;
        // 1. If the game has not started, the server should not process the guess
        // if (event instanceof GuessRequest && !gameController.gameIsStarted())
        // return false;
        // // 2. If the game has started, the server should not process the register
        // // request
        // if (event instanceof RegisterRequest && gameController.gameIsStarted())
        // return false;
        // 3. The server should not process the request if the client is not in the list
        // of clients

        // 4. If the game has ended or in the waiting for players state, the server
        // should not process the guess
        // 5. Ignore duplicate requests from the same client to avoid DOS attack
    }

    @Override
    public void start(@SuppressWarnings("exports") Stage stage) throws Exception {
        // Start the server

        final Thread serverThread = new Thread(server);
        serverThread.setDaemon(true);

        serverScenesManager = new ServerScenesManager(stage, this, serverThread);
    }

    @Override
    public Iterator<SocketChannel> getClients() {
        return server.getClients().values().iterator();
    }

    @Override
    public Response notifyConnectionLost(SocketChannel channel) throws Exception {
        final Request request = new CloseConnectionRequest(null);
        request.setSource(channel.getRemoteAddress().toString());
        request.setDestination(server.getAddress().toString());
        System.err.println("Mediator: Contacting GameController to process the closing connection request of "
                + request.getSource());
        return this.process(request, channel);
    }

    @Override
    public String getKeyWordString() {
        return databaseController.getKeyWordString();
    }

    // @Override
    // public void clearAllConnections() throws IOException {
    //     server.clearAllConnections();
    // }
}
