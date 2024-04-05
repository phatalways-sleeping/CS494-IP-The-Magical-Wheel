package com.example.the_magic_wheel.sockets.Server;

import java.util.Map;
import java.util.Iterator;

import java.nio.channels.SocketChannel;

import com.example.the_magic_wheel.protocols.request.Request;
import com.example.the_magic_wheel.protocols.response.Response;

public interface GameMediator {
    public Response process(Request request, SocketChannel channel);

    public Map<Integer, String> getPlayers();

    public Iterator<SocketChannel> getClients();

    public void addPlayer(String username);

    public void notifyConnectionLost(SocketChannel channel) throws Exception;
}