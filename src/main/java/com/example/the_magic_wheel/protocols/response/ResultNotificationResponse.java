package com.example.the_magic_wheel.protocols.response;

import java.util.Objects;

public class ResultNotificationResponse extends Response {
    private final String username;
    private final int updatedScore;
    private final boolean successful;
    // 0. Guess character
    // 1. Guess the whole word
    private final short code;
    private final String explanation;
    private final String nextPlayer;
    private final short turn;

    public ResultNotificationResponse(String username, int score, boolean successful, short code, String explanation,
            String nextPlayer,
            short turn,
            String requestedAt) {
        super(requestedAt);
        this.username = Objects.requireNonNull(username);
        this.updatedScore = Objects.requireNonNull(score);
        this.successful = Objects.requireNonNull(successful);
        this.code = Objects.requireNonNull(code);
        this.nextPlayer = Objects.requireNonNull(nextPlayer);
        this.explanation = Objects.requireNonNull(explanation);
        this.turn = Objects.requireNonNull(turn);
    }

    public static ResultNotificationResponse successfulGuessChar(String username, int score, short nextTurn,
            String requestedAt) {
        final String explanation = new StringBuilder("Correct guess for ").append(username).append(" !").toString();
        return new ResultNotificationResponse(username, score, true, (short) 0, explanation, username, nextTurn,
                requestedAt);
    }

    public static ResultNotificationResponse successfulGuessWord(String username, int score, short nextTurn,
            String requestedAt) {
        final String explanation = new StringBuilder("Congratulations! You guessed the whole word correctly.")
                .toString();
        return new ResultNotificationResponse(username, score, true, (short) 1, explanation, username, nextTurn,
                requestedAt);
    }

    public static ResultNotificationResponse unsuccessfulGuessChar(char guessChar, String username, int score,
            String nextPlayer,
            short nextTurn,
            String requestedAt) {
        final String explanation = new StringBuilder("Character `").append(guessChar).append("` is not in the keyword.")
                .toString();
        return new ResultNotificationResponse(username, score, false, (short) 0, explanation, username, nextTurn,
                requestedAt);
    }

    public static ResultNotificationResponse unsuccessfulGuessWord(String username, int score, String nextPlayer,
            short nextTurn,
            String requestedAt) {
        final String explanation = new StringBuilder("You guessed the whole word incorrectly.")
                .toString();
        return new ResultNotificationResponse(username, score, false, (short) 1, explanation, username, nextTurn,
                requestedAt);
    }

    public String getUsername() {
        return username;
    }

    public int getUpdatedScore() {
        return updatedScore;
    }

    public boolean guessChar() {
        return code == 0;
    }

    public boolean guessWord() {
        return code == 1;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public boolean playerIsDiabled() {
        return code == 1 && !successful;
    }

    public String getExplanation() {
        return explanation;
    }

    public String getNextPlayer() {
        return nextPlayer;
    }

    public short getTurn() {
        return turn;
    }

    public String getCurrentKeyword() {
        return "default";
    }
}
