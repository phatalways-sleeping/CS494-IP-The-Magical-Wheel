package com.example.the_magic_wheel.protocols.response;

import java.util.Iterator;
import java.util.List;

public class WrongKeywordResponse extends Response {

    public WrongKeywordResponse(String nextTurnUser, String requestedAt) {
        super("WRONG_KEYWORD DISABLE_PLAYER You guessed the whole word incorrectly. NEXT_TURN " + nextTurnUser,
                requestedAt);
    }

    @Override
    public Iterator<String> iterator() {
        final List<String> list = List.of(content.split(" "));
        list.remove(0); // WRONG_KEYWORD
        list.remove(0); // DISABLE_PLAYER
        final String nextTurnPlayer = list.remove(list.size() - 1);
        list.remove(list.size() - 1); // NEXT_TURN
        final String message = String.join(" ", list);
        return List.of(message, nextTurnPlayer).iterator();
    }

}
