package com.wwlc;

import javafx.application.Application;
import javafx.stage.Stage;
import com.wwlc.screens.MainMenuScreen;

public class Main extends Application {

    public static final int W = 1280;
    public static final int H = 720;

    public static Stage primaryStage;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        stage.setTitle("Worm Worm Literature Club");
        stage.setResizable(true);   // allow resize / fullscreen
        stage.setWidth(W);
        stage.setHeight(H);
        stage.setMinWidth(800);
        stage.setMinHeight(450);
        // Hide the ugly "Press ESC to exit fullscreen" hint
        stage.setFullScreenExitHint("");
        MainMenuScreen.show(stage);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
