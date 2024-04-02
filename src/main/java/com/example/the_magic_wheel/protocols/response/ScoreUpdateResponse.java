package com.example.the_magic_wheel.protocols.response;

import java.util.Iterator;
import java.util.List;

public class ScoreUpdateResponse extends Response {

    public ScoreUpdateResponse(String username, int score, String requestedAt) {
        super("SCORE_UPDATE " + username + " " + score, requestedAt);
    }

    @Override
    public Iterator<String> iterator() {
        return List.of(content.split(" ")).subList(1, 3).iterator();
    }

}
