package com.wwlc.dialogue;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import javax.script.*;

/**
 * Reads script files from assets/scripts/<name>.json
 *
 * Script format:
 * [
 *   { "type": "bg",     "file": "clubroom_day" },
 *   { "type": "music",  "file": "scatha_theme" },
 *   { "type": "char",   "name": "Vermia", "expr": "normal", "pos": "left",  "visible": true },
 *   { "type": "line",   "speaker": "Vermia", "text": "Hello!" },
 *   { "type": "line",   "speaker": null,     "text": "Narrator." },
 *   { "type": "choice", "options": [
 *       { "label": "Option A", "jump": "scene_a" },
 *       { "label": "Option B", "jump": "scene_b" }
 *   ]},
 *   { "type": "jump",   "scene": "act2_scene1" },
 *   { "type": "poem" }
 * ]
 */
public class DialogueManager {

    public interface Listener {
        void onLine(String speaker, String text);
        void onBgChange(String file);
        void onCharChange(String name, String expr, String pos, boolean visible);
        void onMusicChange(String file);
        void onChoices(List<Choice> choices);
        void onPoem();
        void onEnd();
        void onActCard(String title, String subtitle, String nextScene);
    }

    public record Choice(String label, String jump) {}

    private final Listener listener;
    private List<Map<String, Object>> lines = new ArrayList<>();
    private int cursor = 0;
    private boolean waitingForChoice = false;

    public DialogueManager(Listener listener) {
        this.listener = listener;
    }

    private String currentScene = "";
    public String getCurrentScene() { return currentScene; }

    public void loadScene(String name) {
        this.currentScene = name;
        // Use localised script if language is FR and file exists
        String localName = com.wwlc.system.LangManager.scriptName(name);
        String path = "assets/scripts/" + localName + ".json";
        if (!localName.equals(name) && !new java.io.File(path).exists()) {
            // FR file missing, fall back to English
            path = "assets/scripts/" + name + ".json";
        }
        try {
            InputStream is = getStream(path);
            if (is == null) { System.err.println("Script not found: " + path); return; }
            String json = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            lines = parseJsonArray(json);
            cursor = 0;
            waitingForChoice = false;
        } catch (Exception e) {
            System.err.println("Error loading scene " + name + ": " + e.getMessage());
        }
    }

    private InputStream getStream(String path) {
        // Try classpath first (when running from jar)
        InputStream is = getClass().getClassLoader().getResourceAsStream(path);
        if (is != null) return is;
        // Try filesystem (when running from IDE/gradle)
        try { return new FileInputStream(path); } catch (Exception e) { return null; }
    }

    public void advance() {
        if (waitingForChoice) return;
        if (cursor >= lines.size()) { listener.onEnd(); return; }
        process(lines.get(cursor++));
    }

    @SuppressWarnings("unchecked")
    private void process(Map<String, Object> e) {
        String type = (String) e.get("type");
        switch (type) {
            case "line" -> {
                Object spk = e.get("speaker");
                listener.onLine(spk == null || spk.toString().equals("null") ? null : spk.toString(),
                                (String) e.get("text"));
            }
            case "bg"    -> { listener.onBgChange((String) e.get("file")); advance(); }
            case "music" -> { listener.onMusicChange((String) e.get("file")); advance(); }
            case "sfx"   -> { com.wwlc.system.AudioManager.get().playSfx((String) e.get("file")); advance(); }
            case "char"  -> {
                Object vis = e.getOrDefault("visible", true);
                listener.onCharChange(
                    (String) e.get("name"),
                    (String) e.getOrDefault("expr", "normal"),
                    (String) e.getOrDefault("pos", "center"),
                    vis instanceof Boolean ? (Boolean) vis : Boolean.parseBoolean(vis.toString())
                );
                advance();
            }
            case "choice" -> {
                List<Map<String, Object>> opts = (List<Map<String, Object>>) e.get("options");
                List<Choice> choices = opts.stream()
                    .map(o -> new Choice((String) o.get("label"), (String) o.get("jump")))
                    .toList();
                waitingForChoice = true;
                listener.onChoices(choices);
            }
            case "jump"    -> { loadScene((String) e.get("scene")); advance(); }
            case "poem"    -> listener.onPoem();
            case "end"     -> listener.onEnd();
            case "actcard" -> listener.onActCard((String) e.get("title"), (String) e.getOrDefault("subtitle", ""), (String) e.get("next"));
        }
    }

