package com.wwlc.screens;

import com.wwlc.Main;
import com.wwlc.system.PlayerState;
import javafx.animation.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.effect.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.util.*;

/**
 * POEM MINIGAME
 *
 * The player builds a poem by picking one word per line from themed word pools.
 * Each choice subtly shapes how characters respond to the poem afterward.
 * The result is stored in PlayerState so later scenes can reference it.
 *
 * Design: 5 lines, each line offers 3 word choices from a thematic category.
 * Words accumulate into a poem that's revealed line by line.
 * After all 5 lines, the finished poem is shown with a slow reveal.
 * The poem's "tone" (computed from word choices) is saved to PlayerState.
 */
public class PoemScreen {

    // Poem tone constants — saved to PlayerState for story use
    public static final String TONE_EARNEST  = "earnest";
    public static final String TONE_SARDONIC = "sardonic";
    public static final String TONE_TENDER   = "tender";

    // Word pools per line. Each line has a lead-in (shown as grey text)
    // and 3 word options the player clicks to complete the line.
    private static final String[] LEAD_INS_EN = {
        "Below the surface of things, there is",
        "What moves through darkness does so",
        "I came to this place not knowing",
        "The worm does not ask if the earth",
        "And so I find that I have stayed, because"
    };

    private static final String[] LEAD_INS_FR = {
        "Sous la surface des choses, il y a",
        "Ce qui se déplace dans l'obscurité le fait",
        "Je suis venu ici sans savoir",
        "Le ver ne demande pas si la terre",
        "Et ainsi je reste, parce que"
    };

    // [line][option] = {word, tone_contribution}
    // Tone: E=earnest, S=sardonic, T=tender
    private static final String[][][] WORDS_EN = {
        { {"something turning",  TONE_EARNEST},
          {"mostly nothing",     TONE_SARDONIC},
          {"what I left behind", TONE_TENDER} },
        { {"without apology",   TONE_EARNEST},
          {"because it has to",  TONE_SARDONIC},
          {"like it belongs",    TONE_TENDER} },
        { {"I would find it worth", TONE_EARNEST},
          {"what I was doing",      TONE_SARDONIC},
          {"I was already looking", TONE_TENDER} },
        { {"deserves the turning", TONE_EARNEST},
          {"has opinions",         TONE_SARDONIC},
          {"will remember",        TONE_TENDER} },
        { {"the ground is worth knowing", TONE_EARNEST},
          {"where else would I go",       TONE_SARDONIC},
          {"something called me here",    TONE_TENDER} }
    };

    private static final String[][][] WORDS_FR = {
        { {"quelque chose qui tourne",  TONE_EARNEST},
          {"presque rien",              TONE_SARDONIC},
          {"ce que j'ai laissé",        TONE_TENDER} },
        { {"sans s'excuser",            TONE_EARNEST},
          {"parce qu'il le doit",       TONE_SARDONIC},
          {"comme s'il y était chez lui", TONE_TENDER} },
        { {"que cela en valait la peine", TONE_EARNEST},
          {"ce que je faisais",           TONE_SARDONIC},
          {"que je cherchais déjà",       TONE_TENDER} },
        { {"mérite le retournement",    TONE_EARNEST},
          {"a des opinions",            TONE_SARDONIC},
          {"se souviendra",             TONE_TENDER} },
        { {"la terre vaut la peine d'être connue", TONE_EARNEST},
          {"où irais-je sinon",                    TONE_SARDONIC},
          {"quelque chose m'a appelé ici",         TONE_TENDER} }
    };

    private String[] leadIns() {
        return com.wwlc.system.LangManager.isFrench() ? LEAD_INS_FR : LEAD_INS_EN;
    }
    private String[][][] words() {
        return com.wwlc.system.LangManager.isFrench() ? WORDS_FR : WORDS_EN;
    }

    private final Stage stage;
    private final GameScreen returnTo;
    private final String playerName;

    // State
    private final String[] chosenWords = new String[5];
    private final String[] chosenTones = new String[5];
    private int currentLine = 0;

    // Layout
    private final StackPane root = new StackPane();
    private final VBox poemAccumulator = new VBox(10); // shows chosen lines
    private final VBox wordChoiceArea  = new VBox(14);
    private final Text leadInText      = new Text();
    private final Text promptText      = new Text("Choose a word to complete the line:");
    private final Text titleText       = new Text();

