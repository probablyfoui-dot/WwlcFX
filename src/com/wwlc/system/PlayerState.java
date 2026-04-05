package com.wwlc.system;

/** Holds runtime state shared across screens. */
public class PlayerState {
    private static PlayerState instance;

    private String playerName   = "Player";
    private String currentScene = "act1_scene1";
    private String poemTone     = "earnest";   // earnest / sardonic / tender
    private String poemText     = "";           // the player's assembled poem

    // Route flags — set by choices across Acts
    private String routeCharacter = null; // "vermia" / "grubwin" / "mawla" / null

    public static PlayerState get() {
        if (instance == null) instance = new PlayerState();
        return instance;
    }

    public String getPlayerName()     { return playerName; }
    public String getCurrentScene()   { return currentScene; }
    public String getPoemTone()       { return poemTone; }
    public String getPoemText()       { return poemText; }
    public String getRouteCharacter() { return routeCharacter; }

    public void setPlayerName(String name) {
        this.playerName = (name == null || name.isBlank()) ? "Player" : name.trim();
    }
    public void setCurrentScene(String scene) { this.currentScene = scene; }
    public void setPoemTone(String tone)       { this.poemTone = tone; }
    public void setPoemText(String text)       { this.poemText = text; }
    public void setRouteCharacter(String c)    { this.routeCharacter = c; }
}
