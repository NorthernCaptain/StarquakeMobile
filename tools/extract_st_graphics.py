"""
Extract all graphics from the Atari ST Starquake RAM dump.

Usage:
    python tools/extract_st_graphics.py /tmp/st_ram_full.bin

Outputs to research/assets/atarist/extracted/
"""

import struct
import sys
import os
import json
from PIL import Image

# === Memory addresses (verified via 68000 disassembly) ===
ADDR_BIGPLAT_TABLE = 0x211C0    # 355 entries x 4 bytes (BR/BL/TR/TL), ends at 0x2174C
ADDR_ROOM_LAYOUT   = 0x2174C    # 512 rooms x 24 bytes (12 x 16-bit big plat indices)
ADDR_TILE_PTRS     = 0x2474C    # 120 x 32-bit pointers
ADDR_PALETTE_TABLE = 0x3AE1A    # Palette table base (index 0 = HUD palette)
                                # Room palettes at indices 4-25 (room_pal_map values)
                                # Each palette = 16 x 16-bit ST colors = 32 bytes
ADDR_ROOM_PAL_IDX  = 0x3B1FA    # 512 bytes, palette index per room
ADDR_SCREEN        = 0xF8000    # 32000 bytes screen buffer

NUM_TILES = 122              # Tile pointer table has entries 0-121
NUM_BIGPLATS = 355
NUM_ROOMS = 512
NUM_PALETTES = 26              # Indices 0-25 used (0=HUD, 4-25=rooms, 26=runtime-only)
ROOM_STRIDE = 24               # 12 entries x 2 bytes each

HUD_PALETTE = [0x000, 0x333, 0x444, 0x666, 0x003, 0x005, 0x007,
               0x033, 0x055, 0x077, 0x030, 0x050, 0x300, 0x500, 0x700, 0x777]


def st_color_to_rgb(c):
    r = ((c >> 8) & 7) * 36
    g = ((c >> 4) & 7) * 36
    b = (c & 7) * 36
    return (min(255, r), min(255, g), min(255, b))


def read_palette(ram, offset):
    """Read a 16-color ST palette from RAM."""
    return [struct.unpack_from('>H', ram, offset + i * 2)[0] for i in range(16)]


def decode_bitplane_block(ram, offset, width_words, height):
    """
    Decode Atari ST interleaved 4-bitplane data.
    width_words: number of 16-pixel groups per row (width_param / 4).
    Returns 2D list of palette indices.
    """
    pixels = []
    groups = width_words
    bytes_per_row = groups * 8  # 4 planes x 2 bytes x groups

    for y in range(height):
        row = []
        for g in range(groups):
            base = offset + y * bytes_per_row + g * 8
            if base + 7 >= len(ram):
                row.extend([0] * 16)
                continue
            planes = [struct.unpack_from('>H', ram, base + p * 2)[0] for p in range(4)]
            for bit in range(16):
                mask = 1 << (15 - bit)
                ci = sum((1 << p) for p in range(4) if planes[p] & mask)
                row.append(ci)
        pixels.append(row)
    return pixels


def render_pixels(pixels, palette_rgb, scale=2, transparent_bg=False):
    """Render pixel array as a PIL Image."""
    if not pixels or not pixels[0]:
        return Image.new('RGBA', (1, 1), (0, 0, 0, 0))
    h = len(pixels)
    w = len(pixels[0])

    # Build 1x image first, then resize for scale
    if transparent_bg:
        data = bytearray(w * h * 4)
        for y in range(h):
            for x in range(w):
                ci = pixels[y][x]
                off = (y * w + x) * 4
                if ci != 0:
                    r, g, b = palette_rgb[ci % 16]
                    data[off] = r; data[off+1] = g; data[off+2] = b; data[off+3] = 255
        img = Image.frombytes('RGBA', (w, h), bytes(data))
    else:
        data = bytearray(w * h * 3)
        for y in range(h):
            for x in range(w):
                ci = pixels[y][x]
                off = (y * w + x) * 3
                r, g, b = palette_rgb[ci % 16]
                data[off] = r; data[off+1] = g; data[off+2] = b
        img = Image.frombytes('RGB', (w, h), bytes(data))

    if scale != 1:
        img = img.resize((w * scale, h * scale), Image.NEAREST)
    return img


