package com.example.the_magic_wheel.sockets;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.example.the_magic_wheel.Configuration;
import com.example.the_magic_wheel.protocols.Request;
import com.example.the_magic_wheel.protocols.Response;

public class Client implements AutoCloseable {
    private final String host;
    private final int port;
    private final Thread worker = new Thread(() -> {
        try {
            runUntilClosed();
        } catch (Exception e) {
            e.printStackTrace();
        }
    });
    private Selector selector;
    private SocketChannel channel;

    private final BlockingQueue<Request> requests = new LinkedBlockingQueue<>();
    private final BlockingQueue<Response> responses = new LinkedBlockingQueue<>();

    public Client(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public Thread getWorker() {
        return worker;
    }

    public void sendRequest(Request request) {
        requests.add(request);
    }

    public void runUntilClosed() throws Exception {
        InetAddress address = Inet4Address.getByName(host);
        InetSocketAddress socketAddress = new InetSocketAddress(address, port);
        this.selector = Selector.open();
        this.channel = SocketChannel.open();
        this.channel.configureBlocking(false);
        this.channel.connect(socketAddress);
        int operations = SelectionKey.OP_CONNECT | SelectionKey.OP_READ | SelectionKey.OP_WRITE;
        this.channel.register(this.selector, operations);

        while (true) {
            // 0. Check if there are any requests to send
            Request request = requests.poll();
            if (Objects.nonNull(request) && this.channel.isConnected()) {
                this.channel.write(ByteBuffer.wrap(request.toBytes()));
                System.out.println("Request sent: " + request);
                continue;
            }

            // 6. Else, listen for incoming data from the server
            if (selector.select() > 0) {
                boolean connected = listenToServer(selector);
                if (!connected) {
                    break;
                }
            }
        }
        this.channel.close();
    }

    @Override
    public void close() {
        if (Objects.isNull(this.channel) || !this.channel.isOpen()) {
            return;
        }
        try {
            // 0. Send a request to the server to close the connection
            Request request = new Request.Builder()
                    .method(Request.Method.WRITE)
                    .clientName(this.host + ":" + this.port)
                    .contentType(Request.ContentType.CLOSE)
                    .content("Goodbye")
                    .build();
            this.sendRequest(request);
            // 1. Stop the worker thread
            this.worker.interrupt();
            // 2. Close the channel
            this.channel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
                Response response = Response.deserialize(bytes);
                responses.add(response);
                // Notify the main thread that a response has been received
                System.out.println("Response received: " + response);
            }
        }
        return true;
    }
}
