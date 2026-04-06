#!/usr/bin/env python3
"""
Generate the complete game-ready asset set for the Starquake clone.

All assets are pre-rendered at 6x native resolution for mobile (1080p+).
Terrain tiles are generated ONLY in the colors they actually appear in across
all 512 rooms. BLOB is always white. No runtime color logic needed.

Naming convention for terrain tiles:
  tiles/tile_NNN_COLOR.png   e.g. tile_009_cyan.png

This way each unique (tile, color) pair is a separate texture region in the
atlas. The game loads metadata.json to know which tile+color to draw per room.

Output:
  game_assets/
    tiles/tile_NNN.png              - fixed-color tiles (one version)
    tiles/tile_NNN_COLOR.png        - terrain tiles (per-color variants)
    sprites/blob_POSE.png           - BLOB walk frames (white)
    sprites/enemy_NNN.png           - enemy sprite frames (white)
    font/char_NNN.png               - font characters (white)
    metadata.json                   - room data, big platforms, color map, tile index
"""

import struct
import os
import sys
import json
import collections
from PIL import Image

SCALE = 6  # 6x native -> 48px blocks, 1536x864 room, 1536x1152 with HUD

# === ZX Spectrum Palette ===
ZX_PALETTE = {
    (0, 0): (0, 0, 0),        (0, 1): (0, 0, 0),
    (1, 0): (0, 0, 205),      (1, 1): (0, 0, 255),
    (2, 0): (205, 0, 0),      (2, 1): (255, 0, 0),
    (3, 0): (205, 0, 205),    (3, 1): (255, 0, 255),
    (4, 0): (0, 205, 0),      (4, 1): (0, 255, 0),
    (5, 0): (0, 205, 205),    (5, 1): (0, 255, 255),
    (6, 0): (205, 205, 0),    (6, 1): (255, 255, 0),
    (7, 0): (205, 205, 205),  (7, 1): (255, 255, 255),
}

COLOR_NAMES = {
    0: "black", 1: "blue", 2: "red", 3: "magenta",
    4: "green", 5: "cyan", 6: "yellow", 7: "white"
}

ADDR_ROOM_DATA = 0x7530
ADDR_PLATFORM_TYPES = 0x9740
ADDR_BIG_PLATFORMS = 0x9840
ADDR_FONT = 0xADD4
ADDR_EXTRA_SPRITES = 0xAFC8   # Materialising effects (16 frames) + extra enemies (65 frames)
ADDR_TELEPORT = 0xD03B
ADDR_SPRITES = 0xE074         # BLOB (32 frames) + 3 main enemies (12 frames)
ADDR_LASER = 0xE8B4           # 8 laser animation frames
ADDR_TILE_LOOKUP = 0xEB23
ADDR_EA62 = 0xEA62
ADDR_EA63 = 0xEA63

# Platform behavior type classification based on Icemark data format + ROM analysis
# High nibble determines behavior category; low nibble is a sub-type parameter
BEHAVIOR_MAP = {
    0x50: "tube",      # Transport tubes - carry BLOB upward
    0x60: "hazard",    # Lethal zones - instant kill on contact
    0x70: "ray",       # Ray/electrode - periodic on/off hazard
    0x80: "marker",    # Invisible gameplay marker
    0x90: "marker",    # Invisible gameplay marker
    0xB0: "crumble",   # Breakable/crumbling platform
    0xC0: "pickup",    # Item pickup location
    0xD0: "teleport",  # Teleporter booth
    0xE0: "marker",    # Invisible gameplay marker
    0xF0: "crumble",   # Breakable/crumbling terrain
}

# Teleporter codes (ZX Spectrum version) - matched to rooms via ROM data + tile scan
TELEPORTER_CODES = {
    40: "RAMIX", 31: "TULSA", 66: "ASOIC", 150: "DELTA", 162: "QUAKE",
    213: "ALGOL", 289: "EXIAL", 343: "KYZIA", 380: "ULTRA", 433: "IRAGE",
    457: "OKTUP", 461: "SONIQ", 470: "AMIGA", 499: "AMAHA", 506: "VEROX",
}


