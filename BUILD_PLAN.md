# Starquake libGDX App — Implementation Plan

## Context

We're building a mobile clone of Starquake (1985 ZX Spectrum) using libGDX 1.14.0 targeting Android and iOS (plus desktop for testing). All game assets are already extracted and packed into texture atlases at 6x scale. The game data (512 rooms, tiles, collision, teleporters) is in `game_assets/metadata.json`. We follow the Quadronia project structure (`other/Quadronia/`) as a template for gradle setup and module organization, but simplified — no multi-resolution assets, no NCore/NContext framework, no Google Play Services.

## Project Structure

```
Starquake/
├── build.gradle                    # Root: versions, subproject deps
├── settings.gradle                 # include 'android', 'core', 'ios', 'desktop'
├── gradle.properties               # JDK 17, daemon, memory
├── gradle/wrapper/                 # Gradle 8.9 wrapper
│
├── android/
│   ├── build.gradle                # compileSdk 35, minSdk 24, sensorLandscape
│   ├── AndroidManifest.xml
│   ├── src/com/starquake/game/android/
│   │   └── AndroidLauncher.java
│   ├── assets/                     # SHARED asset root
│   │   ├── atlases/                # 9 atlas pairs (.atlas + .png)
│   │   └── metadata.json
│   └── res/values/strings.xml
│
├── core/
│   ├── build.gradle
│   └── src/com/starquake/game/
│       ├── StarquakeGame.java      # Main Game class
│       ├── Assets.java             # Atlas loading + region cache
│       ├── GameFont.java           # Text rendering from font atlas
│       ├── screen/
│       │   ├── LoadingScreen.java  # Async asset loading
│       │   └── GameScreen.java     # Main gameplay screen
│       ├── world/
│       │   ├── GameWorld.java      # Central game state + update loop
│       │   ├── Room.java           # Room data + collision/behavior maps
│       │   ├── RoomRenderer.java   # Tile rendering
│       │   ├── Blob.java           # Player state + physics
│       │   ├── BlobRenderer.java   # BLOB animation
│       │   ├── Enemy.java          # Enemy state + patrol AI
│       │   └── EnemyRenderer.java  # Enemy drawing
│       ├── hud/
│       │   └── Hud.java            # Score, lives, resource bars, inventory
│       └── input/
│           ├── TouchControls.java  # On-screen D-pad + action buttons
│           └── InputManager.java   # Unifies touch + keyboard input
│
├── desktop/
│   ├── build.gradle                # LWJGL3, macOS -XstartOnFirstThread
│   └── src/com/starquake/game/desktop/
│       └── DesktopLauncher.java    # 1280x960 window (4:3 landscape)
│
└── ios/
    ├── build.gradle                # RoboVM 2.3.24, arm64
    ├── robovm.xml                  # refs ../android/assets
    ├── Info.plist.xml              # Landscape only, min iOS 11
    └── src/com/starquake/game/ios/
        └── IOSLauncher.java
```

## Key Architectural Decisions

### Viewport: Dual-viewport system
- **Game viewport**: `FitViewport(1536, 1152)` — game area centered, letterboxed on wide screens
- **Overlay viewport**: `ScreenViewport` covering full physical screen — for touch controls in the letterbox margins
- On 19.5:9 phones: ~25% side margins each side for controls
- On 4:3 tablets: controls overlay semi-transparently on game edges

### On-Screen Controls Layout
```
|  LEFT CONTROLS  |     GAME AREA (4:3)      |  RIGHT CONTROLS  |
|  [SHOOT]        |   1536x1152 viewport      |    [UP]          |
|  [PLATFORM]     |   (FitViewport centered)  |  [LEFT][RIGHT]   |
|  [PICKUP]       |                           |    [DOWN]        |
```
- Right side: D-pad for movement (left/right/up/down)
- Left side: Action buttons (shoot, lay platform, pick up)
- Multi-touch required (move + shoot simultaneously)
- Desktop fallback: arrow keys + Z/X/C

