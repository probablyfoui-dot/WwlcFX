package com.wwlc.screens;

import com.wwlc.Main;
import com.wwlc.system.SaveSystem;
import com.wwlc.system.SaveSystem.SaveSlot;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.*;
import javafx.stage.Stage;

public class SaveScreen {

    public enum Mode { SAVE, LOAD }

    public static void show(Stage stage, Scene returnTo, Mode mode,
                            String currentScene, String playerName) {

        VBox panel = buildPanel(stage, returnTo, mode, currentScene, playerName);

        // Try to inject as overlay into the existing scene root
        if (returnTo.getRoot() instanceof StackPane sp) {
            // Remove any previous save overlay
            sp.getChildren().removeIf(n -> "saveOverlay".equals(n.getId()));

            StackPane overlay = new StackPane();
            overlay.setId("saveOverlay");
            Rectangle dim = new Rectangle(Main.W, Main.H, Color.web("#000000", 0.65));
            overlay.getChildren().addAll(dim, panel);
            overlay.setAlignment(Pos.CENTER);
            overlay.setPrefSize(Main.W, Main.H);
            sp.getChildren().add(overlay);
        } else {
            // Fallback
            boolean wasFS = stage.isFullScreen();
            StackPane wrapper = new StackPane();
            wrapper.setStyle("-fx-background-color: black;");
            StackPane overlay = new StackPane();
            Rectangle dim = new Rectangle(Main.W, Main.H, Color.web("#000000", 0.65));
            overlay.getChildren().addAll(dim, panel);
            overlay.setAlignment(Pos.CENTER);
            wrapper.getChildren().addAll(returnTo.getRoot(), overlay);
            Scene popupScene = new Scene(wrapper, Main.W, Main.H);
            popupScene.setFill(Color.BLACK);
            stage.setScene(popupScene);
            if (wasFS) { stage.setFullScreen(true); stage.setFullScreenExitHint(""); }
        }
    }

    private static VBox buildPanel(Stage stage, Scene returnTo, Mode mode,
                                   String currentScene, String playerName) {
        SaveSlot[] slots = SaveSystem.loadAll();

        VBox panel = new VBox(18);
        panel.setAlignment(Pos.CENTER);
        panel.setPadding(new Insets(36, 50, 36, 50));
        panel.setPrefWidth(600);
        panel.setMaxWidth(600);
        panel.setStyle(
            "-fx-background-color: #1a0518;" +
            "-fx-border-color: #cc55aa;" +
            "-fx-border-width: 2;" +
            "-fx-border-radius: 14;" +
            "-fx-background-radius: 14;"
        );

        Text title = new Text(mode == Mode.SAVE ? "Save Game" : "Load Game");
        title.setFont(Font.font("Georgia", FontWeight.BOLD, 36));
        title.setFill(Color.web("#ff88cc"));

        VBox slotList = new VBox(12);
        slotList.setAlignment(Pos.CENTER);

        for (SaveSlot slot : slots) {
            slotList.getChildren().add(
                slotRow(stage, returnTo, mode, slot, currentScene, playerName, panel, slotList));
        }

        Button close = SettingsScreen.pinkButton("Close ✕");
        // Close removes the overlay
        close.setOnAction(e -> {
            if (returnTo.getRoot() instanceof StackPane sp) {
                sp.getChildren().removeIf(n -> "saveOverlay".equals(n.getId()));
            } else {
                stage.setScene(returnTo);
            }
        });

        panel.getChildren().addAll(title, slotList, close);
        return panel;
    }

