package com.wwlc.screens;

import com.wwlc.Main;
import com.wwlc.dialogue.DialogueManager;
import com.wwlc.dialogue.DialogueManager.Choice;
import com.wwlc.system.GameSettings;
import com.wwlc.system.PlayerState;
import com.wwlc.system.SaveSystem;
import com.wwlc.system.LangManager;
import javafx.animation.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.io.InputStream;
import java.util.*;

public class GameScreen implements DialogueManager.Listener {

    private final Stage stage;
    private final String playerName;

    // Layers
    private final StackPane root      = new StackPane();
    private final Pane      bgLayer   = new Pane();
    private final Pane      charLayer = new Pane();
    private final Pane      uiLayer   = new Pane();

    // Dialogue box
    private final Rectangle boxBg    = new Rectangle();
    private final Rectangle nameBg   = new Rectangle();
    private final Text      nameText = new Text();
    private final Text      diagText = new Text();
    private final Text      arrow    = new Text("▼");

    // Choice menu
    private final VBox choiceBox = new VBox(14);

    // HUD
    private final HBox hudBar = new HBox(8);
    private Button hudMenuBtn, hudSaveBtn, hudSettingsBtn;

    // Characters
    private final Map<String, StackPane> charViews = new HashMap<>();
    private static final Map<String, String> CHAR_COLORS = Map.of(
        "Vermia",  "#ff88bb",
        "Grubwin", "#88bbff",
        "Mawla",   "#88ffaa",
        "Dirtus",  "#ffcc55"
    );

    // Auto-advance
    private boolean autoAdvance = false;
    private Timeline autoTimer  = null;
    private Button   hudAutoBtn = null;
    // Skip mode (hold Ctrl)
    private boolean skipMode = false;
    private Timeline skipTimer = null;

    // Typewriter
    private String   fullText   = "";
    private Timeline typewriter = null;
    private boolean  textDone   = false;

    // Scene tracking for save
    private String currentScene = "act1_scene1";

    private final DialogueManager dialogue;
    private Scene gameScene;

    public static void show(Stage stage, String startScene, String playerName) {
        new GameScreen(stage, startScene, playerName);
    }
    public static void show(Stage stage, String startScene) {
        show(stage, startScene, PlayerState.get().getPlayerName());
    }