### Assets: Single resolution, pre-cached
- 9 atlases loaded via AssetManager (~953KB total)
- All TextureRegion lookups pre-cached at init into maps (avoid runtime `findRegion()`)
- metadata.json parsed once with libGDX `JsonReader`, held in memory

### No heavy framework
- Standard libGDX `Game` + `Screen` pattern (no NCore/NContext/ScreenWorkflow)
- No FreeType — using the atlas-based pixel font
- No multi-resolution folders — single set of 6x assets

## Build Configuration

Follow Quadronia patterns with these key values:
- **libGDX**: 1.14.0, **RoboVM**: 2.3.24, **AGP**: 8.7.3
- **Android**: compileSdk 35, minSdk 24, targetSdk 35
- **iOS**: min 11.0, landscape-only orientations
- **Java**: source/target 1.8
- **Package**: `com.starquake.game`
- **Orientation**: `sensorLandscape` (Android), landscape-only (iOS Info.plist)
- **Android config**: `useImmersiveMode = true`, no accelerometer/compass

## Core Class Details

### `Assets.java`
- Loads 9 atlases + metadata.json
- Stores terrain atlases in `Map<String, TextureAtlas>` keyed by color
- Pre-caches: terrain regions → `Map<String, TextureRegion>` (key: `"tileIdx_color"`), fixed regions → `IntMap<TextureRegion>`, sprite regions, font regions → `TextureRegion[128]`

### `Room.java`
- Built from metadata on room entry: `Room.build(metadata, roomIndex)`
- Contains: `boolean[18][32]` collision map, `String[18][32]` behavior map, room color, big platform IDs
- Collision built per ATLAS_GUIDE.md algorithm (Y-flipped for libGDX: `(2-row)*2*3 + (1-qRow)*3`)

### `RoomRenderer.java`
- Iterates 4x3 big platform grid → 2x2 tile quadrants per big platform
- Draws terrain tiles from room's color atlas, fixed tiles from fixed atlas
- Position: `x = (col*2 + qCol) * 192`, `y = ((2-row)*2 + qRow) * 144`
- Uses actual region dimensions for draw (handles non-standard tile sizes)

### `Blob.java`
- Position in room pixels, 144x96px hitbox (3x2 blocks)
- Walking: fixed speed ~192 px/sec, no momentum
- Gravity: ~768 px/sec² acceleration, ~384 px/sec terminal velocity
- No jumping — platform-laying is the core mechanic
- Screen transitions: exit edge → load adjacent room, reposition BLOB

### `TouchControls.java`
- Rectangular touch zones in screen coordinates
- Polls all touch pointers each frame for multi-touch
- Renders semi-transparent button shapes via overlay viewport
- Resizes based on FitViewport margins (`gameViewport.getScreenX()`, etc.)

## Implementation Phases

### Phase 1: Project skeleton + room rendering

#### Step 1.1 — Gradle wrapper

Create `gradle/wrapper/gradle-wrapper.properties`:
```properties
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
distributionUrl=https\://services.gradle.org/distributions/gradle-8.9-bin.zip
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
```

Add the standard `gradlew` / `gradlew.bat` shell scripts (copy from any libGDX project or Quadronia). Run `chmod +x gradlew`.

---

#### Step 1.2 — Root `build.gradle`

Based on `other/Quadronia/build.gradle` with these changes:
- Remove gwt-gradle-plugin and gretty classpaths (no HTML target)
- Remove FreeType, Box2DLights, Ashley, AI, Google Services, Firebase, Otto `ext` vars
- Remove FreeType and Google Play `implementation` / `natives` lines from `:android`, `:core`, `:ios`
- Keep: `gdxVersion = '1.14.0'`, `roboVMVersion = '2.3.24'`, `appName = 'Starquake'`

