# Starquake - Extracted Asset Catalog & Rendering Guide

This document describes every graphic asset extracted from the ZX Spectrum ROM and explains exactly how they compose together to build the game's visuals. Use this as the blueprint for the libGDX renderer.

---

## Rendering Architecture Overview

The game screen is built from a strict hierarchy of primitives:

```
Screen (256x192 px)
  └─ Room (4 wide x 3 tall grid of Big Platforms)
       └─ Big Platform (2x2 grid of Small Tiles = 64x48 px)
            └─ Small Tile (up to 8x6 grid of 8x8-pixel Blocks = max 64x48 px)
                 └─ Block (8x8 pixels, 1-bit bitmap, 2-color attribute)
```

A full screen shows exactly **one room** at a time (flip-screen transitions). The visible area is:
- 4 Big Platforms wide x 3 Big Platforms tall = **256 x 144 pixels** of gameplay area
- The remaining 48 vertical pixels (192 - 144) are used for the HUD status bar

---

## 1. Platform Tiles (Small Tiles)

**Location:** `assets/tiles/`
**Count:** ~60 unique tiles (indices 0-59), plus ~30 dynamically-colored variants (60-85), plus runtime-generated tiles (86+)
**Naming:** `tile_NNN_TYPE.png` where TYPE is: `solid`, `tube`, `lethal`, `ray`, `pickup`, `teleport`

### Tile Dimensions

Most tiles are **32x24 pixels** (4x3 character blocks). Some exceptions:

| Size | Tiles | Description |
|------|-------|-------------|
| 32x24 px | Most tiles | Standard platform tile (4 blocks wide, 3 blocks tall) |
| 24x24 px | 28, 30, 46, 59, 82, 85 | Narrower platforms (3 blocks wide) |
| 16x24 px | 25, 45 | Half-width tiles (2 blocks wide) |
| 8x24 px | 34 | Single-column tile (1 block wide) |
| 32x8 px | 39 | Flat horizontal bar (4 blocks wide, 1 tall) |
| 56x24 px | 58 | Extra-wide tile (7 blocks wide) |
| 64x48 px | 86 | Full-size double tile |
| 56x48 px | 91-119 | Large tiles (runtime recolored, all share same pixel data) |

### Tile Categories by Function

#### Transport Tubes (Tiles 0-3) -- Green
- Vacuum lift tubes that carry BLOB upward
- 4 variants for tube segments (top cap, middle sections, bottom entry)
- **Color:** Green ink on green/black paper
- **Game behavior:** BLOB enters from below and is carried upward automatically
- In the clone: animate with upward particle flow inside the tube

#### Cave Terrain (Tiles 9-24) -- Yellow "Molecular" Pattern
These are the primary terrain tiles that make up cave walls, floors, and ceilings. They form the bulk of the visible game world.

| Tile | Role | Visual |
|------|------|--------|
| 9 | Full solid block | Completely filled yellow square |
| 10 | Top-right corner concave | Solid with rounded top-right eaten away |
| 11 | Bottom-left corner concave | Solid with rounded bottom-left eaten away |
| 12 | Bottom-right corner concave | Solid with rounded bottom-right eaten away |
| 13 | Top-right corner convex | Small solid wedge at top-right |
| 14 | Top-left corner convex | Small solid wedge at top-left |
| 15 | Top-right overhang | Solid top-right portion |
| 16 | Bottom-left floor | Solid bottom-left portion |
| 17 | Left wall | Solid left half, open right |
| 18 | Full with top pattern | Full solid with decorative top edge |
| 19 | Full with bottom pattern | Full solid with decorative bottom edge |
| 21-24 | Additional corner variants | Same shapes as 10-14 but assigned different type codes |

These tiles tessellate to form smooth cave walls with rounded corners. The corner tiles (10-15, 21-24) create the illusion of organic cave shapes out of a grid.

**Color:** All yellow ink on yellow paper (appears as solid yellow with subtle 1px texture from the pixel pattern).

**Key insight for clone:** These tiles share the same pixel artwork but get different runtime behavior codes. In the clone, separate visual appearance from collision/behavior type.

