package com.example.the_magic_wheel.protocols.response;

import java.util.Iterator;
import java.util.List;

public class CorrectGuessResponse extends Response {

    public CorrectGuessResponse(String word, String nextTurnUser, String requestedAt) {
        super("CORRECT_GUESS " + word + " " + " NEXT_TURN " + nextTurnUser, requestedAt);
    }

    @Override
    public Iterator<String> iterator() {
        final List<String> list = List.of(content.split(" "));
        list.remove(0); // CORRECT_GUESS
        list.remove(1); // NEXT_TURN
        return list.iterator();
    }

}
