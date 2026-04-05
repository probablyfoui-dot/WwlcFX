package com.wwlc.screens;

import com.wwlc.Main;
import javafx.animation.*;
import javafx.geometry.Pos;
import javafx.scene.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.Stage;
import javafx.util.Duration;

public class ActCardScreen {

    public static void show(Stage stage, String actTitle, String subtitle, String nextScene, String playerName) {
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: black;");
        root.setPrefSize(Main.W, Main.H);

        Text actText = new Text(actTitle);
        actText.setFont(Font.font("Georgia", FontWeight.BOLD, 64));
        actText.setFill(Color.web("#ff88cc"));
        actText.setOpacity(0);

        Text subText = new Text(subtitle);
        subText.setFont(Font.font("Georgia", FontPosture.ITALIC, 28));
        subText.setFill(Color.web("#ccaacc"));
        subText.setOpacity(0);

        VBox box = new VBox(20, actText, subText);
        box.setAlignment(Pos.CENTER);
        root.getChildren().add(box);

        Scene scene = new Scene(root, Main.W, Main.H);
        scene.setFill(Color.BLACK);

        boolean wasFS = stage.isFullScreen();
        stage.setScene(scene);
        if (wasFS) { stage.setFullScreen(true); stage.setFullScreenExitHint(""); }

        // Fade in → hold → fade out → go to next scene
        FadeTransition fadeIn = new FadeTransition(Duration.seconds(1.5), box);
        fadeIn.setFromValue(0); fadeIn.setToValue(1);

        PauseTransition hold = new PauseTransition(Duration.seconds(2.5));

        FadeTransition fadeOut = new FadeTransition(Duration.seconds(1.2), box);
        fadeOut.setFromValue(1); fadeOut.setToValue(0);

        SequentialTransition seq = new SequentialTransition(fadeIn, hold, fadeOut);
        seq.setOnFinished(e -> {
            GameScreen.show(stage, nextScene, playerName);
            if (wasFS) { stage.setFullScreen(true); stage.setFullScreenExitHint(""); }
        });

        // Click/key to skip
        Runnable skip = () -> { seq.stop(); GameScreen.show(stage, nextScene, playerName); if (wasFS) { stage.setFullScreen(true); stage.setFullScreenExitHint(""); } };
        scene.setOnMouseClicked(ev -> skip.run());
        scene.setOnKeyPressed(ev -> skip.run());

        seq.play();
    }
}