#### Surface Terrain (Tiles 50-54) -- Star Field
| Tile | Role |
|------|------|
| 49 | Object/decoration on surface |
| 50 | Surface terrain block (partial) |
| 51 | Surface terrain block (partial) |
| 52 | Surface terrain block (partial) |
| 53 | Star field / sky (sparse white dots on black) |
| 54 | Star field / sky (different dot pattern) |

**Color:** White ink on black paper. Tiles 53-54 are the starfield backgrounds visible in the surface rooms at the top of the map.

#### Special Structures

| Tile | What It Is | Colors | Notes |
|------|-----------|--------|-------|
| 7 | Hover pad / docking cradle | Cyan on black | U-shaped cradle where BLOB mounts the hover pad |
| 25 | Narrow red column | Red on black | Vertical barrier element |
| 27 | Spike hazard | Red on black | Spiky star pattern -- instant kill |
| 32 | Cheops Pyramid (trade booth) | Cyan on black | Where items are exchanged; shows a small alien figure |
| 36 | Teleporter booth | Cyan/white on black | Booth structure with archway |
| 37 | Teleporter variant | Cyan/magenta/white | Alternate teleporter decoration |
| 38 | Decorated structure | Cyan/yellow/magenta | Multi-colored structural element |
| 39 | Thin horizontal bar | Magenta/white | A flat ledge or barrier |
| 46 | Electrode hazard | Yellow on black | Lightning bolt shape -- rhythmic lethal hazard |

#### Pickup Items (Tiles 33-35)
These tiles represent collectible items placed in the world:

| Tile | Description | Colors |
|------|-------------|--------|
| 33 | Small pickup (single block) | Green/yellow/white on black |
| 34 | Narrow pickup (1 block wide, tall) | Magenta/white mix |
| 35 | Wide pickup (2 blocks) | Blue/magenta/white |

In the original, the specific item is determined by game state, not tile graphics. The tile is just a generic "item here" marker.

#### Empty/Transparent Tiles
| Tile | Purpose |
|------|---------|
| 20 | Primary empty tile (used everywhere for open space) |
| 41 | Secondary empty tile |
| 42, 48, 55, 56 | Additional empty variants |
| 89, 90 | Empty (large tile slots) |

**BigPlat 0** (all four quadrants = tile 20) is used **1,150 times** across 512 rooms -- this is "empty space."

#### Runtime-Recolored Tiles (91-119)
Tiles 91-119 all share the **same pixel data** (at address $FBFB) but receive different color attributes at runtime. The game uses these to create visual variety across rooms by changing ink/paper colors. In the clone, implement this as a **palette-swap shader** rather than separate tile textures.

### Tile Color System

The original uses the ZX Spectrum attribute system: each 8x8 block gets exactly 2 colors (ink for set pixels, paper for unset pixels). The full palette:

```
Normal:  black, blue, red, magenta, green, cyan, yellow, white
Bright:  black, blue, red, magenta, green, cyan, yellow, white  (same hues, higher intensity)
```

**Per-row attributes:** Each tile stores one attribute byte per row (up to 6 rows). All 8x8 blocks in the same row share the same 2-color attribute. This means a 4x3-block tile has 3 different color pairs (one per row).

**For the clone:** Since we're not bound by the Spectrum's 2-color-per-cell limit, we can:
- Use the extracted pixel shapes as **alpha masks**
- Apply richer color palettes (gradients, multiple shades, highlights)
- Keep the blocky pixel grid but add depth through color

---

## 2. Big Platforms

**Location:** `assets/big_platforms/`
**Count:** 128 compositions
**Naming:** `bigplat_NNN.png`

Each Big Platform is a **2x2 grid of Small Tiles**, producing a **64x48 pixel** block. The room grid is filled with these.

### Composition Format

```
┌────┬────┐
│ TL │ TR │   TL = Top-Left tile index
├────┼────┤   TR = Top-Right tile index
│ BL │ BR │   BL = Bottom-Left tile index
└────┴────┘   BR = Bottom-Right tile index
```

