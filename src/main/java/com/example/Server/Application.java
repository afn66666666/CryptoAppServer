package com.example.Server;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.*;
import java.net.ServerSocket;

public class Application extends javafx.application.Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Application.class.getResource("start-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 600, 300);
        stage.setTitle("CryptoServer");
        stage.setScene(scene);
        stage.show();
        Runnable task = () -> {
            try {
                Server.launch();
            }
            catch(Exception e){
                MainViewController.log(e.getMessage());
            }

        };
        var connection = new Thread(task);
        connection.start();
    }

    public static void main(String[] args) {
        launch();
    }
}