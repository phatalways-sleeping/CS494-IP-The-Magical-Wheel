package com.example.the_magic_wheel.sockets.Server;

import java.util.Map;

import com.example.the_magic_wheel.protocols.request.Request;

public interface GameMediator {
    public void process(Request request);
    public Map<Integer, String> getPlayers();
    public void addPlayer(String username);
}