def load_z80_snapshot(filepath):
    with open(filepath, 'rb') as f:
        data = f.read()
    pc = struct.unpack('<H', data[6:8])[0]
    memory = bytearray(65536)
    if pc != 0:
        flags = data[12]
        if (flags >> 5) & 1:
            i, addr = 30, 16384
            while i < len(data) - 4:
                if data[i] == 0xED and data[i+1] == 0xED:
                    memory[addr:addr+data[i+2]] = bytes([data[i+3]]) * data[i+2]
                    addr += data[i+2]; i += 4
                else:
                    memory[addr] = data[i]; addr += 1; i += 1
        else:
            memory[16384:] = data[30:30+49152]
    else:
        extra_len = struct.unpack('<H', data[30:32])[0]
        offset = 32 + extra_len
        while offset < len(data):
            if offset + 3 > len(data): break
            data_len = struct.unpack('<H', data[offset:offset+2])[0]
            page_num = data[offset+2]; offset += 3
            if data_len == 0xFFFF:
                page_data = bytearray(data[offset:offset+16384]); offset += 16384
            else:
                page_data = bytearray(); end = offset + data_len; i = offset
                while i < end:
                    if i+3 < end and data[i] == 0xED and data[i+1] == 0xED:
                        page_data.extend([data[i+3]] * data[i+2]); i += 4
                    else:
                        page_data.append(data[i]); i += 1
                offset = end
            page_map = {4: 0x8000, 5: 0xC000, 8: 0x4000}
            if page_num in page_map:
                base = page_map[page_num]
                memory[base:base+min(len(page_data), 16384)] = page_data[:16384]
    return memory


def resolve_attr(stored_attr, ea62, ea63, replace_white_ink=False):
    lower6 = stored_attr & 0x3F
    if lower6 == 0x36:
        a = (stored_attr & 0xC0) | ea63
    elif lower6 != 0:
        # For terrain tiles: ink=7 with paper=0 means "use room color"
        if replace_white_ink and (stored_attr & 0x3F) == 0x07:
            a = (stored_attr & 0xF8) | ea62
        else:
            a = stored_attr
    else:
        a = (stored_attr & 0xF8) | ea62
    a &= ~0x40
    return a


def attr_to_colors(attr_byte):
    ink = attr_byte & 0x07
    paper = (attr_byte >> 3) & 0x07
    bright = (attr_byte >> 6) & 0x01
    return ZX_PALETTE[(ink, bright)], ZX_PALETTE[(paper, bright)]


def tile_info(mem, tile_index):
    """Get tile metadata: is_empty, has_room_color, raw_attrs."""
    addr = struct.unpack('<H', mem[ADDR_TILE_LOOKUP + tile_index * 2:
                                   ADDR_TILE_LOOKUP + tile_index * 2 + 2])[0]
    bitmask = mem[addr:addr + 6]
    total_blocks = sum(bin(b).count('1') for b in bitmask)
    raw_attrs = [mem[addr - 6 + i] for i in range(6)]
    # Room color dependency: ea63 pattern (0x36), ea62 pattern (lower6==0),
    # or white-ink-on-black-paper (0x07) which is the room color placeholder
    has_room_color = any(
        (a & 0x3F) == 0x36 or (a & 0x3F) == 0 or (a & 0x3F) == 0x07
        for a in raw_attrs
    )
    return total_blocks == 0, has_room_color