Each tile slot references a Small Tile by index (0-119). If the referenced tile is empty (e.g., tile 20), that quadrant is transparent.

### Most-Used Big Platforms

| BigPlat | Uses | TL | TR | BL | BR | Visual Description |
|---------|------|----|----|----|----|--------------------|
| 0 | 1150 | 20 | 20 | 20 | 20 | Completely empty (open air/cave space) |
| 21 | 194 | 17 | 14 | 20 | 20 | Ceiling piece (hanging stalactite shape) |
| 24 | 154 | 10 | 20 | 9 | 20 | Left wall with rounded top corner |
| 20 | 145 | 18 | 19 | 9 | 10 | Full solid block with decorative top edge |
| 25 | 130 | 20 | 9 | 20 | 10 | Right wall with rounded top corner |
| 22 | 99 | 41 | 41 | 16 | 17 | Floor piece (flat top, solid below) |
| 31 | 92 | 20 | 14 | 20 | 20 | Single corner ledge hanging from ceiling |
| 23 | 90 | 41 | 41 | 14 | 16 | Floor piece variant |
| 19 | 62 | 10 | 9 | 9 | 10 | Full solid with rounded top-left & top-right |
| 18 | 62 | 20 | 20 | 7 | 20 | Hover pad docking station |

**Patterns:** Big platforms are the "vocabulary" of room design. Roughly:
- **BigPlats 0-1:** Empty space
- **BigPlats 2-8:** Surface/starfield compositions (tiles 50-54)
- **BigPlats 9-16:** Surface terrain with starfield (tiles 50-52)
- **BigPlats 17-33:** Cave terrain variations (tiles 9-19 corners/walls/floors)
- **BigPlats 34-37:** Terrain with pickups/decorations (tiles 32-35)
- **BigPlats 38-58:** Special structures (hover pads, teleporters, tubes)
- **BigPlats 59+:** Deep cave variations, additional terrain

### How to Render a Big Platform

```
for each of the 4 slots [TL, TR, BL, BR]:
    look up the Small Tile by index
    if tile is not empty:
        render tile's pixel data at the slot's position within the 64x48 canvas
        apply the tile's per-row color attributes
```

**For the clone:** Pre-compose Big Platforms into single textures at load time (or cache them in a TextureAtlas). Since there are only 128 of them, this is trivially small.

---

## 3. Rooms

**Location:** `assets/rooms/`
**Count:** 512 rooms (indices 0-511), 64 pre-rendered + full minimap
**Naming:** `room_NNN_xXX_yYY.png`

### Room Structure

Each room is a **4 wide x 3 tall grid of Big Platforms** = 12 Big Platform references.

```
Screen layout (256x144 gameplay area):

┌──────────┬──────────┬──────────┬──────────┐
│ BP[0]    │ BP[1]    │ BP[2]    │ BP[3]    │  Row 0 (top)
│ 64x48    │ 64x48    │ 64x48    │ 64x48    │
├──────────┼──────────┼──────────┼──────────┤
│ BP[4]    │ BP[5]    │ BP[6]    │ BP[7]    │  Row 1 (middle)
│          │          │          │          │
├──────────┼──────────┼──────────┼──────────┤
│ BP[8]    │ BP[9]    │ BP[10]   │ BP[11]   │  Row 2 (bottom)
│          │          │          │          │
└──────────┴──────────┴──────────┴──────────┘

Total: 256 x 144 pixels
```

### Room Grid (World Map)

The 512 rooms form a **16 x 32 grid**:

```
Room index = y * 16 + x
x = room_index % 16     (column, 0-15)
y = room_index // 16    (row, 0-31)
```

- **Row 0** (rooms 0-15): Surface / planet exterior. Uses starfield tiles (53, 54) in upper Big Platforms and terrain tiles in lower ones
- **Rows 1-5**: Upper caves, moderate density. Mix of terrain and open space
- **Rows 6-15**: Mid-level caves, dense interconnected passages
- **Rows 16-25**: Deep caves, increasingly complex layout
- **Rows 26-31**: Deepest areas, sparse rooms (many are entirely empty BigPlat 0)

