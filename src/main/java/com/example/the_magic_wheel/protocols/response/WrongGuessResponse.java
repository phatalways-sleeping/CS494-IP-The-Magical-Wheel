package com.example.the_magic_wheel.protocols.response;

import java.util.Iterator;
import java.util.List;

public class WrongGuessResponse extends Response {

    public WrongGuessResponse(String guessedChar, String nextTurnUser, String requestedAt) {
        super("WRONG_GUESS Character " + guessedChar + " is not in the keyword. NEXT_TURN "
                + nextTurnUser, requestedAt);
    }

    @Override
    public Iterator<String> iterator() {
        final List<String> list = List.of(content.split(" "));
        list.remove(0); // WRONG_GUESS
        final String nextTurnPlayer = list.remove(list.size() - 1);
        list.remove(list.size() - 1); // NEXT_TURN
        final String message = String.join(" ", list);
        return List.of(message, nextTurnPlayer).iterator();
    }

}
