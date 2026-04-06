# Starquake - Technical Reference

## Display Specifications (ZX Spectrum Original)

| Parameter | Value |
|-----------|-------|
| Screen resolution | 256 x 192 pixels |
| Character cells | 32 x 24 (each 8x8 pixels) |
| Color system | Attribute-based: 1 foreground + 1 background per 8x8 cell |
| Colors available | 8 colors x 2 brightness levels = 15 unique colors |
| Screen transition | Flip-screen (instant room change, no scrolling) |
| Memory | 48K total |

## Sprite Specifications

### BLOB (Player Character)
| Parameter | Value |
|-----------|-------|
| Sprite size | 24 x 16 pixels (3 x 2 character cells) |
| Animation frames | 16 total (8 left-facing, 8 right-facing) |
| Memory location | $E074 (ZX Spectrum) |

### Object Sprites
| Parameter | Value |
|-----------|-------|
| Size | 48 bytes each |

## World Data

| Parameter | Value |
|-----------|-------|
| Total rooms | 512 |
| Grid dimensions | 16 columns x 32 rows |
| Room data size | 12 bytes per room |
| Room composition | 4 x 3 grid of big platforms |
| Big platform data | 4 bytes per big platform (2x2 small platforms) |
| Small platform max size | 64 x 48 pixels (8 x 6 character cells) |
| Base tile size | 8 x 8 pixels |

### Room Coordinate Formula
```
x = room_number MOD 16    (0-15)
y = INT(room_number / 16) (0-31)
room_number = y * 16 + x  (0-511)
```

## Memory Map (ZX Spectrum)

| Address | Content |
|---------|---------|
| $7530 | Room layout data |
| $9740 | Platform behavior data |
| $9840 | Big platform definitions |
| $E074 | BLOB sprite data |
| $EB23 | Platform graphics data |

## Collision System

- Collision buffer granularity: **4 x 2 pixel cells**
- Solidity determined by the **brightness bit** in ZX Spectrum color attributes
- Bright attribute = solid tile
- Normal attribute = non-solid (passthrough)
- This dual-purpose use of the brightness bit saves memory

## Platform Behavior Encoding

Tile behaviors encoded as hex nibbles:

| Hex Range | Behavior Type |
|-----------|--------------|
| 0x5n | Transport tubes (vacuum lifts) |
| 0x6n | Lethal/hazard zones |
| 0x7n | Ray effects |
| 0xCn | Collectible pickups |
| 0xDn | Teleporter zones |

## Game Constants

| Parameter | Value |
|-----------|-------|
| Starting lives | 4 |
| Max inventory items | 4 (FIFO) |
| Core pieces required | 9 |
| Teleporter count | 15 |
| Teleporter code length | 5 characters |
| Enemies per room (max) | 4 |

## Scoring

| Action | Points |
|--------|--------|
| Enter new screen | 250 |
| Kill standard alien | 80 - 320 |
| Deliver core piece | 10,000 |

## Recommended Target Specs for Clone

For a modern mobile libGDX clone, suggested specifications:

| Parameter | Suggested Value | Rationale |
|-----------|----------------|-----------|
| Virtual resolution | 256 x 192 (scaled) or 512 x 384 | Maintain original aspect ratio (4:3) |
| Tile size | 16x16 or 32x32 pixels | Scaled up from 8x8 for mobile readability |
| Room grid | 16 x 32 (512 rooms) | Match original |
| Target FPS | 60 | Smooth mobile gameplay |
| Aspect ratio | 4:3 with letterboxing | Original ratio, adapt for modern screens |

## Disassembly Resources

| Resource | URL | Description |
|----------|-----|-------------|
| Level7 ZX Spectrum | https://level7.org.uk/miscellany/starquake-disassembly.txt | Annotated ZX Spectrum disassembly |
| Reuben Scratton BBC Micro | https://github.com/reubenscratton/starquake | Full buildable BBC Micro disassembly |
| Icemark Data Format | https://www.icemark.com/dataformats/starquake/index.html | Detailed room/tile data format analysis |
| Simon Owen TileMap | https://simonowen.com/articles/tilemap/ | Technical analysis of room system |
