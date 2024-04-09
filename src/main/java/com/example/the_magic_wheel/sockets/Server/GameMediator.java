package com.example.the_magic_wheel.sockets.Server;

import java.util.Iterator;
import java.nio.channels.SocketChannel;

import com.example.the_magic_wheel.protocols.request.Request;

import com.example.the_magic_wheel.protocols.response.Response;

public interface GameMediator {
    public Response process(Request request, SocketChannel channel) throws Exception;

    public Iterator<SocketChannel> getClients();

    public Response notifyConnectionLost(SocketChannel channel) throws Exception;
  // mediator call the getKeyWordString function from DatabaseController, then
    // return a string with format: keywork#hint
    public String getKeyWordString();
}