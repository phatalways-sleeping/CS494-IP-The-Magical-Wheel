package com.example.the_magic_wheel.sockets.Server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

import com.example.the_magic_wheel.Configuration;
import com.example.the_magic_wheel.protocols.request.CloseConnectionRequest;
import com.example.the_magic_wheel.protocols.request.Request;
import com.example.the_magic_wheel.protocols.response.Response;

public abstract class ServerState {
    protected Server server;

    void setServer(Server server) {
        this.server = server;
    }

    protected Request deserializeRequest(SocketChannel socketChannel) throws IOException, ClassNotFoundException {
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

    Request listenForCloseRequest(SelectionKey key) throws IOException, ClassNotFoundException {
        if (key.isReadable()) {
            SocketChannel socketChannel = (SocketChannel) key.channel();
            final Request request = deserializeRequest(socketChannel);
            System.out.println("Request received from " + socketChannel.getRemoteAddress());
            if (request instanceof CloseConnectionRequest) {
                this.server.getClients().remove(socketChannel.getRemoteAddress().toString());
                // Remove the player from the players list
                for (int i = 1; i <= this.server.getPlayers().size(); i++) {
                    if (this.server.getPlayers().get(i).equals(request.iterator().next())) {
                        this.server.getPlayers().remove(i);
                        break;
                    }
                }
                socketChannel.close();
                // Notify the server application
                this.server.notifyServerApp();
            }
            return request;
        }
        return null;
    }

    protected void sendBackResponse(SocketChannel socketChannel, Response response) throws IOException {
        socketChannel.write(ByteBuffer.wrap(response.toBytes()));
    }

    public abstract void handle(Selector selector, SelectionKey key) throws Exception;
}