```groovy
buildscript {
    repositories {
        mavenLocal(); mavenCentral(); gradlePluginPortal()
        maven { url "https://oss.sonatype.org/content/repositories/snapshots/" }
        google()
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:8.7.3'
    }
}

allprojects {
    ext {
        appName       = 'Starquake'
        gdxVersion    = '1.14.0'
        roboVMVersion = '2.3.24'
    }
    repositories {
        mavenLocal(); mavenCentral(); google(); gradlePluginPortal()
        maven { url "https://oss.sonatype.org/content/repositories/snapshots/" }
        maven { url "https://oss.sonatype.org/content/repositories/releases/" }
    }
}

project(":android") {
    apply plugin: 'com.android.application'
    configurations { natives }
    dependencies {
        implementation project(":core")
        implementation "com.badlogicgames.gdx:gdx-backend-android:$gdxVersion"
        natives "com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-armeabi-v7a"
        natives "com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-arm64-v8a"
        natives "com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-x86"
        natives "com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-x86_64"
    }
}

project(":core") {
    apply plugin: "java-library"
    dependencies {
        implementation "com.badlogicgames.gdx:gdx:$gdxVersion"
    }
}

project(":ios") {
    apply plugin: "java-library"
    dependencies {
        implementation project(":core")
        api "com.mobidevelop.robovm:robovm-rt:$roboVMVersion"
        api "com.mobidevelop.robovm:robovm-cocoatouch:$roboVMVersion"
        api "com.badlogicgames.gdx:gdx-backend-robovm-metalangle:$gdxVersion"
        api "com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-ios"
    }
}

project(":desktop") {
    apply plugin: "java-library"
    dependencies {
        implementation project(":core")
        api "com.badlogicgames.gdx:gdx-backend-lwjgl3:$gdxVersion"
        api "com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-desktop"
    }
}
```

---

#### Step 1.3 — `settings.gradle`

```groovy
include 'android', 'core', 'ios', 'desktop'
```

---

#### Step 1.4 — `gradle.properties`

```properties
org.gradle.jvmargs=-Xmx2g -XX:MaxMetaspaceSize=512m
org.gradle.daemon=true
org.gradle.parallel=true
org.gradle.caching=true
```

---

#### Step 1.5 — Module build files

**`core/build.gradle`:**
```groovy
sourceCompatibility = JavaVersion.VERSION_1_8
targetCompatibility = JavaVersion.VERSION_1_8
[compileJava, compileTestJava]*.options*.encoding = 'UTF-8'
sourceSets.main.java.srcDirs = ["src/"]
```

**`desktop/build.gradle`:**
Based on `other/Quadronia/desktop/build.gradle` — change `mainClassName` to `com.starquake.game.desktop.DesktopLauncher`. Keep the `run` task with `workingDir = project.assetsDir` and macOS `-XstartOnFirstThread` jvmArg. Add `sourceSets.main.resources.srcDirs = ["../android/assets"]`.

**`android/build.gradle`:**
Based on `other/Quadronia/android/build.gradle` with:
- `namespace 'com.starquake.game.android'`
- `applicationId 'com.starquake.game.android'`
- Remove signing configs block (add later)
- Remove Google Play dependency block
- `screenOrientation="sensorLandscape"` is set in AndroidManifest (not here)
- Keep `copyAndroidNatives` task and `tasks.whenTaskAdded` hook unchanged

**`ios/build.gradle`:**
Based on `other/Quadronia/ios/build.gradle` — change `mainClassName` to `com.starquake.game.ios.IOSLauncher`. Keep RoboVM plugin setup. Can leave signing fields blank for now (`iosSkipSigning = true`).

---

#### Step 1.6 — Copy assets

```bash
mkdir -p android/assets/atlases
cp game_assets/atlases/*.atlas android/assets/atlases/
cp game_assets/atlases/*.png   android/assets/atlases/
cp game_assets/metadata.json   android/assets/
```

Verify: `android/assets/atlases/` contains 18 files (9 `.atlas` + 9 `.png`), and `android/assets/metadata.json` exists.

---

#### Step 1.7 — Platform launchers