    public GameScreen(Stage stage, String startScene, String playerName) {
        this.stage        = stage;
        this.playerName   = playerName;
        this.currentScene = startScene;
        PlayerState.get().setPlayerName(playerName);
        PlayerState.get().setCurrentScene(startScene);

        root.setPrefSize(Main.W, Main.H);
        root.setMinSize(Main.W, Main.H);
        root.setMaxSize(Main.W, Main.H);
        root.setStyle("-fx-background-color: #0a0a1a;");

        setupDialogueBox();
        setupChoiceBox();
        setupHUD();

        root.getChildren().addAll(bgLayer, charLayer, uiLayer);

        // Click to advance (ignore clicks on HUD buttons)
        root.setOnMouseClicked(e -> {
            if (e.getTarget() instanceof javafx.scene.control.ButtonBase) return;
            handleAdvance();
        });
        root.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case ENTER, SPACE -> {
                    if (choiceBox.isVisible() && !choiceBox.getChildren().isEmpty()) {
                        ((Button) choiceBox.getChildren().get(selectedChoice)).fire();
                    } else handleAdvance();
                }
                case F11, F       -> toggleFullscreen();
                case ESCAPE       -> { if (stage.isFullScreen()) stage.setFullScreen(false); else showConfirmAndGoToMenu(); }
                case A            -> toggleAutoAdvance();
                case UP, DOWN     -> navigateChoice(e.getCode());
                case CONTROL      -> startSkip();
            }
        });
        root.setOnKeyReleased(e -> {
            if (e.getCode() == KeyCode.CONTROL) stopSkip();
        });
        root.setFocusTraversable(true);

        dialogue = new DialogueManager(this);
        dialogue.loadScene(startScene);

        StackPane outerWrapper = new StackPane(root);
        outerWrapper.setStyle("-fx-background-color: black;");
        outerWrapper.setAlignment(javafx.geometry.Pos.CENTER);
        gameScene = new Scene(outerWrapper, Main.W, Main.H);
        gameScene.setFill(javafx.scene.paint.Color.BLACK);
        stage.setScene(gameScene);
        root.requestFocus();
        dialogue.advance();
    }

    // ---- UI setup ----

    private void setupDialogueBox() {
        double boxX = 30, boxY = Main.H - 225, boxW = Main.W - 60, boxH = 200;

        boxBg.setX(boxX); boxBg.setY(boxY);
        boxBg.setWidth(boxW); boxBg.setHeight(boxH);
        boxBg.setFill(Color.web("#2a0520", 0.90));
        boxBg.setStroke(Color.web("#ff80c0", 0.5));
        boxBg.setStrokeWidth(1.5);
        boxBg.setArcWidth(14); boxBg.setArcHeight(14);

        nameBg.setX(boxX); nameBg.setY(boxY - 44);
        nameBg.setWidth(240); nameBg.setHeight(42);
        nameBg.setFill(Color.web("#2a0520", 0.95));
        nameBg.setStroke(Color.web("#ff80c0", 0.5));
        nameBg.setStrokeWidth(1);
        nameBg.setArcWidth(8); nameBg.setArcHeight(8);
        nameBg.setVisible(false);

        nameText.setX(boxX + 16); nameText.setY(boxY - 12);
        nameText.setFont(Font.font("Georgia", FontWeight.BOLD, 22));
        nameText.setFill(Color.web("#ff99cc"));
        nameText.setVisible(false);

        diagText.setX(boxX + 22); diagText.setY(boxY + 40);
        diagText.setFont(Font.font("Georgia", 20));
        diagText.setFill(Color.WHITE);
        diagText.setWrappingWidth(boxW - 44);

        arrow.setFont(Font.font(18));
        arrow.setFill(Color.web("#ff99cc"));
        arrow.setX(boxX + boxW - 32); arrow.setY(boxY + boxH - 14);
        arrow.setVisible(false);

        Timeline blink = new Timeline(
            new KeyFrame(Duration.ZERO,       new KeyValue(arrow.opacityProperty(), 1.0)),
            new KeyFrame(Duration.millis(500), new KeyValue(arrow.opacityProperty(), 0.1))
        );
        blink.setAutoReverse(true);
        blink.setCycleCount(Animation.INDEFINITE);
        blink.play();

        uiLayer.getChildren().addAll(boxBg, nameBg, nameText, diagText, arrow);
    }

    private void setupChoiceBox() {
        choiceBox.setAlignment(Pos.CENTER);
        choiceBox.setVisible(false);
        choiceBox.setLayoutX((Main.W - 620) / 2.0);
        choiceBox.setLayoutY(180);
        uiLayer.getChildren().add(choiceBox);
    }

    private void setupHUD() {
        // Flat text-only buttons, no border/box, left side of dialogue box like DDLC
        hudSaveBtn     = hudButton(LangManager.t("btn.save"));
        hudSettingsBtn = hudButton(LangManager.t("btn.settings"));
        hudMenuBtn     = hudButton(LangManager.t("btn.menu"));

        hudSaveBtn.setOnAction(e -> {
            com.wwlc.system.AudioManager.get().playSfx("menu_select");
            SaveScreen.show(stage, gameScene, SaveScreen.Mode.SAVE, dialogue.getCurrentScene(), playerName); });

        hudSettingsBtn.setOnAction(e -> {
            com.wwlc.system.AudioManager.get().playSfx("menu_select");
            SettingsScreen.show(stage, gameScene, this::refreshHudLabels); });

        hudMenuBtn.setOnAction(e -> {
            com.wwlc.system.AudioManager.get().playSfx("menu_select");
            showConfirmAndGoToMenu();
        });

        hudAutoBtn = hudButton("Auto");
        hudAutoBtn.setOnAction(e -> toggleAutoAdvance());
        hudBar.getChildren().addAll(hudMenuBtn, hudSaveBtn, hudSettingsBtn, hudAutoBtn);
        // Left side inside the dialogue box, vertically centered near bottom
        double boxY = Main.H - 225;
        hudBar.setLayoutX(42);
        hudBar.setLayoutY(boxY + 155);
        uiLayer.getChildren().add(hudBar);
    }

    private void toggleFullscreen() {
        stage.setFullScreen(!stage.isFullScreen());
        stage.setFullScreenExitHint("");
    }

    /** Refreshes HUD button labels after language change */
    public void refreshHudLabels() {
        hudMenuBtn.setText(LangManager.t("btn.menu"));
        hudSaveBtn.setText(LangManager.t("btn.save"));
        hudSettingsBtn.setText(LangManager.t("btn.settings"));
        hudAutoBtn.setText(LangManager.t("btn.auto"));
        // Reload the current scene from the beginning in the new language
        String scene = dialogue.getCurrentScene();
        if (scene != null && !scene.isEmpty()) {
            if (typewriter != null) typewriter.stop();
            dialogue.loadScene(scene);
            dialogue.advance();
        }
    }

    private void showConfirmAndGoToMenu() {
        // ── Overlay on the game scene ─────────────────────────────────────────
        StackPane gameRoot = (StackPane) gameScene.getRoot();

        // Dim the game behind
        Rectangle dim = new Rectangle(Main.W, Main.H, Color.web("#000000", 0.65));
        dim.setMouseTransparent(true);

        // Panel
        StackPane panel = new StackPane();
        panel.setMaxSize(600, 270);

        // Background image as ImageView — CSS would override a Background object
        InputStream bgStream = loadAsset("assets/backgrounds/confirm_bg.png");
        if (bgStream != null) {
            ImageView bgIv = new ImageView(new Image(bgStream));
            bgIv.setFitWidth(600); bgIv.setFitHeight(270);
            bgIv.setPreserveRatio(false); bgIv.setSmooth(true);
            // Clip to rounded corners without CSS
            javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(600, 270);
            clip.setArcWidth(24); clip.setArcHeight(24);
            bgIv.setClip(clip);
            panel.getChildren().add(bgIv);
        }

        // Dark tint over image so text is readable
        javafx.scene.shape.Rectangle tint = new javafx.scene.shape.Rectangle(600, 270, Color.web("#000000", 0.50));
        tint.setArcWidth(24); tint.setArcHeight(24);
        tint.setMouseTransparent(true);
        panel.getChildren().add(tint);

        // Text
        Text msg = new Text(LangManager.t("menu.confirm"));
        msg.setFont(Font.font("Georgia", FontWeight.BOLD, 22));
        msg.setFill(Color.WHITE);
        msg.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        msg.setWrappingWidth(500);
        msg.setEffect(new javafx.scene.effect.DropShadow(14, Color.BLACK));

        // Buttons
        Button leave = confirmBtn(LangManager.t("confirm.leave"), "#cc55aa");
        Button stay  = confirmBtn(LangManager.t("confirm.stay"),  "#2a0520");
        HBox btns = new HBox(28, leave, stay);
        btns.setAlignment(Pos.CENTER);

        VBox content = new VBox(26, msg, btns);
        content.setAlignment(Pos.CENTER);
        panel.getChildren().add(content);

        // Pink border drawn as a stroke rectangle on top (no CSS so image stays)
        javafx.scene.shape.Rectangle border = new javafx.scene.shape.Rectangle(600, 270);
        border.setFill(Color.TRANSPARENT);
        border.setStroke(Color.web("#cc55aa")); border.setStrokeWidth(3);
        border.setArcWidth(24); border.setArcHeight(24);
        border.setMouseTransparent(true);
        panel.getChildren().add(border);

        // Full overlay
        StackPane overlay = new StackPane(dim, panel);
        overlay.setAlignment(Pos.CENTER);
        gameRoot.getChildren().add(overlay);

        leave.setOnAction(e -> { gameRoot.getChildren().remove(overlay); if (typewriter != null) typewriter.stop(); MainMenuScreen.show(stage); });
        stay.setOnAction(e  -> gameRoot.getChildren().remove(overlay));
    }

    private Button confirmBtn(String text, String bgColor) {
        Button btn = new Button(text);
        btn.setFont(Font.font("Georgia", FontWeight.BOLD, 17));
        btn.setPrefWidth(200); btn.setPrefHeight(46);
        String base  = "-fx-background-color: " + bgColor + "; -fx-text-fill: white; -fx-border-color: #ff88cc; -fx-border-radius: 8; -fx-background-radius: 8; -fx-cursor: hand;";
        String hover = "-fx-background-color: #7a1550; -fx-text-fill: white; -fx-border-color: #ff88cc; -fx-border-radius: 8; -fx-background-radius: 8; -fx-cursor: hand;";
        btn.setStyle(base);
        btn.setOnMouseEntered(e -> btn.setStyle(hover));
        btn.setOnMouseExited(e  -> btn.setStyle(base));
        return btn;
    }

    // ---- DialogueManager.Listener ----

    @Override
    public void onLine(String speaker, String text) {
        text = text.replace("[player]", playerName);
        if ("[player]".equals(speaker)) speaker = playerName;

        boolean hasName = speaker != null;
        nameBg.setVisible(hasName);
        nameText.setVisible(hasName);
        if (hasName) nameText.setText(speaker);

        boxBg.setVisible(true);
        arrow.setVisible(false);
        fullText = text;
        textDone = false;
        diagText.setText("");
        if (typewriter != null) typewriter.stop();

        double delayMs = GameSettings.get().getCharDelayMs();
        if (delayMs <= 0) {
            diagText.setText(fullText);
            textDone = true;
            arrow.setVisible(true);
            return;
        }

        final int[] i = {0};
        // Re-read delay each frame so live settings changes apply immediately
        typewriter = new Timeline();
        typewriter.setCycleCount(Animation.INDEFINITE);
        javafx.animation.KeyFrame kf = new javafx.animation.KeyFrame(
            Duration.millis(Math.max(1, GameSettings.get().getCharDelayMs())),
            e -> {
                // Reschedule with current speed each tick
                double currentDelay = Math.max(1, GameSettings.get().getCharDelayMs());
                if (typewriter.getKeyFrames().isEmpty() ||
                    typewriter.getKeyFrames().get(0).getTime().toMillis() != currentDelay) {
                    typewriter.stop();
                    typewriter.getKeyFrames().setAll(new javafx.animation.KeyFrame(
                        Duration.millis(currentDelay), ev -> {
                            if (i[0] <= fullText.length()) {
                                diagText.setText(fullText.substring(0, i[0]++));
                    com.wwlc.system.AudioManager.get().playSfx("text_blip");
                            } else {
                                typewriter.stop();
                                textDone = true;
                                arrow.setVisible(true);
                            }
                        }));
                    typewriter.setCycleCount(fullText.length() + 2 - i[0]);
                    typewriter.play();
                    return;
                }
                if (i[0] <= fullText.length()) {
                    diagText.setText(fullText.substring(0, i[0]++));
                    com.wwlc.system.AudioManager.get().playSfx("text_blip");
                } else {
                    typewriter.stop();
                    textDone = true;
                    arrow.setVisible(true);
                    if (autoAdvance) scheduleAutoAdvance();
                }
            });
        typewriter.getKeyFrames().add(kf);
        typewriter.setCycleCount(fullText.length() + 2);
        typewriter.play();
    }

    @Override
    public void onBgChange(String file) {
        bgLayer.getChildren().clear();
        InputStream is = loadAsset("assets/backgrounds/" + file + ".png");
        if (is == null) is = loadAsset("assets/backgrounds/" + file + ".jpg");
        if (is != null) {
            ImageView iv = new ImageView(new Image(is));
            iv.setFitWidth(Main.W); iv.setFitHeight(Main.H);
            iv.setPreserveRatio(false);
            bgLayer.getChildren().add(iv);
        } else {
            bgLayer.getChildren().add(new Rectangle(Main.W, Main.H, Color.web("#1a0a14")));
        }
    }

    @Override
    public void onCharChange(String name, String expr, String pos, boolean visible) {
        if (!charViews.containsKey(name)) {
            StackPane sp = new StackPane();
            sp.setBackground(Background.EMPTY);
            sp.setStyle("-fx-background-color: transparent;");
            sp.setVisible(false);
            charViews.put(name, sp);
            charLayer.getChildren().add(sp);
        }
        StackPane sp = charViews.get(name);
        if (!visible) {
            // Fade out
            FadeTransition ft = new FadeTransition(Duration.millis(180), sp);
            ft.setToValue(0);
            ft.setOnFinished(e -> sp.setVisible(false));
            ft.play();
            return;
        }
        sp.setVisible(true);
        if (visible) {
            double x = switch (pos) {
                case "left"  -> Main.W * 0.10;
                case "right" -> Main.W * 0.60;
                default      -> Main.W * 0.35;
            };
            sp.setLayoutX(x);
            sp.setLayoutY(Main.H - 430);
            sp.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
            sp.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
            InputStream is = loadAsset(
                "assets/characters/" + name.toLowerCase() + "/normal.png");
            if (is != null) {
                Image img = new Image(is);
                ImageView iv = new ImageView(img);
                iv.setFitHeight(410);
                iv.setPreserveRatio(true);
                iv.setSmooth(true);
                sp.getChildren().setAll(iv);
            } else {
                String color = CHAR_COLORS.getOrDefault(name, "#aaaaaa");
                Rectangle rect = new Rectangle(165, 400, Color.web(color, 0.70));
                rect.setArcWidth(12); rect.setArcHeight(12);
                Text lbl = new Text(name);
                lbl.setFont(Font.font("Georgia", FontWeight.BOLD, 18));
                lbl.setFill(Color.WHITE);
                sp.getChildren().setAll(rect, lbl);
            }
            // Fade in
            sp.setOpacity(0);
            FadeTransition ft = new FadeTransition(Duration.millis(220), sp);
            ft.setToValue(1);
            ft.play();
        }
    }

    @Override public void onMusicChange(String file) {
        com.wwlc.system.AudioManager.get().playBgm(file);
    }

    @Override
    public void onChoices(List<Choice> choices) {
        boxBg.setVisible(false);
        nameBg.setVisible(false);
        nameText.setVisible(false);
        diagText.setText("");
        arrow.setVisible(false);

        choiceBox.getChildren().clear();
        selectedChoice = 0;
        for (Choice c : choices) {
            Button btn = choiceButton(c.label());
            btn.setOnAction(e -> {
                choiceBox.setVisible(false);
                choiceBox.getChildren().clear();
                boxBg.setVisible(true);
                dialogue.pickChoice(c);
            });
            choiceBox.getChildren().add(btn);
        }
        choiceBox.setVisible(true);
        choiceBox.toFront();
    }

    @Override public void onPoem() { PoemScreen.show(stage, this); }
    @Override public void onEnd()  { CreditsScreen.show(stage); }
    @Override public void onActCard(String title, String subtitle, String nextScene) {
        if (typewriter != null) typewriter.stop();
        ActCardScreen.show(stage, title, subtitle, nextScene, playerName);
    }

    private void handleAdvance() {
        if (dialogue.isWaiting()) return;
        if (!textDone) {
            if (typewriter != null) typewriter.stop();
            diagText.setText(fullText);
            textDone = true;
            arrow.setVisible(true);
        } else {
            dialogue.advance();
        }
    }

    public void returnFromPoem() {
        // Re-use the existing scene — JavaFX forbids putting root in a new Scene
        // if it already belongs to one. Just switch back to the stored scene.
        stage.setScene(gameScene);
        root.requestFocus();
        dialogue.advance();
    }

    // ---- Helpers ----

    private StackPane createCharPlaceholder(String name) {
        String color = CHAR_COLORS.getOrDefault(name, "#aaaaaa");
        Rectangle rect = new Rectangle(165, 400, Color.web(color, 0.70));
        rect.setArcWidth(12); rect.setArcHeight(12);
        DropShadow ds = new DropShadow(15, Color.BLACK);
        rect.setEffect(ds);
        Text lbl = new Text(name);
        lbl.setFont(Font.font("Georgia", FontWeight.BOLD, 18));
        lbl.setFill(Color.WHITE);
        StackPane sp = new StackPane(rect, lbl);
        sp.setBackground(Background.EMPTY);
        sp.setStyle("-fx-background-color: transparent;");
        sp.setVisible(false);
        return sp;
    }

    private Button choiceButton(String text) {
        Button btn = new Button(text);
        btn.setFont(Font.font("Georgia", 21));
        btn.setPrefWidth(620); btn.setPrefHeight(54);
        String base  = "-fx-background-color: rgba(42,5,32,0.92); -fx-text-fill: white; -fx-border-color: rgba(255,100,180,0.4); -fx-border-radius: 6; -fx-background-radius: 6; -fx-cursor: hand;";
        String hover = "-fx-background-color: rgba(100,20,70,0.85); -fx-text-fill: #ffddee; -fx-border-color: rgba(255,150,200,0.9); -fx-border-radius: 6; -fx-background-radius: 6; -fx-cursor: hand;";
        btn.setStyle(base);
        btn.setOnMouseEntered(e -> btn.setStyle(hover));
        btn.setOnMouseExited(e  -> btn.setStyle(base));
        return btn;
    }

    /** Yellow HUD buttons that sit on the pink box */
    private Button hudButton(String text) {
        Button btn = new Button(text);
        btn.setFont(Font.font("Georgia", FontWeight.BOLD, 13));
        btn.setPrefHeight(24);
        // No background, no border — pure text like DDLC
        String base  = "-fx-background-color: transparent; -fx-text-fill: #ffee44; -fx-border-color: transparent; -fx-cursor: hand; -fx-padding: 2 8 2 0;";
        String hover = "-fx-background-color: transparent; -fx-text-fill: #ffffff; -fx-border-color: transparent; -fx-cursor: hand; -fx-padding: 2 8 2 0;";
        btn.setStyle(base);
        btn.setOnMouseEntered(e -> btn.setStyle(hover));
        btn.setOnMouseExited(e  -> btn.setStyle(base));
        return btn;
    }

    private void toggleAutoAdvance() {
        autoAdvance = !autoAdvance;
        String base  = "-fx-background-color: transparent; -fx-text-fill: %s; -fx-border-color: transparent; -fx-cursor: hand; -fx-padding: 2 8 2 0;";
        hudAutoBtn.setStyle(String.format(base, autoAdvance ? "#00ffaa" : "#ffee44"));
        if (autoAdvance && textDone && !dialogue.isWaiting()) scheduleAutoAdvance();
        else if (!autoAdvance && autoTimer != null) { autoTimer.stop(); autoTimer = null; }
    }

    private void scheduleAutoAdvance() {
        if (autoTimer != null) autoTimer.stop();
        autoTimer = new Timeline(new KeyFrame(Duration.seconds(3), e -> {
            if (autoAdvance && textDone && !dialogue.isWaiting()) dialogue.advance();
        }));
        autoTimer.play();
    }

    private void startSkip() {
        if (skipTimer != null) return;
        skipTimer = new Timeline(new KeyFrame(Duration.millis(60), e -> {
            if (textDone && !dialogue.isWaiting()) dialogue.advance();
            else if (!textDone) { if (typewriter != null) typewriter.stop(); diagText.setText(fullText); textDone = true; arrow.setVisible(true); }
        }));
        skipTimer.setCycleCount(Animation.INDEFINITE);
        skipTimer.play();
    }

    private void stopSkip() {
        if (skipTimer != null) { skipTimer.stop(); skipTimer = null; }
    }

    private int selectedChoice = 0;
    private void navigateChoice(KeyCode code) {
        if (!choiceBox.isVisible() || choiceBox.getChildren().isEmpty()) return;
        int n = choiceBox.getChildren().size();
        selectedChoice = (code == KeyCode.UP) ? (selectedChoice - 1 + n) % n : (selectedChoice + 1) % n;
        for (int i = 0; i < n; i++) {
            Button btn = (Button) choiceBox.getChildren().get(i);
            boolean sel = i == selectedChoice;
            String bg = sel ? "rgba(150,30,100,0.95)" : "rgba(42,5,32,0.92)";
            String border = sel ? "rgba(255,180,220,1.0)" : "rgba(255,100,180,0.4)";
            btn.setStyle("-fx-background-color: " + bg + "; -fx-text-fill: " + (sel ? "#ffe0f0" : "white") +
                "; -fx-border-color: " + border + "; -fx-border-radius: 6; -fx-background-radius: 6; -fx-cursor: hand;");
        }
    }

    private InputStream loadAsset(String path) {
        InputStream is = getClass().getClassLoader().getResourceAsStream(path);
        if (is != null) return is;
        try { return new java.io.FileInputStream(path); } catch (Exception e) { return null; }
    }
}
