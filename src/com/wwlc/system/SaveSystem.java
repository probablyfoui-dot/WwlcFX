package com.wwlc.system;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Manages save slots. Each save stores:
 *  - current scene name
 *  - player name
 *  - timestamp
 *
 * Saves live in ~/.wwlc/saves/slot_N.properties
 */
public class SaveSystem {

    public static final int NUM_SLOTS = 3;
    private static final String SAVE_DIR =
        System.getProperty("user.home") + "/.wwlc/saves/";

    public record SaveSlot(int slot, String scene, String playerName, String timestamp) {
        public boolean isEmpty() { return scene == null; }
        public String displayName() {
            if (isEmpty()) return "Empty";
            return playerName + " — " + scene.replace("_", " ") + "\n" + timestamp;
        }
    }

    private static final java.util.Map<String, String> SCENE_NAMES = new java.util.HashMap<>();
    static {
        SCENE_NAMES.put("act1_scene1",          "Act 1 — First Day");
        SCENE_NAMES.put("act1_scene1b_confident","Act 1 — First Day (confident)");
        SCENE_NAMES.put("act1_scene1b_potato",   "Act 1 — First Day (potato)");
        SCENE_NAMES.put("act1_scene1b_stare",    "Act 1 — First Day (stare)");
        SCENE_NAMES.put("act1_scene2",           "Act 1 — First Poem");
        SCENE_NAMES.put("act1_scene3",           "Act 1 — The Hymn");
        SCENE_NAMES.put("act1_scene3b_hymn",     "Act 1 — The Hymn (recite)");
        SCENE_NAMES.put("act1_scene3b_dirtus",   "Act 1 — The Hymn (Dirtus)");
        SCENE_NAMES.put("act1_scene3b_mawla",    "Act 1 — The Hymn (Mawla)");
        SCENE_NAMES.put("act1_scene4",           "Act 1 — After Club");
        SCENE_NAMES.put("act1_scene5",           "Act 1 — The Offering");
        SCENE_NAMES.put("act1_scene5b_vermia",   "Act 1 — The Offering (Vermia)");
        SCENE_NAMES.put("act1_scene5b_dirtus",   "Act 1 — The Offering (Dirtus)");
        SCENE_NAMES.put("act1_scene5b_grubwin",  "Act 1 — The Offering (Grubwin)");
        SCENE_NAMES.put("act1_scene6",           "Act 1 — Sunset");
        SCENE_NAMES.put("act1_scene7",           "Act 1 — Club Competition");
        SCENE_NAMES.put("act1_scene7b_vermia",   "Act 1 — Competition (Vermia)");
        SCENE_NAMES.put("act1_scene7b_grubwin",  "Act 1 — Competition (Grubwin)");
        SCENE_NAMES.put("act1_scene7b_mawla",    "Act 1 — Competition (Mawla)");
        SCENE_NAMES.put("act1_scene8",           "Act 1 — The Ritual");
        SCENE_NAMES.put("act1_scene8b_yes",      "Act 1 — The Ritual (yes)");
        SCENE_NAMES.put("act1_scene8b_compromise","Act 1 — The Ritual (compromise)");
        SCENE_NAMES.put("act1_scene8b_no",       "Act 1 — The Ritual (no)");
        SCENE_NAMES.put("act1_scene9",           "Act 1 — Worm Moon");
        SCENE_NAMES.put("act2_scene1",           "Act 2 — A New Day");
        SCENE_NAMES.put("act2_scene1b_hymn2",    "Act 2 — A New Day (hymn)");
        SCENE_NAMES.put("act2_scene1b_mawla",    "Act 2 — A New Day (Mawla)");
        SCENE_NAMES.put("act2_scene1b_dirtus",   "Act 2 — A New Day (Dirtus)");
        SCENE_NAMES.put("act2_scene2",           "Act 2 — The Library");
        SCENE_NAMES.put("act2_scene3",           "Act 2 — Rooftop");
        SCENE_NAMES.put("act2_scene4",           "Act 2 — The Invitation");
        SCENE_NAMES.put("act2_scene4b_yes",      "Act 2 — The Invitation (yes)");
        SCENE_NAMES.put("act2_scene4b_questions","Act 2 — The Invitation (questions)");
        SCENE_NAMES.put("act2_scene4b_hesitate", "Act 2 — The Invitation (hesitate)");
        SCENE_NAMES.put("act2_scene5",           "Act 2 — Second Poem");
        SCENE_NAMES.put("act2_scene6",           "Act 2 — After the Poem");
        SCENE_NAMES.put("act2_scene7",           "Act 2 — Revelations");
        SCENE_NAMES.put("act2_scene8",           "Act 2 — Underground");
        SCENE_NAMES.put("act3_scene1",           "Act 3 — The Change");
        SCENE_NAMES.put("act3_scene2",           "Act 3 — Unraveling");
        SCENE_NAMES.put("act3_scene3",           "Act 3 — Breaking Point");
        SCENE_NAMES.put("act3_scene4",           "Act 3 — The Truth");
        SCENE_NAMES.put("act3_scene5",           "Act 3 — End of Days");
        SCENE_NAMES.put("act4_scene1",           "Act 4 — The Presentation");
        SCENE_NAMES.put("act4_scene2",           "Act 4 — Final Words");
        SCENE_NAMES.put("act4_ending_true",      "Act 4 — True Ending");
        SCENE_NAMES.put("act4_ending_potato",    "Act 4 — Potato Ending");
    }

    public static String getDisplayName(String scene) {
        return SCENE_NAMES.getOrDefault(scene, scene.replace("_", " "));
    }


    public static SaveSlot load(int slot) {
        File f = slotFile(slot);
        if (!f.exists()) return new SaveSlot(slot, null, "Player", "");
        Properties p = new Properties();
        try (FileInputStream fis = new FileInputStream(f)) {
            p.load(fis);
            return new SaveSlot(
                slot,
                p.getProperty("scene"),
                p.getProperty("playerName", "Player"),
                p.getProperty("timestamp", "")
            );
        } catch (Exception e) {
            return new SaveSlot(slot, null, "Player", "");
        }
    }

    public static void save(int slot, String scene, String playerName) {
        File f = slotFile(slot);
        f.getParentFile().mkdirs();
        Properties p = new Properties();
        p.setProperty("scene",      scene);
        p.setProperty("playerName", playerName);
        p.setProperty("timestamp",
            new SimpleDateFormat("MMM d, HH:mm").format(new Date()));
        try (FileOutputStream fos = new FileOutputStream(f)) {
            p.store(fos, "WWLC Save");
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static void delete(int slot) {
        slotFile(slot).delete();
    }

    public static SaveSlot[] loadAll() {
        SaveSlot[] slots = new SaveSlot[NUM_SLOTS];
        for (int i = 0; i < NUM_SLOTS; i++) slots[i] = load(i);
        return slots;
    }

    private static File slotFile(int slot) {
        return new File(SAVE_DIR + "slot_" + slot + ".properties");
    }
}
