package com.example.the_magic_wheel.sockets.Server.manager;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Objects;
import java.util.Iterator;

import com.example.the_magic_wheel.Configuration;
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
        this.mediator = Objects.requireNonNull(mediator);
    }

    @Override
    public void run() {
        try {
            for (int i = 0; i < Configuration.RETRY_ATTEMPTS; i++) {
                try {
                    this.handle();
                    break;
                } catch (Exception e) {
                    System.err.println("RequestHandler: Failed to handle the request: " + request.toString());
                    // Check if this channel is still open
                    if (!this.channel.isOpen()) {
                        System.err.println("RequestHandler: Channel is closed");
                        break;
                    }
                    // Random sleep time to avoid busy waiting from 0 to Configuration.RETRY_SLEEP *
                    // (i + 1)
                    final int sleepTime = (int) (Math.random() * (Configuration.RETRY_INTERVAL * (i + 1)));
                    System.err.println("RequestHandler: Retrying in " + sleepTime + "ms");
                    Thread.sleep(sleepTime);
                }
            }
        } catch (InterruptedException e) {
            System.err.println("RequestHandler: Error when sleeping - " + e.getMessage());
            System.err.println("RequestHandler: Dropping the request - " + request.toString());
        } finally {
            // Release the resources
            this.mediator = null;
            this.channel = null;
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

    @Override
    public String getSource() {
        return this.request.getSource();
    }
}
