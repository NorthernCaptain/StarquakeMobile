# Starquake - Atlas System Guide

## Approach

All graphics are pre-rendered from the ZX Spectrum ROM at **6x scale** with correct per-room colors baked in. There is **no runtime color logic** -- the game simply looks up which color variant of a tile to use for the current room and draws it directly.

The original Spectrum game uses an attribute-based color system where each 8x8 pixel cell has one ink (foreground) and one paper (background) color. Each room has a single terrain color (red, magenta, cyan, yellow, green, or white). Some tiles (tubes, teleporters, hazards) have fixed colors that don't change per room.

We pre-rendered every tile in every color variant it actually needs (traced through the room/big-platform/tile hierarchy), then packed everything into libGDX texture atlases grouped by color.

## Scale and Dimensions

| Original (Spectrum) | Game Assets (6x) |
|---------------------|-------------------|
| 8x8 px block | 48x48 px |
| 32x24 block tile (standard) | 192x144 px |
| 24x16 px BLOB sprite | 144x96 px |
| 256x192 px screen | 1536x1152 px viewport |

Use `FitViewport(1536, 1152)` -- the viewport is taller than the gameplay area (1536x864) to leave room for HUD. FitViewport handles letterboxing on different phone screens.

## Atlas Inventory

### Terrain Atlases (one per color)

Each contains all terrain tiles that appear in rooms of that color. Region names follow the pattern `tile_NNN_color` (e.g., `tile_014_red`).

| Atlas | Regions | Page Size | PNG Size |
|-------|---------|-----------|----------|
| `terrain_red` | 47 | 2048x1024 | 142 KB |
| `terrain_magenta` | 43 | 2048x1024 | 134 KB |
| `terrain_cyan` | 43 | 2048x1024 | 130 KB |
| `terrain_yellow` | 48 | 2048x1024 | 143 KB |
| `terrain_green` | 46 | 2048x1024 | 136 KB |
| `terrain_white` | 45 | 2048x1024 | 140 KB |

### Fixed Atlas

Tiles with built-in colors that don't change per room (tubes, teleporters, hazards, decorations). Region names: `tile` with index (e.g., `tile` index=0).

| Atlas | Regions | Page Size | PNG Size |
|-------|---------|-----------|----------|
| `fixed` | 32 | 1024x1024 | 85 KB |

### Sprites Atlas

BLOB walk animation, enemy animation, laser projectile, and materialise effect. Region names use descriptive names with frame indices.

| Atlas | Regions | Page Size | PNG Size |
|-------|---------|-----------|----------|
| `sprites` | 39 | 2048x512 | 17 KB |

Regions:
- `blob_walk_left` index 0-3 (4 animation frames)
- `blob_walk_right` index 0-3 (4 animation frames)
- `enemy` index 0-18 (19 animation frames — all enemy types share one animated strip)
- `laser` index 0-7 (8 animation frames — horizontal laser bolt)
- `materialise` index 0-3 (4 animation frames — enemy spawn/death effect)

### Font Atlas

Characters from the game's built-in font. Region name: `char` with index = ASCII code.

| Atlas | Regions | Page Size | PNG Size |
|-------|---------|-----------|----------|
| `font` | 88 | 1024x256 | 26 KB |

Each character is 48x48 px. Index values are ASCII codes (32=space, 48-57=digits, 65-90=uppercase, 97-122=lowercase, etc.).

## Total Size

9 atlases, 9 PNG pages, ~953 KB total. All pages fit within 2048x2048 max.

## metadata.json Structure

The `game_assets/metadata.json` file contains everything needed to render rooms AND handle gameplay interactions:

