package com.wwlc.screens;

import com.wwlc.system.GameSettings;
import com.wwlc.system.LangManager;
import javafx.geometry.*;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.*;
import javafx.stage.Stage;

public class SettingsScreen {

    public static void show(Stage stage, Scene returnTo) {
        show(stage, returnTo, null);
    }

    public static void show(Stage stage, Scene returnTo, Runnable onLangChange) {
        GameSettings s = GameSettings.get();

        VBox panel = new VBox(20);
        panel.setAlignment(Pos.CENTER);
        panel.setPadding(new Insets(40, 60, 40, 60));
        panel.setPrefWidth(580);
        panel.setMaxWidth(580);
        panel.setStyle(
            "-fx-background-color: #1a0518;" +
            "-fx-border-color: #cc55aa;" +
            "-fx-border-width: 2;" +
            "-fx-border-radius: 14;" +
            "-fx-background-radius: 14;"
        );

        Text title = new Text(LangManager.t("settings.title"));
        title.setFont(Font.font("Georgia", FontWeight.BOLD, 36));
        title.setFill(Color.web("#ff88cc"));

        Node speedRow = settingRow(
            LangManager.t("settings.speed"),
            LangManager.t("settings.fast"),
            LangManager.t("settings.slow"),
            s.getTextSpeed() / 3.0,
            val -> s.setTextSpeed((float)(val * 3.0))
        );
        Node musicRow = settingRow(
            LangManager.t("settings.music"),
            LangManager.t("settings.off"),
            LangManager.t("settings.max"),
            s.getMusicVolume(),
            val -> { s.setMusicVolume((float) val); com.wwlc.system.AudioManager.get().setBgmVolume((float) val); }
        );
        Node sfxRow = settingRow(
            LangManager.t("settings.sfx"),
            LangManager.t("settings.off"),
            LangManager.t("settings.max"),
            s.getSfxVolume(),
            val -> s.setSfxVolume((float) val)
        );

        // ── Language toggle ───────────────────────────────────────────────────
        Text langLabel = new Text(LangManager.t("settings.lang"));
        langLabel.setFont(Font.font("Georgia", FontWeight.BOLD, 19));
        langLabel.setFill(Color.web("#f0a0cc"));

        Button enBtn = langBtn("English", !LangManager.isFrench());
        Button frBtn = langBtn("Français", LangManager.isFrench());

        enBtn.setOnAction(e -> {
            s.setLang("en");
            if (onLangChange != null) onLangChange.run();
            // Rebuild settings panel in new language
            show(stage, returnTo, onLangChange);
        });
        frBtn.setOnAction(e -> {
            s.setLang("fr");
            if (onLangChange != null) onLangChange.run();
            // Rebuild settings panel in new language
            show(stage, returnTo, onLangChange);
        });

        HBox langRow = new HBox(12, enBtn, frBtn);
        langRow.setAlignment(Pos.CENTER);
        VBox langBox = new VBox(8, langLabel, langRow);
        langBox.setAlignment(Pos.CENTER);

        // ── Fullscreen + Close ────────────────────────────────────────────────
        Button fsBtn = pinkButton(stage.isFullScreen() ?
            LangManager.t("btn.windowed") : LangManager.t("btn.fullscreen"));
        fsBtn.setOnAction(e -> {
            stage.setFullScreen(!stage.isFullScreen());
            stage.setFullScreenExitHint("");
            fsBtn.setText(stage.isFullScreen() ?
                LangManager.t("btn.windowed") : LangManager.t("btn.fullscreen"));
        });

        Button close = pinkButton(LangManager.t("btn.close"));

        HBox btnRow = new HBox(20, fsBtn, close);
        btnRow.setAlignment(Pos.CENTER);

        panel.getChildren().addAll(title, speedRow, musicRow, sfxRow, langBox, btnRow);

        // ── Overlay injection ─────────────────────────────────────────────────
        StackPane overlay = makeOverlay(panel);

        if (returnTo.getRoot() instanceof StackPane sp) {
            sp.getChildren().removeIf(n -> "settingsOverlay".equals(n.getId()));
            overlay.setId("settingsOverlay");
            sp.getChildren().add(overlay);
            close.setOnAction(e -> {
                sp.getChildren().remove(overlay);
                if (onLangChange != null) onLangChange.run();
            });
        } else {
            boolean wasFS = stage.isFullScreen();
            StackPane wrapper = new StackPane(returnTo.getRoot(), overlay);
            wrapper.setStyle("-fx-background-color: black;");
            Scene popupScene = new Scene(wrapper, com.wwlc.Main.W, com.wwlc.Main.H);
            popupScene.setFill(Color.BLACK);
            stage.setScene(popupScene);
            if (wasFS) { stage.setFullScreen(true); stage.setFullScreenExitHint(""); }
            close.setOnAction(e -> {
                if (onLangChange != null) onLangChange.run();
                else { stage.setScene(returnTo); if (wasFS) { stage.setFullScreen(true); stage.setFullScreenExitHint(""); } }
            });
        }
    }