**`android/AndroidManifest.xml`** — based on Quadronia's but:
- Remove `INTERNET`, `ACCESS_NETWORK_STATE` permissions (offline game)
- Keep `VIBRATE`, `WAKE_LOCK`, OpenGL ES 2.0 feature
- Change activity name to `com.starquake.game.android.AndroidLauncher`
- `android:screenOrientation="sensorLandscape"`
- `android:configChanges="keyboard|keyboardHidden|orientation|screenSize"`
- No Google Play `<meta-data>`

**`android/res/values/strings.xml`:**
```xml
<resources>
    <string name="app_name">Starquake</string>
</resources>
```

**`android/src/com/starquake/game/android/AndroidLauncher.java`:**
```java
package com.starquake.game.android;

import android.os.Bundle;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.starquake.game.StarquakeGame;

public class AndroidLauncher extends AndroidApplication {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        config.useImmersiveMode = true;
        config.useAccelerometer = false;
        config.useCompass = false;
        initialize(new StarquakeGame(), config);
    }
}
```

**`desktop/src/com/starquake/game/desktop/DesktopLauncher.java`:**
```java
package com.starquake.game.desktop;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.starquake.game.StarquakeGame;

public class DesktopLauncher {
    public static void main(String[] arg) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Starquake");
        config.setWindowedMode(1280, 960);  // 4:3 at 960p
        config.setResizable(true);
        new Lwjgl3Application(new StarquakeGame(), config);
    }
}
```

**`ios/src/com/starquake/game/ios/IOSLauncher.java`:**
```java
package com.starquake.game.ios;

import com.badlogic.gdx.backends.iosrobovm.IOSApplication;
import com.badlogic.gdx.backends.iosrobovm.IOSApplicationConfiguration;
import com.starquake.game.StarquakeGame;
import org.robovm.apple.foundation.NSAutoreleasePool;
import org.robovm.apple.uikit.UIApplication;

public class IOSLauncher extends IOSApplication.Delegate {
    @Override
    protected IOSApplication createApplication() {
        IOSApplicationConfiguration config = new IOSApplicationConfiguration();
        config.orientationLandscape = true;
        config.orientationPortrait = false;
        return new IOSApplication(new StarquakeGame(), config);
    }
    public static void main(String[] argv) {
        NSAutoreleasePool pool = new NSAutoreleasePool();
        UIApplication.main(argv, null, IOSLauncher.class);
        pool.close();
    }
}
```

**`ios/robovm.xml`** — copy from `other/Quadronia/ios/robovm.xml` and change `<mainClass>` to `com.starquake.game.ios.IOSLauncher`. Remove the `data` resource directory block (only `../android/assets` is needed).

**`ios/Info.plist.xml`** — copy from `other/Quadronia/ios/Info.plist.xml` and change `UISupportedInterfaceOrientations` to only landscape:
```xml
<key>UISupportedInterfaceOrientations</key>
<array>
    <string>UIInterfaceOrientationLandscapeRight</string>
    <string>UIInterfaceOrientationLandscapeLeft</string>
</array>
```

---

#### Step 1.8 — `StarquakeGame.java`

```java
package com.starquake.game;

import com.badlogic.gdx.Game;
import com.starquake.game.screen.LoadingScreen;

public class StarquakeGame extends Game {
    public Assets assets;

    @Override
    public void create() {
        assets = new Assets();
        setScreen(new LoadingScreen(this));
    }

    @Override
    public void dispose() {
        super.dispose();
        assets.dispose();
    }
}
```

---

#### Step 1.9 — `Assets.java`

Responsibilities:
- Owns the `AssetManager` and queues all 9 atlas loads
- Exposes `update()` / `isFinished()` for the loading screen progress bar
- After loading completes, pre-caches all `TextureRegion` lookups into fast maps so `findRegion()` is never called at runtime

