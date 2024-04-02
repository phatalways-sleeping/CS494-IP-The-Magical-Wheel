package com.example.the_magic_wheel.protocols.response;

import java.util.Iterator;
import java.util.List;

public class GameEndResponse extends Response {

    public GameEndResponse(String reason, String requestedAt) {
        super("GAME_END " + reason, requestedAt);
    }

    @Override
    public Iterator<String> iterator() {
        return List.of(content.split(" ")).subList(1, 2).iterator();
    }

}
