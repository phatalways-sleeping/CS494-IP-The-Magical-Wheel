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

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.example.the_magic_wheel.Configuration;
import com.example.the_magic_wheel.protocols.Event;
import com.example.the_magic_wheel.protocols.request.Request;
import com.example.the_magic_wheel.protocols.response.Response;

public class Server implements Runnable, Component {
    public static void main(String[] args) {
        final ServerConfiguration configuration = new ServerConfiguration(8080, 10, "localhost");
        final Server server = Server.spawn(configuration);
        final Thread worker = new Thread(server);
        worker.start();
    }

    private final ServerConfiguration configuration;
    private ServerState state;
    private final BlockingQueue<Response> responseQueue = new LinkedBlockingQueue<>();
    GameMediator mediator;

    private Map<String, SocketChannel> clients = new TreeMap<>();
    private static Map<String, Server> servers = new TreeMap<>();

    Map<String, SocketChannel> getClients() {
        return clients;
    }

    public static Server spawn(ServerConfiguration configuration) {
        if (servers.containsKey(configuration.host + ":" + configuration.port)) {
            return servers.get(configuration.host + ":" + configuration.port);
        }
        Server server = new Server(configuration);
        servers.put(configuration.host + ":" + configuration.port, server);
        return server;
    }

    public static void stop(String host, int port) {
        String key = host + ":" + port;
        if (servers.containsKey(key)) {
            servers.remove(key);
        }
    }

    int getMaxConnections() {
        return configuration.maxConnections;
    }

    public BlockingQueue<Response> getResponses() {
        return responseQueue;
    }

    @Override
    public void run() {
        try {
            // Setting up the server
            InetAddress hostIpAddress = InetAddress.getByName(configuration.host);
            Selector selector = Selector.open();
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.socket().bind(new InetSocketAddress(hostIpAddress, configuration.port));
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

            // Logging the server start
            System.out.println("Server started at " + configuration.host + ":" + configuration.port);

            // Keep the server running until it is interrupted
            while (!Thread.currentThread().isInterrupted()) {
                selector.select(Configuration.TIMEOUT);
                Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
                while (keys.hasNext()) {
                    SelectionKey key = keys.next();
                    keys.remove();
                    if (!key.isValid()) {
                        continue;
                    }
                    // Depend on the state of the server
                    // the server will handle the request differently
                    this.state.handle(selector, key);

                    // After handling the request, the server will send back the response
                    // until the response queue is empty
                    // state.sendBackResponse(socketChannel, response); takes care of
                    // sending the response back to the client or broadcasting the response
                    while (!responseQueue.isEmpty()) {
                        Response response = responseQueue.poll();
                        for (SocketChannel socketChannel : clients.values()) {
                            state.sendBackResponse(socketChannel, response);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // The idea is to recover from the connection failure
    // such as:
    // - The client disconnects unexpectedly from the server during the game
    // - The client loses the connection with the server when in waiting for players state
    // - ...
    void recoverFromConnectionFailure(SocketChannel socketChannel) {
        // Handle exception to ensure the server
        // still runs at a valid state after connection failure
        try {
            System.out.println("Connection lost with " + socketChannel.getRemoteAddress());
            final String address = socketChannel.getRemoteAddress().toString();
            this.clients.remove(address);
            socketChannel.close();
            System.out.println("Unregistered " + address);
        } catch (Exception e) {
            // Aborting the server
            e.printStackTrace();
            System.err.println("Server experienced a fatal error. Aborting...");
            System.exit(1);
        }
    }

    public void setState(ServerState state) {
        if (this.state != null) {
            this.state = null; // for garbage collection
        }
        this.state = state;
        this.state.setServer(this);
    }

    public void sendResponse(Response response) {
        this.responseQueue.add(response);
    }

    private Server(ServerConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public void setMediator(GameMediator mediator) {
        this.mediator = mediator;
    }

    @Override
    public void notify(Event event) {
        // Socket will delegate the event to the mediator
        // for further processing
        this.mediator.process((Request) event);
    }
}

class ServerConfiguration {
    final int port;
    final String host;
    int maxConnections;

    public ServerConfiguration(int port, int maxConnections, String host) {
        this.port = port;
        this.maxConnections = maxConnections;
        this.host = host;
    }

    public void changeMaxConnections(int maxConnections) {
        this.maxConnections = maxConnections;
    }
}

class IllegalConnectionException extends Exception {
    public IllegalConnectionException(String message) {
        super(message);
    }
}