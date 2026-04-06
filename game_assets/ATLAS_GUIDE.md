# Starquake - Game Assets Guide

## Source

All assets are extracted from the **Atari ST** version, except collectible items which come from the **ZX Spectrum** version (no ST source found for those).

## Asset Structure

```
game_assets/
├── metadata.json          - Room layouts, big platforms, palette data, tile info
├── ATLAS_GUIDE.md         - This file
├── atlases/               - Packed libGDX texture atlases (6 atlases)
├── tiles/                 - 116 terrain tile index maps (grayscale, 1x)
├── palettes/              - 26 palette textures (16x1 RGBA)
├── sprites/               - 76 sprite frames (RGBA, 1x)
├── items/                 - 35 collectible item icons (RGBA, 1x, from ZX)
├── screens/               - 5 pre-rendered screen backgrounds (RGBA, 1x)
└── font/                  - 60 font characters (RGBA, 6x from ZX)
```

## Rendering Approach

### Terrain Tiles — Palette Shader

Tiles are stored as **grayscale index maps** (pixel value 0-15 = palette color index). At render time, a fragment shader performs the palette lookup:

```glsl
uniform sampler2D u_texture;   // tile atlas (R channel = palette index)
uniform sampler2D u_palette;   // 16x1 (or 256x1) RGBA palette texture

void main() {
    float index = texture2D(u_texture, v_texCoords).r;
    gl_FragColor = texture2D(u_palette, vec2(index, 0.0));
}
```

- Use `GL_NEAREST` filtering on both textures for pixel-perfect scaling
- Change room palette by swapping the palette texture uniform
- 26 palettes provided; each room maps to one via `metadata.json`
- Extensible to 256 colors by widening the palette texture and adding more color indices

### Everything Else — Standard SpriteBatch

Sprites, items, screens, and font use regular RGBA textures rendered with standard libGDX SpriteBatch (no shader needed).

### Room Transitions

For scrolling between rooms with different palettes: render each room's visible portion to an offscreen RGBA buffer using its palette shader, then scroll the RGBA result as a regular texture.

## Atlas Contents

| Atlas | Contents | Format | Count |
|-------|----------|--------|-------|
| `tiles.atlas` | Terrain tile index maps | Grayscale (L) | 116 tiles |
| `palettes.atlas` | Room palette textures | RGBA 16x1 | 26 palettes |
| `sprites.atlas` | BLOB, laser, effects, enemies | RGBA | 76 frames |
| `items.atlas` | Collectible item icons (from ZX) | RGBA | 35 items |
| `screens.atlas` | HUD, teleport, trading, title, circuit | RGBA | 5 screens |
| `font.atlas` | Digits, letters, HUD elements, icons | RGBA | 60 chars |

## Native Resolutions

| Asset | Size | Notes |
|-------|------|-------|
| Tile | 32×24 px | 4-bitplane indexed color (0-15) |
| Sprite | 16×16 px | Masked 4-bitplane, RGBA output |
| Laser | 16×8 px | Smaller sprite variant |
| Screen | 320×200 px | Full Atari ST screen |
| Font char | 8×8 px | Stored at 6x (48×48) from ZX |
| Item icon | 16×16 px | From ZX, white on transparent |
| Room | 256×144 px | 4×3 grid of big platforms |
| Big platform | 64×48 px | 2×2 grid of tiles |

## Sprite Breakdown

| Range | Section | Count | Description |
|-------|---------|-------|-------------|
| blob_00–10 | BLOB | 11 | Player walk animation (full rotation) |
| laser_00–02 | Laser | 3 | Laser/diamond rotation (8px tall) |
| effect_00–23 | Effects | 24 | Particles, materialise, transitions |
| enemy_24–61 | Enemies | 38 | All enemy types with animation frames |

## Screen Backgrounds

Pre-rendered UI templates. Dynamic content (scores, items, text) is drawn on top at runtime.

| Screen | Usage |
|--------|-------|
| `hud_gameplay` | In-game HUD with score, BLOB icon, resource bars, inventory |
| `teleport` | Inside teleporter — destination selector |
| `trading_pyramid` | Cheops Pyramid trading interface |
| `title` | Title screen with STARQUAKE logo |
| `circuit_board` | Trading exchange view with inventory slots |

## metadata.json

Contains all game data needed at runtime:

- **palettes[]**: 26 entries with ST color values and RGB conversions
- **rooms[]**: 512 rooms with palette index and 12 big platform indices each
- **big_platforms[]**: 355 entries with TL/TR/BL/BR tile indices
- **tiles{}**: Tile dimensions per index

## Verified Addresses (Atari ST RAM)

| Structure | Address | Notes |
|-----------|---------|-------|
| Big platform table | 0x211C0 | 355 × 4 bytes, format [BR,BL,TR,TL] |
| Room layout table | 0x2174C | 512 × 24 bytes (12 × 16-bit indices) |
| Tile pointer table | 0x2474C | 122 × 32-bit pointers |
| Palette table | 0x3AE1A | 26 × 32 bytes (index 0 = HUD) |
| Room palette map | 0x3B1FA | 512 bytes, palette index per room |
| Sprite data (BLOB) | 0x34978 | 11 × 160 bytes |
| Sprite data (laser) | 0x35058 | 3 × 80 bytes |
| Sprite data (effects) | 0x35148 | 62 × 160 bytes |