def tile_gameplay(mem, tile_index):
    """Extract gameplay-relevant data for a tile: behavior, solidity grid, dimensions.

    Returns dict with:
      behavior: str - what this tile does (terrain, tube, hazard, ray, pickup, teleport, etc.)
      raw_type: int - original ROM type byte at $9740
      solidity: list of lists - per-block solidity grid (True=solid, False=passable, None=no block)
      blocks_wide: int - tile width in 8x8 blocks
      blocks_tall: int - tile height in 8x8 blocks
    """
    addr = struct.unpack('<H', mem[ADDR_TILE_LOOKUP + tile_index * 2:
                                   ADDR_TILE_LOOKUP + tile_index * 2 + 2])[0]
    bitmask = mem[addr:addr + 6]
    raw_attrs = [mem[addr - 6 + i] for i in range(6)]
    ptype = mem[ADDR_PLATFORM_TYPES + tile_index] if tile_index < 256 else 0

    # Determine behavior from type byte
    high_nibble = ptype & 0xF0
    if ptype < 0x50:
        behavior = "terrain"
    else:
        behavior = BEHAVIOR_MAP.get(high_nibble, "terrain")

    # Find tile dimensions
    max_col = max_row = -1
    for row in range(6):
        for col in range(8):
            if bitmask[row] & (0x80 >> col):
                max_col = max(max_col, col)
                max_row = max(max_row, row)

    if max_col < 0:
        # Empty tile (no blocks)
        return {
            "behavior": behavior,
            "raw_type": ptype,
            "solidity": [],
            "blocks_wide": 0,
            "blocks_tall": 0,
        }

    w = max_col + 1
    h = max_row + 1

    # Build solidity grid: brightness bit (bit 6) of each row's attribute = solid
    solidity = []
    for row in range(h):
        bright = (raw_attrs[row] >> 6) & 1 if row < 6 else 0
        row_data = []
        for col in range(w):
            if bitmask[row] & (0x80 >> col):
                row_data.append(bool(bright))
            else:
                row_data.append(None)  # no block at this position
        solidity.append(row_data)

    return {
        "behavior": behavior,
        "raw_type": ptype,
        "solidity": solidity,
        "blocks_wide": w,
        "blocks_tall": h,
    }


def render_tile(mem, tile_index, ea63_val, ea62_val=None):
    """Render a tile at 1x with the given ea63 color. Returns Image or None.
    If ea62_val is provided, it overrides the snapshot ea62 value AND enables
    white-ink replacement for terrain tiles."""
    addr = struct.unpack('<H', mem[ADDR_TILE_LOOKUP + tile_index * 2:
                                   ADDR_TILE_LOOKUP + tile_index * 2 + 2])[0]
    bitmask = mem[addr:addr + 6]
    raw_attrs = [mem[addr - 6 + i] for i in range(6)]

    ea62 = ea62_val if ea62_val is not None else (mem[ADDR_EA62] & 0x07)
    replace_white = ea62_val is not None
    ea63 = ea63_val & 0x3F
    attrs = [resolve_attr(a, ea62, ea63, replace_white_ink=replace_white) for a in raw_attrs]

    blocks = []
    max_col = max_row = -1
    for row in range(6):
        for col in range(8):
            if bitmask[row] & (0x80 >> col):
                blocks.append((row, col))
                max_col = max(max_col, col)
                max_row = max(max_row, row)
    if not blocks:
        return None

    w = (max_col + 1) * 8
    h = (max_row + 1) * 8

    pixel_offset = addr + 6
    block_pixels = {}
    for row, col in blocks:
        block_pixels[(row, col)] = mem[pixel_offset:pixel_offset + 8]
        pixel_offset += 8

    img = Image.new('RGBA', (w, h), (0, 0, 0, 0))
    for (row, col), pixels in block_pixels.items():
        attr = attrs[row] if row < 6 else 0x07
        ink_rgb, paper_rgb = attr_to_colors(attr)
        for py in range(8):
            byte_val = pixels[py]
            for px in range(8):
                bit = (byte_val >> (7 - px)) & 1
                color = ink_rgb if bit else paper_rgb
                x, y = col * 8 + px, row * 8 + py
                if x < img.width and y < img.height:
                    img.putpixel((x, y), color + (255,))
    return img


def render_sprite(mem, frame_index, color_rgb=(205, 205, 205), base_addr=None):
    """Render a 24x16 sprite frame in the given color."""
    offset = (base_addr if base_addr is not None else ADDR_SPRITES) + frame_index * 48
    img = Image.new('RGBA', (24, 16), (0, 0, 0, 0))
    for row in range(16):
        for byte_idx in range(3):
            byte_val = mem[offset + row * 3 + byte_idx]
            for px in range(8):
                if (byte_val >> (7 - px)) & 1:
                    img.putpixel((byte_idx * 8 + px, row), color_rgb + (255,))
    return img


def scale_nn(img, factor):
    return img.resize((img.width * factor, img.height * factor), Image.NEAREST)


