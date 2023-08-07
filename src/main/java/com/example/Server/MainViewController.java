package com.example.Server;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;

import java.net.ServerSocket;
import java.net.URL;
import java.util.ResourceBundle;

public class MainViewController implements Initializable {

    @FXML
    private TextArea logArea;
    @FXML
    private static TextArea stLogArea;


    public static void log(String msg){
        if(stLogArea == null) {
            System.out.println("cant log. logger is null!");
            stLogArea.appendText(msg);
            stLogArea.appendText("\n");
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        stLogArea = logArea;
        try {
        }catch (Exception e){
            log(e.getMessage());
        }
    }
}