def extract_palettes(ram, outdir):
    """Extract all room palettes."""
    paldir = os.path.join(outdir, "palettes")
    os.makedirs(paldir, exist_ok=True)

    palettes = []
    for i in range(NUM_PALETTES):
        pal = read_palette(ram, ADDR_PALETTE_TABLE + i * 32)
        palettes.append(pal)

    # Room-to-palette mapping
    room_pal_map = list(ram[ADDR_ROOM_PAL_IDX:ADDR_ROOM_PAL_IDX + NUM_ROOMS])

    # Save as JSON
    pal_data = {
        "hud_palette": [f"0x{c:03x}" for c in HUD_PALETTE],
        "room_palettes": [[f"0x{c:03x}" for c in p] for p in palettes],
        "room_palette_indices": room_pal_map,
    }
    with open(os.path.join(paldir, "palettes.json"), "w") as f:
        json.dump(pal_data, f, indent=2)

    # Render palette swatches
    swatch_h = 20
    for i, pal in enumerate(palettes):
        img = Image.new('RGB', (16 * 20, swatch_h), (0, 0, 0))
        for ci in range(16):
            rgb = st_color_to_rgb(pal[ci])
            for x in range(20):
                for y in range(swatch_h):
                    img.putpixel((ci * 20 + x, y), rgb)
        img.save(os.path.join(paldir, f"palette_{i:02d}.png"))

    print(f"  Extracted {len(palettes)} palettes + room mapping")
    return palettes, room_pal_map


def extract_tiles(ram, outdir, palettes):
    """Extract all tiles from the pointer table."""
    tiledir = os.path.join(outdir, "tiles")
    os.makedirs(tiledir, exist_ok=True)

    # Default palette for rendering: use palette index 8 (neutral gray 222/444/666)
    default_pal = palettes[min(12, len(palettes) - 1)]
    default_rgb = [st_color_to_rgb(c) for c in default_pal]

    tile_info = []

    for i in range(NUM_TILES):
        ptr_off = ADDR_TILE_PTRS + i * 4
        ptr = struct.unpack_from('>I', ram, ptr_off)[0]

        if ptr == 0 or ptr + 4 >= len(ram):
            tile_info.append({"index": i, "ptr": 0, "width": 0, "height": 0, "empty": True})
            continue

        height = struct.unpack_from('>H', ram, ptr)[0]
        width_param = struct.unpack_from('>H', ram, ptr + 2)[0]

        if height == 0 or width_param == 0 or height > 64 or width_param > 32:
            tile_info.append({"index": i, "ptr": ptr, "width": 0, "height": 0, "skip": True})
            continue

        width_groups = width_param // 4
        width_px = width_groups * 16
        data_offset = ptr + 4

        pixels = decode_bitplane_block(ram, data_offset, width_groups, height)

        # Check if tile is empty (all zeros)
        all_zero = all(pixels[y][x] == 0 for y in range(height) for x in range(width_px))

        info = {
            "index": i,
            "ptr": f"0x{ptr:05X}",
            "width": width_px,
            "height": height,
            "width_param": width_param,
            "empty": all_zero,
        }

        if not all_zero:
            img = render_pixels(pixels, default_rgb, scale=2, transparent_bg=True)
            img.save(os.path.join(tiledir, f"tile_{i:03d}.png"))

        tile_info.append(info)

    # Save tile metadata
    with open(os.path.join(tiledir, "tile_info.json"), "w") as f:
        json.dump(tile_info, f, indent=2)

    non_empty = sum(1 for t in tile_info if not t.get("empty", True) and not t.get("skip", False))
    print(f"  Extracted {non_empty}/{NUM_TILES} non-empty tiles")
    return tile_info


ADDR_SPRITE_DATA = 0x34978    # Sprite data (immediately after tile graphics)
SPRITE_FRAME_SIZE = 160       # 16 rows x 10 bytes/row
SPRITE_ROWS = 16
ADDR_ITEM_BANK = 0x26B56      # 35 collectible item icons, 16x16 masked frames
ITEM_COUNT = 35
ADDR_WEAPON_FX = 0x25096      # 8-row masked weapon/flying projectile effects
WEAPON_FX_FRAME_SIZE = 80
WEAPON_FX_COUNT = 12          # 0-7 walking laser cycle, 8-11 flying electro-ball cycle
ADDR_CLOUD_BANK = 0x3C650     # 24 frames: GAME OVER glyphs + player death cloud bank
CLOUD_FRAME_COUNT = 24
CLOUD_FRAME_WIDTH_WORDS = 2   # 32 pixels wide
CLOUD_FRAME_HEIGHT = 32
CLOUD_FRAME_SIZE = CLOUD_FRAME_WIDTH_WORDS * 10 * CLOUD_FRAME_HEIGHT

