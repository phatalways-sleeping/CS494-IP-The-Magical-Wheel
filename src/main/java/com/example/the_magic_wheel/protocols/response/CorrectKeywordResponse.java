package com.example.the_magic_wheel.protocols.response;

import java.util.Iterator;
import java.util.List;

public class CorrectKeywordResponse extends Response {

    public CorrectKeywordResponse(String keyword, String requestedAt) {
        super("CORRECT_KEYWORD " + keyword + " Congratulations! You guessed the whole word correctly.", requestedAt);
    }

    @Override
    public Iterator<String> iterator() {
        final List<String> list = List.of(content.split(" "));
        list.remove(0); // CORRECT_KEYWORD
        // Merge the rest of the list into one string except the first element
        final List<String> extracted = list.subList(1, list.size());
        final String congratulationMsg = String.join(" ", extracted);
        final List<String> result = List.of(congratulationMsg);
        result.add(0, list.get(0));
        return result.iterator();
    }

}