    public static void show(Stage stage, GameScreen returnTo) {
        new PoemScreen(stage, returnTo);
    }

    public PoemScreen(Stage stage, GameScreen returnTo) {
        this.stage      = stage;
        this.returnTo   = returnTo;
        this.playerName = PlayerState.get().getPlayerName();
        buildScene();
        showCurrentLine();
    }

    private void buildScene() {
        root.setPrefSize(Main.W, Main.H);
        root.setMinSize(Main.W, Main.H);
        root.setStyle("-fx-background-color: #0c0818;");

        // Solid full-screen backdrop — covers the game screen entirely
        Rectangle backdrop = new Rectangle(Main.W, Main.H);
        backdrop.setFill(Color.web("#0c0818"));

        // Paper texture bg — soft parchment feel
        Rectangle paper = new Rectangle(Main.W * 0.78, Main.H * 0.92);
        paper.setFill(Color.web("#f9f5ec", 0.06));
        paper.setArcWidth(18); paper.setArcHeight(18);
        DropShadow paperShadow = new DropShadow(30, Color.web("#6600aa", 0.3));
        paper.setEffect(paperShadow);

        // Title
        titleText.setText("~ Write Your Poem ~");
        titleText.setFont(Font.font("Georgia", FontWeight.BOLD, 36));
        titleText.setFill(Color.web("#e8d0ff"));
        DropShadow titleGlow = new DropShadow(12, Color.web("#aa66ff"));
        titleText.setEffect(titleGlow);

        Text subtitle = new Text("Words you choose here shape how the club sees your heart.");
        subtitle.setFont(Font.font("Georgia", FontPosture.ITALIC, 16));
        subtitle.setFill(Color.web("#888888"));

        // Accumulated poem lines
        poemAccumulator.setAlignment(Pos.CENTER_LEFT);
        poemAccumulator.setPadding(new Insets(0, 0, 20, 0));

        // Separator
        Rectangle sep = new Rectangle(560, 1);
        sep.setFill(Color.web("#aa66ff", 0.3));

        // Lead-in and prompt
        leadInText.setFont(Font.font("Georgia", FontPosture.ITALIC, 22));
        leadInText.setFill(Color.web("#ccbbee"));
        leadInText.setWrappingWidth(660);

        promptText.setFont(Font.font("Georgia", 16));
        promptText.setFill(Color.web("#776688"));

        // Word choices
        wordChoiceArea.setAlignment(Pos.CENTER);

        // Assemble content
        VBox contentBox = new VBox(18);
        contentBox.setAlignment(Pos.CENTER);
        contentBox.setPadding(new Insets(48, 60, 48, 60));
        contentBox.setMaxWidth(760);
        contentBox.getChildren().addAll(
            titleText, subtitle, poemAccumulator, sep, leadInText, promptText, wordChoiceArea
        );

        root.getChildren().addAll(backdrop, paper, contentBox);
        root.setAlignment(Pos.CENTER);

        Scene scene = new Scene(root, Main.W, Main.H);
        scene.setFill(Color.web("#0c0818"));
        stage.setScene(scene);
    }

    private void showCurrentLine() {
        if (currentLine >= LEADS()) {
            showFinishedPoem();
            return;
        }

        // Update lead-in
        leadInText.setText(leadIns()[currentLine] + "...");

        // Build word buttons
        wordChoiceArea.getChildren().clear();
        for (int i = 0; i < words()[currentLine].length; i++) {
            final int idx = i;
            String word = words()[currentLine][i][0];
            Button btn = wordButton(leadIns()[currentLine] + " " + word + ".");
            btn.setOnAction(e -> pickWord(idx));
            wordChoiceArea.getChildren().add(btn);
        }
    }

    private void pickWord(int idx) {
        chosenWords[currentLine] = words()[currentLine][idx][0];
        chosenTones[currentLine] = words()[currentLine][idx][1];

        // Add chosen line to accumulator with a fade-in
        String fullLine = leadIns()[currentLine] + " " + chosenWords[currentLine] + ".";
        Text lineText = new Text(fullLine);
        lineText.setFont(Font.font("Georgia", FontPosture.ITALIC, 19));
        lineText.setFill(Color.web("#e8d8ff"));
        lineText.setOpacity(0);
        poemAccumulator.getChildren().add(lineText);

        FadeTransition ft = new FadeTransition(Duration.millis(500), lineText);
        ft.setFromValue(0); ft.setToValue(1); ft.play();

        currentLine++;

        PauseTransition pause = new PauseTransition(Duration.millis(400));
        pause.setOnFinished(ev -> showCurrentLine());
        pause.play();
    }

