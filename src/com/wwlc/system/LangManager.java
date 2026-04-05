package com.wwlc.system;

import java.util.Properties;

/**
 * Manages game language. Supported: "en", "fr"
 * Persisted to ~/.wwlc/settings.properties via GameSettings.
 */
public class LangManager {

    private static String lang = "en";

    public static String getLang() { return lang; }

    public static void setLang(String l) {
        lang = l;
    }

    public static boolean isFrench() { return "fr".equals(lang); }

    /** Returns the script filename to load, inserting _fr suffix if needed */
    public static String scriptName(String base) {
        if (isFrench()) {
            // e.g. act1_scene1 → act1_scene1_fr
            return base + "_fr";
        }
        return base;
    }

    // ── UI strings ────────────────────────────────────────────────────────────

    public static String t(String key) {
        return isFrench() ? FR.getProperty(key, key) : EN.getProperty(key, key);
    }

    private static final Properties EN = new Properties();
    private static final Properties FR = new Properties();

    static {
        // English
        EN.setProperty("btn.menu",      "Menu");
        EN.setProperty("btn.auto",      "Auto");
        EN.setProperty("btn.save",      "Save");
        EN.setProperty("btn.settings",  "Settings");
        EN.setProperty("btn.close",     "Close ✕");
        EN.setProperty("btn.back",      "← Back");
        EN.setProperty("btn.newgame",   "New Game");
        EN.setProperty("btn.loadgame",  "Load Game");
        EN.setProperty("btn.quit",      "Quit");
        EN.setProperty("btn.fullscreen","Fullscreen ⛶");
        EN.setProperty("btn.windowed",  "Windowed ⛶");
        EN.setProperty("settings.title","Settings");
        EN.setProperty("settings.speed","Text Speed");
        EN.setProperty("settings.music","Music Volume");
        EN.setProperty("settings.sfx",  "SFX Volume");
        EN.setProperty("settings.lang", "Language");
        EN.setProperty("settings.fast", "Fast");
        EN.setProperty("settings.slow", "Slow");
        EN.setProperty("settings.off",  "Off");
        EN.setProperty("settings.max",  "Max");
        EN.setProperty("save.title",    "Save Game");
        EN.setProperty("load.title",    "Load Game");
        EN.setProperty("save.empty",    "Empty");
        EN.setProperty("save.slot",     "Slot");
        EN.setProperty("menu.confirm",  "Return to main menu?\nUnsaved progress will be lost.");
        EN.setProperty("name.prompt",   "What is your name?");
        EN.setProperty("name.sub",      "(This is how you'll appear in the story)");
        EN.setProperty("name.placeholder", "Enter your name...");
        EN.setProperty("name.confirm",  "Let's go!");
        EN.setProperty("name.title",    "Welcome");
        EN.setProperty("name.start",    "Begin");
        EN.setProperty("title.game",    "Worm Worm Literature Club");
        EN.setProperty("title.sub",     "~ Praise be to Scatha ~");

        EN.setProperty("confirm.leave", "Leave");
        EN.setProperty("confirm.stay",  "Stay");

        // French
        FR.setProperty("confirm.leave", "Partir");
        FR.setProperty("confirm.stay",  "Rester");
        FR.setProperty("btn.menu",      "Menu");
        FR.setProperty("btn.auto",      "Auto");
        FR.setProperty("btn.save",      "Sauvegarder");
        FR.setProperty("btn.settings",  "Paramètres");
        FR.setProperty("btn.close",     "Fermer ✕");
        FR.setProperty("btn.back",      "← Retour");
        FR.setProperty("btn.newgame",   "Nouvelle Partie");
        FR.setProperty("btn.loadgame",  "Charger");
        FR.setProperty("btn.quit",      "Quitter");
        FR.setProperty("btn.fullscreen","Plein écran ⛶");
        FR.setProperty("btn.windowed",  "Fenêtré ⛶");
        FR.setProperty("settings.title","Paramètres");
        FR.setProperty("settings.speed","Vitesse du texte");
        FR.setProperty("settings.music","Volume musique");
        FR.setProperty("settings.sfx",  "Volume effets");
        FR.setProperty("settings.lang", "Langue");
        FR.setProperty("settings.fast", "Rapide");
        FR.setProperty("settings.slow", "Lent");
        FR.setProperty("settings.off",  "Muet");
        FR.setProperty("settings.max",  "Max");
        FR.setProperty("save.title",    "Sauvegarder");
        FR.setProperty("load.title",    "Charger");
        FR.setProperty("save.empty",    "Vide");
        FR.setProperty("save.slot",     "Slot");
        FR.setProperty("menu.confirm",  "Retourner au menu principal ?\nLa progression non sauvegardée sera perdue.");
        FR.setProperty("name.prompt",   "Quel est ton prénom ?");
        FR.setProperty("name.sub",      "(C'est ainsi que tu apparaîtras dans l'histoire)");
        FR.setProperty("name.placeholder", "Entre ton prénom...");
        FR.setProperty("name.confirm",  "C'est parti !");
        FR.setProperty("name.title",    "Bienvenue");
        FR.setProperty("name.start",    "Commencer");
        FR.setProperty("title.game",    "Worm Worm Literature Club");
        FR.setProperty("title.sub",     "~ Louange à Scatha ~");
    }
}
