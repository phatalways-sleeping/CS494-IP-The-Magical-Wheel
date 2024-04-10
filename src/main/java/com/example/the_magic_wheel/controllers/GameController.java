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

public class GameController extends Controller {

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
        if (!nickname.equals(players.get(0))) {
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


    private void setLeaderboard() {
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

    private void sortPlayersScore() {
        Map<String, Integer> sortedMap = playerScores.entrySet()
        .stream()
        .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                (oldValue, newValue) -> oldValue, LinkedHashMap::new));

        // Assign the sorted map back to playerScoresMap
        playerScores = sortedMap;
    }

    private void updatePlayerScore(String nickname, Integer score) {
        playerScores.put(nickname, score);
    }

    private void updateLeaderboard(String nickname, Integer score) {
        updatePlayerScore(nickname, score);
        sortPlayersScore();
        leaderBoardVBox.getChildren().clear();
        setLeaderboard();
    }

    private void setEnableSubmitButton() {
        submitButton.setDisable(false);
    }

    private void setDisableSubmitButton() {
        submitButton.setDisable(true);
    }

    private void updateTurn(Short n) {
        turnText.setText(String.valueOf(n));
    }

    private void updateNotification(String notification) {
        notificationTextField.setText(notification);
    }

    public void setNickname(String nname) {
        nickname = nname;
    }

    @FXML
    private void submitAnswer(ActionEvent event) throws IOException {
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
            System.out.println("GameController: duplicate GameStartResponse");
            return;
        }
        
        isPlaying = true;

        String hint = response.getHints();
        int wordLength = response.getWordLength();
        Map<Integer, String> players = response.getPlayers();

        initializeGame(hint, wordLength, players);
    }
   
    private void handleResultNotificationResponse(ResultNotificationResponse response) {
        updateLeaderboard(response.getUsername(), response.getUpdatedScore());
        updateTurn(response.getTurn());
        updateKeyword(response.getCurrentKeyword());
        updateNotification(response.getExplanation());

        if (isDisqualified) {
            return;
        }

        // Client is the person guessing forward turn
        if (nickname.equals(response.getUsername())) {
            if (response.guessWord() && !response.isSuccessful()) {
                isDisqualified = true;
                setDisableSubmitButton();
            }
        } else if (nickname.equals(response.getNextPlayer())) {
            setEnableSubmitButton();
        } else {
            setDisableSubmitButton();
        }
    }
    
    private void updateKeyword(String currentKeyword) {
        String spacedKeyword = currentKeyword.replaceAll("", "  ");
        keywordText.setText(spacedKeyword);
    }


    private boolean validateGuessChar(String guess) {
        return guess != null && !guess.isEmpty() && guess.length() == 1 && guess.matches("[a-zA-Z]");
    }

    private boolean validateGuessKeyword(String guess) {
        return guess != null && guess.matches("[a-zA-Z]+");
    }
    
    private void setErrorLabel(Label label, String message) {
        label.setText(message);
        label.setStyle("-fx-text-fill: red; -fx-font-family: 'DejaVu Sans';");
    }

    private void clearErrorLabel(Label label) {
        label.setText(null);
    }


    @FXML
    public void initialize() {
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
}
