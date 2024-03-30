package com.example.the_magic_wheel.sockets;

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
import java.util.concurrent.atomic.AtomicBoolean;

import com.example.the_magic_wheel.Configuration;
import com.example.the_magic_wheel.protocols.Request;
import com.example.the_magic_wheel.protocols.Response;

public class Server {
    private final int port;
    private final String host;

    private int connections;
    private final int maxConnections;

    private AtomicBoolean isServingGame;

    private static Map<String, Server> servers = new TreeMap<>();

    public boolean isServingGame() {
        return isServingGame.get();
    }

    public static Server spawn(String host, int port, int maxConnections) {
        String key = host + ":" + port;
        if (servers.containsKey(key)) {
            return servers.get(key);
        }
        Server server = new Server(host, port, maxConnections);
        servers.put(key, server);
        return server;
    }

    public static void stop(String host, int port) {
        String key = host + ":" + port;
        if (servers.containsKey(key)) {
            servers.remove(key);
        }
    }

    private Server(String host, int port, int maxConnections) {
        this.maxConnections = maxConnections;
        this.port = port;
        this.host = host;
        this.connections = 0;
        this.isServingGame = new AtomicBoolean(false);
    }

    public void start() throws Exception {
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
                if (key.isAcceptable() && !isServingGame.get()) {
                    // Accept the connection
                    // Register the new connection with the selector
                    // Make the connection non-blocking
                    // Add the new connection to the list of connections
                    try {
                        final SocketChannel socketChannel = tryToSpawnNewConnection(selector, key);
                        // Send the success message to the client
                        socketChannel.write(ByteBuffer.wrap(ackResponse().toBytes()));
                        System.out.println("Connection established with " + socketChannel.getRemoteAddress());
                        if (connections == maxConnections) {
                            this.isServingGame.set(true);
                            System.out.println("Maximum number of connections reached");
                        }
                    } catch (IllegalConnectionException e) {
                        // Send the error message to the client
                        continue;
                    }
                }
                if (key.isReadable()) {
                    // Deserialize the message into Request object
                    // Extract the data then pass it to the ServerApp
                    try {
                        final Request request = tryToParseRequest(key);
                        System.out.println("Request received: " + request);
                        switch (request.getHeader().getContentType()) {
                            case CLOSE:
                                // Close the connection
                                decreaseConnections();
                                System.out
                                        .println("Connection closed - Number of connections remaining: " + connections);
                                break;
                            default:
                                break;
                        }
                    } catch (IOException | ClassNotFoundException e) {
                        // Send the error message to the client
                        continue;
                    }
                }
            }
        }
    }

    private Request tryToParseRequest(SelectionKey key) throws IOException, ClassNotFoundException {
        SocketChannel socketChannel = (SocketChannel) key.channel();
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
        return (Request) Request.deserialize(bytes);
    }

    private SocketChannel tryToSpawnNewConnection(Selector selector, SelectionKey key)
            throws IllegalConnectionException, IOException, ClosedChannelException {
        increaseConnections();
        ServerSocketChannel serverSocket = (ServerSocketChannel) key.channel();
        SocketChannel socketChannel = serverSocket.accept();
        socketChannel.configureBlocking(false);
        socketChannel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
        System.out.println("Connection accepted from " + socketChannel.getRemoteAddress());
        System.out.println("Number of connections: " + connections);
        return socketChannel;
    }

    private Response ackResponse() {
        return new Response.Builder().status(Response.Status.OK).serverName("Server")
                .contentType(Response.ContentType.ACK)
                .build();
    }

    void increaseConnections() throws IllegalConnectionException {
        if (connections >= maxConnections) {
            throw new IllegalConnectionException("Number of connections exceeds the limit");
        }
        connections++;
    }

    void decreaseConnections() throws IllegalConnectionException {
        connections--;

        if (connections < 0 || connections < Configuration.MIN_CONNECTIONS) {
            this.isServingGame.set(false);
            String errMsg = connections < 0 ? "Number of connections is below the limit"
                    : connections < Configuration.MIN_CONNECTIONS ? "Number of connections is below the limit" : null;
            throw new IllegalConnectionException(errMsg);
        }
    }
}

class IllegalConnectionException extends Exception {
    public IllegalConnectionException(String message) {
        super(message);
    }
}