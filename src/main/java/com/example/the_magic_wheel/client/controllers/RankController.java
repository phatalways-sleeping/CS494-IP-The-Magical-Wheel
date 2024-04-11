package com.example.the_magic_wheel.client.controllers;

import java.io.IOException;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.example.the_magic_wheel.Configuration;
import com.example.the_magic_wheel.client.views.App;
import com.example.the_magic_wheel.protocols.request.CloseConnectionRequest;
import com.example.the_magic_wheel.protocols.response.GameEndResponse;
import com.example.the_magic_wheel.protocols.response.Response;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class RankController extends Controller {

    public RankController(App app) {
        super(app);
    }

    @FXML
    private Button exitButton;

    @FXML
    private Button playAgainButton;

    @FXML
    private VBox rankingVBox;

    @FXML
    private Text reasonText;

    @FXML
    private TextFlow reasonTextFlow;

    @FXML
    void closeGame(ActionEvent event) {
        app.getClient().sendRequest(new CloseConnectionRequest(null));
        app.getScenesManager().exitGame();
    }

    @FXML
    void playAgain(ActionEvent event) throws IOException {
        app.getScenesManager().resetController();
        app.getScenesManager().switchScene(Configuration.CLIENT_REGISTER_FXML);
    }

    @Override
    public void handleResponse(Response response) {
        if (response instanceof GameEndResponse) {
            Map<String, Integer> finalScores = ((GameEndResponse)response).getFinalScores();
            String reason = ((GameEndResponse)response).getReason();
            initializeRanking(finalScores);
            initializeReason(reason);
        } else {
            System.out.println("RankController: undefined response");
        }
    }

    private void initializeRanking(Map<String, Integer> scores) {
        rankingVBox.getChildren().clear();

        // Sort the scores map by value in descending order
        Map<String, Integer> sortedScores = scores.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

        // Iterate over the sorted scores map to create ranking items
        int rank = 1;
        for (Map.Entry<String, Integer> entry : sortedScores.entrySet()) {
            String nickname = entry.getKey();
            int score = entry.getValue();

            // Create the ranking item and add it to the rankingVBox
            HBox rankingItem = createItemRanking(rank++, nickname, score);
            rankingVBox.getChildren().add(rankingItem);
        }

        // Set the width of the VBox
        rankingVBox.setPrefWidth(300);
    }


    private HBox createItemRanking(int rank, String nickname, int score) {
        HBox itemHBox = new HBox(10);
        itemHBox.setAlignment(Pos.CENTER_LEFT);
        itemHBox.setStyle("-fx-background-color: #176B87; -fx-border-color: #000000; -fx-background-radius: 15; -fx-border-radius: 15; -fx-padding: 5; -fx-pref-height: 40px;");

        // Create Labels for ranking number, name, and score
        Label rankLabel = new Label("No." + String.valueOf(rank));
        rankLabel.setStyle("-fx-text-fill: #dafffb; -fx-font-size: 16.0; -fx-font-family: 'DejaVu Sans'; -fx-padding: 0 0 0 5px;");

        Label nameLabel = new Label(nickname);
        nameLabel.setStyle("-fx-text-fill: #dafffb; -fx-font-size: 16.0; -fx-font-family: 'DejaVu Sans';");

        Label scoreLabel = new Label(String.valueOf(score));
        scoreLabel.setStyle("-fx-text-fill: #dafffb; -fx-font-size: 16.0; -fx-font-family: 'DejaVu Sans'; -fx-padding: 0 5px 0 0;");

        Region spacer1 = new Region();
        HBox.setHgrow(spacer1, Priority.ALWAYS);

        Region spacer2 = new Region();
        HBox.setHgrow(spacer2, Priority.ALWAYS);

        // Add Labels to the HBox
        itemHBox.getChildren().addAll(rankLabel, spacer1, nameLabel, spacer2, scoreLabel);

        return itemHBox;
    }

    private void initializeReason(String reason) {
        reasonText.setText(reason);
        reasonText.setStyle("-fx-font-family: 'DejaVu Sans';");
        reasonTextFlow.getChildren().clear();
        reasonTextFlow.getChildren().addAll(reasonText);
    }
}
