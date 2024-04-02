package com.example.the_magic_wheel.sockets.Server;

import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

import java.nio.channels.SocketChannel;
import com.example.the_magic_wheel.protocols.response.Response;

public class GameEndState extends ServerState {
    @Override
    public void handle(Selector selector, SelectionKey key) throws Exception {
        try {
            this.listenForCloseRequest(key);
            // Keep sending the game end response to the clients
            while (!this.server.getResponses().isEmpty()) {
                final Response response = this.server.getResponses().poll();
                for (SocketChannel socketChannel : this.server.getClients().values()) {
                    this.sendBackResponse(socketChannel, response);
                }
            }
            // Close all the connections
            for (SocketChannel socketChannel : this.server.getClients().values()) {
                socketChannel.close();
            }
        } catch (Exception e) {
            this.server.recoverFromConnectionFailure((SocketChannel) key.channel());
        }
    }

}