### How to Render a Room

```python
for row in range(3):
    for col in range(4):
        bp_index = room_data[row * 4 + col]
        big_platform = big_platforms[bp_index]
        draw(big_platform, x=col*64, y=row*48)
```

### Room Transitions

Original: **flip-screen** (instant cut to adjacent room when BLOB crosses an edge).

For the clone, options:
1. **Faithful:** Instant flip (simplest, most authentic)
2. **Enhanced:** Quick scroll/slide transition (0.2s)
3. **Modern:** Seamless scrolling with adjacent rooms pre-loaded

If doing scrolling, pre-render adjacent rooms (up/down/left/right) and scroll the camera.

### Minimap

`_minimap_all_512.png` shows all 512 rooms as thumbnails in the 16x32 grid. This gives a bird's-eye view of the entire planet. The upper portion is dense with cave terrain; the lower portion opens up with more empty space. The full map is approximately:
- 16 rooms x 256px = **4096 pixels wide**
- 32 rooms x 144px = **4608 pixels tall**

---

## 4. Sprites

**Location:** `assets/sprites/`
**Count:** 44 frames total
**Naming:** `sprite_NNN.png` (individual), `sprite_group_NN-NN.png` (groups of 4), `blob_COLOR.png` (colored sheets)

### Sprite Dimensions

All sprites are **24x16 pixels** (3x2 character blocks). At 4x scale in the extracted PNGs, that's 96x64.

### Frame Organization

The 44 frames are organized in **11 groups of 4 frames each**. Within each group, the 4 frames show the **same pose shifted 2 pixels horizontally** (positions 0, +2, +4, +6). This is how the original achieved smooth sub-tile movement on the character-cell-based Spectrum display.

**For the clone:** You don't need the 4 horizontal sub-positions -- that was a Spectrum hardware trick. Use **1 frame per pose** (pick frame 0 or 2 from each group, whichever is most centered) and let the game engine handle smooth pixel positioning.

### BLOB Walk Cycle (Groups 0-7)

| Group | Frames | Facing | Pose Description |
|-------|--------|--------|------------------|
| 0 | 0-3 | Left | Walk frame A (legs spread, leaning forward) |
| 1 | 4-7 | Left | Walk frame B (legs together, upright) |
| 2 | 8-11 | Left | Walk frame C (legs spread, leaning back) |
| 3 | 12-15 | Left | Walk frame D (legs together, alternate) |
| 4 | 16-19 | Right | Walk frame A (mirror of group 0) |
| 5 | 20-23 | Right | Walk frame B (mirror of group 1) |
| 6 | 24-27 | Right | Walk frame C (mirror of group 2) |
| 7 | 28-31 | Right | Walk frame D (mirror of group 3) |

**Walk animation sequence:** A -> B -> C -> D -> A -> B -> ... (4-frame cycle for each direction)

Groups 0-3 face LEFT (pixel mass concentrated in left byte-columns).
Groups 4-7 face RIGHT (pixel mass concentrated in right byte-columns).

**For the clone:** Extract frames 0, 4, 8, 12 for left-walk, frames 16, 20, 24, 28 for right-walk. That gives you a clean 4-frame walk cycle per direction. Or just use one direction and flip horizontally in the engine.

### Other Sprite Groups (Groups 8-10)

| Group | Frames | Description |
|-------|--------|-------------|
| 8 | 32-35 | Smaller character (enemy type A or BLOB on hover pad) |
| 9 | 36-39 | Different character pose (enemy type B or alternate BLOB) |
| 10 | 40-43 | Another character variant (enemy type C, slightly smaller) |

These last 3 groups appear to be **enemy sprite variants** or alternate BLOB states (riding hover pad, shooting, etc.). They follow the same 4-position-shift pattern.

### Colored BLOB Sheets

