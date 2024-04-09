package com.example.the_magic_wheel.sockets.Server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
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
import java.util.Objects;
import java.util.TreeMap;
import java.util.Set;
import java.util.HashSet;

import com.example.the_magic_wheel.Configuration;
import com.example.the_magic_wheel.protocols.request.Request;
import com.example.the_magic_wheel.protocols.response.Response;
import com.example.the_magic_wheel.sockets.Server.defense.DoSAttackException;
import com.example.the_magic_wheel.sockets.Server.manager.ExecutionManager;
import com.example.the_magic_wheel.sockets.Server.manager.RequestHandler;
import com.example.the_magic_wheel.sockets.Server.manager.ServerExecutor;

public class Server extends Component implements Runnable {
    private final ServerConfiguration configuration;
    private final ExecutionManager executionManager = new ServerExecutor(5);
    // The key is stored as the address of the client
    private final Map<String, SocketChannel> clients = new TreeMap<>();

    // The black list is stored as the address of the client
    private final Set<String> blackList = new HashSet<>();

    // The servers are stored in a map to ensure that there is only one server for
    // each host and port
    // The key is stored in the format host:port
    private static Map<String, Server> servers = new TreeMap<>();

    Map<String, SocketChannel> getClients() {
        return clients;
    }

    public static Server spawn(@SuppressWarnings("exports") ServerConfiguration configuration) {
        if (servers.containsKey(configuration.host + ":" + configuration.port)) {
            return servers.get(configuration.host + ":" + configuration.port);
        }
        Server server = new Server(configuration);
        servers.put(configuration.host + ":" + configuration.port, server);
        return server;
    }

    public static void stop(@SuppressWarnings("exports") ServerConfiguration configuration) {
        final String key = configuration.host + ":" + configuration.port;
        if (servers.containsKey(key)) {
            servers.remove(key);
        }
    }

    @Override
    public void run() {
        try {
            // Setting up the server
            final Selector selector = startUpServer();
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
                            if (blackList.contains(socketChannel.getRemoteAddress().toString())) {
                                System.out.println("Server: Connection from " + socketChannel.getRemoteAddress()
                                        + " has been blacklisted");
                                socketChannel.close();
                                System.out
                                        .println("Server: Connection closed with " + socketChannel.getRemoteAddress());
                            } else {
                                System.out.println(
                                        "Server: Spawned new connection from " + socketChannel.getRemoteAddress());
                            }
                        }
                        if (key.isReadable()) {
                            // Deserialize the request
                            SocketChannel socketChannel = (SocketChannel) key.channel();
                            final Request request = deserializeRequest(socketChannel);
                            // Logging the request
                            executionManager.execute(new RequestHandler(request, socketChannel, mediator));
                        }
                    } catch (DoSAttackException e) {
                        handleDoSAttack(e);
                    } catch (IOException | ClassNotFoundException e) {
                        recoverFromConnectionFailure((SocketChannel) key.channel());
                    }

                }
            }
        } catch (UnknownHostException e) {
            System.err.println("Server: Unknown host - " + e.getMessage());
        } catch (ClosedChannelException e) {
            System.err.println("Server: Closed channel - " + e.getMessage());
        } catch (IOException e) {
            System.err.println("Server: IO error - " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Server: Fatal error - " + e.getMessage());
        } finally {
            cleanUpServer();
        }
    }

    private void cleanUpServer() {
        System.out.println("Server: Server stopped");
        System.out.println("Server: Cleaning up...");
        servers.remove(configuration.host + ":" + configuration.port);
        System.out.println("Server: Server cleaned up");
        executionManager.shutdown();
        System.out.println("Server: Execution manager shut down");
    }

    private void handleDoSAttack(DoSAttackException e) throws IOException {
        final String source = e.getSource();
        System.err.println("Server: DoS attack detected - " + source);
        System.err.println("Server: Closing the connection...");
        final SocketChannel socketChannel = clients.get(source);
        if (socketChannel != null) {
            final String address = socketChannel.getRemoteAddress().toString();
            blackList.add(address);
            System.err.println("Server: " + address + " has been blacklisted");
            socketChannel.close();
            clients.remove(address);
            System.err.println("Server: Connection closed with " + address);
            return;
        }
        System.err.println("Server: Connection not found");
    }

    private Selector startUpServer() throws UnknownHostException, IOException, ClosedChannelException {
        InetAddress hostIpAddress = InetAddress.getByName(configuration.host);
        Selector selector = Selector.open();
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.socket().bind(new InetSocketAddress(hostIpAddress, configuration.port));
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        // Logging the server start
        System.out.println("Server: Server started at " + configuration.host + ":" + configuration.port);
        return selector;
    }

    // The idea is to recover from the connection failure
    // such as:
    // - The client disconnects unexpectedly from the server during the game
    // - The client loses the connection with the server when in waiting for players
    // state
    // - ...
    private void recoverFromConnectionFailure(SocketChannel socketChannel) {
        // Handle exception to ensure the server
        // still runs at a valid state after connection failure
        try {
            System.out.println("Server: Connection lost with " + socketChannel.getRemoteAddress());
            System.out.println("Server: Notifying the mediator...");
            final Response response = mediator.notifyConnectionLost(socketChannel);
            if (Objects.nonNull(response)) {
                System.out.println("Server: Sending the response back to the client...");
                if (Objects.nonNull(response.getDestination())) {
                    // Broadcast the response to all clients
                    for (SocketChannel client : clients.values()) {
                        client.write(ByteBuffer.wrap(response.toBytes()));
                    }
                } else {
                    // Send the response to the specific client
                    clients.get(response.getDestination()).write(ByteBuffer.wrap(response.toBytes()));
                }
            }
            System.out.println("Server: The mediator has been notified and recovered from the connection failure");
        } catch (Exception e) {
            // Aborting the server
            e.printStackTrace();
            System.err.println("Server experienced a fatal error. Aborting...");
            System.exit(1);
        }
    }

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

    public ServerConfiguration(int port, String host) {
        this.port = port;
        this.host = host;
    }
}

class IllegalConnectionException extends Exception {
    public IllegalConnectionException(String message) {
        super(message);
    }
}