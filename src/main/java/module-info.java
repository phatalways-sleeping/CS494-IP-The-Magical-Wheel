module com.example.the_magic_wheel {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.example.the_magic_wheel to javafx.fxml;
    exports com.example.the_magic_wheel;
}