def main():
    base_dir = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', 'research', 'assets')
    out_dir = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', 'game_assets')

    print(f"Loading snapshot...")
    mem = load_z80_snapshot(os.path.join(base_dir, 'starquake.z80'))

    with open(os.path.join(base_dir, 'room_colors.json')) as f:
        room_colors = json.load(f)['colors']

    # Find max tile index
    max_tile = 0
    for i in range(128):
        for j in range(4):
            val = mem[ADDR_BIG_PLATFORMS + i * 4 + j]
            if val > max_tile and val < 128:
                max_tile = val
    num_tiles = max_tile + 1

    # === Determine which colors each terrain tile needs ===
    tile_needed_colors = collections.defaultdict(set)
    for room_idx in range(512):
        color = room_colors[room_idx]
        if color == 0:
            continue
        offset = ADDR_ROOM_DATA + room_idx * 12
        for bp_slot in range(12):
            bp_idx = mem[offset + bp_slot]
            bp_off = ADDR_BIG_PLATFORMS + bp_idx * 4
            for j in range(4):
                tile_idx = mem[bp_off + j]
                tile_needed_colors[tile_idx].add(color)

    # === Classify tiles ===
    terrain_tiles = {}  # tile_idx -> set of colors
    fixed_tiles = []
    empty_tiles = []

    for i in range(num_tiles):
        is_empty, has_room_color = tile_info(mem, i)
        if is_empty:
            empty_tiles.append(i)
        elif has_room_color:
            colors = tile_needed_colors.get(i, set())
            if colors:
                terrain_tiles[i] = sorted(colors)
        else:
            if i in tile_needed_colors:  # only if actually used
                fixed_tiles.append(i)

    total_terrain_images = sum(len(c) for c in terrain_tiles.values())
    print(f"\nTiles: {len(terrain_tiles)} terrain ({total_terrain_images} images) + {len(fixed_tiles)} fixed + {len(empty_tiles)} empty")
    print(f"Scale: {SCALE}x (block={8*SCALE}px, tile={32*SCALE}x{24*SCALE}px)")

    # === Create output dirs ===
    os.makedirs(os.path.join(out_dir, 'tiles'), exist_ok=True)
    os.makedirs(os.path.join(out_dir, 'sprites'), exist_ok=True)
    os.makedirs(os.path.join(out_dir, 'font'), exist_ok=True)

    # === Generate terrain tiles (only needed color variants) ===
    print(f"\n=== Generating terrain tiles ===")
    terrain_generated = 0
    tile_registry = {}  # maps "tile_NNN_color" -> { index, color, filename }

    for tile_idx, colors in sorted(terrain_tiles.items()):
        for color_idx in colors:
            ea63 = color_idx & 0x07
            img = render_tile(mem, tile_idx, ea63, ea62_val=ea63)
            if img:
                name = f"tile_{tile_idx:03d}_{COLOR_NAMES[color_idx]}"
                scaled = scale_nn(img, SCALE)
                scaled.save(os.path.join(out_dir, 'tiles', f'{name}.png'))
                tile_registry[name] = {
                    "tile": tile_idx, "color": COLOR_NAMES[color_idx],
                    "width": scaled.width, "height": scaled.height
                }
                terrain_generated += 1
    print(f"  {terrain_generated} terrain tile images")

    # === Generate fixed-color tiles ===
    print(f"\n=== Generating fixed-color tiles ===")
    ea63_default = mem[ADDR_EA63] & 0x3F
    for tile_idx in fixed_tiles:
        img = render_tile(mem, tile_idx, ea63_default)
        if img:
            name = f"tile_{tile_idx:03d}"
            scaled = scale_nn(img, SCALE)
            scaled.save(os.path.join(out_dir, 'tiles', f'{name}.png'))
            tile_registry[name] = {
                "tile": tile_idx, "color": "fixed",
                "width": scaled.width, "height": scaled.height
            }
    print(f"  {len(fixed_tiles)} fixed tile images")

    # === Generate BLOB sprites (white only) ===
    print(f"\n=== Generating sprites ===")
    white_rgb = (205, 205, 205)
    blob_poses = [
        ("walk_left_0", 0), ("walk_left_1", 4),
        ("walk_left_2", 8), ("walk_left_3", 12),
        ("walk_right_0", 16), ("walk_right_1", 20),
        ("walk_right_2", 24), ("walk_right_3", 28),
    ]
    for name, frame_idx in blob_poses:
        img = render_sprite(mem, frame_idx, white_rgb)
        scaled = scale_nn(img, SCALE)
        scaled.save(os.path.join(out_dir, 'sprites', f'blob_{name}.png'))
    print(f"  BLOB: {len(blob_poses)} frames (white)")

    # Enemy sprites (white only)
    # Main enemies at $E074: 3 types (groups 8-10, frame 0 of each shift group)
    # Extra enemies at $AFC8+16*48=$B2C8: 16 types (every 4th frame)
    # Each group of 4 consecutive frames = 1 logical sprite + 3 pixel-shift variants
    # We take frame 0 of each group (the unshifted version)
    enemy_count = 0
    # 3 main enemies from BLOB sprite area
    for i in range(3):
        frame_idx = (8 + i) * 4  # groups 8, 9, 10
        img = render_sprite(mem, frame_idx, white_rgb)
        scaled = scale_nn(img, SCALE)
        scaled.save(os.path.join(out_dir, 'sprites', f'enemy_{enemy_count:02d}.png'))
        enemy_count += 1

    # 16 extra enemy types from $B2C8 area (frames 16-80 of $AFC8 block)
    for i in range(16):
        offset = ADDR_EXTRA_SPRITES + (16 + i * 4) * 48
        img = render_sprite(mem, 0, white_rgb, base_addr=offset)
        scaled = scale_nn(img, SCALE)
        scaled.save(os.path.join(out_dir, 'sprites', f'enemy_{enemy_count:02d}.png'))
        enemy_count += 1
    print(f"  Enemies: {enemy_count} types (white)")

    # Materialising/spawn effect: 4 animation stages at $AFC8
    # (4 stages × 4 pixel-shift variants = 16 frames; take frame 0 of each stage)
    materialise_count = 0
    for i in range(4):
        offset = ADDR_EXTRA_SPRITES + i * 4 * 48
        img = render_sprite(mem, 0, white_rgb, base_addr=offset)
        scaled = scale_nn(img, SCALE)
        scaled.save(os.path.join(out_dir, 'sprites', f'materialise_{i}.png'))
        materialise_count += 1
    print(f"  Materialise: {materialise_count} frames (white)")

    # Laser animation: 8 frames at $E8B4
    laser_count = 0
    for i in range(8):
        offset = ADDR_LASER + i * 48
        img = render_sprite(mem, 0, white_rgb, base_addr=offset)
        scaled = scale_nn(img, SCALE)
        scaled.save(os.path.join(out_dir, 'sprites', f'laser_{i}.png'))
        laser_count += 1
    print(f"  Laser: {laser_count} frames (white)")

    # === Generate font (white) ===
    print(f"\n=== Generating font ===")
    font_count = 0
    for i in range(96):
        offset = ADDR_FONT + i * 8
        img = Image.new('RGBA', (8, 8), (0, 0, 0, 0))
        has_pixels = False
        for row in range(8):
            byte_val = mem[offset + row]
            if byte_val: has_pixels = True
            for px in range(8):
                if (byte_val >> (7 - px)) & 1:
                    img.putpixel((px, row), (255, 255, 255, 255))
        if has_pixels or i < 64:
            scaled = scale_nn(img, SCALE)
            scaled.save(os.path.join(out_dir, 'font', f'char_{32+i:03d}.png'))
            font_count += 1
    print(f"  {font_count} characters")

    # === Save metadata ===
    # Build per-room tile mapping: for each room, which tile texture to use per slot
    big_platforms = []
    for i in range(128):
        off = ADDR_BIG_PLATFORMS + i * 4
        big_platforms.append({
            "tl": mem[off + 3], "tr": mem[off + 2],
            "bl": mem[off + 1], "br": mem[off]
        })

    rooms = []
    for room_idx in range(512):
        off = ADDR_ROOM_DATA + room_idx * 12
        bp_indices = [mem[off + j] for j in range(12)]
        color = room_colors[room_idx]
        rooms.append({
            "big_platforms": bp_indices,
            "color": COLOR_NAMES[color],
            "color_index": color
        })

    # Build tile-to-texture lookup: given a tile index and room color,
    # return the texture name to use
    tile_texture_map = {}
    behavior_counts = collections.Counter()
    for tile_idx in range(num_tiles):
        is_empty, has_room_color = tile_info(mem, tile_idx)
        gp = tile_gameplay(mem, tile_idx)
        behavior_counts[gp["behavior"]] += 1

        entry = {}
        if is_empty:
            entry["type"] = "empty"
        elif has_room_color:
            # Terrain tile: texture name depends on room color
            available_colors = [COLOR_NAMES[c] for c in terrain_tiles.get(tile_idx, [])]
            entry["type"] = "terrain"
            entry["available_colors"] = available_colors
        else:
            entry["type"] = "fixed"
            entry["texture"] = f"tile_{tile_idx:03d}"

        # Add gameplay data
        entry["behavior"] = gp["behavior"]
        entry["raw_type"] = gp["raw_type"]
        entry["solidity"] = gp["solidity"]
        entry["blocks_wide"] = gp["blocks_wide"]
        entry["blocks_tall"] = gp["blocks_tall"]

        tile_texture_map[str(tile_idx)] = entry

    print(f"\n=== Tile behaviors ===")
    for beh, cnt in sorted(behavior_counts.items(), key=lambda x: -x[1]):
        print(f"  {beh:12s}: {cnt}")

    # === Build teleporter table ===
    # Find rooms containing teleporter tile (36) and match to known codes
    teleporters = []
    for room_idx in range(512):
        off = ADDR_ROOM_DATA + room_idx * 12
        bp_indices = [mem[off + j] for j in range(12)]
        has_teleporter = False
        for bp_idx in bp_indices:
            bp_off = ADDR_BIG_PLATFORMS + bp_idx * 4
            tiles = [mem[bp_off + j] for j in range(4)]
            if 36 in tiles:
                has_teleporter = True
                break
        if has_teleporter:
            code = TELEPORTER_CODES.get(room_idx, f"TELE{len(teleporters):02d}")
            teleporters.append({
                "room": room_idx,
                "code": code,
                "grid_x": room_idx % 16,
                "grid_y": room_idx // 16,
            })
    print(f"\n=== Teleporters: {len(teleporters)} ===")
    for tp in teleporters:
        print(f"  Room {tp['room']:3d} ({tp['grid_x']:2d},{tp['grid_y']:2d}): {tp['code']}")

    metadata = {
        "scale": SCALE,
        "block_px": 8 * SCALE,
        "tile_standard_px": [32 * SCALE, 24 * SCALE],
        "sprite_px": [24 * SCALE, 16 * SCALE],
        "font_char_px": 8 * SCALE,
        "room_gameplay_px": [256 * SCALE, 144 * SCALE],
        "viewport": [256 * SCALE, 192 * SCALE],
        "big_platforms": big_platforms,
        "rooms": rooms,
        "tiles": tile_texture_map,
        "tile_registry": tile_registry,
        "teleporters": teleporters,
        "behaviors": {
            "terrain": "Standard walkable/blocking terrain. Solidity per block from brightness bit.",
            "tube": "Transport tube - carries BLOB upward automatically when entered.",
            "hazard": "Lethal zone - instant kill on contact.",
            "ray": "Periodic hazard - cycles on/off, safe during off phase (electrodes).",
            "pickup": "Item pickup location - collectible spawns here.",
            "teleport": "Teleporter booth - entering triggers code-entry UI for fast travel.",
            "crumble": "Breakable/crumbling platform - collapses after player stands on it.",
            "marker": "Invisible gameplay marker - no graphics, triggers runtime behavior.",
        },
    }

    with open(os.path.join(out_dir, 'metadata.json'), 'w') as f:
        json.dump(metadata, f, indent=2)

    sprite_count = len(blob_poses) + enemy_count + materialise_count + laser_count
    total = terrain_generated + len(fixed_tiles) + sprite_count + font_count
    print(f"\n=== Complete ===")
    print(f"  Tiles:    {terrain_generated + len(fixed_tiles)} ({terrain_generated} terrain + {len(fixed_tiles)} fixed)")
    print(f"  Sprites:  {sprite_count} ({len(blob_poses)} BLOB + {enemy_count} enemy + {materialise_count} materialise + {laser_count} laser)")
    print(f"  Font:     {font_count}")
    print(f"  Total:    {total} images")
    print(f"  Viewport: {256*SCALE}x{192*SCALE} (FitViewport in libGDX)")
    print(f"  Output:   {out_dir}/")


if __name__ == '__main__':
    main()
