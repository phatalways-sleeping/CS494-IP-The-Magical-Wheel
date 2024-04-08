package com.example.the_magic_wheel.controllers;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TreeMap;
import java.util.stream.Collectors;

import com.example.the_magic_wheel.App;
import com.example.the_magic_wheel.Client;
import com.example.the_magic_wheel.Configuration;
import com.example.the_magic_wheel.protocols.request.GuessRequest;
import com.example.the_magic_wheel.protocols.request.RegisterRequest;
import com.example.the_magic_wheel.protocols.response.GameEndResponse;
import com.example.the_magic_wheel.protocols.response.GameStartResponse;
import com.example.the_magic_wheel.protocols.response.RegisterFailureResponse;
import com.example.the_magic_wheel.protocols.response.RegisterSuccessResponse;
import com.example.the_magic_wheel.protocols.response.Response;
import com.example.the_magic_wheel.protocols.response.ResultNotificationResponse;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class GameController extends Controller  {

    public GameController(App app) {
        super(app);
    }

    public static final Character FREE_CHARACTER = '_';

    private Map<String, Integer> playerScores;
    
    private boolean isPlaying = false;

    private boolean isDisqualified = false;

    private String nickname;
    private int numberOfPlayers;

    @FXML
    private Label errorGuessLabel;
    

    @FXML
    private Label errorKeywordLabel;

    @FXML
    private TextField guessTextField;

    @FXML
    private Text hintText;

    @FXML
    private Text keywordText;

    @FXML
    private TextField keywordTextField;

    @FXML
    private VBox leaderBoardVBox;

    @FXML
    private Text notificationTextField;

    @FXML
    private Button submitButton;

    @FXML
    private Text turnText;

    @Override
    public void handleResponse(Response response) {
        if (response instanceof GameEndResponse) {
            handleGameEndResponse((GameEndResponse) response);
        } else if (response instanceof GameStartResponse) {
            handleGameStartResponse((GameStartResponse) response);
        } else if (response instanceof ResultNotificationResponse) {
            handleResultNotificationResponse((ResultNotificationResponse) response);
        } else {
            System.out.println("GameController: undefined response");
        }
    }

    
    public void initializeGame(String hint, int wordLength, Map<Integer, String> players) {

        hintText.setText(hint);
        keywordText.setText(" * ".repeat(wordLength));
        updateTurn((short) 1);

        // Initialize the playerScoresMap based on the players in the GameStartResponse
        playerScores = new HashMap<>();

        for (Map.Entry<Integer, String> entry : players.entrySet()) {
            String username = entry.getValue();
            playerScores.put(username, 0);
        }

        leaderBoardVBox.getChildren().clear();
        setLeaderboard();

        // Check if client is the first person to make guess
        if (!nickname.equals(players.get(1))) {
            setDisableSubmitButton();
        }
        else {
            setEnableSubmitButton();
        }
    }      

    @SuppressWarnings("exports")
    public HBox createLeaderboardItem(String username, String score) {
        HBox itemHBox = new HBox(10);
        itemHBox.setStyle("-fx-padding: 5px 0;");
        Label label1 = new Label(username);
        label1.setStyle("-fx-text-fill: #dafffb; -fx-font-size: 14;");
        Label label2 = new Label(score);
        label2.setStyle("-fx-text-fill: #dafffb; -fx-font-size: 14;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        // Add labels to the itemHBox
        itemHBox.getChildren().addAll(label1, spacer, label2);
        return itemHBox;
    }


    public void setLeaderboard() {
        leaderBoardVBox.getChildren().clear();
        leaderBoardVBox.setStyle("-fx-padding: 0 10px;");

        // Iterate over the entries of the playerScores map
        for (Map.Entry<String, Integer> entry : playerScores.entrySet()) {
            String username = entry.getKey();
            String score = String.valueOf(entry.getValue());

            HBox userItem = createLeaderboardItem(username, score);
            leaderBoardVBox.getChildren().add(userItem);
        }
    }

    public void sortPlayersScore() {
        Map<String, Integer> sortedMap = playerScores.entrySet()
        .stream()
        .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                (oldValue, newValue) -> oldValue, LinkedHashMap::new));

        // Assign the sorted map back to playerScoresMap
        playerScores = sortedMap;
    }

    public void updatePlayerScore(String nickname, Integer score) {
        playerScores.put(nickname, score);
    }

    public void updateLeaderboard(String nickname, Integer score) {
        updatePlayerScore(nickname, score);
        sortPlayersScore();
        leaderBoardVBox.getChildren().clear();
        setLeaderboard();
    }

    public void setEnableSubmitButton() {
        submitButton.setDisable(false);
    }

    public void setDisableSubmitButton() {
        submitButton.setDisable(true);
    }

    public void setDisqualified() {
        setDisableSubmitButton();
    }

    public void updateTurn(Short n) {
        turnText.setText(String.valueOf(n));
    }

    public void updateNotification(String notification) {
        notificationTextField.setText(notification);
    }

    public void setNickname(String nname) {
        nickname = nname;
    }

    void initialize() {
        guessTextField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                Platform.runLater(() -> clearErrorLabel(errorGuessLabel));
            }
        });
    
        keywordTextField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                Platform.runLater(() -> clearErrorLabel(errorKeywordLabel));
            }
        });
    }

    @FXML
    void submitAnswer(ActionEvent event) throws IOException {
        String guessChar = guessTextField.getText();
        String guessKeyword = keywordTextField.getText();
        
        boolean isValidGuessChar = validateGuessChar(guessChar);
        boolean isValidGuessKeyword = validateGuessKeyword(guessKeyword);
        
        if (isValidGuessChar && isValidGuessKeyword) {
            app.getClient().sendRequest(new GuessRequest(nickname, guessChar, guessKeyword));
        } else {
            if (!isValidGuessChar) {
                setErrorLabel(errorGuessLabel, "Invalid input! Guess should be a single alphabetic character");
            }
            if (!isValidGuessKeyword) {
                setErrorLabel(errorKeywordLabel, "Invalid input! Keyword should contain only alphabetic characters.");
            }
        }

    }

    private void handleGameEndResponse(GameEndResponse response) {
        app.getScenesManager().switchScene(Configuration.CLIENT_RANKING_FXML);
        RankController rankController = (RankController) app.getScenesManager().getCurrentController();
        rankController.handleResponse(response);
    }

    private void handleGameStartResponse(GameStartResponse response) {
        if (isPlaying) {
            System.out.println("Duplicate GameStartResponse");
            return;
        }
        
        isPlaying = true;

        String hint = response.getHints();
        int wordLength = response.getWordLength();
        Map<Integer, String> players = response.getPlayers();

        initializeGame(hint, wordLength, players);
    }
   
    private void handleResultNotificationResponse(ResultNotificationResponse response) {
        
        String username = response.getUsername();
        int updatedScore = response.getUpdatedScore();


        boolean successful = response.isSuccessful();
        boolean guessChar = response.guessChar();
        boolean guessWord = response.guessWord();
        String explanation = response.getExplanation();
        String nextPlayer = response.getNextPlayer();
        short turn = response.getTurn();

        updateLeaderboard(response.getUsername(), response.getUpdatedScore());

        // updateKeyword();

        updateTurn(response.getTurn());

        if (isDisqualified) {
            return;
        }

        // Client is the person guessing forward turn
        if (nickname.equals(response.getUsername())) {
            updateNotification(response.getExplanation());
            if (response.guessWord() && !response.isSuccessful()) {
                isDisqualified = true;
            }
        }

        if (nickname.equals(response.getNextPlayer())) {
            setEnableSubmitButton();
        }
        else {
            setDisableSubmitButton();
        }

        // Debug
        System.out.println("Result Notification:");
        System.out.println("Nicknameeeeeeee: " + nickname);
        System.out.println("Username: " + username);
        System.out.println("Updated Score: " + updatedScore);
        System.out.println("Successful: " + successful);
        System.out.println("Guess Type: " + (guessChar ? "Character" : "Word"));
        System.out.println("Explanation: " + explanation);
        System.out.println("Next Player: " + nextPlayer);
        System.out.println("Current Turn: " + turn);

    }
    
    private boolean validateGuessChar(String guess) {
        return guess != null && !guess.isEmpty() && guess.length() == 1 && guess.matches("[a-zA-Z]");
    }

    private boolean validateGuessKeyword(String guess) {
        return guess != null && !guess.isEmpty() && guess.matches("[a-zA-Z]+");
    }
    
    private void setErrorLabel(Label label, String message) {
        label.setText(message);
        label.setStyle("-fx-text-fill: red;");
    }

    private void clearErrorLabel(Label label) {
        label.setText(null);
    }
}