    private void showFinishedPoem() {
        // Compute dominant tone
        Map<String, Integer> toneCount = new HashMap<>();
        for (String t : chosenTones) toneCount.merge(t, 1, Integer::sum);
        String dominant = toneCount.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey).orElse(TONE_EARNEST);
        PlayerState.get().setPoemTone(dominant);

        // Save full poem text
        StringBuilder poemText = new StringBuilder();
        for (int i = 0; i < LEADS(); i++) {
            poemText.append(leadIns()[i]).append(" ").append(chosenWords[i]).append(".\n");
        }
        PlayerState.get().setPoemText(poemText.toString().trim());

        // Clear word area, show poem title, show continue
        wordChoiceArea.getChildren().clear();
        leadInText.setText("");
        promptText.setText("");
        titleText.setText("~ " + playerName + "'s Poem ~");

        // Add tone flavour note
        String toneNote = switch (dominant) {
            case TONE_EARNEST  -> "\"Scatha will be pleased. This poem has good topsoil energy.\" — Vermia";
            case TONE_SARDONIC -> "\"It has a certain... dry quality. I respect it.\" — Mawla";
            case TONE_TENDER   -> "\"Oh.\" (Grubwin needs a moment.)";
            default -> "";
        };

        Text toneText = new Text(toneNote);
        toneText.setFont(Font.font("Georgia", FontPosture.ITALIC, 17));
        toneText.setFill(Color.web("#aa88cc"));
        toneText.setOpacity(0);
        toneText.setWrappingWidth(620);
        toneText.setTextAlignment(TextAlignment.CENTER);

        Button continueBtn = continueButton();
        continueBtn.setOpacity(0);
        continueBtn.setOnAction(e -> returnTo.returnFromPoem());

        wordChoiceArea.setAlignment(Pos.CENTER);
        wordChoiceArea.getChildren().addAll(toneText, continueBtn);

        // Staggered fade-in: tone text appears first, then continue button
        continueBtn.setDisable(true);

        FadeTransition f1 = new FadeTransition(Duration.millis(700), toneText);
        f1.setFromValue(0); f1.setToValue(1);

        FadeTransition f2 = new FadeTransition(Duration.millis(500), continueBtn);
        f2.setFromValue(0); f2.setToValue(1);
        f2.setOnFinished(ev -> continueBtn.setDisable(false));

        SequentialTransition seq = new SequentialTransition(
            new PauseTransition(Duration.millis(400)),
            f1,
            new PauseTransition(Duration.millis(300)),
            f2
        );
        seq.play();
    }

    private int LEADS() { return leadIns().length; }

    // ── Styled buttons ─────────────────────────────────────────────────────

    private Button wordButton(String fullLine) {
        Button btn = new Button(fullLine);
        btn.setFont(Font.font("Georgia", FontPosture.ITALIC, 19));
        btn.setWrapText(true);
        btn.setPrefWidth(680);
        btn.setMinHeight(52);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setPadding(new Insets(10, 20, 10, 20));

        String base  = "-fx-background-color: rgba(30,10,50,0.7); -fx-text-fill: #ccbbee; -fx-border-color: rgba(150,80,220,0.25); -fx-border-radius: 8; -fx-background-radius: 8; -fx-cursor: hand;";
        String hover = "-fx-background-color: rgba(80,20,120,0.75); -fx-text-fill: #eedcff; -fx-border-color: rgba(200,130,255,0.7); -fx-border-radius: 8; -fx-background-radius: 8; -fx-cursor: hand;";
        btn.setStyle(base);
        btn.setOnMouseEntered(e -> btn.setStyle(hover));
        btn.setOnMouseExited(e  -> btn.setStyle(base));
        return btn;
    }

    private Button continueButton() {
        Button btn = new Button("Continue →");
        btn.setFont(Font.font("Georgia", FontWeight.BOLD, 22));
        btn.setPrefWidth(260); btn.setPrefHeight(50);
        btn.setStyle(
            "-fx-background-color: rgba(80,20,120,0.85); -fx-text-fill: #ffeecc;" +
            "-fx-border-color: #aa66ff; -fx-border-radius: 8; -fx-background-radius: 8; -fx-cursor: hand;"
        );
        return btn;
    }
}
