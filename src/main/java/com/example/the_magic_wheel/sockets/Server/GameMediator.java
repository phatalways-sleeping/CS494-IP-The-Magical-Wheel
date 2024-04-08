package com.example.the_magic_wheel.sockets.Server;

import java.util.Map;

import com.example.the_magic_wheel.protocols.request.Request;
import com.example.the_magic_wheel.protocols.response.ResultNotificationResponse;

public interface GameMediator {
    public void process(Request request);
    public Map<Integer, String> getPlayers();
    public void addPlayer(String username);
    // mediator call the getKeyWordString function from DatabaseController, then
    // return a string with format: keywork#hint
    public String getKeyWordString();    
}