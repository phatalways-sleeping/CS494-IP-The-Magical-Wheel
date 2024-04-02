package com.example.the_magic_wheel.protocols.response;

import java.util.Iterator;
import java.util.List;

public class FinalScoreResponse extends Response {

    public FinalScoreResponse(String finalScoreStr, String requestedAt) {
        // final StringBuilder content = new StringBuilder("FINAL_SCORE ");
        // scores.forEach((username, score) -> {
        //     content.append(username).append(" ").append(score).append(" ");
        // });
        // content.trimToSize();
        super("FINAL_SCORE " + finalScoreStr, requestedAt);
    }

    @Override
    public Iterator<String> iterator() {
        final List<String> list = List.of(content.split(" "));
        list.remove(0); // FINAL_SCORE
        return list.iterator();
    }

}
