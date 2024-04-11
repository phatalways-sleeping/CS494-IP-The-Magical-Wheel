package com.example.the_magic_wheel.server.utils;

import com.example.the_magic_wheel.protocols.request.CloseConnectionRequest;
import com.example.the_magic_wheel.protocols.request.GuessRequest;
import com.example.the_magic_wheel.protocols.request.RegisterRequest;
import com.example.the_magic_wheel.protocols.request.Request;
import com.example.the_magic_wheel.protocols.response.GameEndResponse;
import com.example.the_magic_wheel.protocols.response.GameStartResponse;
import com.example.the_magic_wheel.protocols.response.RegisterFailureResponse;
import com.example.the_magic_wheel.protocols.response.RegisterSuccessResponse;
import com.example.the_magic_wheel.protocols.response.Response;
import com.example.the_magic_wheel.protocols.response.ResultNotificationResponse;
import com.example.the_magic_wheel.server.sockets.Component;
import com.example.the_magic_wheel.server.sockets.GameMediator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameController extends Component {
    private List<String> playerList;
    private List<String> disqualifiedList;
    private Map<String, Integer> scores;
    private Map<String, Integer> guessCountMap;
    private Map <String, String> mapIPToUsername;
    private int currentPlayerIndex;
    private int turn;
    private GameMediator mediator;
    private Keyword keyword;
    private StringBuffer currentKeyword;
    private int maxConnections;
    private boolean isEndGame;
    // currentPlayerIndex != -1 => game is running
    // else waiting for enough players to join

    public GameController(GameMediator mediator) {
        this.mediator = mediator;
        this.playerList = new ArrayList<>();
        this.disqualifiedList = new ArrayList<>();
        this.currentPlayerIndex = -1;
        this.turn = 1;
        this.maxConnections = 3;
        this.scores = new HashMap<>();
        this.guessCountMap = new HashMap<>();
        this.isEndGame = false;
        this.mapIPToUsername = new HashMap<>();
    }

    public String addPlayer(String username, RegisterRequest registerRequest) {
        if (isEndGame == true)
        {
            return "Game is ended, waiting for admin to start a new game";
        }
        while (playerList.size() > maxConnections) {
            String userName = playerList.get(playerList.size() - 1);
            if (scores.containsKey(userName))
                scores.remove(username);
            if (disqualifiedList.contains(userName))
                disqualifiedList.remove(userName);
            if (guessCountMap.containsKey(userName))
                guessCountMap.remove(userName);
            // if (mapIPToUsername.containsKey(userName))
            //     mapIPToUsername.remove(userName); to do : remove the key from mapIPToUsername (may be never happer  :))) )
            playerList.remove(playerList.size() - 1);
        }
        if (playerList.size() == maxConnections) {
            currentPlayerIndex = 0;
            return "Room is full";
        }
        if (scores.containsKey(username)) {
            return "Username already exists!";
        }
        playerList.add(username);
        scores.put(username, 0);
        mapIPToUsername.put(registerRequest.getSource(), username);
        if (playerList.size() == maxConnections)
            currentPlayerIndex = 0;
        return "Register success";
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
            if (isEndGame)
            {
                return getGameEndResponse(request, "Game is ended, waiting for admin to start a new game");
            }
            GuessRequest guessRequest = (GuessRequest) request;
            increaseTurnAndGuessCount(guessRequest);
            if (turn > 3 && guessRequest.getGuessWord() != null && guessRequest.getGuessWord().length() > 0) {
                if (guessRequest.getGuessWord().equals(keyword.getKeyword())) {
                    return getKeyWordSucessful(guessRequest, guessRequest.getUsername());
                }
                if (canNotGuessWord()) {
                    return getGameEndResponse(guessRequest,
                            "End game because each active player is already guess more than 3 times");
                }
                return getKeyWordUnsucessful(guessRequest, guessRequest.getUsername());
            }
            if(checkTimeout(guessRequest))
            {
                return getTimeout(guessRequest, guessRequest.getUsername());
            }
            if (isGuessCharactorSucessful(guessRequest)) {
                if (currentKeyword.toString().equals(keyword.getKeyword())) {
                    return getKeyWordSucessful(guessRequest, guessRequest.getUsername());
                }
                if (canNotGuessWord()) {
                    return getGameEndResponse(guessRequest,
                            "End game because each active player is already guess more than 3 times");
                }
                return guessCharactorSucessful(guessRequest, guessRequest.getUsername());
            }
            if (canNotGuessWord()) {
                return getGameEndResponse(guessRequest,
                        "End game because each active player is already guess more than 3 times");
            }
            return guessCharactorUnsucessful(guessRequest, guessRequest.getUsername());

        }

        else if (request instanceof RegisterRequest) {
            RegisterRequest registerRequest = (RegisterRequest) request;
            String responeString = addPlayer(registerRequest.getUsername(), registerRequest);
            if (responeString == "Register success") {
                if (playerList.size() == maxConnections && currentPlayerIndex == 0) {
                    return getGameStartResponse(registerRequest);
                }
                return getRegisterSuccessResponse(registerRequest);
            } else {
                return getRegisterFailureResponse(registerRequest, responeString);
            }
        }

        // from here: CloseConnectionRequest
        CloseConnectionRequest closeConnectionRequest = (CloseConnectionRequest) request;
        if (currentPlayerIndex == -1) {
            // print all players list

            String closedConnectionPlayer = mapIPToUsername.get(closeConnectionRequest.getSource());
            System.err.println("Player that close connection: " + closedConnectionPlayer);

            if (playerList.contains( closedConnectionPlayer)) {
                playerList.remove(closedConnectionPlayer);
            }
            if (scores.containsKey(closedConnectionPlayer)) {
                scores.remove(closedConnectionPlayer);
            }
            if (disqualifiedList.contains(closedConnectionPlayer)) {
                disqualifiedList.remove(closedConnectionPlayer);
            }
            if (guessCountMap.containsKey(closedConnectionPlayer)) {
                guessCountMap.remove(closedConnectionPlayer);
            }

            return null;
        }
        return getGameEndResponse(request, "Some players close connection");
    }

    private ResultNotificationResponse getTimeout(GuessRequest request, String currentUser) {
        currentPlayerIndex = (currentPlayerIndex + 1) % playerList.size();
        return ResultNotificationResponse.timeout(currentUser, scores.get(currentUser),
                playerList.get(currentPlayerIndex), (short) turn, request.getRequestedAt(),
                currentKeyword.toString());

    }

    private boolean checkTimeout(GuessRequest guessRequest) {
        if ( (guessRequest.getGuessChar() == null || guessRequest.getGuessChar() == "") && (guessRequest.getGuessWord() == null || guessRequest.getGuessWord() == "") ){
            return true;
        }
        return false;
    }

    private void increaseTurnAndGuessCount(GuessRequest guessRequest) {
        turn++;
        if (!guessCountMap.containsKey(guessRequest.getUsername())) {
            guessCountMap.put(guessRequest.getUsername(), 1);
        } else {
            guessCountMap.put(guessRequest.getUsername(), guessCountMap.get(guessRequest.getUsername()) + 1);
        }
    }

    private boolean canNotGuessWord() {
        for (int i = 0; i < playerList.size(); i++) {
            if (guessCountMap.containsKey(playerList.get(i)) == false ||   guessCountMap.get(playerList.get(i)) < 3) {
                return false;
            }
        }
        return true;
    }

    private GameEndResponse getGameEndResponse(Request request, String reason) {
        Map<String, Integer> copyscore = copyScore(scores);
        this.playerList = new ArrayList<>();
        this.disqualifiedList = new ArrayList<>();
        this.currentPlayerIndex = -1;
        this.turn = 0;
        this.scores = new HashMap<>();
        isEndGame = true;
        return new GameEndResponse(reason, copyscore, request.getRequestedAt());
    }

    private ResultNotificationResponse guessCharactorUnsucessful(GuessRequest guessRequest, String currentUser) {
        char character = guessRequest.getGuessChar().charAt(0);
        currentPlayerIndex = (currentPlayerIndex + 1) % playerList.size();
        return ResultNotificationResponse.unsuccessfulGuessChar(character, currentUser, scores.get(currentUser),
                playerList.get(currentPlayerIndex), (short) turn, guessRequest.getRequestedAt(),
                currentKeyword.toString());
    }

    private ResultNotificationResponse guessCharactorSucessful(GuessRequest guessRequest, String currentUser) {
        scores.put(currentUser, scores.get(currentUser) + 1);
        return ResultNotificationResponse.successfulGuessChar(currentUser, scores.get(currentUser),
                (short) turn, guessRequest.getRequestedAt(), currentKeyword.toString());
    }

    private boolean isGuessCharactorSucessful(GuessRequest guessRequest) {

        boolean isCorrect = false;
        if (guessRequest.getGuessChar().length() == 0) {
            return isCorrect;
        }
        char character = guessRequest.getGuessChar().charAt(0);

        for (int idx = 0; idx < keyword.getKeyword().length(); idx++) {
            if (keyword.getKeyword().charAt(idx) == character && currentKeyword.toString().charAt(idx) == '*'){
                isCorrect = true;
                currentKeyword.replace(idx, idx+1, Character.toString(character));
            }
        }
        System.err.println("In game controller,isGuessCharactorSucessful,  current keyword is " + currentKeyword.toString());
        return isCorrect;
    }

    private Response getKeyWordUnsucessful(GuessRequest guessRequest, String currentUser) {
        disqualifiedList.add(currentUser);
        playerList.remove(currentUser);
        currentPlayerIndex %= playerList.size();
        if (playerList.size() == 0) {
            return getGameEndResponse(guessRequest, "All players are disqualified.");
        }
        return ResultNotificationResponse.unsuccessfulGuessWord(currentUser, scores.get(currentUser),
                playerList.get(currentPlayerIndex), (short) turn, guessRequest.getRequestedAt(),
                currentKeyword.toString());
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

    public void setMaxConnections(int maxConnections2) {
        this.maxConnections = maxConnections2;
    }

}
