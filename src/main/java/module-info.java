module com.example.demo {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.gson;


    opens com.example.Server to javafx.fxml, com.google.gson;
    exports com.example.Server;
    exports com.example.Server.T;
    opens com.example.Server.T to com.google.gson, javafx.fxml;
}