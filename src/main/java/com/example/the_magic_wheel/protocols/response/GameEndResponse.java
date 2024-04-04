package com.example.the_magic_wheel.protocols.response;

import java.util.Map;
import java.util.Objects;

public class GameEndResponse extends Response {
    private final Map<String, Integer> scores;
    private final String reason;

    public GameEndResponse(String reason, Map<String, Integer> scores, String requestedAt) {
        super(requestedAt);
        this.reason = Objects.requireNonNull(reason);
        this.scores = Objects.requireNonNull(scores);
    }

    public String getReason() {
        return reason;
    }

    public Map<String, Integer> getFinalScores() {
        return scores;
    }
}
