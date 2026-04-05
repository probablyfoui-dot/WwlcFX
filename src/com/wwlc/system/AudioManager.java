package com.wwlc.system;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import java.io.File;
import java.net.URL;

/**
 * Handles BGM and SFX. Supports .ogg and .mp3.
 * BGM loops seamlessly. SFX plays once.
 */
public class AudioManager {

    private static AudioManager instance;
    public static AudioManager get() {
        if (instance == null) instance = new AudioManager();
        return instance;
    }

    private MediaPlayer bgmPlayer;
    private String currentBgm = "";

    // ── BGM ───────────────────────────────────────────────────────────────────

    public void playBgm(String filename) {
        // Randomly pick between clubroom_normal and clubroom_normal2 for variety
        if (filename.equals("clubroom_normal") && Math.random() > 0.5) {
            String alt = resolveAudio("assets/audio/clubroom_normal2");
            if (alt != null) filename = "clubroom_normal2";
        }
        if (filename.equals(currentBgm)) return; // already playing
        stopBgm();
        String uri = resolveAudio("assets/audio/" + filename);
        if (uri == null) {
            System.out.println("[Audio] BGM not found: " + filename);
            return;
        }
        try {
            MediaPlayer player = new MediaPlayer(new Media(uri));
            player.setVolume(GameSettings.get().getMusicVolume());
            player.setCycleCount(MediaPlayer.INDEFINITE); // loop forever
            player.setOnEndOfMedia(() -> player.seek(Duration.ZERO));
            player.play();
            bgmPlayer = player;
            currentBgm = filename;
        } catch (Exception e) {
            System.out.println("[Audio] Failed to play BGM: " + filename + " — " + e.getMessage());
        }
    }

    public void stopBgm() {
        if (bgmPlayer != null) {
            bgmPlayer.stop();
            bgmPlayer.dispose();
            bgmPlayer = null;
            currentBgm = "";
        }
    }

    public void setBgmVolume(float v) {
        if (bgmPlayer != null) bgmPlayer.setVolume(v);
    }

    // ── SFX ───────────────────────────────────────────────────────────────────

    public void playSfx(String filename) {
        String uri = resolveAudio("assets/audio/" + filename);
        if (uri == null) return;
        try {
            MediaPlayer sfx = new MediaPlayer(new Media(uri));
            sfx.setVolume(GameSettings.get().getSfxVolume());
            sfx.play();
            // Dispose after playback
            sfx.setOnEndOfMedia(sfx::dispose);
        } catch (Exception e) {
            System.out.println("[Audio] Failed to play SFX: " + filename + " — " + e.getMessage());
        }
    }

    // ── Resolve path: tries .ogg then .mp3, classpath then filesystem ─────────

    private String resolveAudio(String basePath) {
        // Strip any existing extension so we can try both
        String base = basePath.replaceAll("\\.(ogg|mp3)$", "");

        for (String ext : new String[]{".ogg", ".mp3"}) {
            String path = base + ext;
            // Try classpath (inside jar / maven resources)
            URL url = getClass().getClassLoader().getResource(path);
            if (url != null) return url.toExternalForm();
            // Try filesystem
            File f = new File(path);
            if (f.exists()) return f.toURI().toString();
        }
        return null;
    }
}
