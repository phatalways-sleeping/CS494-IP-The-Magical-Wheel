package com.example.the_magic_wheel.server.views;

import com.example.the_magic_wheel.protocols.request.Request;
import com.example.the_magic_wheel.protocols.response.Response;
import com.example.the_magic_wheel.protocols.response.ResultNotificationResponse;
import com.example.the_magic_wheel.server.sockets.GameMediator;
import com.example.the_magic_wheel.server.sockets.Server;
import com.example.the_magic_wheel.server.sockets.ServerConfiguration;
import com.example.the_magic_wheel.server.utils.DatabaseController;
import com.example.the_magic_wheel.server.utils.GameController;
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

public class App extends Application implements GameMediator {
    public static void main(String[] args) {
        launch(args);
    }

    static boolean isEndGame = false;
    static boolean gameIsStarted = false;
    static int maxConnections;
    private final Server server;
    private GameController gameController;
    private final DatabaseController databaseController;
    private ServerScenesManager serverScenesManager;

    public App() {
        final Server server = Server.spawn(new ServerConfiguration(8080,
                "localhost"));
        this.server = server;
        this.gameController = new GameController((GameMediator) this);
        this.databaseController = DatabaseController.getInstance();
        this.server.setMediator(this);
    }

    public void setMaxconnection(int maxConnections) {
        this.gameController.setMaxConnections(maxConnections);
        App.maxConnections = maxConnections;
    }

    public ServerScenesManager getServerScenesManager() {
        return serverScenesManager;
    }

    public static boolean isEndGame() {
        return isEndGame;
    }

    public static void setPlayAgain(boolean isEndGame) {
        App.isEndGame = isEndGame;
    }

    public static boolean gameIsStarted() {
        return gameIsStarted;
    }

    public static void setGameStart() {
        App.gameIsStarted = true;
    }

    public void playAgain() {
        gameController = new GameController((GameMediator) this);
        gameController.setMaxConnections(App.maxConnections);
    }

    @Override
    public Response process(Request request, SocketChannel channel) throws IOException {
        // Syncronize the process method since this.process() is called by the multiple
        // threads spanwned by the ExecutionManager
        synchronized (this) {
            System.out.println("Mediator: Processing request " + request.toString());
            Response response = null;
            // if (guard((Event) request) == true) {
            // return response;
            // }
            if (request instanceof CloseConnectionRequest) {
                server.getClients().remove(request.getSource());
                channel.close();
                System.out.println(
                        "Mediator: Client " + request.getSource() + " has been removed from the list of clients");
            }
            response = gameController.process(request);
            if (response == null) {
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

    @Override
    public void start(@SuppressWarnings("exports") Stage stage) throws IOException {
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
    public Response notifyConnectionLost(SocketChannel channel) throws IOException {
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
    // server.clearAllConnections();
    // }
}