    private static HBox slotRow(Stage stage, Scene returnTo, Mode mode,
                                 SaveSlot slot, String currentScene, String playerName,
                                 VBox panel, VBox slotList) {
        HBox row = new HBox(16);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPrefWidth(500);
        row.setPadding(new Insets(12, 18, 12, 18));
        row.setStyle(
            "-fx-background-color: #2a0520;" +
            "-fx-border-color: rgba(200,80,160,0.35);" +
            "-fx-border-radius: 8; -fx-background-radius: 8;"
        );

        VBox info = new VBox(3);
        Text slotLabel = new Text("Slot " + (slot.slot() + 1));
        slotLabel.setFont(Font.font("Georgia", FontWeight.BOLD, 17));
        slotLabel.setFill(Color.web("#ff88cc"));

        Text detail = new Text(slot.isEmpty() ? "Empty" : slot.playerName() + " — " +
                               SaveSystem.getDisplayName(slot.scene()) + "\n" + slot.timestamp());
        detail.setFont(Font.font("Georgia", 13));
        detail.setFill(slot.isEmpty() ? Color.web("#886688") : Color.web("#ddbbdd"));
        info.getChildren().addAll(slotLabel, detail);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button action = SettingsScreen.pinkButton(mode == Mode.SAVE ? "Save" : (slot.isEmpty() ? "—" : "Load"));
        action.setPrefWidth(80);
        if (mode == Mode.LOAD && slot.isEmpty()) {
            action.setDisable(true);
        } else {
            action.setOnAction(e -> {
                if (mode == Mode.SAVE) {
                    // Confirm overwrite if slot is not empty
                    if (!slot.isEmpty()) {
                        // Show confirm inside the overlay panel
                        VBox confirmBox = new VBox(14);
                        confirmBox.setAlignment(Pos.CENTER);
                        confirmBox.setStyle(
                            "-fx-background-color: #0d0010;" +
                            "-fx-border-color: #cc55aa; -fx-border-width: 2;" +
                            "-fx-border-radius: 10; -fx-background-radius: 10;" +
                            "-fx-padding: 20 30 20 30;"
                        );
                        Text warn = new Text("Overwrite this save?");
                        warn.setFont(Font.font("Georgia", FontWeight.BOLD, 18));
                        warn.setFill(Color.web("#ff88cc"));
                        Button yes = SettingsScreen.pinkButton("Overwrite");
                        Button no  = SettingsScreen.pinkButton("Cancel");
                        HBox btns = new HBox(16, yes, no);
                        btns.setAlignment(Pos.CENTER);
                        confirmBox.getChildren().addAll(warn, btns);
                        // Inject above current panel
                        if (returnTo.getRoot() instanceof StackPane sp) {
                            StackPane confirmOverlay = new StackPane(
                                new Rectangle(Main.W, Main.H, Color.web("#000000", 0.5)),
                                confirmBox
                            );
                            confirmOverlay.setId("confirmSaveOverlay");
                            confirmOverlay.setAlignment(Pos.CENTER);
                            sp.getChildren().add(confirmOverlay);
                            yes.setOnAction(ev -> {
                                sp.getChildren().removeIf(n -> "confirmSaveOverlay".equals(n.getId()));
                                SaveSystem.save(slot.slot(), currentScene, playerName);
                                sp.getChildren().removeIf(n -> "saveOverlay".equals(n.getId()));
                                show(stage, returnTo, mode, currentScene, playerName);
                            });
                            no.setOnAction(ev -> sp.getChildren().removeIf(n -> "confirmSaveOverlay".equals(n.getId())));
                        }
                    } else {
                        SaveSystem.save(slot.slot(), currentScene, playerName);
                        if (returnTo.getRoot() instanceof StackPane sp) {
                            sp.getChildren().removeIf(n -> "saveOverlay".equals(n.getId()));
                        }
                        show(stage, returnTo, mode, currentScene, playerName);
                    }
                } else {
                    if (returnTo.getRoot() instanceof StackPane sp) {
                        sp.getChildren().removeIf(n -> "saveOverlay".equals(n.getId()));
                    }
                    GameScreen.show(stage, slot.scene(), slot.playerName());
                }
            });
        }

        row.getChildren().addAll(info, spacer, action);

        if (!slot.isEmpty()) {
            Button del = SettingsScreen.pinkButton("✕");
            del.setPrefWidth(44);
            del.setStyle(del.getStyle());
            del.setOnAction(e -> {
                SaveSystem.delete(slot.slot());
                if (returnTo.getRoot() instanceof StackPane sp) {
                    sp.getChildren().removeIf(n -> "saveOverlay".equals(n.getId()));
                }
                show(stage, returnTo, mode, currentScene, playerName);
            });
            row.getChildren().add(del);
        }

        return row;
    }
}
