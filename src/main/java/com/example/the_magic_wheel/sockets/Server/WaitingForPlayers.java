package com.example.the_magic_wheel.sockets.Server;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import com.example.the_magic_wheel.protocols.request.CloseConnectionRequest;
import com.example.the_magic_wheel.protocols.request.RegisterRequest;
import com.example.the_magic_wheel.protocols.request.Request;
import com.example.the_magic_wheel.protocols.response.RegisterFailureResponse;
import com.example.the_magic_wheel.protocols.response.RegisterSuccessResponse;
import com.example.the_magic_wheel.protocols.response.Response;

/// WaitingForConnectionState is a concrete class that extends ServerState
/// Responsible for:
/// 1) Listening for incoming requests for connection
/// 2) Accepting the connection and send back an acknowledgment
/// 3) Listening for incoming requests for closing the connection
/// 4) Closing the connection
public class WaitingForPlayers extends ServerState {
    @Override
    public void handle(Selector selector, SelectionKey key) throws Exception {
        try {
            if (key.isAcceptable()) {
                if (!this.server.isServingGame()) {
                    final SocketChannel socketChannel = tryToSpawnNewConnection(selector, key);
                    System.out.println("Spawned new connection from " + socketChannel.getRemoteAddress());
                } else {
                    System.out.println("Server is full and not accepting new connections");
                }
            }
            if (key.isReadable()) {
                SocketChannel socketChannel = (SocketChannel) key.channel();
                final Request request = deserializeRequest(socketChannel);
                System.out.println("Request received from " + socketChannel.getRemoteAddress());
                System.out.println("Request: " + request.getContent());
                if (request instanceof RegisterRequest) {
                    // The remote address has been registered before
                    if (this.server.getClients().containsKey(socketChannel.getRemoteAddress().toString())) {
                        this.sendBackResponse(socketChannel,
                                new RegisterFailureResponse("Remote address has been registered",
                                        request.getRequestedAt()));
                        return;
                    }
                    final String username = request.iterator().next();
                    // If the username has been registered before
                    final Response response;
                    if (this.server.getPlayers().values().contains(username)) {
                        response = new RegisterFailureResponse("Username has been registered",
                                request.getRequestedAt());
                    } else if (this.server.getPlayers().size() == this.server.getMaxConnections()) {
                        response = new RegisterFailureResponse("Server is full", request.getRequestedAt());
                    } else if (!this.server.appropriateUsername(username)) {
                        response = new RegisterFailureResponse("Username is inappropriate", request.getRequestedAt());
                    } else {
                        this.server.getClients().put(socketChannel.getRemoteAddress().toString(), socketChannel);
                        final int order = this.server.getClients().size();
                        this.server.getPlayers().put(order, username + "@" + socketChannel.getRemoteAddress());
                        response = new RegisterSuccessResponse(username, order,
                                request.getRequestedAt());
                    }
                    this.sendBackResponse(socketChannel, response);
                } else if (request instanceof CloseConnectionRequest) {
                    this.server.getClients().remove(socketChannel.getRemoteAddress().toString());
                    // Remove the player from the players list
                    for (int i = 1; i <= this.server.getPlayers().size(); i++) {
                        final String username = request.iterator().next();
                        if (this.server.getPlayers().get(i).split("@")[0].contains(username)) {
                            this.server.getPlayers().remove(i);
                            break;
                        }
                    }
                    socketChannel.close();
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            this.server.recoverFromConnectionFailure((SocketChannel) key.channel());
        }
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