Pre-generated colored versions of the 16 BLOB walk frames (groups 0-3 = left, groups 4-7 = right):
- `blob_white.png` -- Default/neutral color
- `blob_green.png` -- Matches cave theme
- `blob_cyan.png` -- High visibility, matches teleporter/hover colors
- `blob_yellow.png` -- Matches terrain color

**For the clone:** BLOB was monochrome on the Spectrum (single color on black). In the enhanced version, consider:
- Multi-color BLOB sprite with shading (e.g., 4-8 colors)
- Palette swap for invincibility/damage states
- Separate head/body/legs layers for smoother animation

### Sprite Rendering

Sprites are drawn **on top of** the room tilemap. In the original, sprites are monochrome (1 color + transparent) with no masking against background -- they XOR or overlay onto the tile background.

**For the clone:**
1. Render the room tilemap as the background layer
2. Render sprites on top with alpha transparency (the extracted PNGs already have transparent backgrounds)
3. Enemies render the same way -- they're the same 24x16 format, just different pixel patterns

---

## 5. Font

**Location:** `assets/font/`
**Count:** 88 characters
**Naming:** `char_NNN_0xHH[_C].png` where HH is ASCII hex code and C is the character

### Character Set

The font covers ASCII codes 32-127 (standard printable range):
- **32:** Space (blank)
- **33-47:** Punctuation and symbols
- **48-57:** Digits 0-9 (chunky, highly readable)
- **65-90:** Uppercase A-Z (bold block letters)
- **91+:** Additional symbols and graphics characters

Each character is **8x8 pixels**, rendered as white-on-transparent.

### Font Sprite Sheet

`_font_sheet.png` shows all characters in a 16-column grid. The font style is blocky and bold -- typical Spectrum custom font, highly legible at small sizes.

### Usage in the Clone

The font is used for:
- **HUD text:** Score, lives count, resource bar labels
- **Teleporter code entry:** 5-character code display
- **Menu/UI text:** Pause screen, game over, etc.

**For the clone:** This 8x8 font is charming but very small on mobile screens. Options:
1. Use the extracted font at 2x-4x scale (nearest-neighbor) for authentic look
2. Create a new higher-resolution font inspired by the same blocky style
3. Use the extracted font for flavor/branding, use a readable font for gameplay UI

Load the font as a **BitmapFont** in libGDX from the sprite sheet.

---

## 6. UI Elements

**Location:** `assets/ui/`
**Count:** 8 border corners + laser variants

### Border Corners (`border_corner_N.png`)

8x8 pixel decorative elements used in the title screen and HUD frame. The title screen has eyes that blink in the border corners -- the different corner images are animation frames for this effect.

### Laser Sprite

The laser is a simple horizontal projectile. In the snapshot, the laser data at $E8B4 may not be visible (it could be drawn procedurally or the data is context-dependent). In the clone, create a simple 8x2 or 16x2 pixel bright-colored bolt.

### HUD Layout

The original HUD occupies the bottom 48 pixels of the 256x192 screen:

```
┌─────────────────────────────────────────────┐
│                                             │
│          Gameplay Area (256x144)            │
│                                             │
├─────────────────────────────────────────────┤
│  SCORE: 000000   LIVES: ♥♥♥♥               │
│  ████████░░  ████████░░  ████████░░         │
│  ENERGY      AMMO        PLATFORMS          │
└─────────────────────────────────────────────┘
                HUD (256x48)
```

Three resource bars + score + lives counter + 4-item inventory display.

**For the clone:** Redesign the HUD for mobile (larger touch targets, readable at phone resolution). Keep the 3-bar resource system but make bars larger and more colorful.

---

## 7. Teleporters

15 teleporter booths are distributed across the map. Each has a fixed location and a 5-character code:

