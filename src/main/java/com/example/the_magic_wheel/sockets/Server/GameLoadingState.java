package com.example.the_magic_wheel.sockets.Server;

import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

import java.util.List;
import java.util.Map;

import com.example.the_magic_wheel.protocols.response.GameLoadingResponse;
import com.example.the_magic_wheel.protocols.response.GameStartResponse;
import com.example.the_magic_wheel.protocols.response.Response;

import java.util.Date;

/// GameLoadingState is a subclass of ServerState
/// Responsible for:
/// 1) Loading the word and hint for the game
/// 2) Notifying the clients that the game is about to start
/// 3) Notifying the clients of the word length and hints
/// 4) If any client loses the connection ro send the request for closing the connection, , notify the server application
public class GameLoadingState extends ServerState {
    @Override
    public void handle(Selector selector, SelectionKey key) throws Exception {
        try {
            this.listenForCloseRequest(key);
            // Notify the clients that the game is about to start
            final StringBuilder content = new StringBuilder("");
            this.server.getPlayers().forEach((order, value) -> {
                final String username = value.split("@")[0];
                content.append(order).append(" ").append(username).append(" ");
            });
            content.trimToSize();
            final Response response = new GameStartResponse(content.toString(), new Date().toString());
            for (Map.Entry<String, SocketChannel> entry : this.server.getClients().entrySet()) {
                final SocketChannel socketChannel = entry.getValue();
                this.sendBackResponse(socketChannel, response);
            }
            // Notify the clients of the word length and hints
            final List<String> topic = this.server.getTopicForGame();
            final String word = topic.get(0);
            final String hint = topic.get(1);
            final Response wordResponse = new GameLoadingResponse(word.length(), hint, this.server.getNextPlayer(),
                    new Date().toString());
            for (Map.Entry<String, SocketChannel> entry : this.server.getClients().entrySet()) {
                final SocketChannel socketChannel = entry.getValue();
                this.sendBackResponse(socketChannel, wordResponse);
            }
        } catch (Exception e) {
            this.server.recoverFromConnectionFailure((SocketChannel) key.channel());
        }
    }

}