```json
{
  "scale": 6,
  "block_px": 48,
  "tile_standard_px": [192, 144],
  "sprite_px": [144, 96],
  "font_char_px": 48,
  "room_gameplay_px": [1536, 864],
  "viewport": [1536, 1152],

  "big_platforms": [
    { "tl": 20, "tr": 20, "bl": 20, "br": 20 },
    ...
  ],

  "rooms": [
    {
      "big_platforms": [2, 2, 5, 5, 100, 7, 7, 0, 20, 0, 0, 13],
      "color": "white",
      "color_index": 7
    },
    ...
  ],

  "tiles": {
    "0": {
      "type": "fixed",
      "texture": "tile_000",
      "behavior": "tube",
      "raw_type": 81,
      "solidity": [[true,true,true,true],[false,false,false,false],[false,false,false,false]],
      "blocks_wide": 4,
      "blocks_tall": 3
    },
    "9": {
      "type": "terrain",
      "available_colors": ["red", "magenta", "green", "cyan", "yellow", "white"],
      "behavior": "terrain",
      "raw_type": 16,
      "solidity": [[false,false,false,false],[false,false,false,false],[false,false,false,false]],
      "blocks_wide": 4,
      "blocks_tall": 3
    },
    ...
  },

  "tile_registry": {
    "tile_009_red": { "tile": 9, "color": "red", "width": 192, "height": 144 },
    ...
  },

  "teleporters": [
    { "room": 31, "code": "TULSA", "grid_x": 15, "grid_y": 1 },
    { "room": 40, "code": "RAMIX", "grid_x": 8, "grid_y": 2 },
    ...
  ],

  "behaviors": {
    "terrain": "Standard walkable/blocking terrain. Solidity per block from brightness bit.",
    "tube": "Transport tube - carries BLOB upward automatically when entered.",
    "hazard": "Lethal zone - instant kill on contact.",
    "ray": "Periodic hazard - cycles on/off, safe during off phase (electrodes).",
    "pickup": "Item pickup location - collectible spawns here.",
    "teleport": "Teleporter booth - entering triggers code-entry UI for fast travel.",
    "crumble": "Breakable/crumbling platform - collapses after player stands on it.",
    "marker": "Invisible gameplay marker - no graphics, triggers runtime behavior."
  }
}
```

### Key sections:

- **big_platforms** (128 entries): Each has `tl`, `tr`, `bl`, `br` -- four small tile indices arranged as a 2x2 grid.
- **rooms** (512 entries): Each has 12 big platform indices (4 columns x 3 rows) plus `color` name and `color_index`.
- **tiles** (90 entries, 0-89): Rendering info (`type`, `texture`/`available_colors`) plus gameplay data (`behavior`, `solidity`, `blocks_wide`, `blocks_tall`, `raw_type`).
- **tile_registry**: Maps every rendered tile image name to its tile index, color, and pixel dimensions.
- **teleporters** (15 entries): Room index, 5-character code, and grid position for each teleporter booth.
- **behaviors**: Human-readable descriptions of each behavior type.

## How to Load Atlases in libGDX

```java
// Load all atlases via AssetManager
AssetManager assets = new AssetManager();
assets.load("atlases/terrain_red.atlas", TextureAtlas.class);
assets.load("atlases/terrain_magenta.atlas", TextureAtlas.class);
assets.load("atlases/terrain_cyan.atlas", TextureAtlas.class);
assets.load("atlases/terrain_yellow.atlas", TextureAtlas.class);
assets.load("atlases/terrain_green.atlas", TextureAtlas.class);
assets.load("atlases/terrain_white.atlas", TextureAtlas.class);
assets.load("atlases/fixed.atlas", TextureAtlas.class);
assets.load("atlases/sprites.atlas", TextureAtlas.class);
assets.load("atlases/font.atlas", TextureAtlas.class);
assets.finishLoading();

// Store terrain atlases in a map for easy lookup
Map<String, TextureAtlas> terrainAtlases = new HashMap<>();
for (String color : new String[]{"red","magenta","cyan","yellow","green","white"}) {
    terrainAtlases.put(color, assets.get("atlases/terrain_" + color + ".atlas"));
}
TextureAtlas fixedAtlas = assets.get("atlases/fixed.atlas");
TextureAtlas spritesAtlas = assets.get("atlases/sprites.atlas");
```

## How to Render a Room

Given room index `roomIdx`:

```java
// 1. Look up room data
JsonValue room = metadata.get("rooms").get(roomIdx);
String roomColor = room.getString("color");
TextureAtlas terrainAtlas = terrainAtlases.get(roomColor);

// 2. Iterate the 4x3 grid of big platforms
int[] bigPlatformIds = room.get("big_platforms").asIntArray();  // 12 entries

for (int row = 0; row < 3; row++) {
    for (int col = 0; col < 4; col++) {
        int bpIdx = bigPlatformIds[row * 4 + col];
        JsonValue bp = metadata.get("big_platforms").get(bpIdx);

        // 3. Each big platform is 2x2 small tiles
        int[][] quadrants = {
            {bp.getInt("tl"), 0, 1},  // tile index, col offset, row offset
            {bp.getInt("tr"), 1, 1},
            {bp.getInt("bl"), 0, 0},
            {bp.getInt("br"), 1, 0}
        };

        for (int[] q : quadrants) {
            int tileIdx = q[0];
            int qCol = q[1], qRow = q[2];

            JsonValue tileDef = metadata.get("tiles").get(String.valueOf(tileIdx));
            String tileType = tileDef.getString("type");

            if (tileType.equals("empty")) continue;

            TextureRegion region;
            if (tileType.equals("fixed")) {
                // Fixed tiles: look up by "tile" + index
                region = fixedAtlas.findRegion("tile", tileIdx);
            } else {
                // Terrain tiles: look up by name with color suffix
                String regionName = String.format("tile_%03d_%s", tileIdx, roomColor);
                region = terrainAtlas.findRegion(regionName);
            }

            // 4. Draw at correct position
            float x = (col * 2 + qCol) * 192;  // 192 = standard tile width
            float y = (2 - row) * 2 * 144 + qRow * 144;  // flip Y for libGDX
            batch.draw(region, x, y);
        }
    }
}
```

