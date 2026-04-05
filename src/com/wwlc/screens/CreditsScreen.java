package com.wwlc.screens;

import com.wwlc.Main;
import com.wwlc.system.LangManager;
import javafx.animation.*;
import javafx.geometry.Pos;
import javafx.scene.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.*;
import javafx.stage.Stage;
import javafx.util.Duration;

public class CreditsScreen {

    private static final String[][] CREDITS = {
        {"~ WORM WORM LITERATURE CLUB ~", "title"},
        {"Praise be to Scatha.", "subtitle"},
        {"", "spacer"},
        {"Story & Writing", "header"},
        {"Clément Escudier", "name"},
        {"", "spacer"},
        {"Programming", "header"},
        {"Clément Escudier", "name"},
        {"", "spacer"},
        {"Art & Character Design", "header"},
        {"Clément Escudier", "name"},
        {"", "spacer"},
        {"Music", "header"},
        {"Original Soundtrack", "name"},
        {"", "spacer"},
        {"Special Thanks", "header"},
        {"Scatha, the Great Worm", "name"},
        {"The Hypixel Skyblock Community", "name"},
        {"Everyone who stayed.", "name"},
        {"", "spacer"},
        {"", "spacer"},
        {"Thank you for playing.", "subtitle"},
    };

    public static void show(Stage stage) {
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: black;");
        root.setPrefSize(Main.W, Main.H);

        // Scrolling credits VBox
        VBox credits = new VBox(12);
        credits.setAlignment(Pos.CENTER);
        credits.setPrefWidth(Main.W);

        // Top spacer so text starts below screen
        credits.getChildren().add(spacer(Main.H));

        for (String[] entry : CREDITS) {
            String text = entry[0];
            String type = entry[1];
            if (type.equals("spacer")) {
                credits.getChildren().add(spacer(32));
                continue;
            }
            Text t = new Text(text);
            switch (type) {
                case "title"    -> { t.setFont(Font.font("Georgia", FontWeight.BOLD, 42)); t.setFill(Color.web("#ff88cc")); }
                case "subtitle" -> { t.setFont(Font.font("Georgia", FontPosture.ITALIC, 24)); t.setFill(Color.web("#ccaacc")); }
                case "header"   -> { t.setFont(Font.font("Georgia", FontWeight.BOLD, 20)); t.setFill(Color.web("#ff88cc")); }
                case "name"     -> { t.setFont(Font.font("Georgia", 18)); t.setFill(Color.WHITE); }
            }
            t.setTextAlignment(TextAlignment.CENTER);
            credits.getChildren().add(t);
        }

        // Bottom spacer
        credits.getChildren().add(spacer(Main.H));

        // Scroll animation
        double totalHeight = CREDITS.length * 34.0 + Main.H * 2;
        TranslateTransition scroll = new TranslateTransition(Duration.seconds(totalHeight / 40.0), credits);
        scroll.setFromY(0);
        scroll.setToY(-totalHeight);
        scroll.setInterpolator(Interpolator.LINEAR);

        root.getChildren().add(credits);
        StackPane.setAlignment(credits, Pos.TOP_CENTER);

        // Skip hint
        Text skip = new Text(LangManager.isFrench() ? "Appuyez sur Entrée pour passer" : "Press Enter to skip");
        skip.setFont(Font.font("Georgia", 16));
        skip.setFill(Color.web("#888888"));
        StackPane.setAlignment(skip, Pos.BOTTOM_CENTER);
        root.getChildren().add(skip);
        StackPane.setMargin(skip, new javafx.geometry.Insets(0, 0, 24, 0));

        Scene scene = new Scene(root, Main.W, Main.H);
        scene.setFill(Color.BLACK);

        Runnable goToMenu = () -> {
            scroll.stop();
            boolean wasFS = stage.isFullScreen();
            MainMenuScreen.show(stage);
            if (wasFS) { stage.setFullScreen(true); stage.setFullScreenExitHint(""); }
        };

        scene.setOnKeyPressed(e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.ENTER ||
                e.getCode() == javafx.scene.input.KeyCode.SPACE ||
                e.getCode() == javafx.scene.input.KeyCode.ESCAPE) goToMenu.run();
        });
        scene.setOnMouseClicked(e -> goToMenu.run());

        scroll.setOnFinished(e -> goToMenu.run());

        boolean wasFS = stage.isFullScreen();
        stage.setScene(scene);
        if (wasFS) { stage.setFullScreen(true); stage.setFullScreenExitHint(""); }
        root.requestFocus();
        scroll.play();
    }

    private static Region spacer(double h) {
        Region r = new Region();
        r.setPrefHeight(h);
        return r;
    }
}
