# Worm Worm Literature Club — JavaFX Version

## Setup (one time)

### 1. Install Maven
```bash
brew install maven
```

### 2. Run the game
```bash
cd wwlc-fx
mvn javafx:run
```

That's it. No Gradle, no wrapper, no JVM flags needed.

---

## Project structure

```
wwlc-fx/
├── pom.xml                          ← build config (touch rarely)
├── src/com/wwlc/
│   ├── Main.java                    ← entry point
│   ├── screens/
│   │   ├── MainMenuScreen.java      ← title screen
│   │   ├── GameScreen.java          ← VN engine + renderer
│   │   └── PoemScreen.java          ← poem minigame
│   └── dialogue/
│       └── DialogueManager.java     ← script reader
└── assets/
    ├── scripts/                     ← JSON story files
    ├── backgrounds/                 ← PNG 1280x720
    ├── characters/                  ← PNG transparent sprites
    └── audio/                       ← OGG music/sfx
```

---

## Writing story scripts

Files go in `assets/scripts/yourscene.json`:

```json
[
  { "type": "bg",    "file": "clubroom_day" },
  { "type": "music", "file": "scatha_theme" },
  { "type": "char",  "name": "Vermia", "expr": "normal", "pos": "left", "visible": true },
  { "type": "line",  "speaker": "Vermia", "text": "What she says." },
  { "type": "line",  "speaker": null,     "text": "Narrator text." },
  { "type": "choice", "options": [
      { "label": "Option A", "jump": "act1_scene2" },
      { "label": "Option B", "jump": "act1_scene3" }
  ]},
  { "type": "jump",  "scene": "act2_scene1" },
  { "type": "poem" }
]
```

Positions: `"left"`, `"center"`, `"right"`

---

## Art specs (same as before)
- **Backgrounds:** PNG, 1280×720px → `assets/backgrounds/name.png`
- **Characters:** PNG with transparency, 512×720px → `assets/characters/vermia/normal.png`

## Controls
- **Click** or **Space/Enter** = advance / skip typewriter
- **Click** a choice button = select it
