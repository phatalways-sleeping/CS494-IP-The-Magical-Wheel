package com.example.the_magic_wheel.protocols.response;

import java.util.Iterator;
import java.util.List;

public class GameLoadingResponse extends Response {

    public GameLoadingResponse(int length, String hint, String nextTurnUserString, String requestedAt) {
        super("GAME_LOADING " + length + " " + hint + " NEXT_TURN " + nextTurnUserString, requestedAt);
    }

    @Override
    public Iterator<String> iterator() {
        final List<String> list = List.of(content.split(" "));
        list.remove(0); // GAME_LOADING
        final String length = list.remove(0);
        final String nextTurnPlayer = list.remove(list.size() - 1);
        list.remove(list.size() - 1); // NEXT_TURN
        final String hint = String.join(" ", list);
        return List.of(length, hint, nextTurnPlayer).iterator();
    }

}