```java
package com.starquake.game;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.IntMap;
import java.util.HashMap;
import java.util.Map;

public class Assets {
    public final AssetManager manager = new AssetManager();

    // Raw atlas references (set after loading)
    public TextureAtlas fixedAtlas;
    public TextureAtlas spritesAtlas;
    public TextureAtlas fontAtlas;
    public final Map<String, TextureAtlas> terrainAtlases = new HashMap<>();

    // Pre-cached region lookups
    /** Key: "tileIdx_color" e.g. "14_red" → terrain region */
    public final Map<String, TextureRegion> terrainRegions = new HashMap<>();
    /** Key: tile index → fixed-color region */
    public final IntMap<TextureRegion> fixedRegions = new IntMap<>();
    /** ASCII code 0–127 → font glyph region (null if absent) */
    public final TextureRegion[] fontRegions = new TextureRegion[128];

    private static final String[] COLORS = {"red","magenta","cyan","yellow","green","white"};

    public Assets() {
        for (String color : COLORS)
            manager.load("atlases/terrain_" + color + ".atlas", TextureAtlas.class);
        manager.load("atlases/fixed.atlas",   TextureAtlas.class);
        manager.load("atlases/sprites.atlas", TextureAtlas.class);
        manager.load("atlases/font.atlas",    TextureAtlas.class);
    }

    /** Call each frame from LoadingScreen; returns true when done. */
    public boolean update() {
        if (!manager.update()) return false;
        if (fixedAtlas == null) buildCaches();
        return true;
    }

    public float getProgress() { return manager.getProgress(); }

    private void buildCaches() {
        for (String color : COLORS) {
            TextureAtlas atlas = manager.get("atlases/terrain_" + color + ".atlas");
            terrainAtlases.put(color, atlas);
            for (TextureAtlas.AtlasRegion r : atlas.getRegions())
                terrainRegions.put(r.name + "_" + color, r);
                // name already contains color (e.g. "tile_014_red") but key is
                // region.name so use it directly: terrainRegions.put(r.name, r)
                // Caller looks up by "tile_NNN_color" which matches region name.
        }
        fixedAtlas   = manager.get("atlases/fixed.atlas");
        spritesAtlas = manager.get("atlases/sprites.atlas");
        fontAtlas    = manager.get("atlases/font.atlas");

        for (TextureAtlas.AtlasRegion r : fixedAtlas.getRegions())
            fixedRegions.put(r.index, r);  // index = tile index

        for (TextureAtlas.AtlasRegion r : fontAtlas.getRegions())
            if (r.index >= 0 && r.index < 128) fontRegions[r.index] = r;
    }

    public void dispose() { manager.dispose(); }
}
```

> **Cache key clarification**: terrain region names in the atlas already include color (e.g. `tile_014_red`). The lookup key used throughout the codebase is `String.format("tile_%03d_%s", tileIdx, roomColor)` — this matches the atlas region name directly, so `terrainRegions` should be keyed by `r.name` (not `r.name + "_" + color`). Fix the `buildCaches()` line accordingly:
> ```java
> terrainRegions.put(r.name, r);  // key = "tile_014_red"
> ```

---

#### Step 1.10 — `LoadingScreen.java`

Shows a simple progress bar while `AssetManager` loads asynchronously, then transitions to `GameScreen`.

```java
package com.starquake.game.screen;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.Gdx;
import com.starquake.game.StarquakeGame;

public class LoadingScreen implements Screen {
    private final StarquakeGame game;
    private final ShapeRenderer shapes = new ShapeRenderer();

    public LoadingScreen(StarquakeGame game) { this.game = game; }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (game.assets.update()) {
            dispose();
            game.setScreen(new GameScreen(game, 0));  // start at room 0
            return;
        }

        // Draw a simple white progress bar (centered, 400x20px in screen coords)
        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();
        float barW = w * 0.5f;
        float barH = 20;
        float progress = game.assets.getProgress();

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.3f, 0.3f, 0.3f, 1);
        shapes.rect(w * 0.25f, h * 0.5f - barH / 2, barW, barH);
        shapes.setColor(1, 1, 1, 1);
        shapes.rect(w * 0.25f, h * 0.5f - barH / 2, barW * progress, barH);
        shapes.end();
    }

    @Override public void show() {}
    @Override public void resize(int w, int h) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void dispose() { shapes.dispose(); }
}
```

