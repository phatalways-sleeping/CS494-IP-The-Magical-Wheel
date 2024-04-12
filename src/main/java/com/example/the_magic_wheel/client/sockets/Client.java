package com.example.the_magic_wheel.client.sockets;

import com.example.the_magic_wheel.Configuration;
import com.example.the_magic_wheel.protocols.request.CloseConnectionRequest;
import com.example.the_magic_wheel.protocols.request.Request;
import com.example.the_magic_wheel.protocols.response.Response;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;

public class Client implements Runnable {
    private final String host;
    private final int port;
    private Selector selector;
    private SocketChannel channel;

    private final BlockingQueue<Request> requests;
    private final BlockingQueue<Response> responses;

    public Client(String host, int port, BlockingQueue<Request> requests, BlockingQueue<Response> responses) {
        this.host = host;
        this.port = port;
        this.requests = requests;
        this.responses = responses;
    }

    // To send request to the remote server through the socket channel
    // call the sendRequest method with the request object as the parameter
    // The request object will be wrapped with the source and destination
    // before sending it to the server
    public void sendRequest(Request request) {
        requests.add(wrapWithHeader(request));
    }

    private Request wrapWithHeader(Request request) {
        request.setSource(channel.socket().getLocalSocketAddress().toString());
        request.setDestination(channel.socket().getRemoteSocketAddress().toString());
        return request;
    }

    public Response receiveResponse() {
        return responses.poll();
    }

    private boolean listenToServer(Selector selector) throws IOException, ClassNotFoundException {
        Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
        while (keys.hasNext()) {
            SelectionKey key = keys.next();
            keys.remove();
            if (key.isConnectable()) {
                SocketChannel client = (SocketChannel) key.channel();
                try {
                    while (client.isConnectionPending()) {
                        client.finishConnect();
                    }
                } catch (IOException e) {
                    key.cancel();
                    System.out.println("Connection failed");
                    e.printStackTrace();
                    return false;
                }
            }
            if (key.isReadable()) {
                SocketChannel client = (SocketChannel) key.channel();
                ByteBuffer buffer = ByteBuffer.allocate(Configuration.BUFFER_SIZE);
                int bytesRead = client.read(buffer);
                List<Byte> data = new ArrayList<>();
                while (bytesRead > 0) {
                    buffer.flip();
                    while (buffer.hasRemaining()) {
                        data.add(buffer.get());
                    }
                    buffer.clear();
                    bytesRead = client.read(buffer);
                }
                byte[] bytes = new byte[data.size()];
                for (int i = 0; i < data.size(); i++) {
                    bytes[i] = data.get(i);
                }
                final Response response = Response.fromBytes(bytes);
                responses.add(response);

                // Notify the main thread that a response has been received
                System.out.println("Response received: " + response.toString());
            }
        }
        return true;
    }

    @Override
    public void run() {
        try {
            InetAddress address = Inet4Address.getByName(host);
            InetSocketAddress socketAddress = new InetSocketAddress(address, port);
            this.selector = Selector.open();
            this.channel = SocketChannel.open();
            this.channel.configureBlocking(false);
            this.channel.connect(socketAddress);
            int operations = SelectionKey.OP_CONNECT | SelectionKey.OP_READ | SelectionKey.OP_WRITE;
            this.channel.register(this.selector, operations);
            while (!Thread.currentThread().isInterrupted()) {
                Request request = requests.poll();
                if (Objects.nonNull(request) && this.channel.isConnected()) {
                    this.channel.write(ByteBuffer.wrap(request.toBytes()));
                    System.out.println("Request sent: " + request.toString());
                }
                if (selector.select() > 0) {
                    boolean connected = listenToServer(selector);
                    // System.out.println("listen to server");
                    if (!connected) {
                        break;
                    }
                }
            }
            // Send a close connection request to the server
            Request closeConnection = new CloseConnectionRequest("Alice");
            this.channel.write(ByteBuffer.wrap(closeConnection.toBytes()));
            System.out.println("Close connection request sent");
            this.channel.close();
            this.selector.close();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Connection failed. Server has been closed");
            // Interrupt the main thread
            Thread.currentThread().interrupt();
        }
    }
}