# Sprite memory layout (verified via 68000 disassembly):
#   BLOB:    11 frames at 0x34978, 160 bytes each, stored top-to-bottom
#   Laser:    3 frames at 0x35058, 80 bytes each (8 rows), top-to-bottom
#   Effects: 62 frames at 0x35148, 160 bytes each, stored bottom-half-first
# The game uses separate base addresses for each section (found in blit code).
BLOB_FRAME_COUNT = 11
ADDR_LASER = 0x35058          # 3 frames x 80 bytes (8 rows each)
LASER_FRAME_COUNT = 3
LASER_FRAME_SIZE = 80
ADDR_EFFECTS = 0x35148        # 62 frames x 160 bytes (verified from frame offset table at 0x26622)
EFFECTS_FRAME_COUNT = 62


def decode_masked_sprite_generic(ram, addr, width_words, height):
    """
    Decode masked sprite: 10 bytes/row per 16-pixel block = mask(2) + 4 planes(8).
    Mask bit 1 = transparent, 0 = opaque.
    Returns (pixels, masks) as 2D lists.
    """
    width_px = width_words * 16
    row_stride = width_words * 10
    pixels = []
    masks = []
    for y in range(height):
        off = addr + y * row_stride
        if off + row_stride > len(ram):
            pixels.append([0] * width_px)
            masks.append([1] * width_px)
            continue
        row = []
        mrow = []
        for group in range(width_words):
            group_off = off + group * 10
            mask_w = struct.unpack_from('>H', ram, group_off)[0]
            planes = [struct.unpack_from('>H', ram, group_off + 2 + p * 2)[0] for p in range(4)]
            for bit in range(16):
                bm = 1 << (15 - bit)
                ci = sum((1 << p) for p in range(4) if planes[p] & bm)
                m = 1 if (mask_w & bm) else 0
                row.append(ci)
                mrow.append(m)
        pixels.append(row)
        masks.append(mrow)
    return pixels, masks


def decode_masked_sprite(ram, addr, height):
    return decode_masked_sprite_generic(ram, addr, 1, height)


def render_masked_sprite(pixels, masks, palette_rgb, scale=6):
    """Render a masked sprite as a PIL Image with transparency."""
    h = len(pixels)
    w = len(pixels[0])
    data = bytearray(w * h * 4)
    for y in range(h):
        for x in range(w):
            if masks[y][x] == 0:  # opaque
                off = (y * w + x) * 4
                r, g, b = palette_rgb[pixels[y][x] % 16]
                data[off] = r; data[off+1] = g; data[off+2] = b; data[off+3] = 255
    img = Image.frombytes('RGBA', (w, h), bytes(data))
    if scale != 1:
        img = img.resize((w * scale, h * scale), Image.NEAREST)
    return img