---

#### Step 1.11 — `Room.java`

Holds all data for a single room. Built once on room entry, discarded on exit.

```java
package com.starquake.game.world;

import com.badlogic.gdx.utils.JsonValue;

public class Room {
    public final int roomIndex;
    public final String color;
    public final int colorIndex;
    public final int[] bigPlatformIds;       // 12 entries (4 cols x 3 rows)
    public final boolean[][] collision;      // [18 rows][32 cols] of 48px blocks
    public final String[][] behaviorMap;     // same grid, behavior string per block

    private Room(int roomIndex, String color, int colorIndex,
                 int[] bigPlatformIds, boolean[][] collision, String[][] behaviorMap) {
        this.roomIndex      = roomIndex;
        this.color          = color;
        this.colorIndex     = colorIndex;
        this.bigPlatformIds = bigPlatformIds;
        this.collision      = collision;
        this.behaviorMap    = behaviorMap;
    }

    /** Factory — builds Room from metadata. Call on room entry. */
    public static Room build(JsonValue metadata, int roomIndex) {
        JsonValue roomData  = metadata.get("rooms").get(roomIndex);
        String color        = roomData.getString("color");
        int colorIndex      = roomData.getInt("color_index");
        int[] bpIds         = roomData.get("big_platforms").asIntArray();

        boolean[][] collision  = new boolean[18][32];
        String[][] behaviorMap = new String[18][32];
        // Default behavior is empty string (no special behavior)
        for (String[] row : behaviorMap)
            java.util.Arrays.fill(row, "");

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 4; col++) {
                int bpIdx   = bpIds[row * 4 + col];
                JsonValue bp = metadata.get("big_platforms").get(bpIdx);

                int[][] quads = {
                    {bp.getInt("tl"), 0, 1},
                    {bp.getInt("tr"), 1, 1},
                    {bp.getInt("bl"), 0, 0},
                    {bp.getInt("br"), 1, 0}
                };

                for (int[] q : quads) {
                    int tileIdx = q[0], qCol = q[1], qRow = q[2];
                    JsonValue tile = metadata.get("tiles").get(String.valueOf(tileIdx));
                    if (tile == null) continue;

                    String behavior = tile.getString("behavior", "");
                    JsonValue solidity = tile.get("solidity");

                    // Base block coords in the 32x18 collision grid
                    int baseCol = (col * 2 + qCol) * 4;
                    int baseRow = (2 - row) * 6 + (1 - qRow) * 3;  // Y-flip

                    if (solidity != null) {
                        for (int tr = 0; tr < solidity.size; tr++) {
                            JsonValue solidRow = solidity.get(tr);
                            for (int tc = 0; tc < solidRow.size; tc++) {
                                int cr = baseRow + tr;
                                int cc = baseCol + tc;
                                if (cr < 0 || cr >= 18 || cc < 0 || cc >= 32) continue;
                                if (!solidRow.get(tc).isNull() && solidRow.get(tc).asBoolean())
                                    collision[cr][cc] = true;
                                if (!behavior.isEmpty())
                                    behaviorMap[cr][cc] = behavior;
                            }
                        }
                    }
                }
            }
        }

        return new Room(roomIndex, color, colorIndex, bpIds, collision, behaviorMap);
    }
}
```

---

#### Step 1.12 — `RoomRenderer.java`

Draws the 4×3 big-platform grid. Uses pre-cached regions from `Assets` — no `findRegion()` at draw time.

