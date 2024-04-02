package com.example.the_magic_wheel.protocols.request;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class GuessRequest extends Request {
    public GuessRequest(String nextChar, String word) {
        super("GUESS " + nextChar + (Objects.isNull(word) ? "" : (" " + word)));
    }

    @Override
    public Iterator<String> iterator() {
        final List<String> list = List.of(content.split(" "));
        final List<String> extracted = list.subList(1, list.size());
        return extracted.iterator();
    }

}
