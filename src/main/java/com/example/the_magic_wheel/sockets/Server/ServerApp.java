package com.example.the_magic_wheel.sockets.Server;

import com.example.the_magic_wheel.severGameController.GameController;
import com.example.the_magic_wheel.ServerScenesManager;
import com.example.the_magic_wheel.protocols.Event;
import com.example.the_magic_wheel.severGameController.DatabaseController;
import com.example.the_magic_wheel.protocols.request.Request;
import com.example.the_magic_wheel.protocols.response.Response;
import com.example.the_magic_wheel.protocols.request.CloseConnectionRequest;
import com.example.the_magic_wheel.protocols.request.GuessRequest;
import com.example.the_magic_wheel.protocols.request.RegisterRequest;
import com.example.the_magic_wheel.protocols.response.GameEndResponse;
import com.example.the_magic_wheel.protocols.response.GameStartResponse;
import com.example.the_magic_wheel.protocols.response.RegisterSuccessResponse;
import javafx.application.Application;
import javafx.stage.Stage;

import java.util.Iterator;

import java.nio.channels.SocketChannel;;

public class ServerApp extends Application implements GameMediator {
    public static void main(String[] args) {
        launch(args);
    }

    private final Server server;
    private final GameController gameController;
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
    }

    @Override
    public Response process(Request request, SocketChannel channel) {
        // Syncronize the process method since this.process() is called by the multiple
        // threads spanwned by the ExecutionManager
        synchronized (this) {
            System.out.println("Mediator: Processing request " + request.toString());
            Response response = null;
            if (!guard((Event) request)) {
                return response;
            }
            response = gameController.process(request);
            response.setSource(request.getDestination());
            response.setDestination(request.getSource()); // Send the response back to the client, not broadcast
            if (request instanceof RegisterRequest
                    && (response instanceof RegisterSuccessResponse || response instanceof GameStartResponse))
                // Add new client to the list of clients
                server.getClients().put(request.getSource(), channel);
            if (response instanceof GameStartResponse || response instanceof GameEndResponse) {
                response.setDestination(null);
            }
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
        // if (event instanceof GuessRequest && !gameController.gameIsStarted())
        // return false;
        // // 2. If the game has started, the server should not process the register
        // // request
        // if (event instanceof RegisterRequest && gameController.gameIsStarted())
        // return false;
        // 3. The server should not process the request if the client is not in the list
        // of clients
        if (event instanceof GuessRequest && !server.getClients().containsKey(event.getSource()))
            return false;
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

        // Scene scene = new Scene(new Group(), 300, 250);
        // stage.setTitle("Simple Window");
        // stage.setScene(scene);
        // stage.show();
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
}
