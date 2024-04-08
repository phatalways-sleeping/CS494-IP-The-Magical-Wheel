package com.example.the_magic_wheel;

public class Configuration {
    private Configuration() {
    }

    public static final String APP_NAME = "The Magical Wheel";

    public static final int WIDTH = 900;

    public static final int HEIGHT = 600;


    public static final String CLIENT_GREET_FXML = "greet";

    public static final String CLIENT_REGISTER_FXML = "register";

    public static final String CLIENT_HALL_FXML = "client_hall";

    public static final String CLIENT_GAME_FXML = "game";

    public static final String CLIENT_RANKING_FXML = "rank";

    public static final String SERVER_HALL_FXML = "server_hall";

    public static final String SERVER_END_GAME_FXML = "server_endgame";

    public static final String HOST = "localhost";
    public static final int PORT = 4000;
    public static final int BUFFER_SIZE = 2048;

    public static final int TIMEOUT = 5000; // milliseconds
    public static final int RETRY_ATTEMPTS = 3;
    public static final int RETRY_INTERVAL = 3000; // milliseconds

    public static final int MAX_CONNECTIONS = 10;
    public static final int MIN_CONNECTIONS = 2;
}
