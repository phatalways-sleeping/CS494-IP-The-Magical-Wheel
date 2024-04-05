package com.example.the_magic_wheel.sockets.Server;

import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

import java.nio.channels.SocketChannel;

public class GameEndState extends ServerState {
    @Override
    public void handle(Selector selector, SelectionKey key) throws Exception {
        try {
            if (key.isReadable()) {
               // TODO: Implement the logic for handling the end of the game
            }
        } catch (Exception e) {
            this.server.recoverFromConnectionFailure((SocketChannel) key.channel());
        }
    }

}
