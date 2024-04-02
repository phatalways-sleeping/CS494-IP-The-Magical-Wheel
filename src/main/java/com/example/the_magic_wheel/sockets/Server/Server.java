package com.example.the_magic_wheel.sockets.Server;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.List;
import java.util.ArrayList;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ArrayBlockingQueue;

import com.example.the_magic_wheel.Configuration;
import com.example.the_magic_wheel.protocols.request.Request;
import com.example.the_magic_wheel.protocols.response.Response;

public class Server implements Runnable {
    public static void main(String[] args) throws Exception {
        final BlockingQueue<Request> requests = new ArrayBlockingQueue<>(Configuration.BUFFER_SIZE);
        final BlockingQueue<Response> responses = new ArrayBlockingQueue<>(Configuration.BUFFER_SIZE);
        final Server server = new Server.Builder("localhost", 8080).witMaxConnections(1)
                .withRequests(requests).withResponses(responses)
                .withPlayers(new ConcurrentHashMap<>()).build();
        final Thread worker = new Thread(server);
        worker.start();

        try {
            while (server.players.size() < server.maxConnections) {
                Thread.sleep(1000);
            }
            // Start the game
            server.isServingGame.set(true);
        } catch (Exception e) {

        }
    }

    private final int port;
    private final String host;

    private int maxConnections;

    private AtomicBoolean isServingGame;

    private AtomicBoolean gameEnded;

    private ServerState state;

    private Map<String, SocketChannel> clients = new TreeMap<>();

    // Shared between main thread and worker thread
    private Map<Integer, String> players;
    private BlockingQueue<Request> requests;
    private BlockingQueue<Response> responses;

    private static Map<String, Server> servers = new TreeMap<>();

    public static class Builder {
        private final String host;
        private final int port;
        private int maxConnections;
        private Map<Integer, String> players;
        private BlockingQueue<Request> requests;
        private BlockingQueue<Response> responses;

        public Builder(String host, int port) {
            this.host = host;
            this.port = port;
            this.maxConnections = Configuration.MAX_CONNECTIONS;
        }

        public Builder witMaxConnections(int maxConnections) {
            this.maxConnections = maxConnections;
            return this;
        }

        public Builder withPlayers(Map<Integer, String> players) {
            this.players = players;
            return this;
        }

        public Builder withRequests(BlockingQueue<Request> requests) {
            this.requests = requests;
            return this;
        }

        public Builder withResponses(BlockingQueue<Response> responses) {
            this.responses = responses;
            return this;
        }

        public Server build() {
            String key = host + ":" + port;
            if (servers.containsKey(key)) {
                return servers.get(key);
            }

            Server server = new Server(host, port, maxConnections);
            server.players = players;
            server.requests = requests;
            server.responses = responses;

            server.setState(new WaitingForPlayers());
            servers.put(key, server);
            return server;
        }
    }

    Map<String, SocketChannel> getClients() {
        return clients;
    }

    Map<Integer, String> getPlayers() {
        return players;
    }

    BlockingQueue<Request> getRequests() {
        return requests;
    }

    BlockingQueue<Response> getResponses() {
        return responses;
    }

    List<String> getTopicForGame() {
        final List<String> topics = new ArrayList<>();
        topics.add("python");
        topics.add("Top 10 programming languages");
        return topics;
    }

    public String getNextPlayer() {
        return "Alice";
    }

    public static void stop(String host, int port) {
        String key = host + ":" + port;
        if (servers.containsKey(key)) {
            servers.remove(key);
        }
    }

    boolean isServingGame() {
        return isServingGame.get();
    }

    int getMaxConnections() {
        return maxConnections;
    }

    boolean appropriateUsername(String username) {
        return true;
    }

    @Override
    public void run() {
        try {
            InetAddress hostIpAddress = InetAddress.getByName(host);
            Selector selector = Selector.open();
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.socket().bind(new InetSocketAddress(hostIpAddress, port));
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            System.out.println("Server started at " + host + ":" + port);
            while (!Thread.currentThread().isInterrupted()) {
                selector.select(Configuration.TIMEOUT);
                Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
                while (keys.hasNext()) {
                    SelectionKey key = keys.next();
                    keys.remove();
                    if (!key.isValid()) {
                        continue;
                    }
                    this.state.handle(selector, key);
                    if (state instanceof GameEndState) {
                        // Reset the state to waiting for players
                        this.setState(new WaitingForPlayers());
                        this.isServingGame.set(false);
                        this.gameEnded.set(false);
                        continue;
                    }
                    // If the server is serving game, switch to the next state
                    if (isServingGame.get() && this.state instanceof WaitingForPlayers) {
                        this.setState(new GameLoadingState());
                        continue;
                    }
                    // If the game has ended, switch to the next state
                    if (this.gameEnded.get()) {
                        this.setState(new GameEndState());
                    }
                    // After the game has been loaded, switch to the next state
                    // Assume that the topic has been loaded successfully and
                    // being sent to the clients
                    if (this.state instanceof GameLoadingState) {
                        this.setState(new GamePlayState());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void recoverFromConnectionFailure(SocketChannel socketChannel) {
        // Handle exception to ensure the server
        // still runs at a valid state after connection failure
        try {
            System.out.println("Connection lost with " + socketChannel.getRemoteAddress());
            final String address = socketChannel.getRemoteAddress().toString();
            this.clients.remove(address);
            this.players.values().removeIf(value -> value.split("@")[1].contains(address));
            // unregister the channel
            socketChannel.close();
            System.out.println("Unregistered " + address);
            // If the server is serving the game and there is no player left
            // switch to the waiting for players state
            if (this.isServingGame.get() && this.players.size() == 0) {
                this.isServingGame.set(false);
                this.setState(new WaitingForPlayers());
                this.gameEnded.set(false);
                System.out.println("No player left, switch to waiting for players state");
                this.notifyServerApp();
            }
        } catch (Exception e) {
            // Aborting the server
            e.printStackTrace();
            System.err.println("Server experienced a fatal error. Aborting...");
            System.exit(1);
        }
    }

    private void setState(ServerState state) {
        if (this.state != null) {
            this.state = null; // for garbage collection
        }
        this.state = state;
        this.state.setServer(this);
    }

    private Server(String host, int port, int maxConnections) {
        this.maxConnections = maxConnections;
        this.port = port;
        this.host = host;
        this.isServingGame = new AtomicBoolean(false);
        this.gameEnded = new AtomicBoolean(false);
    }

    public void notifyServerApp() {
        // Notify about the change of state
        // and isServingGame, gameEnded
    }
}

class IllegalConnectionException extends Exception {
    public IllegalConnectionException(String message) {
        super(message);
    }
}