    public void pickChoice(Choice c) {
        waitingForChoice = false;
        loadScene(c.jump());
        advance();
    }

    public boolean isWaiting() { return waitingForChoice; }

    // ---- Minimal JSON parser (no external deps) ----
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> parseJsonArray(String json) {
        // Use the built-in Nashorn/scripting engine to parse JSON
        // This avoids adding a JSON library dependency
        try {
            ScriptEngine engine = new ScriptEngineManager().getEngineByName("javascript");
            if (engine == null) {
                // Fallback: manual parse for simple cases
                return manualParse(json);
            }
            engine.eval("var result = JSON.parse(" + escapeForJs(json) + ");");
            Object result = engine.get("result");
            return convertJsArray(result);
        } catch (Exception e) {
            return manualParse(json);
        }
    }

    private String escapeForJs(String json) {
        // Wrap in backtick template literal to safely pass to JS engine
        return "`" + json.replace("`", "\\`") + "`";
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> convertJsArray(Object obj) {
        List<Map<String, Object>> result = new ArrayList<>();
        if (obj instanceof List<?> list) {
            for (Object item : list) {
                if (item instanceof Map<?, ?> map) {
                    result.add((Map<String, Object>) map);
                }
            }
        }
        return result;
    }

    // Simple manual JSON parser as fallback
    private List<Map<String, Object>> manualParse(String json) {
        List<Map<String, Object>> entries = new ArrayList<>();
        // Split by "}, {" pattern to get individual objects
        json = json.trim();
        if (json.startsWith("[")) json = json.substring(1);
        if (json.endsWith("]")) json = json.substring(0, json.length() - 1);

        // Find each {...} block
        int depth = 0, start = -1;
        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '{') { if (depth++ == 0) start = i; }
            else if (c == '}') { if (--depth == 0 && start >= 0) {
                entries.add(parseObject(json.substring(start, i + 1)));
                start = -1;
            }}
        }
        return entries;
    }

    private Map<String, Object> parseObject(String obj) {
        Map<String, Object> map = new LinkedHashMap<>();
        obj = obj.trim();
        if (obj.startsWith("{")) obj = obj.substring(1);
        if (obj.endsWith("}")) obj = obj.substring(0, obj.length() - 1);

        // Extract key-value pairs - handles "key": "value" and "key": true/false/null
        int i = 0;
        while (i < obj.length()) {
            // Find key
            int ks = obj.indexOf('"', i); if (ks < 0) break;
            int ke = obj.indexOf('"', ks + 1); if (ke < 0) break;
            String key = obj.substring(ks + 1, ke);
            int colon = obj.indexOf(':', ke); if (colon < 0) break;
            i = colon + 1;
            while (i < obj.length() && obj.charAt(i) == ' ') i++;
            if (i >= obj.length()) break;
            char first = obj.charAt(i);
            if (first == '"') {
                int vs = i + 1;
                int ve = vs;
                while (ve < obj.length() && (obj.charAt(ve) != '"' || (ve > 0 && obj.charAt(ve-1) == '\\'))) ve++;
                map.put(key, obj.substring(vs, ve).replace("\\\"", "\""));
                i = ve + 1;
            } else if (first == '[') {
                // nested array - store as raw string for options
                int depth = 0, as = i;
                do { char c = obj.charAt(i); if (c == '[') depth++; else if (c == ']') depth--; i++; } while (depth > 0 && i < obj.length());
                map.put(key, parseOptions(obj.substring(as, i)));
            } else {
                // boolean / null / number
                int end = i;
                while (end < obj.length() && obj.charAt(end) != ',' && obj.charAt(end) != '}') end++;
                String val = obj.substring(i, end).trim();
                if (val.equals("true")) map.put(key, true);
                else if (val.equals("false")) map.put(key, false);
                else if (val.equals("null")) map.put(key, null);
                else map.put(key, val);
                i = end;
            }
            // skip comma
            while (i < obj.length() && (obj.charAt(i) == ',' || obj.charAt(i) == ' ')) i++;
        }
        return map;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> parseOptions(String arr) {
        return manualParse(arr);
    }
}