```java
package com.starquake.game.world;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.JsonValue;
import com.starquake.game.Assets;

public class RoomRenderer {
    private static final int TILE_W = 192;
    private static final int TILE_H = 144;

    private final Assets assets;
    private final JsonValue metadata;

    public RoomRenderer(Assets assets, JsonValue metadata) {
        this.assets   = assets;
        this.metadata = metadata;
    }

    public void render(SpriteBatch batch, Room room) {
        int[] bpIds = room.bigPlatformIds;

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 4; col++) {
                int bpIdx   = bpIds[row * 4 + col];
                JsonValue bp = metadata.get("big_platforms").get(bpIdx);

                int[][] quads = {
                    {bp.getInt("tl"), 0, 1},
                    {bp.getInt("tr"), 1, 1},
                    {bp.getInt("bl"), 0, 0},
                    {bp.getInt("br"), 1, 0}
                };

                for (int[] q : quads) {
                    int tileIdx = q[0], qCol = q[1], qRow = q[2];
                    JsonValue tile = metadata.get("tiles").get(String.valueOf(tileIdx));
                    if (tile == null) continue;

                    String tileType = tile.getString("type", "empty");
                    if (tileType.equals("empty")) continue;

                    TextureRegion region;
                    if (tileType.equals("fixed")) {
                        region = assets.fixedRegions.get(tileIdx);
                    } else {
                        String key = String.format("tile_%03d_%s", tileIdx, room.color);
                        region = assets.terrainRegions.get(key);
                    }
                    if (region == null) continue;

                    float x = (col * 2 + qCol) * TILE_W;
                    float y = ((2 - row) * 2 + qRow) * TILE_H;
                    batch.draw(region, x, y, region.getRegionWidth(), region.getRegionHeight());
                }
            }
        }
    }
}
```

---

#### Step 1.13 — `GameScreen.java`

Sets up dual viewports, holds the current `Room` + `RoomRenderer`, and renders the scene. In Phase 1, hardcode `roomIndex = 0` and no BLOB/HUD yet.

```java
package com.starquake.game.screen;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.starquake.game.StarquakeGame;
import com.starquake.game.world.Room;
import com.starquake.game.world.RoomRenderer;

public class GameScreen implements Screen {
    // Viewport constants
    public static final int VIEWPORT_W = 1536;
    public static final int VIEWPORT_H = 1152;

    private final StarquakeGame game;
    private final SpriteBatch batch = new SpriteBatch();

    // Game area: FitViewport(1536×1152) — letterboxed, 4:3
    private final FitViewport gameViewport = new FitViewport(VIEWPORT_W, VIEWPORT_H);
    // Overlay: ScreenViewport — covers full physical screen (for touch controls later)
    private final ScreenViewport overlayViewport = new ScreenViewport();

    private final JsonValue metadata;
    private Room room;
    private final RoomRenderer roomRenderer;

    public GameScreen(StarquakeGame game, int startRoom) {
        this.game = game;
        metadata    = new JsonReader().parse(Gdx.files.internal("metadata.json"));
        room        = Room.build(metadata, startRoom);
        roomRenderer = new RoomRenderer(game.assets, metadata);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // --- Game layer ---
        gameViewport.apply();
        batch.setProjectionMatrix(gameViewport.getCamera().combined);
        batch.begin();
        roomRenderer.render(batch, room);
        batch.end();

        // --- Overlay layer (Phase 2+: touch controls drawn here) ---
        // overlayViewport.apply();
        // batch.setProjectionMatrix(overlayViewport.getCamera().combined);
        // batch.begin(); ... batch.end();
    }

    @Override
    public void resize(int width, int height) {
        gameViewport.update(width, height, true);
        overlayViewport.update(width, height, true);
    }

    @Override public void show() {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        batch.dispose();
    }
}
```

---

#### Step 1.14 — Verify

```bash
./gradlew :desktop:run
```

Expected result:
- 1280×960 window opens
- Black background with room 0 tiles rendered in correct color (room 0 color = "white" per metadata)
- No console errors about missing atlas regions or missing files
- Change `startRoom` argument in `GameScreen` constructor to e.g. `50` or `100` → different room colors render

