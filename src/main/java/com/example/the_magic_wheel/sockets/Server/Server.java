package com.example.the_magic_wheel.sockets.Server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.example.the_magic_wheel.Configuration;
import com.example.the_magic_wheel.protocols.request.Request;
import com.example.the_magic_wheel.sockets.Server.manager.ExecutionManager;
import com.example.the_magic_wheel.sockets.Server.manager.RequestHandler;
import com.example.the_magic_wheel.sockets.Server.manager.ServerExecutor;

public class Server implements Runnable, Component {
    public static void main(String[] args) {
        final ServerConfiguration configuration = new ServerConfiguration(8080, 10, "localhost");
        final Server server = Server.spawn(configuration);
        final Thread worker = new Thread(server);
        worker.start();
    }

    private final ServerConfiguration configuration;
    private final ExecutionManager executionManager = new ServerExecutor(10);
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
        // server.setState(new WaitingForPlayers());
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

    // public BlockingQueue<Response> getResponses() {
    // return responseQueue;
    // }

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
            System.out.println("Server: Server started at " + configuration.host + ":" + configuration.port);

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
                    try {
                        if (key.isAcceptable()) {
                            final SocketChannel socketChannel = tryToSpawnNewConnection(selector, key);
                            System.out
                                    .println("Server: Spawned new connection from " + socketChannel.getRemoteAddress());
                        }
                        if (key.isReadable()) {
                            // Deserialize the request
                            SocketChannel socketChannel = (SocketChannel) key.channel();
                            final Request request = deserializeRequest(socketChannel);

                            // Logging the request
                            System.out.println("Server: Request received from " + socketChannel.getRemoteAddress());
                            System.out.println("Server: Request: " + request.toString());

                            executionManager.execute(new RequestHandler(request, socketChannel, mediator));
                        }
                    } catch (IOException | ClassNotFoundException e) {
                        recoverFromConnectionFailure((SocketChannel) key.channel());
                    }

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.out.println("Server: Server stopped");
            System.out.println("Server: Cleaning up...");
            servers.remove(configuration.host + ":" + configuration.port);
            System.out.println("Server: Server cleaned up");
            executionManager.shutdown();
            System.out.println("Server: Execution manager shut down");
        }
    }

    // The idea is to recover from the connection failure
    // such as:
    // - The client disconnects unexpectedly from the server during the game
    // - The client loses the connection with the server when in waiting for players
    // state
    // - ...
    void recoverFromConnectionFailure(SocketChannel socketChannel) {
        // Handle exception to ensure the server
        // still runs at a valid state after connection failure
        try {
            System.out.println("Server: Connection lost with " + socketChannel.getRemoteAddress());
            System.out.println("Server: Notifying the mediator...");
            mediator.notifyConnectionLost(socketChannel);
            System.out.println("Server: The mediator has been notified and recovered from the connection failure");
        } catch (Exception e) {
            // Aborting the server
            e.printStackTrace();
            System.err.println("Server experienced a fatal error. Aborting...");
            System.exit(1);
        }
    }

    // public void setState(ServerState state) {
    // if (this.state != null) {
    // this.state = null; // for garbage collection
    // }
    // this.state = state;
    // this.state.setServer(this);
    // }

    // public void sendResponse(Response response) {
    // this.responseQueue.add(response);
    // }

    private Server(ServerConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public void setMediator(GameMediator mediator) {
        this.mediator = mediator;
    }

    private Request deserializeRequest(SocketChannel socketChannel) throws IOException, ClassNotFoundException {
        ByteBuffer buffer = ByteBuffer.allocate(Configuration.BUFFER_SIZE);
        int bytesRead = socketChannel.read(buffer);
        List<Byte> data = new ArrayList<>();
        while (bytesRead > 0) {
            buffer.flip();
            while (buffer.hasRemaining()) {
                data.add(buffer.get());
            }
            buffer.clear();
            bytesRead = socketChannel.read(buffer);
        }
        byte[] bytes = new byte[data.size()];
        for (int i = 0; i < data.size(); i++) {
            bytes[i] = data.get(i);
        }
        return (Request) Request.fromBytes(bytes);
    }

    private SocketChannel tryToSpawnNewConnection(Selector selector, SelectionKey key)
            throws IllegalConnectionException, IOException, ClosedChannelException {
        ServerSocketChannel serverSocket = (ServerSocketChannel) key.channel();
        SocketChannel socketChannel = serverSocket.accept();
        socketChannel.configureBlocking(false);
        socketChannel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
        return socketChannel;
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