def extract_sprites(ram, outdir, palettes):
    """Extract sprite data — BLOB, laser, effects, enemies.

    Sprites use a masked format: 10 bytes/row (mask word + 4 plane words),
    16 pixels wide.  Three separate memory regions (verified via 68000 disassembly):
      - BLOB:    11 frames at 0x34978, 160 bytes/frame, top-to-bottom
      - Laser:    3 frames at 0x35058,  80 bytes/frame (8 rows), top-to-bottom
      - Effects: 62 frames at 0x35148, 160 bytes/frame, bottom-half-first (swap needed)
    """
    spritedir = os.path.join(outdir, "sprites")
    os.makedirs(spritedir, exist_ok=True)

    default_pal = palettes[min(12, len(palettes) - 1)]
    default_rgb = [st_color_to_rgb(c) for c in default_pal]

    # Build frame list: (address, height, needs_swap, section_name)
    frames = []

    # BLOB: 11 frames, 16 rows, no swap
    for i in range(BLOB_FRAME_COUNT):
        addr = ADDR_SPRITE_DATA + i * SPRITE_FRAME_SIZE
        frames.append((addr, 16, False, "blob"))

    # Laser: 3 frames, 8 rows, no swap
    for i in range(LASER_FRAME_COUNT):
        addr = ADDR_LASER + i * LASER_FRAME_SIZE
        frames.append((addr, 8, False, "laser"))

    # Effects + enemies: 62 frames, 16 rows, no swap needed (correct base alignment)
    for i in range(EFFECTS_FRAME_COUNT):
        addr = ADDR_EFFECTS + i * SPRITE_FRAME_SIZE
        frames.append((addr, 16, False, "effect" if i < 24 else "enemy"))

    sprite_info = []

    for idx, (addr, height, swap, section) in enumerate(frames):
        pixels, masks = decode_masked_sprite(ram, addr, height)

        if swap:
            pixels = pixels[8:16] + pixels[0:8]
            masks = masks[8:16] + masks[0:8]

        opaque = sum(1 for y in range(height)
                     for x in range(16) if masks[y][x] == 0)

        img = render_masked_sprite(pixels, masks, default_rgb, scale=6)
        img.save(os.path.join(spritedir, f"st_sprite_{idx:03d}.png"))

        sprite_info.append({
            "index": idx,
            "address": f"0x{addr:05X}",
            "section": section,
            "height": height,
            "opaque_pixels": opaque,
            "half_swapped": swap,
        })

    # Create sprite sheet at 3x scale
    total = len(frames)
    scale = 3
    cols = 16
    rows = (total + cols - 1) // cols
    cell_w = 16 * scale + 2
    cell_h = 16 * scale + 2
    sheet = Image.new('RGBA', (cell_w * cols, cell_h * rows), (20, 20, 20, 255))
    for idx, (addr, height, swap, section) in enumerate(frames):
        pixels, masks = decode_masked_sprite(ram, addr, height)
        if swap:
            pixels = pixels[8:16] + pixels[0:8]
            masks = masks[8:16] + masks[0:8]
        img = render_masked_sprite(pixels, masks, default_rgb, scale)
        gx = (idx % cols) * cell_w
        gy = (idx // cols) * cell_h
        sheet.paste(img, (gx + 1, gy + 1), img)
    sheet.save(os.path.join(spritedir, "st_sprite_sheet.png"))

    with open(os.path.join(spritedir, "st_sprite_catalog.json"), "w") as f:
        json.dump({
            "format": {
                "type": "Atari ST 4-bitplane masked sprite",
                "bytes_per_row": 10,
                "row_layout": "mask_word(2) + plane0(2) + plane1(2) + plane2(2) + plane3(2)",
                "width_pixels": 16,
            },
            "sections": {
                "blob": {"base": f"0x{ADDR_SPRITE_DATA:05X}", "count": BLOB_FRAME_COUNT,
                         "frame_size": SPRITE_FRAME_SIZE, "height": 16, "swap": False},
                "laser": {"base": f"0x{ADDR_LASER:05X}", "count": LASER_FRAME_COUNT,
                          "frame_size": LASER_FRAME_SIZE, "height": 8, "swap": False},
                "effects_enemies": {"base": f"0x{ADDR_EFFECTS:05X}", "count": EFFECTS_FRAME_COUNT,
                                    "frame_size": SPRITE_FRAME_SIZE, "height": 16, "swap": False},
            },
            "total_frames": total,
            "frames": sprite_info,
        }, f, indent=2)

    print(f"  Extracted {total} sprite frames ({BLOB_FRAME_COUNT} blob + "
          f"{LASER_FRAME_COUNT} laser + {EFFECTS_FRAME_COUNT} effects/enemies)")
    return sprite_info


def extract_items(ram, outdir):
    """Extract the 35 collectible item icons from the ST RAM dump."""
    itemdir = os.path.join(outdir, "items")
    os.makedirs(itemdir, exist_ok=True)

    hud_rgb = [st_color_to_rgb(c) for c in HUD_PALETTE]
    item_info = []

    for i in range(ITEM_COUNT):
        addr = ADDR_ITEM_BANK + i * SPRITE_FRAME_SIZE
        pixels, masks = decode_masked_sprite(ram, addr, 16)
        img = render_masked_sprite(pixels, masks, hud_rgb, scale=1)
        img.save(os.path.join(itemdir, f"item_{i:02d}.png"))

        opaque = sum(1 for y in range(16) for x in range(16) if masks[y][x] == 0)
        colored = sum(1 for y in range(16) for x in range(16)
                      if masks[y][x] == 0 and pixels[y][x] != 0)
        colors = sorted({pixels[y][x] for y in range(16) for x in range(16)
                         if masks[y][x] == 0 and pixels[y][x] != 0})
        item_info.append({
            "index": i,
            "address": f"0x{addr:05X}",
            "opaque_pixels": opaque,
            "colored_pixels": colored,
            "palette_indices": colors,
        })

    with open(os.path.join(itemdir, "item_catalog.json"), "w") as f:
        json.dump({
            "format": {
                "type": "Atari ST 4-bitplane masked sprite",
                "bytes_per_row": 10,
                "frame_size": SPRITE_FRAME_SIZE,
                "width_pixels": 16,
                "height_pixels": 16,
                "mask_rule": "mask bit 1 = transparent, 0 = opaque",
                "palette": "HUD palette at 0x3AE1A",
            },
            "base_address": f"0x{ADDR_ITEM_BANK:05X}",
            "count": ITEM_COUNT,
            "frames": item_info,
        }, f, indent=2)

    # Contact sheet for quick visual verification.
    cols = 5
    rows = (ITEM_COUNT + cols - 1) // cols
    cell = 40
    sheet = Image.new('RGBA', (cols * cell, rows * cell), (20, 20, 20, 255))
    for i in range(ITEM_COUNT):
        img = Image.open(os.path.join(itemdir, f"item_{i:02d}.png")).resize((32, 32), Image.NEAREST)
        x = (i % cols) * cell + 4
        y = (i // cols) * cell + 4
        sheet.paste(img, (x, y), img)
    sheet.save(os.path.join(itemdir, "item_sheet.png"))

    print(f"  Extracted {ITEM_COUNT} item icons from 0x{ADDR_ITEM_BANK:05X}")
    return item_info


def extract_weapon_effects(ram, outdir):
    """Extract the small 8-row weapon/projectile effects from the ST RAM dump."""
    fxdir = os.path.join(outdir, "weapon_fx")
    os.makedirs(fxdir, exist_ok=True)

    hud_rgb = [st_color_to_rgb(c) for c in HUD_PALETTE]
    frame_info = []

    for i in range(WEAPON_FX_COUNT):
        addr = ADDR_WEAPON_FX + i * WEAPON_FX_FRAME_SIZE
        pixels, masks = decode_masked_sprite(ram, addr, 8)
        img = render_masked_sprite(pixels, masks, hud_rgb, scale=1)
        img.save(os.path.join(fxdir, f"weapon_fx_{i:02d}.png"))

        frame_info.append({
            "index": i,
            "address": f"0x{addr:05X}",
            "opaque_pixels": sum(1 for y in range(8) for x in range(16) if masks[y][x] == 0),
            "colored_pixels": sum(1 for y in range(8) for x in range(16)
                                  if masks[y][x] == 0 and pixels[y][x] != 0),
            "usage": (
                "flying electro-ball cycle" if i >= 8 else
                "walking laser cycle"
            ),
        })

    with open(os.path.join(fxdir, "weapon_fx_catalog.json"), "w") as f:
        json.dump({
            "format": {
                "type": "Atari ST 4-bitplane masked sprite",
                "bytes_per_row": 10,
                "frame_size": WEAPON_FX_FRAME_SIZE,
                "width_pixels": 16,
                "height_pixels": 8,
                "mask_rule": "mask bit 1 = transparent, 0 = opaque",
                "palette": "HUD palette at 0x3AE1A",
            },
            "base_address": f"0x{ADDR_WEAPON_FX:05X}",
            "count": WEAPON_FX_COUNT,
            "notes": {
                "walking_laser": "Ground shot logic starts at frame -4 or -3, producing the cycling wave frames around 3-7.",
                "flying_electro_ball": "Flying shot logic starts at frame -8, cycling frames 8-11.",
            },
            "frames": frame_info,
        }, f, indent=2)

    cols = 4
    rows = (WEAPON_FX_COUNT + cols - 1) // cols
    cell_w = 40
    cell_h = 28
    sheet = Image.new('RGBA', (cols * cell_w, rows * cell_h), (20, 20, 20, 255))
    for i in range(WEAPON_FX_COUNT):
        img = Image.open(os.path.join(fxdir, f"weapon_fx_{i:02d}.png")).resize((32, 16), Image.NEAREST)
        x = (i % cols) * cell_w + 4
        y = (i // cols) * cell_h + 6
        sheet.paste(img, (x, y), img)
    sheet.save(os.path.join(fxdir, "weapon_fx_sheet.png"))

    print(f"  Extracted {WEAPON_FX_COUNT} small weapon FX frames from 0x{ADDR_WEAPON_FX:05X}")
    return frame_info


def extract_cloud_bank(ram, outdir):
    """Extract the 32x32 masked bank at 0x3C650 at native resolution."""
    bank_dir = os.path.join(outdir, "blob_death_clouds")
    os.makedirs(bank_dir, exist_ok=True)

    hud_rgb = [st_color_to_rgb(c) for c in HUD_PALETTE]
    frame_info = []

    for i in range(CLOUD_FRAME_COUNT):
        addr = ADDR_CLOUD_BANK + i * CLOUD_FRAME_SIZE
        pixels, masks = decode_masked_sprite_generic(
            ram, addr, CLOUD_FRAME_WIDTH_WORDS, CLOUD_FRAME_HEIGHT
        )
        img = render_masked_sprite(pixels, masks, hud_rgb, scale=1)
        img.save(os.path.join(bank_dir, f"blob_death_cloud_{i:02d}.png"))
        frame_info.append({
            "index": i,
            "address": f"0x{addr:05X}",
            "opaque_pixels": sum(
                1 for y in range(CLOUD_FRAME_HEIGHT) for x in range(CLOUD_FRAME_WIDTH_WORDS * 16)
                if masks[y][x] == 0
            ),
            "usage": (
                "game_over_glyph" if i <= 8 else
                "unused_gap" if i <= 11 else
                "blob_death_cloud"
            ),
        })

    with open(os.path.join(bank_dir, "blob_death_cloud_catalog.json"), "w") as f:
        json.dump({
            "format": {
                "type": "Atari ST 4-bitplane masked sprite",
                "bytes_per_row": CLOUD_FRAME_WIDTH_WORDS * 10,
                "frame_size": CLOUD_FRAME_SIZE,
                "width_pixels": CLOUD_FRAME_WIDTH_WORDS * 16,
                "height_pixels": CLOUD_FRAME_HEIGHT,
                "mask_rule": "mask bit 1 = transparent, 0 = opaque",
                "palette": "HUD palette at 0x3AE1A",
            },
            "base_address": f"0x{ADDR_CLOUD_BANK:05X}",
            "count": CLOUD_FRAME_COUNT,
            "notes": {
                "game_over_frames": "Indices 0..8 are the GAME OVER glyph tiles.",
                "blob_death_frames": "Indices 12..23 are the player death cloud animation.",
            },
            "frames": frame_info,
        }, f, indent=2)

    cols = 4
    rows = (CLOUD_FRAME_COUNT + cols - 1) // cols
    cell = 40
    sheet = Image.new('RGBA', (cols * cell, rows * cell), (20, 20, 20, 255))
    for i in range(CLOUD_FRAME_COUNT):
        img = Image.open(os.path.join(bank_dir, f"blob_death_cloud_{i:02d}.png"))
        x = (i % cols) * cell + 4
        y = (i // cols) * cell + 4
        sheet.paste(img, (x, y), img)
    sheet.save(os.path.join(bank_dir, "blob_death_clouds_full.png"))

    print(f"  Extracted {CLOUD_FRAME_COUNT} native 32x32 frames from 0x{ADDR_CLOUD_BANK:05X}")
    return frame_info


def render_tile_at(ram, tile_idx, pal_rgb, img, px, py, scale=2):
    """Render a single tile onto an image at the given pixel position."""
    if tile_idx >= NUM_TILES:
        return
    ptr_off = ADDR_TILE_PTRS + tile_idx * 4
    ptr = struct.unpack_from('>I', ram, ptr_off)[0]
    if ptr == 0 or ptr + 4 >= len(ram):
        return
    height = struct.unpack_from('>H', ram, ptr)[0]
    width_param = struct.unpack_from('>H', ram, ptr + 2)[0]
    if height == 0 or width_param == 0:
        return
    width_groups = width_param // 4
    pixels = decode_bitplane_block(ram, ptr + 4, width_groups, height)
    tile_img = render_pixels(pixels, pal_rgb, scale=scale, transparent_bg=True)
    img.paste(tile_img, (px, py), tile_img)


def extract_big_platforms(ram, outdir, tile_info, palettes):
    """Extract big platform compositions (2x2 tile grids).

    Big platform format at 0x211C0: 4 bytes = [BR, BL, TR, TL] tile indices.
    Same byte ordering as ZX Spectrum version.
    """
    platdir = os.path.join(outdir, "platforms")
    os.makedirs(platdir, exist_ok=True)

    default_pal = palettes[min(12, len(palettes) - 1)]
    default_rgb = [st_color_to_rgb(c) for c in default_pal]

    plat_info = []
    non_empty = 0

    for i in range(NUM_BIGPLATS):
        off = ADDR_BIGPLAT_TABLE + i * 4
        br, bl, tr, tl = ram[off], ram[off + 1], ram[off + 2], ram[off + 3]
        plat_info.append({"index": i, "tiles_BR_BL_TR_TL": [br, bl, tr, tl]})

        img = Image.new('RGBA', (64 * 2, 48 * 2), (0, 0, 0, 0))
        for tile_idx, dx, dy in [
            (tl, 0, 0), (tr, 32, 0), (bl, 0, 24), (br, 32, 24)
        ]:
            render_tile_at(ram, tile_idx, default_rgb, img, dx * 2, dy * 2)

        has_content = any(img.getpixel((x, y))[3] > 0
                         for x in range(0, img.width, 8)
                         for y in range(0, img.height, 8))
        if has_content:
            img.save(os.path.join(platdir, f"bigplat_{i:03d}.png"))
            non_empty += 1

    with open(os.path.join(platdir, "bigplat_info.json"), "w") as f:
        json.dump(plat_info, f, indent=2)

    print(f"  Extracted {non_empty}/{NUM_BIGPLATS} non-empty big platforms")
    return plat_info


def decode_all_tiles(ram):
    """Pre-decode all tile pixel data. Returns dict of tile_idx -> pixel_2d_array."""
    tile_pixels = {}
    for t in range(NUM_TILES):
        ptr_off = ADDR_TILE_PTRS + t * 4
        ptr = struct.unpack_from('>I', ram, ptr_off)[0]
        if ptr == 0 or ptr + 4 >= len(ram):
            continue
        h = struct.unpack_from('>H', ram, ptr)[0]
        w = struct.unpack_from('>H', ram, ptr + 2)[0]
        if h == 0 or w == 0:
            continue
        width_groups = w // 4
        pixels = decode_bitplane_block(ram, ptr + 4, width_groups, h)
        tile_pixels[t] = pixels
    return tile_pixels


def render_tile_cached(tile_pixels, tile_idx, pal_rgb, scale):
    """Render a tile from pre-decoded pixel data. Returns RGBA Image or None."""
    if tile_idx not in tile_pixels:
        return None
    return render_pixels(tile_pixels[tile_idx], pal_rgb, scale=scale, transparent_bg=True)


def extract_rooms(ram, outdir, palettes, room_pal_map, count=512):
    """Extract all 512 room screenshots.

    Room data at 0x2174C: 24 bytes per room = 12 x 16-bit big platform indices.
    Layout: row-major (4 cols x 3 rows), top-to-bottom, left-to-right.
    Big platform table at 0x211C0: [BR, BL, TR, TL] per entry.
    """
    roomdir = os.path.join(outdir, "rooms")
    os.makedirs(roomdir, exist_ok=True)

    # Pre-decode all tile pixel data (decode once, render per palette)
    tile_pixels = decode_all_tiles(ram)

    scale = 2
    rendered = 0
    # Cache rendered tiles per palette (pal_idx -> {tile_idx -> Image})
    tile_cache = {}

    for room_idx in range(min(count, NUM_ROOMS)):
        x = room_idx % 16
        y = room_idx // 16

        pal_idx = room_pal_map[room_idx] if room_idx < len(room_pal_map) else 0
        pal = palettes[min(pal_idx, len(palettes) - 1)]
        if all(c == 0 for c in pal):
            pal = palettes[12]
            pal_idx = 12
        pal_rgb = [st_color_to_rgb(c) for c in pal]

        # Get or create tile image cache for this palette
        if pal_idx not in tile_cache:
            cache = {}
            for t in tile_pixels:
                img = render_tile_cached(tile_pixels, t, pal_rgb, scale)
                if img:
                    cache[t] = img
            tile_cache[pal_idx] = cache
        cache = tile_cache[pal_idx]

        room_off = ADDR_ROOM_LAYOUT + room_idx * ROOM_STRIDE
        bp_indices = [struct.unpack_from('>H', ram, room_off + i * 2)[0]
                      for i in range(12)]

        img = Image.new('RGBA', (256 * scale, 144 * scale), (0, 0, 0, 255))

        for row in range(3):
            for col in range(4):
                bp_idx = bp_indices[row * 4 + col]
                if bp_idx >= NUM_BIGPLATS:
                    continue

                bp_off = ADDR_BIGPLAT_TABLE + bp_idx * 4
                br, bl, tr, tl = ram[bp_off], ram[bp_off + 1], ram[bp_off + 2], ram[bp_off + 3]

                base_x = col * 64 * scale
                base_y = row * 48 * scale
                for tile_idx, tdx, tdy in [
                    (tl, 0, 0), (tr, 32 * scale, 0),
                    (bl, 0, 24 * scale), (br, 32 * scale, 24 * scale)
                ]:
                    if tile_idx in cache:
                        img.paste(cache[tile_idx],
                                  (base_x + tdx, base_y + tdy),
                                  cache[tile_idx])

        rgb_img = Image.new('RGB', img.size, (0, 0, 0))
        rgb_img.paste(img, mask=img.split()[3])
        rgb_img.save(os.path.join(roomdir, f"room_{room_idx:03d}_x{x:02d}_y{y:02d}.png"))
        rendered += 1

    print(f"  Rendered {rendered} rooms")


def extract_screen(ram, outdir, palettes, room_pal_map):
    """Extract the current screen buffer with correct split palette."""
    # Current room palette — figure out which room we're in from screen content
    # Use palette index 8 (the gray one that matched) as default
    game_pal = palettes[8]
    hud_rgb = [st_color_to_rgb(c) for c in HUD_PALETTE]
    game_rgb = [st_color_to_rgb(c) for c in game_pal]

    img = Image.new('RGB', (640, 400), (0, 0, 0))
    for y in range(200):
        pal_rgb = hud_rgb if y < 47 else game_rgb
        for group in range(20):
            base = ADDR_SCREEN + y * 160 + group * 8
            if base + 7 >= len(ram):
                continue
            planes = [struct.unpack_from('>H', ram, base + p * 2)[0] for p in range(4)]
            for bit in range(16):
                mask = 1 << (15 - bit)
                ci = sum((1 << p) for p in range(4) if planes[p] & mask)
                x = group * 16 + bit
                for dy in range(2):
                    for dx in range(2):
                        img.putpixel((x * 2 + dx, y * 2 + dy), pal_rgb[ci])

    img.save(os.path.join(outdir, "screen_capture.png"))
    print("  Saved screen capture")


def main():
    if len(sys.argv) < 2:
        print(f"Usage: {sys.argv[0]} <st_ram_dump.bin>")
        sys.exit(1)

    ram_path = sys.argv[1]
    with open(ram_path, "rb") as f:
        ram = f.read()

    print(f"Loaded RAM dump: {len(ram)} bytes ({len(ram) // 1024}KB)")

    outdir = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))),
                          "research", "assets", "atarist", "extracted")
    os.makedirs(outdir, exist_ok=True)

    print("\n1. Extracting palettes...")
    palettes, room_pal_map = extract_palettes(ram, outdir)

    print("\n2. Extracting tiles...")
    tile_info = extract_tiles(ram, outdir, palettes)

    print("\n3. Extracting sprites...")
    sprite_info = extract_sprites(ram, outdir, palettes)

    print("\n4. Extracting item icons...")
    item_info = extract_items(ram, outdir)

    print("\n5. Extracting weapon FX...")
    weapon_fx_info = extract_weapon_effects(ram, outdir)

    print("\n6. Extracting GAME OVER / death cloud bank...")
    cloud_info = extract_cloud_bank(ram, outdir)

    print("\n7. Extracting big platforms...")
    plat_info = extract_big_platforms(ram, outdir, tile_info, palettes)

    print("\n8. Extracting rooms (all 512)...")
    extract_rooms(ram, outdir, palettes, room_pal_map)

    print("\n9. Extracting current screen...")
    extract_screen(ram, outdir, palettes, room_pal_map)

    print(f"\nDone! Output in {outdir}")


if __name__ == "__main__":
    main()