## Collision and Gameplay

### Solidity Grid

Each tile has a `solidity` grid -- a 2D array of booleans matching the tile's block layout:
- `true` = solid block (BLOB can stand on it, cannot walk through it)
- `false` = passable block (BLOB passes through, visible but non-physical)
- `null` = no block at this position (empty space)

The solidity comes from the ZX Spectrum's brightness bit trick: bright blocks are solid, dull blocks are passable.

```java
// Build collision map for a room
// Each block is 48x48 pixels (SCALE * 8)
// Room is 8 blocks wide x 6 blocks tall per tile quadrant (= 32x18 blocks total)

boolean[][] buildCollisionMap(JsonValue metadata, int roomIdx) {
    boolean[][] collision = new boolean[18][32]; // 18 rows x 32 cols of 48px blocks
    JsonValue room = metadata.get("rooms").get(roomIdx);
    int[] bpIds = room.get("big_platforms").asIntArray();

    for (int row = 0; row < 3; row++) {
        for (int col = 0; col < 4; col++) {
            int bpIdx = bpIds[row * 4 + col];
            JsonValue bp = metadata.get("big_platforms").get(bpIdx);
            int[][] quads = {{bp.getInt("tl"),0,1},{bp.getInt("tr"),1,1},
                             {bp.getInt("bl"),0,0},{bp.getInt("br"),1,0}};

            for (int[] q : quads) {
                int tileIdx = q[0]; int qCol = q[1]; int qRow = q[2];
                JsonValue tile = metadata.get("tiles").get(String.valueOf(tileIdx));
                JsonValue solidity = tile.get("solidity");
                if (solidity == null || solidity.size == 0) continue;

                int baseCol = (col * 2 + qCol) * 4; // 4 blocks per tile width
                int baseRow = (2 - row) * 2 * 3 + (1 - qRow) * 3; // 3 blocks per tile height, flip Y

                for (int tr = 0; tr < solidity.size; tr++) {
                    JsonValue solidRow = solidity.get(tr);
                    for (int tc = 0; tc < solidRow.size; tc++) {
                        if (!solidRow.get(tc).isNull() && solidRow.get(tc).asBoolean()) {
                            int cr = baseRow + tr;
                            int cc = baseCol + tc;
                            if (cr >= 0 && cr < 18 && cc >= 0 && cc < 32)
                                collision[cr][cc] = true;
                        }
                    }
                }
            }
        }
    }
    return collision;
}
```

### Tile Behaviors

Check tile behavior during collision to trigger interactions:

```java
String getBehaviorAt(JsonValue metadata, int roomIdx, int blockCol, int blockRow) {
    // Reverse-map block position to tile index (similar to collision map logic)
    // ... resolve which tile occupies this block position ...
    JsonValue tile = metadata.get("tiles").get(String.valueOf(tileIdx));
    return tile.getString("behavior");
}

// In player update:
String behavior = getBehaviorAt(metadata, currentRoom, playerBlockX, playerBlockY);
switch (behavior) {
    case "tube":     applyTubeForce(player); break;     // push BLOB upward
    case "hazard":   killPlayer(); break;                 // instant death
    case "ray":      if (rayIsActive()) killPlayer(); break; // periodic on/off
    case "teleport": openTeleportUI(); break;             // 5-char code entry
    case "pickup":   collectItem(tileIdx); break;         // grab item
    case "crumble":  startCrumbleTimer(tileIdx); break;  // begin collapse
}
```

### Teleporter Lookup

