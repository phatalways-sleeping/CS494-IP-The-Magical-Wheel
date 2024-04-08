package com.example.the_magic_wheel.severGameController;

import com.example.the_magic_wheel.protocols.request.GuessRequest;
import com.example.the_magic_wheel.protocols.request.RegisterRequest;
import com.example.the_magic_wheel.protocols.request.Request;
import com.example.the_magic_wheel.protocols.response.GameEndResponse;
import com.example.the_magic_wheel.protocols.response.GameStartResponse;
import com.example.the_magic_wheel.protocols.response.RegisterFailureResponse;
import com.example.the_magic_wheel.protocols.response.RegisterSuccessResponse;
import com.example.the_magic_wheel.protocols.response.Response;
import com.example.the_magic_wheel.protocols.response.ResultNotificationResponse;
import com.example.the_magic_wheel.sockets.Server.GameMediator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameController {
    private List<String> playerList;
    private List<String> disqualifiedList;
    private Map<String, Integer> scores;
    private int currentPlayerIndex;
    private int turn;
    private GameMediator mediator;
    private Keyword keyword;
    private StringBuffer currentKeyword;
    private final int maxConnections;

    // currentPlayerIndex != -1 => game is running
    // else waiting for enough players to join

    public GameController(GameMediator mediator, int maxConnections) {
        this.mediator = mediator;
        this.playerList = new ArrayList<>();
        this.disqualifiedList = new ArrayList<>();
        this.currentPlayerIndex = -1;
        this.turn = 1;
        this.maxConnections = maxConnections;
        this.scores = new HashMap<>();
    }

    public String addPlayer(String username) {

        while (playerList.size() > maxConnections) {
            playerList.remove(playerList.size() - 1);
        }
        if (playerList.size() == maxConnections) {
            currentPlayerIndex = 0;
            return "room is full";
        }
        if (scores.containsKey(username)) {
            return "username already exists!";
        }
        playerList.add(username);
        scores.put(username, 0);
        return "register success";
    }

    

    // deep copy for the hash map before clear all state
    private Map<String, Integer> copyScore(Map<String, Integer> originalMap) {
        Map<String, Integer> newMap = new HashMap<>();
        for (int i = 0; i < playerList.size(); i++) {
            String playerName = playerList.get(i);
            newMap.put(playerName, originalMap.get(playerName));
        }
        for (int i = 0; i < disqualifiedList.size(); i++) {
            String playerName = disqualifiedList.get(i);
            newMap.put(playerName, originalMap.get(playerName));
        }
        return newMap;
    }

    public synchronized Response process(Request request) {
        if (request instanceof GuessRequest) {
            GuessRequest guessRequest = (GuessRequest) request;
            if (playerList.get(playerList.size() - 1) == guessRequest.getUsername()) {
                turn++;
            }
            if (turn > 2 && guessRequest.getGuessWord() != null && guessRequest.getGuessWord().length() > 0) {
                if (guessRequest.getGuessWord().equals(keyword.getKeyword())) {
                    return getKeyWordSucessful(guessRequest, guessRequest.getUsername());
                }
                return getKeyWordUnsucessful(guessRequest, guessRequest.getUsername());
            }

            if (isGuessCharactorSucessful(guessRequest)) {
                return guessCharactorSucessful(guessRequest, guessRequest.getUsername());
            }
            return guessCharactorUnsucessful(guessRequest, guessRequest.getUsername());

        }

        else if (request instanceof RegisterRequest) {
            RegisterRequest registerRequest = (RegisterRequest) request;
            String responeString = addPlayer(registerRequest.getUsername());
            if (responeString == "register success") {
                if (playerList.size() == maxConnections && currentPlayerIndex == 0) {
                    return getGameStartResponse(registerRequest);
                }
                return getRegisterSuccessResponse(registerRequest);
            } else {
                return getRegisterFailureResponse(registerRequest, responeString);
            }
        }
        return new GameEndResponse("Some players close connection", scores, request.getRequestedAt());
    }

    private GameEndResponse getGameEndResponse(GuessRequest guessRequest, String reason) {
        Map<String, Integer> copyscore = copyScore(scores);
        this.playerList = new ArrayList<>();
        this.disqualifiedList = new ArrayList<>();
        this.currentPlayerIndex = -1;
        this.turn = 0;
        this.scores = new HashMap<>();
        return new GameEndResponse(reason, copyscore, guessRequest.getRequestedAt());
    }

    private ResultNotificationResponse guessCharactorUnsucessful(GuessRequest guessRequest, String currentUser) {
        char character = guessRequest.getGuessChar().charAt(0);
        currentPlayerIndex = (currentPlayerIndex + 1) % playerList.size();
        return ResultNotificationResponse.unsuccessfulGuessChar(character, currentUser, scores.get(currentUser),
            playerList.get(currentPlayerIndex), (short) turn, guessRequest.getRequestedAt());
    }

    private ResultNotificationResponse guessCharactorSucessful(GuessRequest guessRequest, String currentUser) {
        scores.put(currentUser, scores.get(currentUser) + 1);
        return ResultNotificationResponse.successfulGuessChar(currentUser, scores.get(currentUser),
                (short) turn, guessRequest.getRequestedAt());
    }

    private boolean isGuessCharactorSucessful(GuessRequest guessRequest) {
        char character = guessRequest.getGuessChar().charAt(0);
        boolean isCorrect = false;

        for (int idx = 0; idx < keyword.getKeyword().length(); idx++) {
            if (keyword.getKeyword().charAt(idx) == character) {
                isCorrect = true;
                currentKeyword.replace(idx, idx, Character.toString(character));
            }
        }
        return isCorrect;
    }

    private ResultNotificationResponse getKeyWordUnsucessful(GuessRequest guessRequest, String currentUser) {
        disqualifiedList.add(currentUser);
        playerList.remove(currentUser);
        currentPlayerIndex %= playerList.size();
        return ResultNotificationResponse.unsuccessfulGuessWord(currentUser, scores.get(currentUser),
                playerList.get(currentPlayerIndex), (short) turn, guessRequest.getRequestedAt());
    }

    private GameEndResponse getKeyWordSucessful(GuessRequest guessRequest, String currentUser) {
        int newScore = scores.get(currentUser) + 5;
        scores.put(currentUser, newScore);
        String reason = "Congratulations to the winner " + currentUser + " with the correct keyword is "
                + keyword.getKeyword();
        return getGameEndResponse(guessRequest, reason);
    }

    private RegisterFailureResponse getRegisterFailureResponse(RegisterRequest registerRequest, String reason) {
        RegisterFailureResponse registerFailureResponse = new RegisterFailureResponse(reason,
                registerRequest.getRequestedAt());
        registerFailureResponse.setSource(registerRequest.getDestination());
        registerFailureResponse.setDestination(registerRequest.getSource());
        return registerFailureResponse;
    }

    private RegisterSuccessResponse getRegisterSuccessResponse(RegisterRequest registerRequest) {
        RegisterSuccessResponse registerSuccessResponse = new RegisterSuccessResponse(
                registerRequest.getUsername(), playerList.size() - 1, registerRequest.getRequestedAt());
        registerSuccessResponse.setSource(registerRequest.getDestination());
        registerSuccessResponse.setDestination(registerRequest.getSource());
        return registerSuccessResponse;
    }

    private GameStartResponse getGameStartResponse(RegisterRequest registerRequest) {
        String keywordAndHint = mediator.getKeyWordString();
        for (int i = 0; i < keywordAndHint.length(); i++) {
            if (keywordAndHint.charAt(i) == '#') {
                keyword = new Keyword(keywordAndHint.substring(0, i), keywordAndHint.substring(i + 1));
                break;
            }
        }
        currentKeyword = new StringBuffer("");
        for (int idx = 0; idx < keyword.getKeyword().length(); idx++) {
            currentKeyword.append("*");
        }
        Map<Integer, String> players = new HashMap<>();
        for (int i = 0; i < playerList.size(); i++) {
            players.put(i, playerList.get(i));
        }
        return new GameStartResponse(players, keyword.getHint(), keyword.getKeyword().length(),
                registerRequest.getRequestedAt());
    }

}
