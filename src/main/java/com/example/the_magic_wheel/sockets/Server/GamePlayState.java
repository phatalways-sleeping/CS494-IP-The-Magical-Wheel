package com.example.the_magic_wheel.sockets.Server;

import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Objects;

import com.example.the_magic_wheel.protocols.request.Request;
import com.example.the_magic_wheel.protocols.response.Response;

/// GamePlayState is a concrete class that extends ServerState.
/// Responsible for:
/// 1) Receiving the guesses from the clients and push them to the game controller
/// 2) Notifying the clients of the result of the guess, the current score, and the next player
/// 3) If any client loses the connection ro send the request for closing the connection, , notify the server application
public class GamePlayState extends ServerState {
    @Override
    public void handle(Selector selector, SelectionKey key) throws Exception {
        try {
            final Request request = this.listenForCloseRequest(key);
            if (Objects.nonNull(request)) {
                // Push the guess to the game controller
                this.server.getRequests().add(request);
            }
            // Notify the clients of the result of the guess, the current score, and the
            // next player
            while (!this.server.getResponses().isEmpty()) {
                final Response response = this.server.getResponses().poll();
                for (SocketChannel socketChannel : this.server.getClients().values()) {
                    this.sendBackResponse(socketChannel, response);
                }
            }
        } catch (Exception e) {
            final SocketChannel socketChannel = (SocketChannel) key.channel();
            this.server.recoverFromConnectionFailure(socketChannel);
        }
    }
}