    static StackPane makeOverlay(Node panel) {
        Rectangle dim = new Rectangle(com.wwlc.Main.W, com.wwlc.Main.H, Color.web("#000000", 0.65));
        StackPane overlay = new StackPane(dim, panel);
        overlay.setAlignment(Pos.CENTER);
        overlay.setPrefSize(com.wwlc.Main.W, com.wwlc.Main.H);
        return overlay;
    }

    private static Node settingRow(String label, String leftLbl, String rightLbl,
                                   double initial, java.util.function.DoubleConsumer onChange) {
        Text lbl = new Text(label);
        lbl.setFont(Font.font("Georgia", FontWeight.BOLD, 19));
        lbl.setFill(Color.web("#f0a0cc"));

        Slider slider = new Slider(0, 1, initial);
        slider.setPrefWidth(300);
        slider.setStyle("-fx-control-inner-background: #3a0830; -fx-accent: #ee66aa;");
        slider.valueProperty().addListener((obs, o, val) -> onChange.accept(val.doubleValue()));

        HBox row = new HBox(10, tiny(leftLbl), slider, tiny(rightLbl));
        row.setAlignment(Pos.CENTER);

        VBox box = new VBox(5, lbl, row);
        box.setAlignment(Pos.CENTER);
        return box;
    }

    private static Button langBtn(String label, boolean active) {
        Button btn = new Button(label);
        btn.setFont(Font.font("Georgia", FontWeight.BOLD, 16));
        btn.setPrefWidth(120); btn.setPrefHeight(36);
        btn.setStyle(langBtnStyle(active));
        return btn;
    }

    private static String langBtnStyle(boolean active) {
        return active
            ? "-fx-background-color: #cc55aa; -fx-text-fill: white; -fx-border-color: #ff88cc; -fx-border-radius: 8; -fx-background-radius: 8; -fx-cursor: hand;"
            : "-fx-background-color: #2a0520; -fx-text-fill: #ffaadd; -fx-border-color: #7a2555; -fx-border-radius: 8; -fx-background-radius: 8; -fx-cursor: hand;";
    }

    private static Text tiny(String t) {
        Text txt = new Text(t);
        txt.setFont(Font.font("Georgia", 13));
        txt.setFill(Color.web("#cc88aa"));
        return txt;
    }

    static Button pinkButton(String text) {
        Button btn = new Button(text);
        btn.setFont(Font.font("Georgia", FontWeight.BOLD, 17));
        btn.setPrefHeight(40); btn.setPrefWidth(180);
        String base  = "-fx-background-color: #2a0520; -fx-text-fill: #ffaadd; -fx-border-color: #cc55aa; -fx-border-radius: 8; -fx-background-radius: 8; -fx-cursor: hand;";
        String hover = "-fx-background-color: #4a1040; -fx-text-fill: #ffffff; -fx-border-color: #ff88cc; -fx-border-radius: 8; -fx-background-radius: 8; -fx-cursor: hand;";
        btn.setStyle(base);
        btn.setOnMouseEntered(e -> btn.setStyle(hover));
        btn.setOnMouseExited(e  -> btn.setStyle(base));
        return btn;
    }
}
