package com.example.the_magic_wheel.protocols.response;

import java.util.Map;
import java.util.Objects;

public class GameStartResponse extends Response {
    private final Map<Integer, String> players;

    private final int wordLength;

    private final String hints;

    public GameStartResponse(Map<Integer, String> players, String hints, Integer wordLength, String requestedAt) {
        super(requestedAt);
        this.players = Objects.requireNonNull(players);
        this.wordLength = Objects.requireNonNull(wordLength);
        this.hints = Objects.requireNonNull(hints);
    }

    public String getHints() {
        return hints;
    }

    public int getWordLength() {
        return wordLength;
    }

    public Map<Integer, String> getPlayers() {
        return players;
    }

}
