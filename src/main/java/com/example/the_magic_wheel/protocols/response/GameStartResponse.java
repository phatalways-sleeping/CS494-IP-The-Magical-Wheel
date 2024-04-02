package com.example.the_magic_wheel.protocols.response;

import java.util.Iterator;
import java.util.List;

public class GameStartResponse extends Response {

    public GameStartResponse(String playersAndOrders, String requestedAt) {

        super("GAME_START " + playersAndOrders, requestedAt);
    }

    @Override
    public Iterator<String> iterator() {
        final List<String> list = List.of(content.split(" "));
        list.remove(0); // GAME_START
        return list.iterator();
    }

}