| # | Code | Room | Grid (x,y) |
|---|------|------|-------------|
| 0 | RAMIX | 40 | (8, 2) |
| 1 | TULSA | 31 | (15, 1) |
| 2 | ASOIC | 66 | (2, 4) |
| 3 | DELTA | 150 | (6, 9) |
| 4 | QUAKE | 162 | (2, 10) |
| 5 | ALGOL | 213 | (5, 13) |
| 6 | EXIAL | 289 | (1, 18) |
| 7 | KYZIA | 343 | (7, 21) |
| 8 | ULTRA | 380 | (12, 23) |
| 9 | IRAGE | 433 | (1, 27) |
| 10 | OKTUP | 457 | (9, 28) |
| 11 | SONIQ | 461 | (13, 28) |
| 12 | AMIGA | 470 | (6, 29) |
| 13 | AMAHA | 499 | (3, 31) |
| 14 | VEROX | 506 | (10, 31) |

The teleporter tile (tile 36) shows the booth structure: a cyan archway with a dark interior. The code entry interface is purely UI (text input overlay).

---

## 8. How to Build the Full Renderer (libGDX)

### Data Loading Pipeline

```
1. Parse starquake_memory.bin (64KB raw memory dump)
2. Read tile lookup table at $EB23 (word addresses)
3. For each tile: decode bitmask + pixel data + attributes -> Texture
4. Read big platform table at $9840 (4-byte records) -> compose from tiles
5. Read room table at $7530 (12-byte records) -> compose from big platforms
6. Read sprite data at $E074 (48 bytes per frame) -> Texture per frame
7. Read teleporter data at $D03B -> code strings + room indices
8. Pack all textures into a TextureAtlas
```

### Texture Atlas Layout

Suggested atlas regions:

| Region | Textures | Size Each |
|--------|----------|-----------|
| `tile_NNN` | ~60 unique tiles | 32x24 typical |
| `bigplat_NNN` | 128 big platforms | 64x48 |
| `sprite_NNN` | 44 sprite frames | 24x16 |
| `font_NNN` | 88 font chars | 8x8 |
| `room_NNN` | 512 rooms (optional, render on demand) | 256x144 |

**Recommended:** Don't pre-render rooms. Instead, render rooms dynamically from Big Platform textures (only 12 texture draws per room). Pre-compose Big Platforms from tiles at load time.

### Rendering Order (per frame)

```
1. Clear screen to black
2. Draw current room (12 Big Platform textures in 4x3 grid)
3. Draw item pickups (overlay sprites at item positions)
4. Draw enemies (sprite frames at enemy positions)
5. Draw BLOB (sprite frame at player position)
6. Draw laser projectile if active
7. Draw HUD overlay (resource bars, score, inventory)
8. Draw any UI overlay (teleporter code entry, pause menu)
```

### Scale Factor for Mobile

The original is 256x144 gameplay pixels. For mobile:

| Approach | Effective Resolution | Scale |
|----------|---------------------|-------|
| Pixel-perfect 2x | 512x288 | Good for tablets |
| Pixel-perfect 3x | 768x432 | Good for phones |
| Pixel-perfect 4x | 1024x576 | Good for modern phones |
| Fit to screen | Variable | Use `FitViewport` in libGDX, integer scaling |

Use libGDX `FitViewport` with a virtual resolution of 256x192 (or 256x144 + HUD). Set texture filtering to `Nearest` for crisp pixels.

### Collision Data

Collision is encoded in the tile attribute data: the **brightness bit** (bit 6) of each 8x8 block's attribute determines solidity. Bright = passable, dark = solid. However for the clone, it's simpler to:

1. Store a collision type per Small Tile index (solid, passthrough, lethal, tube, teleport)
2. For each Big Platform, pre-compute a 2x2 collision grid
3. For each room, build an 8x6 collision grid from the Big Platforms

The tile type data at $9740 provides the behavior:

| Type Code | Collision | Behavior |
|-----------|-----------|----------|
| 0x00-0x4F | Solid | Standard walkable/blockable terrain |
| 0x50-0x5F | Tube | BLOB moves upward automatically |
| 0x60-0x6F | Lethal | Instant kill on contact |
| 0x70-0x7F | Ray | Periodic hazard (safe during off-phase) |
| 0xC0-0xCF | Pickup | Collectible item location |
| 0xD0-0xDF | Teleport | Triggers teleporter UI |

---

## 9. Runtime Color Variation

