package com.example.the_magic_wheel.sockets.Server;

import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

import com.example.the_magic_wheel.protocols.request.Request;


public class GamePlayState extends ServerState {
    @Override
    public void handle(Selector selector, SelectionKey key) throws Exception {
        try {
            if (key.isReadable()) {
                // Deserialize the request
                SocketChannel socketChannel = (SocketChannel) key.channel();
                final Request request = deserializeRequest(socketChannel);

                // Logging the request
                System.out.println("Request received from " + socketChannel.getRemoteAddress());
                System.out.println("Request: " + request.toString());

                // Notify the server application to process the request
                this.server.notify(request);
            }
        } catch (Exception e) {
            final SocketChannel socketChannel = (SocketChannel) key.channel();
            this.server.recoverFromConnectionFailure(socketChannel);
        }
    }
}