Smoke-test the atlas cache: add a temporary log line in `Assets.buildCaches()`:
```java
Gdx.app.log("Assets", "Loaded " + terrainRegions.size() + " terrain regions, "
    + fixedRegions.size + " fixed regions, "
    + spritesAtlas.getRegions().size + " sprite regions");
```
Expect: **272 terrain regions** (47+43+43+48+46+45 across 6 atlases), **32 fixed regions**, **39 sprite regions**.

### Phase 2: BLOB + movement + collision + touch controls
- Implement `Blob` + `BlobRenderer` (walk animation)
- Build collision map in `Room`
- Horizontal movement with collision resolution
- Gravity + landing on solid blocks
- Implement `InputManager` with keyboard support (desktop)
- Implement `TouchControls` with dual-viewport overlay (D-pad right, actions left)
- Multi-touch support for simultaneous movement + actions
- **Verify**: BLOB walks/falls on desktop with keyboard, and on Android device with touch controls

### Phase 3: Screen transitions
- Exit-edge detection → load adjacent room → reposition BLOB
- Handle world boundaries (no transition off edge of 16x32 grid)
- **Verify**: Walk through multiple rooms on both desktop and device

### Phase 4: HUD + font
- Implement `GameFont` from font atlas (cache all 88 char regions)
- Implement `Hud` in top 288px of viewport (score, lives, resource bars)
- **Verify**: HUD displays with placeholder values

### Phase 5: Platform-laying mechanic
- Lay temp platforms at BLOB's feet, add to collision map
- Timer → crumble (remove from collision map)
- Decrement platforms resource
- **Verify**: BLOB can build platforms to reach higher areas

### Phase 6: Tile behaviors (hazards, tubes, crumble)
- Check behavior map at BLOB position each frame
- Hazard → lose life, Tube → upward force, Ray → periodic on/off, Crumble → collapse on contact
- **Verify**: Tubes transport BLOB upward, hazards kill

### Phase 7: Enemies + shooting
- Simple patrol AI (horizontal/vertical bounce)
- Procedural spawning (1-3 per room, enemy data not in metadata)
- Collision with BLOB → drain energy
- Laser projectile → kills enemies, decrements ammo
- Sprites: `enemy` region index 0–18 (19-frame looped animation); `laser` index 0–7 (8-frame looped); `materialise` index 0–3 (4-frame one-shot on spawn/death)
- **Verify**: Enemies patrol, BLOB can shoot them

### Phase 8: Teleporters
- Detect teleporter tiles → show code-entry UI
- Look up code → warp to target room
- **Verify**: Teleporting with known codes works

### Phase 9: Items + inventory
- Pickup detection at pickup-behavior tiles
- 4-slot FIFO inventory in HUD
- Resource pickups refill energy/ammo/platforms

## Critical Reference Files

| File | Purpose |
|------|---------|
| `game_assets/ATLAS_GUIDE.md` | Rendering algorithm, collision system, all code patterns |
| `game_assets/metadata.json` | Room layouts, tiles, collision, teleporters (copy to android/assets/) |
| `game_assets/atlases/*` | 9 atlas pairs (copy to android/assets/atlases/) |
| `other/Quadronia/build.gradle` | Template for root gradle config |
| `other/Quadronia/android/build.gradle` | Template for Android module |
| `other/Quadronia/ios/build.gradle` + `robovm.xml` + `Info.plist.xml` | Template for iOS module |
| `other/Quadronia/desktop/build.gradle` | Template for desktop module |
| `research/gameplay-mechanics.md` | Movement speeds, resource system, mechanics details |
| `research/map-and-levels.md` | Room grid layout, tile hierarchy, screen transitions |

## Verification (Phase 1)

After Phase 1 implementation:
1. `cd Starquake && ./gradlew :desktop:run` — should open a 1280x960 window showing room 0 with correct tiles and colors
2. Check that all 9 atlases load without errors in console
3. Navigate to different rooms by changing the hardcoded room index — verify different room colors render correctly
