package com.example.the_magic_wheel.sockets.Server;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import com.example.the_magic_wheel.protocols.request.Request;

public class WaitingForPlayers extends ServerState {
    @Override
    public void handle(Selector selector, SelectionKey key) throws Exception {
        try {
            if (key.isAcceptable()) {
                final SocketChannel socketChannel = tryToSpawnNewConnection(selector, key);
                System.out.println("Spawned new connection from " + socketChannel.getRemoteAddress());
            }
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
