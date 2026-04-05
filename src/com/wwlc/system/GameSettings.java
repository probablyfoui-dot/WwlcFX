package com.wwlc.system;

import java.io.*;
import java.util.Properties;

public class GameSettings {

    private static GameSettings instance;
    private final Properties props = new Properties();
    private final File file;

    private float textSpeed   = 1.0f;
    private float musicVolume = 0.8f;
    private float sfxVolume   = 0.8f;
    private String lang       = "en";

    private GameSettings() {
        file = new File(System.getProperty("user.home") + "/.wwlc/settings.properties");
        load();
    }

    public static GameSettings get() {
        if (instance == null) instance = new GameSettings();
        return instance;
    }

    public float  getTextSpeed()   { return textSpeed; }
    public float  getMusicVolume() { return musicVolume; }
    public float  getSfxVolume()   { return sfxVolume; }
    public String getLang()        { return lang; }

    public void setTextSpeed(float v)   { textSpeed   = Math.max(0, Math.min(3f, v)); save(); }
    public void setMusicVolume(float v) { musicVolume = Math.max(0, Math.min(1f, v)); save(); }
    public void setSfxVolume(float v)   { sfxVolume   = Math.max(0, Math.min(1f, v)); save(); }
    public void setLang(String l)       { lang = l; LangManager.setLang(l); save(); }

    public double getCharDelayMs() {
        if (textSpeed <= 0) return 0;
        return 25.0 / textSpeed;
    }

    private void load() {
        if (!file.exists()) return;
        try (FileInputStream fis = new FileInputStream(file)) {
            props.load(fis);
            textSpeed   = Float.parseFloat(props.getProperty("textSpeed",   "1.0"));
            musicVolume = Float.parseFloat(props.getProperty("musicVolume", "0.8"));
            sfxVolume   = Float.parseFloat(props.getProperty("sfxVolume",   "0.8"));
            lang        = props.getProperty("lang", "en");
            LangManager.setLang(lang);
        } catch (Exception e) { /* use defaults */ }
    }

    public void save() {
        try {
            file.getParentFile().mkdirs();
            props.setProperty("textSpeed",   String.valueOf(textSpeed));
            props.setProperty("musicVolume", String.valueOf(musicVolume));
            props.setProperty("sfxVolume",   String.valueOf(sfxVolume));
            props.setProperty("lang",        lang);
            try (FileOutputStream fos = new FileOutputStream(file)) {
                props.store(fos, "WWLC Settings");
            }
        } catch (Exception e) { e.printStackTrace(); }
    }
}
