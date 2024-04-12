package com.example.the_magic_wheel.server.sockets;

import com.example.the_magic_wheel.protocols.request.Request;
import com.example.the_magic_wheel.protocols.response.Response;

import java.nio.channels.SocketChannel;
import java.util.Iterator;

public interface GameMediator {
    public Response process(Request request, SocketChannel channel) throws Exception;

    public Iterator<SocketChannel> getClients();

    //public void clearAllConnections() throws IOException;

    public Response notifyConnectionLost(SocketChannel channel) throws Exception;
  // mediator call the getKeyWordString function from DatabaseController, then
    // return a string with format: keywork#hint
    public String getKeyWordString();
}