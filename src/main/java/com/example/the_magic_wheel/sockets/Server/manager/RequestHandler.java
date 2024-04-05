package com.example.the_magic_wheel.sockets.Server.manager;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Objects;
import java.util.Iterator;

import com.example.the_magic_wheel.protocols.request.Request;
import com.example.the_magic_wheel.protocols.response.Response;
import com.example.the_magic_wheel.sockets.Server.GameMediator;

public class RequestHandler implements Handler {
    // The request to be handled
    private final Request request;
    // The channel to send the response, get by calling (SocketChannel)
    // key.channel()
    private SocketChannel channel;
    // The mediator to access the game controller
    private GameMediator mediator;

    public RequestHandler(Request request, SocketChannel channel, GameMediator mediator) {
        this.request = Objects.requireNonNull(request);
        this.channel = Objects.requireNonNull(channel);
        // this.mediator = Objects.requireNonNull(mediator);
        this.mediator = mediator;
    }

    @Override
    public void run() {
        try {
            this.handle();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            this.channel = null; // Release the channel
            this.mediator = null; // Release the mediator
        }
    }

    @Override
    public void handle() throws Exception {
        if (Objects.isNull(mediator)) {
            return;
        }
        final Response response = this.mediator.process(request, channel);
        // Send the response back to the client
        final boolean isBroadcast = response.getDestination() == null;
        if (isBroadcast) {
            // Accessing the players from the mediator
            final Iterator<SocketChannel> clients = this.mediator.getClients();
            while (clients.hasNext()) {
                final SocketChannel client = clients.next();
                if (client.isOpen()) {
                    client.write(ByteBuffer.wrap(response.toBytes()));
                }
            }
        } else if (response.getDestination().equals(this.channel.getRemoteAddress().toString())) {
            this.channel.write(ByteBuffer.wrap(response.toBytes()));
        }
    }

}