```java
// Find teleporter code for current room
String getTeleporterCode(JsonValue metadata, int roomIdx) {
    JsonValue teleporters = metadata.get("teleporters");
    for (int i = 0; i < teleporters.size; i++) {
        if (teleporters.get(i).getInt("room") == roomIdx) {
            return teleporters.get(i).getString("code");
        }
    }
    return null;
}

// Find room for a given code (player types code to teleport)
int findTeleporterRoom(JsonValue metadata, String code) {
    JsonValue teleporters = metadata.get("teleporters");
    for (int i = 0; i < teleporters.size; i++) {
        if (teleporters.get(i).getString("code").equals(code)) {
            return teleporters.get(i).getInt("room");
        }
    }
    return -1;
}
```

### Tile Behavior Summary

| Behavior | Count | Tiles | Description |
|----------|-------|-------|-------------|
| terrain | 67 | 4-6, 8-19, 21-26, 28-32, 37-39, 47, 49-54, 57-81, 83-86, 89 | Standard terrain (solid/passable from brightness bit) |
| tube | 4 | 0, 1, 2, 3 | Transport tubes (carry BLOB upward) |
| hazard | 3 | 27, 46, 82 | Instant-kill zones (spikes, energy fields) |
| ray | 2 | 7, 40 | Periodic on/off hazards (electrodes) |
| pickup | 4 | 33, 34, 35, 55 | Item spawn locations |
| teleport | 1 | 36 | Teleporter booth |
| crumble | 5 | 43, 44, 45, 87, 88 | Breakable/crumbling platforms |
| marker | 4 | 41, 42, 48, 56 | Invisible gameplay zones (0 blocks) |

## How to Animate BLOB

```java
TextureAtlas spritesAtlas = assets.get("atlases/sprites.atlas");

// BLOB walk animations
Array<TextureAtlas.AtlasRegion> leftFrames = spritesAtlas.findRegions("blob_walk_left");
Array<TextureAtlas.AtlasRegion> rightFrames = spritesAtlas.findRegions("blob_walk_right");

Animation<TextureRegion> walkLeft  = new Animation<>(0.15f, leftFrames,  Animation.PlayMode.LOOP);
Animation<TextureRegion> walkRight = new Animation<>(0.15f, rightFrames, Animation.PlayMode.LOOP);

// In render loop
TextureRegion frame = walkRight.getKeyFrame(stateTime);
batch.draw(frame, blobX, blobY);
```

## How to Animate Enemies, Laser, and Materialise

All enemy types share a single animated strip (`enemy` index 0–18). Use the full 19-frame animation looped, or slice subranges per enemy type if needed.

```java
// Enemy animation (all 19 frames, looped)
Array<TextureAtlas.AtlasRegion> enemyFrames = spritesAtlas.findRegions("enemy");
Animation<TextureRegion> enemyAnim = new Animation<>(0.1f, enemyFrames, Animation.PlayMode.LOOP);

// Laser bolt (8 frames, looped while in flight)
Array<TextureAtlas.AtlasRegion> laserFrames = spritesAtlas.findRegions("laser");
Animation<TextureRegion> laserAnim = new Animation<>(0.08f, laserFrames, Animation.PlayMode.LOOP);

// Materialise effect (4 frames, played NORMAL once on enemy spawn/death)
Array<TextureAtlas.AtlasRegion> materialiseFrames = spritesAtlas.findRegions("materialise");
Animation<TextureRegion> materialiseAnim = new Animation<>(0.1f, materialiseFrames, Animation.PlayMode.NORMAL);
```

## How to Draw Text

```java
TextureAtlas fontAtlas = assets.get("atlases/font.atlas");

void drawText(SpriteBatch batch, String text, float x, float y) {
    for (int i = 0; i < text.length(); i++) {
        int ascii = (int) text.charAt(i);
        TextureAtlas.AtlasRegion glyph = fontAtlas.findRegion("char", ascii);
        if (glyph != null) {
            batch.draw(glyph, x + i * 48, y);
        }
    }
}
```

## Pipeline Scripts

All in `tools/`:

| Script | Purpose |
|--------|---------|
| `extract_graphics.py` | Extract tiles/sprites from ZX Spectrum ROM into `research/assets/` |
| `generate_game_assets.py` | Render 6x colored tile variants + sprites into `game_assets/` |
| `pack_atlases.py` | Pack `game_assets/` into libGDX texture atlases in `game_assets/atlases/` |

To regenerate everything:
```bash
python tools/extract_graphics.py
python tools/generate_game_assets.py
python tools/pack_atlases.py
```

Requires: Python 3, Pillow, Java (for TexturePacker), `tools/runnable-texturepacker.jar`.