The original game applies different colors to the same tile shapes across different rooms. This is how 512 rooms look visually distinct despite sharing ~60 tile shapes.

### How It Works in the Original

The platform type byte (at $9740) doubles as a color index for tiles with type < 0x50. The game uses this value to set the attribute (ink/paper/bright) for that tile when rendering a specific room. This means the same tile 9 (full solid block) can appear yellow in one room, cyan in another, and magenta in a third.

### How to Implement in the Clone

**Option A: Palette-swap shader (recommended)**
- Store tiles as grayscale alpha masks
- Per-room, set a uniform `u_inkColor` and `u_paperColor` in the fragment shader
- The shader maps white pixels to ink color and black pixels to paper color
- Change the uniform when transitioning rooms

**Option B: Pre-render colored variants**
- For each tile, generate N color variants at load time
- Store in the atlas as `tile_NNN_colorScheme`
- Uses more memory but simpler rendering code

**Option C: Tinted SpriteBatch**
- Use libGDX `SpriteBatch.setColor()` to tint tiles per-room
- Quick to implement but less control over ink vs. paper colors

---

## 10. File Manifest

```
research/assets/
├── starquake.z80               # Z80 snapshot (converted from TZX)
├── starquake_memory.bin        # Raw 64KB memory dump
├── STARQUAK.TAP                # Original tape image (TAP format)
├── Starquake.tzx               # Original tape image (TZX format)
├── starquake-map-speccy.png    # Fan-made complete map (4096x4656)
│
├── tiles/                      # Platform tiles (the rendering primitives)
│   ├── tile_000_tube.png  ...  tile_119_solid.png    (112 non-empty tiles, 4x scaled)
│   └── _all_tiles_sheet.png                          (overview sprite sheet)
│
├── sprites/                    # Character sprites
│   ├── sprite_000.png  ...  sprite_043.png           (44 frames, 4x scaled)
│   ├── sprite_group_00-03.png  ...                   (11 group sheets)
│   ├── blob_white.png                                (16-frame BLOB, white)
│   ├── blob_green.png                                (16-frame BLOB, green)
│   ├── blob_cyan.png                                 (16-frame BLOB, cyan)
│   ├── blob_yellow.png                               (16-frame BLOB, yellow)
│   └── _all_sprites_sheet.png                        (overview sheet)
│
├── font/                       # Game font characters
│   ├── char_000_0x20.png  ...  char_087_0x77.png     (88 chars, 4x scaled)
│   └── _font_sheet.png                               (overview sheet)
│
├── ui/                         # UI/misc graphics
│   ├── border_corner_0.png  ...  border_corner_7.png (border decorations)
│   └── laser_*.png                                   (laser variants)
│
├── big_platforms/              # Composed 2x2 tile groups
│   ├── bigplat_000.png  ...  bigplat_127.png         (128 compositions, 2x scaled)
│   └── _all_big_platforms_sheet.png                   (overview sheet)
│
└── rooms/                      # Pre-rendered room screenshots
    ├── room_000_x00_y00.png  ...  room_063_x15_y03.png  (64 rooms, 2x scaled)
    └── _minimap_all_512.png                              (all 512 rooms)
```

### Scale Factors in Extracted PNGs

| Asset Type | Native Size | Extracted Scale | PNG Size |
|------------|-------------|-----------------|----------|
| Tiles | 32x24 typical | 4x | 128x96 |
| Sprites | 24x16 | 4x | 96x64 |
| Font chars | 8x8 | 4x | 32x32 |
| Big platforms | 64x48 | 2x | 128x96 |
| Rooms | 256x144 | 2x | 512x288 |
| Minimap | 1024x1152 | 1x | 1024x1152 |

**For libGDX import:** Either use the PNGs as-is (they'll scale fine with nearest-neighbor filtering) or re-extract at 1x native resolution for more control.

### Extraction Script

`tools/extract_graphics.py` -- Run this to re-extract all assets from the Z80 snapshot. Modify the `SCALE` constant at the top to change output resolution.

```bash
source .venv/bin/activate
python tools/extract_graphics.py [path/to/starquake.z80]
```
