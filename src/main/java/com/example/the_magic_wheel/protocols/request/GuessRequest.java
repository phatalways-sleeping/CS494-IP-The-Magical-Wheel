package com.example.the_magic_wheel.protocols.request;

import java.util.Objects;

public class GuessRequest extends Request {
    private final String guessChar;
    private final String guessWord;
    private final String username;

    public String getGuessWord() {
        return guessWord;
    }

    public String getGuessChar() {
        return guessChar;
    }

    public String getUsername() {
        return username;
    }

    public GuessRequest(String username, String guessChar, String word) {
        super();
        this.guessChar = Objects.requireNonNull(guessChar);
        this.guessWord = Objects.requireNonNull(word);
        this.username = Objects.requireNonNull(username);
    }
}
