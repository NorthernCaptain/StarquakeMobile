# Atari ST Starquake - Extraction Progress

## Successfully Extracted
- **IPF to .ST conversion**: `tools/ipf_to_st.py` — built capsimg from source, wrote MFM decoder
- **Full 1MB RAM dump**: `/tmp/st_ram_full.bin` via Hatari debugger
- **Screen rendering**: Pixel-perfect 320x200 game screen from RAM at `0xF8000`
- **Palette system**: Fully decoded — HUD palette + 27 room palettes with raster split at ~scanline 47
- **114 tiles**: Correctly decoded from 4-bitplane format at 32x24 pixels each
- **Tile pointer table**: 120 entries at `0x2474C`, pointing to tile graphics data in `0x28D98-0x34978`

## Verified Addresses
| Structure | Address | Status |
|-----------|---------|--------|
| Tile pointer table | `0x2474C` | ✅ Verified — 120 valid pointers, tiles render correctly |
| Tile graphics data | `0x28D98-0x34978` | ✅ ~48KB of tile bitplane data |
| Palette table | `0x3AE9A` | ✅ 27 palettes, all valid ST colors |
| Room-to-palette index | `0x3B1FA` | ✅ 512 bytes, max index = 25 |
| Screen buffer | `0xF8000` | ✅ 32000 bytes, renders correctly |
| **Sprite data** | `0x34978` | ✅ 102 frames, 10 bytes/row masked format, 16px wide |
| **Sprite blit code** | `0x1D200` | ✅ Fully decoded — mask+shift drawing |
| **Enemy def table** | `0x3A42A` | ⚠️ 8-byte records, needs full decoding |
| **HUD palette** | `0x3AE1A` | ✅ Same 16 colors as documented HUD palette |
| Big platform table | `0x211C0` | ⚠️ Entry 0 = [20,20,20,20] matches ZX, but room composition needs verification |
| Room layout table | `0x213B0` | ⚠️ Rooms render with correct tiles but layout looks wrong |

## Known Palettes
- **HUD**: `000 333 444 666 003 005 007 033 055 077 030 050 300 500 700 777`
- **Room (gray accent)**: `000 222 444 666 320 542 764 030 050 070 030 050 300 500 700 777`
- Palette format: colors 1-3 vary per room (accent), colors 4-15 shared

## Sprites — Fully Decoded ✅

Sprite data format (different from tiles — NOT in the tile pointer table):
- **Format**: 10 bytes per row = 1 mask word (2 bytes) + 4 bitplane words (8 bytes)
- **Width**: 16 pixels (mask bit 1 = transparent, 0 = opaque)
- **Height**: 16 rows (main sprites) or 8 rows (laser/small sprites)
- **Frame size**: 160 bytes (16-row) or 80 bytes (8-row)
- **Pixel alignment**: sprites are shifted at draw time via ROL/ROR for sub-16px positioning
- **Total**: 76 frames (11 BLOB + 3 laser + 24 effects + 38 enemies)
- **Row ordering**: All frames stored top-to-bottom. No half-swap needed.

### Sprite memory regions (three separate sections)

The game uses **three separate base addresses** for sprites (verified via 68000 disassembly).
The laser section uses 80-byte frames, creating a gap that prevents treating all sprites as
one continuous block of 160-byte frames.

| Base address | Frames | Size | Content |
|-------------|--------|------|---------|
| `0x34978` | 0–10 (11) | 160 bytes each | BLOB walk animation (full rotation) |
| `0x35058` | 11–13 (3) | 80 bytes each | Laser/diamond rotation (8 rows) |
| `0x35148` | 14–75 (62) | 160 bytes each | Effects (24) + enemies (38) |

Note: the enemy base address `0x36048` referenced in game code is simply `0x35148 + 24×160` —
effects and enemies form one continuous block of 62 frames.

### Frame offset table
At `0x26622`: 62 entries × 4 bytes, containing byte offsets from `0x35148` for each
effect/enemy frame. All offsets are clean multiples of 160.

### Blit code
- **BLOB draw**: `0x1D1EC` — frame index × 160 + `0x34978`, 16 rows
- **Enemy/effect draw**: `0x1D166` — takes raw sprite pointer (not frame index), 16 rows
- **Laser draw**: `0x1D39E` — `LEA $35058,A3`, 8 rows, 3 frames
- **Blit subroutine**: `0x1D22E` (called by all above)
- **Shift routines**: `0x1D27A` (left-shift), `0x1D2E8` (right-shift)
- **Restore background**: `0x1D434` — restores screen under sprite from save buffer
- **Screen pointer**: variable at `0x39978` (= `0xF8000`)

### Enemy definition table
At `0x3A42A`: 256 × 8-byte records. Format per record:
- Bytes 0–1: X position (word)
- Bytes 2–3: Y position (word)
- Byte 4: X velocity (signed)
- Byte 5: Y velocity (signed)
- Byte 6: sprite/collision data
- Byte 7: unknown

## Still Needs Work
1. **Room layout decoding** — rooms render with correct individual tiles but the 4x3 grid composition is wrong. May need different byte ordering, different table address, or 16-bit indices.
2. **Big platform table verification** — the 2x2 tile compositions may use a different encoding than ZX version (TL/TR/BL/BR ordering may differ).
3. **Sprite identification** — all 76 frames extracted and rendered, but individual enemy types not yet named/mapped to ZX equivalents.
4. **Missing tiles** — some tiles from the ZX set may be in different locations or use different indices.

## Tools Built
- `tools/ipf_to_st.py` — IPF disk image to raw .ST converter
- `tools/extract_st_graphics.py` — Main extraction script (sprites done, rooms need fixes)
- capsimg library built at `/tmp/capsimg-fsuae/CAPSImg/libcapsimage.dylib`

## Key Files
- `/tmp/st_ram_full.bin` — 1MB Atari ST RAM dump (game running)
- `research/assets/atarist/starquake.st` — raw disk image (converted from IPF)
- `research/assets/atarist/starquake.tos` — extracted game executable
- `/tmp/grab0001.png` — Hatari screenshot of running game
