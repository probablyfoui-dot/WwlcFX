package com.wwlc.screens;

import com.wwlc.Main;
import com.wwlc.system.PlayerState;
import com.wwlc.system.SaveSystem;
import javafx.animation.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.*;
import javafx.util.Duration;
import java.io.InputStream;
import com.wwlc.system.LangManager;

public class MainMenuScreen {

    public static void show(Stage stage) {
        StackPane root = new StackPane();
        root.setPrefSize(Main.W, Main.H);

        // ---- Background: try animated GIF first, then static, then solid ----
        addBackground(root);

        // Semi-transparent overlay so text is readable over any bg
        javafx.scene.shape.Rectangle overlay =
            new javafx.scene.shape.Rectangle(Main.W, Main.H, Color.web("#050510", 0.55));
        root.getChildren().add(overlay);

        // ---- Content ----
        VBox content = new VBox(18);
        content.setAlignment(Pos.CENTER);

        Text title = new Text(LangManager.t("title.game"));
        title.setFont(Font.font("Georgia", FontWeight.BOLD, 58));
        title.setFill(Color.CYAN);
        DropShadow glow = new DropShadow(20, Color.CYAN);
        glow.setSpread(0.3);
        title.setEffect(glow);

        Text subtitle = new Text(LangManager.t("title.sub"));
        subtitle.setFont(Font.font("Georgia", FontPosture.ITALIC, 26));
        subtitle.setFill(Color.web("#aaffaa"));

        Region spacer = new Region(); spacer.setPrefHeight(40);

        Button newGame     = menuButton(LangManager.t("btn.newgame"));
        Button loadGame    = menuButton(LangManager.t("btn.loadgame"));
        Button settings    = menuButton(LangManager.t("btn.settings"));
        Button quit        = menuButton(LangManager.t("btn.quit"));

        newGame.setOnAction(e  -> showNameEntry(stage));
        loadGame.setOnAction(e -> {
            Scene current = stage.getScene();
            boolean fs = stage.isFullScreen(); SaveScreen.show(stage, current, SaveScreen.Mode.LOAD, null, null); if (fs) { stage.setFullScreen(true); stage.setFullScreenExitHint(""); }
        });
        settings.setOnAction(e -> {
            boolean fs = stage.isFullScreen();
            SettingsScreen.show(stage, stage.getScene(), () -> {
                MainMenuScreen.show(stage);
                if (fs) { stage.setFullScreen(true); stage.setFullScreenExitHint(""); }
            });
        });

        quit.setOnAction(e -> stage.close());

        // F11 also toggles fullscreen from main menu
        root.setOnKeyPressed(e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.F11) {
                stage.setFullScreen(!stage.isFullScreen());
                stage.setFullScreenExitHint("");
            }
        });
        root.setFocusTraversable(true);

        content.getChildren().addAll(title, subtitle, spacer,
                                     newGame, loadGame, settings, quit);
        root.getChildren().add(content);

        // Pulse glow
        Timeline pulse = new Timeline(
            new KeyFrame(Duration.ZERO,        new KeyValue(glow.radiusProperty(), 15)),
            new KeyFrame(Duration.seconds(1.5), new KeyValue(glow.radiusProperty(), 32))
        );
        pulse.setAutoReverse(true);
        pulse.setCycleCount(Animation.INDEFINITE);
        pulse.play();

        StackPane menuWrapper = new StackPane(root);
        menuWrapper.setStyle("-fx-background-color: black;");
        menuWrapper.setAlignment(javafx.geometry.Pos.CENTER);
        root.setMinSize(Main.W, Main.H);
        root.setMaxSize(Main.W, Main.H);
        Scene menuScene = new Scene(menuWrapper, Main.W, Main.H);
        menuScene.setFill(javafx.scene.paint.Color.BLACK);
        stage.setScene(menuScene);
    }

    // ---- Name entry dialog ----
    private static void showNameEntry(Stage stage) {
        Stage dialog = new Stage();
        dialog.initOwner(stage);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle(LangManager.t("name.title"));
        dialog.setResizable(false);

        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(40, 60, 40, 60));
        root.setStyle("-fx-background-color: #0d1235;");

        Text prompt = new Text(LangManager.t("name.prompt"));
        prompt.setFont(Font.font("Georgia", FontWeight.BOLD, 28));
        prompt.setFill(Color.CYAN);

        Text sub = new Text(LangManager.t("name.sub"));
        sub.setFont(Font.font("Georgia", FontPosture.ITALIC, 16));
        sub.setFill(Color.GRAY);

        TextField nameField = new TextField();
        nameField.setPromptText(LangManager.t("name.placeholder"));
        nameField.setPrefWidth(320);
        nameField.setPrefHeight(44);
        nameField.setFont(Font.font("Georgia", 20));
        nameField.setStyle(
            "-fx-background-color: #1a1a3a;" +
            "-fx-text-fill: white;" +
            "-fx-prompt-text-fill: #555577;" +
            "-fx-border-color: #334466;" +
            "-fx-border-radius: 6; -fx-background-radius: 6;" +
            "-fx-padding: 6 12 6 12;"
        );
        // Pre-fill with existing name if they came back
        nameField.setText(PlayerState.get().getPlayerName().equals("Player")
            ? "" : PlayerState.get().getPlayerName());

        Button confirm = new Button(LangManager.t("name.confirm"));
        confirm.setFont(Font.font("Georgia", FontWeight.BOLD, 22));
        confirm.setPrefWidth(200); confirm.setPrefHeight(48);
        confirm.setStyle(
            "-fx-background-color: rgba(0,150,200,0.7);" +
            "-fx-text-fill: white;" +
            "-fx-border-color: #00ccff;" +
            "-fx-border-radius: 6; -fx-background-radius: 6;" +
            "-fx-cursor: hand;"
        );

        Runnable start = () -> {
            String name = nameField.getText().trim();
            if (name.isEmpty()) name = "Player";
            PlayerState.get().setPlayerName(name);
            dialog.close();
            { boolean wasFS = stage.isFullScreen(); GameScreen.show(stage, "act1_scene1", name); if (wasFS) { stage.setFullScreen(true); stage.setFullScreenExitHint(""); } }
        };

        confirm.setOnAction(e -> start.run());
        nameField.setOnAction(e -> start.run()); // Enter key

        root.getChildren().addAll(prompt, sub, nameField, confirm);
        dialog.setScene(new Scene(root, 460, 280));
        dialog.showAndWait();
    }

    // ---- Background helper ----
    private static void addBackground(StackPane root) {
        // Try GIF first
        InputStream gif = loadStream("assets/backgrounds/menu_bg.gif");
        if (gif != null) {
            ImageView iv = new ImageView(new Image(gif));
            iv.setFitWidth(Main.W); iv.setFitHeight(Main.H);
            iv.setPreserveRatio(false);
            root.getChildren().add(iv);
            return;
        }
        // Try static PNG
        InputStream png = loadStream("assets/backgrounds/menu_bg.png");
        if (png != null) {
            ImageView iv = new ImageView(new Image(png));
            iv.setFitWidth(Main.W); iv.setFitHeight(Main.H);
            iv.setPreserveRatio(false);
            root.getChildren().add(iv);
            return;
        }
        // Fallback: animated gradient-ish solid
        javafx.scene.shape.Rectangle bg =
            new javafx.scene.shape.Rectangle(Main.W, Main.H, Color.web("#060614"));
        root.getChildren().add(bg);
    }

    private static InputStream loadStream(String path) {
        InputStream is = MainMenuScreen.class.getClassLoader().getResourceAsStream(path);
        if (is != null) return is;
        try { return new java.io.FileInputStream(path); } catch (Exception e) { return null; }
    }

    // ---- Button style ----
    private static Button menuButton(String text) {
        Button btn = new Button(text);
        btn.setFont(Font.font("Georgia", 26));
        btn.setPrefWidth(280); btn.setPrefHeight(52);
        String base =
            "-fx-background-color: transparent;" +
            "-fx-text-fill: #cccccc;" +
            "-fx-border-color: transparent;" +
            "-fx-cursor: hand;";
        String hover =
            "-fx-background-color: rgba(255,100,180,0.12);" +
            "-fx-text-fill: #ffaadd;" +
            "-fx-border-color: rgba(255,100,180,0.35);" +
            "-fx-border-radius: 6; -fx-background-radius: 6;" +
            "-fx-cursor: hand;";
        btn.setStyle(base);
        btn.setOnMouseEntered(e -> btn.setStyle(hover));
        btn.setOnMouseExited(e  -> btn.setStyle(base));
        return btn;
    }
}
