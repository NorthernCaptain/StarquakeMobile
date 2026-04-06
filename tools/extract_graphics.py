#!/usr/bin/env python3
"""
Starquake (ZX Spectrum) - Complete Graphics Extractor

Extracts all graphic assets from a Z80 snapshot and saves them as PNG files.
Based on the Icemark data format specification.

Output structure:
  assets/tiles/          - Individual platform tile PNGs
  assets/sprites/        - BLOB and character sprite frames
  assets/font/           - Font character PNGs
  assets/ui/             - Border corners, laser, misc UI
  assets/big_platforms/  - Composed 2x2 big platform PNGs
  assets/rooms/          - Sample room renders
"""

import struct
import os
import sys
import json
from PIL import Image

# === ZX Spectrum Palette ===
ZX_PALETTE = {
    # (color_index, bright) -> (R, G, B)
    (0, 0): (0, 0, 0),        (0, 1): (0, 0, 0),
    (1, 0): (0, 0, 205),      (1, 1): (0, 0, 255),
    (2, 0): (205, 0, 0),      (2, 1): (255, 0, 0),
    (3, 0): (205, 0, 205),    (3, 1): (255, 0, 255),
    (4, 0): (0, 205, 0),      (4, 1): (0, 255, 0),
    (5, 0): (0, 205, 205),    (5, 1): (0, 255, 255),
    (6, 0): (205, 205, 0),    (6, 1): (255, 255, 0),
    (7, 0): (205, 205, 205),  (7, 1): (255, 255, 255),
}

# === Memory Addresses ===
ADDR_ROOM_DATA = 0x7530       # 512 rooms, 12 bytes each
ADDR_PLATFORM_TYPES = 0x9740  # 1 byte per platform
ADDR_BIG_PLATFORMS = 0x9840   # 4 bytes per big platform
ADDR_FONT = 0xADD4            # Font data
ADDR_TELEPORT = 0xD03B        # Teleport data
ADDR_BLANK = 0xDF40           # Blank graphic
ADDR_SPRITES = 0xE074         # BLOB/character sprites
ADDR_LASER = 0xE8B4           # Laser sprite
ADDR_TILE_LOOKUP = 0xEB23     # Platform graphics lookup table
ADDR_BORDER = 0x6661          # Border corners
ADDR_EA62 = 0xEA62            # Runtime room ink color variable
ADDR_EA63 = 0xEA63            # Runtime room color variable

SCALE = 4  # Scale factor for output images


def load_z80_snapshot(filepath):
    """Load a .z80 snapshot and return 64K memory as bytearray."""
    with open(filepath, 'rb') as f:
        data = f.read()

    pc = struct.unpack('<H', data[6:8])[0]
    memory = bytearray(65536)

    if pc != 0:
        # V1 format - 30 byte header, possibly compressed
        flags = data[12]
        compressed = (flags >> 5) & 1
        if compressed:
            i = 30
            addr = 16384
            while i < len(data) - 4:
                if data[i] == 0xED and data[i + 1] == 0xED:
                    count = data[i + 2]
                    val = data[i + 3]
                    memory[addr:addr + count] = bytes([val]) * count
                    addr += count
                    i += 4
                else:
                    memory[addr] = data[i]
                    addr += 1
                    i += 1
        else:
            memory[16384:] = data[30:30 + 49152]
    else:
        # V2/V3 format
        extra_len = struct.unpack('<H', data[30:32])[0]
        header_end = 32 + extra_len
        offset = header_end

        while offset < len(data):
            if offset + 3 > len(data):
                break
            data_len = struct.unpack('<H', data[offset:offset + 2])[0]
            page_num = data[offset + 2]
            offset += 3

            if data_len == 0xFFFF:
                page_data = bytearray(data[offset:offset + 16384])
                offset += 16384
            else:
                page_data = bytearray()
                end = offset + data_len
                i = offset
                while i < end:
                    if i + 3 < end and data[i] == 0xED and data[i + 1] == 0xED:
                        count = data[i + 2]
                        val = data[i + 3]
                        page_data.extend([val] * count)
                        i += 4
                    else:
                        page_data.append(data[i])
                        i += 1
                offset = end

            page_map = {4: 0x8000, 5: 0xC000, 8: 0x4000}
            if page_num in page_map:
                base = page_map[page_num]
                memory[base:base + min(len(page_data), 16384)] = page_data[:16384]

    return memory


def resolve_attr(stored_attr, ea62, ea63):
    """
    Apply the Icemark runtime attribute calculation.
    See: https://www.icemark.com/dataformats/starquake/index.html

    Stored attribute 0x36 (lower 6 bits) = "use room color variable _ea63"
    Stored attribute 0x00 (lower 6 bits) = "use room ink variable _ea62"
    Anything else = use stored attribute as-is.
    Finally, the bright bit (bit 6) is always cleared.
    """
    lower6 = stored_attr & 0x3F
    if lower6 == 0x36:
        a = (stored_attr & 0xC0) | ea63
    elif lower6 != 0:
        a = stored_attr
    else:
        a = (stored_attr & 0xF8) | ea62
    a &= ~0x40  # Clear bright bit
    return a


def attr_to_colors(attr_byte):
    """Convert ZX Spectrum attribute byte to (ink_rgb, paper_rgb)."""
    ink = attr_byte & 0x07
    paper = (attr_byte >> 3) & 0x07
    bright = (attr_byte >> 6) & 0x01
    return ZX_PALETTE[(ink, bright)], ZX_PALETTE[(paper, bright)]


def type_to_ea63(type_val):
    """Derive the correct _ea63 color from a tile type value.

    Type values encode the room color but with ink/paper sometimes swapped
    relative to how they render. Types like 0x10 store the color in the
    paper bits (3-5), but in-game the color renders as INK on black PAPER.

    When ink bits are 0 and paper bits have the color, swap them:
      0x10 (black/red)    -> 0x02 (red/black)
      0x20 (black/green)  -> 0x04 (green/black)
      0x30 (black/yellow) -> 0x06 (yellow/black)
    Types like 0x04 (green/black) are already correct.
    """
    ink = type_val & 0x07
    paper = (type_val >> 3) & 0x07
    if ink == 0 and paper != 0:
        return paper  # move paper color to ink, paper becomes black(0)
    return type_val & 0x3F


def extract_platform_tile(mem, tile_index, ea62_override=None, ea63_override=None):
    """
    Extract a platform tile's pixel and attribute data.
    Returns (image, width_chars, height_chars, tile_type) or None if empty.

    ea62_override/ea63_override: if provided, use these instead of snapshot values.
    For room rendering, pass the room's color as ea63_override.
    For standalone tile extraction (tile sheet), tiles with room-variable colors
    (stored attr 0x36) are rendered as white-on-black neutral masks, since their
    actual color depends on which room they appear in.
    """
    addr = struct.unpack('<H', mem[ADDR_TILE_LOOKUP + tile_index * 2:
                                   ADDR_TILE_LOOKUP + tile_index * 2 + 2])[0]

    bitmask = mem[addr:addr + 6]
    raw_attrs = [mem[addr - 6 + i] for i in range(6)]

    # Determine ea62/ea63 color variables
    if ea63_override is not None:
        ea63 = ea63_override & 0x3F
    else:
        # Standalone extraction: use white(7) for room-colored tiles so
        # the pixel pattern is clearly visible as a neutral mask.
        ptype = mem[ADDR_PLATFORM_TYPES + tile_index] if tile_index < 256 else 0
        has_room_attr = any((a & 0x3F) == 0x36 for a in raw_attrs[:6])
        if has_room_attr:
            ea63 = 0x07  # white ink on black paper -- neutral mask
        else:
            ea63 = mem[ADDR_EA63] & 0x3F

    if ea62_override is not None:
        ea62 = ea62_override & 0x07
    else:
        ea62 = mem[ADDR_EA62] & 0x07

    attrs = [resolve_attr(a, ea62, ea63) for a in raw_attrs]

    # Find which blocks are present and determine dimensions
    blocks = []
    max_col = -1
    max_row = -1
    for row in range(6):
        for col in range(8):
            if bitmask[row] & (0x80 >> col):
                blocks.append((row, col))
                max_col = max(max_col, col)
                max_row = max(max_row, row)

    if not blocks:
        return None

    w_chars = max_col + 1
    h_chars = max_row + 1

    # Read pixel data for present blocks (in order: row by row, left to right)
    pixel_offset = addr + 6
    block_pixels = {}
    for row, col in blocks:
        block_pixels[(row, col)] = mem[pixel_offset:pixel_offset + 8]
        pixel_offset += 8

    # Render to image
    img = Image.new('RGBA', (w_chars * 8, h_chars * 8), (0, 0, 0, 0))
    for (row, col), pixels in block_pixels.items():
        # Attribute: one per row
        attr = attrs[row] if row < 6 else 0x07
        ink_rgb, paper_rgb = attr_to_colors(attr)

        for py in range(8):
            byte_val = pixels[py]
            for px in range(8):
                bit = (byte_val >> (7 - px)) & 1
                color = ink_rgb if bit else paper_rgb
                x = col * 8 + px
                y = row * 8 + py
                if x < img.width and y < img.height:
                    img.putpixel((x, y), color + (255,))

    # Get tile type
    ptype = mem[ADDR_PLATFORM_TYPES + tile_index] if tile_index < 256 else 0
    type_names = {
        0x50: "tube", 0x60: "lethal", 0x70: "ray",
        0xC0: "pickup", 0xD0: "teleport"
    }
    type_name = type_names.get(ptype & 0xF0, "solid")

    return img, w_chars, h_chars, type_name


def extract_sprite_frame(mem, frame_index, base_addr=ADDR_SPRITES):
    """Extract a 24x16 sprite frame (3x2 characters)."""
    offset = base_addr + frame_index * 48
    img = Image.new('RGBA', (24, 16), (0, 0, 0, 0))

    for row in range(16):
        for byte_idx in range(3):
            byte_val = mem[offset + row * 3 + byte_idx]
            for px in range(8):
                bit = (byte_val >> (7 - px)) & 1
                if bit:
                    x = byte_idx * 8 + px
                    img.putpixel((x, row), (255, 255, 255, 255))

    return img


def extract_font_char(mem, char_index):
    """Extract an 8x8 font character."""
    offset = ADDR_FONT + char_index * 8
    img = Image.new('RGBA', (8, 8), (0, 0, 0, 0))

    for row in range(8):
        byte_val = mem[offset + row]
        for px in range(8):
            bit = (byte_val >> (7 - px)) & 1
            if bit:
                img.putpixel((px, row), (255, 255, 255, 255))

    return img


def extract_border_corner(mem, corner_index):
    """Extract an 8x8 border corner graphic."""
    offset = ADDR_BORDER + corner_index * 8
    img = Image.new('RGBA', (8, 8), (0, 0, 0, 0))

    for row in range(8):
        byte_val = mem[offset + row]
        for px in range(8):
            bit = (byte_val >> (7 - px)) & 1
            if bit:
                img.putpixel((px, row), (205, 205, 205, 255))

    return img


def compose_big_platform(mem, bp_index, tile_images):
    """
    Compose a big platform from 4 small tiles (2x2 grid).
    Big platform format: byte0=BR, byte1=BL, byte2=TR, byte3=TL
    """
    offset = ADDR_BIG_PLATFORMS + bp_index * 4
    br_idx = mem[offset]
    bl_idx = mem[offset + 1]
    tr_idx = mem[offset + 2]
    tl_idx = mem[offset + 3]

    # Standard small tile is 32x24 pixels (4x3 chars)
    tile_w, tile_h = 32, 24
    img = Image.new('RGBA', (tile_w * 2, tile_h * 2), (0, 0, 0, 0))

    for idx, (x, y) in [(tl_idx, (0, 0)), (tr_idx, (tile_w, 0)),
                          (bl_idx, (0, tile_h)), (br_idx, (tile_w, tile_h))]:
        if idx in tile_images and tile_images[idx] is not None:
            tile_img = tile_images[idx]
            # Paste tile, handling size mismatches
            paste_w = min(tile_img.width, tile_w)
            paste_h = min(tile_img.height, tile_h)
            region = tile_img.crop((0, 0, paste_w, paste_h))
            img.paste(region, (x, y), region)

    return img


def render_room(mem, room_index, bp_images):
    """Render a full room from its 4x3 grid of big platforms.
    Uses pre-composed bp_images (with snapshot default colors)."""
    offset = ADDR_ROOM_DATA + room_index * 12
    bp_w, bp_h = 64, 48

    img = Image.new('RGBA', (bp_w * 4, bp_h * 3), (0, 0, 0, 255))

    for row in range(3):
        for col in range(4):
            bp_idx = mem[offset + row * 4 + col]
            if bp_idx in bp_images:
                bp_img = bp_images[bp_idx]
                paste_w = min(bp_img.width, bp_w)
                paste_h = min(bp_img.height, bp_h)
                region = bp_img.crop((0, 0, paste_w, paste_h))
                img.paste(region, (col * bp_w, row * bp_h), region)

    return img


def load_room_colors(base_dir):
    """Load the per-room color table extracted from the fan-made map."""
    path = os.path.join(base_dir, 'room_colors.json')
    if os.path.exists(path):
        with open(path) as f:
            data = json.load(f)
        return data['colors']
    return None


def render_room_colored(mem, room_index, room_colors=None):
    """Render a room with correct per-room runtime colors.

    The game uses a PRNG seeded from room data to pick one of 4 colors
    (red, magenta, cyan, yellow) per room. The room_colors table provides
    the correct color per room, extracted from the fan-made gameplay map.
    """
    offset = ADDR_ROOM_DATA + room_index * 12
    tile_w, tile_h = 32, 24
    bp_w, bp_h = 64, 48

    img = Image.new('RGBA', (bp_w * 4, bp_h * 3), (0, 0, 0, 255))

    # Get room color from the lookup table
    if room_colors and room_index < len(room_colors):
        room_color = room_colors[room_index]
    else:
        room_color = 2  # default red

    # ea63 = room color as ink on black paper
    ea63 = room_color & 0x07  # ink = color, paper = black(0)
    ea62 = mem[ADDR_EA62] & 0x07

    for bp_row in range(3):
        for bp_col in range(4):
            bp_idx = mem[offset + bp_row * 4 + bp_col]
            bp_offset = ADDR_BIG_PLATFORMS + bp_idx * 4
            br_idx = mem[bp_offset]
            bl_idx = mem[bp_offset + 1]
            tr_idx = mem[bp_offset + 2]
            tl_idx = mem[bp_offset + 3]

            for tile_idx, (tx, ty) in [(tl_idx, (0, 0)), (tr_idx, (tile_w, 0)),
                                        (bl_idx, (0, tile_h)), (br_idx, (tile_w, tile_h))]:
                result = extract_platform_tile(mem, tile_idx,
                                                ea62_override=ea62,
                                                ea63_override=ea63)
                if result is None:
                    continue
                tile_img = result[0]

                px = bp_col * bp_w + tx
                py = bp_row * bp_h + ty
                paste_w = min(tile_img.width, tile_w)
                paste_h = min(tile_img.height, tile_h)
                region = tile_img.crop((0, 0, paste_w, paste_h))
                img.paste(region, (px, py), region)

    return img


def scale_image(img, factor):
    """Scale image by integer factor using nearest-neighbor."""
    return img.resize((img.width * factor, img.height * factor), Image.NEAREST)


def make_spritesheet(images, cols=8, bg_color=(32, 32, 32, 255)):
    """Combine multiple images into a sprite sheet."""
    if not images:
        return Image.new('RGBA', (1, 1), (0, 0, 0, 0))

    max_w = max(img.width for img in images)
    max_h = max(img.height for img in images)
    rows = (len(images) + cols - 1) // cols
    padding = 2

    sheet_w = cols * (max_w + padding) + padding
    sheet_h = rows * (max_h + padding) + padding
    sheet = Image.new('RGBA', (sheet_w, sheet_h), bg_color)

    for i, img in enumerate(images):
        col = i % cols
        row = i // cols
        x = padding + col * (max_w + padding)
        y = padding + row * (max_h + padding)
        sheet.paste(img, (x, y), img)

    return sheet


def main():
    if len(sys.argv) < 2:
        snapshot_path = os.path.join(os.path.dirname(__file__),
                                      '..', 'research', 'assets', 'starquake.z80')
    else:
        snapshot_path = sys.argv[1]

    snapshot_path = os.path.abspath(snapshot_path)
    if not os.path.exists(snapshot_path):
        print(f"Error: snapshot not found at {snapshot_path}")
        sys.exit(1)

    base_dir = os.path.join(os.path.dirname(__file__), '..', 'research', 'assets')
    base_dir = os.path.abspath(base_dir)

    print(f"Loading snapshot: {snapshot_path}")
    mem = load_z80_snapshot(snapshot_path)

    # Create output directories
    dirs = ['tiles', 'sprites', 'font', 'ui', 'big_platforms', 'rooms']
    for d in dirs:
        os.makedirs(os.path.join(base_dir, d), exist_ok=True)

    # === 1. Extract Platform Tiles ===
    print("\n=== Extracting Platform Tiles ===")

    # Find total tile count by scanning lookup table
    num_tiles = 0
    for i in range(128):
        addr = struct.unpack('<H', mem[ADDR_TILE_LOOKUP + i * 2:
                                       ADDR_TILE_LOOKUP + i * 2 + 2])[0]
        if addr < 0x4000:
            break
        num_tiles = i + 1
        # Stop if we see repeated $FFE7 entries (empty tile sentinel)
        if i > 55 and addr == 0xFFE7:
            # Check if next few are also sentinels
            next_addr = struct.unpack('<H', mem[ADDR_TILE_LOOKUP + (i+1) * 2:
                                               ADDR_TILE_LOOKUP + (i+1) * 2 + 2])[0]
            if next_addr == 0xFFE7 or next_addr < 0x4000:
                num_tiles = i + 1
                break

    # Scan big platforms to find the max tile index actually used
    max_tile_ref = 0
    for i in range(128):
        offset = ADDR_BIG_PLATFORMS + i * 4
        for j in range(4):
            val = mem[offset + j]
            if val > max_tile_ref and val < 128:
                max_tile_ref = val

    # Use only tiles actually referenced by big platforms -- entries beyond
    # max_tile_ref are phantom/junk data in the lookup table.
    num_tiles = max_tile_ref + 1
    print(f"  Total tiles to extract: {num_tiles}")

    tile_images = {}
    tile_list = []
    for i in range(num_tiles):
        result = extract_platform_tile(mem, i)
        if result:
            img, w, h, ttype = result
            tile_images[i] = img
            scaled = scale_image(img, SCALE)
            filename = f"tile_{i:03d}_{ttype}.png"
            scaled.save(os.path.join(base_dir, 'tiles', filename))
            tile_list.append(img)
            print(f"  Tile {i:2d}: {w*8}x{h*8}px type={ttype}")
        else:
            tile_images[i] = None
            print(f"  Tile {i:2d}: (empty)")

    # Sprite sheet of all tiles
    non_empty = [img for img in tile_list if img is not None]
    if non_empty:
        sheet = make_spritesheet([scale_image(img, 2) for img in non_empty], cols=10)
        sheet.save(os.path.join(base_dir, 'tiles', '_all_tiles_sheet.png'))
        print(f"  Saved tile sprite sheet ({len(non_empty)} tiles)")

    # === 2. Extract BLOB / Character Sprites ===
    print("\n=== Extracting Sprites ===")

    sprite_frames = []
    for i in range(44):
        img = extract_sprite_frame(mem, i)
        sprite_frames.append(img)
        scaled = scale_image(img, SCALE)
        scaled.save(os.path.join(base_dir, 'sprites', f'sprite_{i:03d}.png'))

    # Group into logical sets (4 directional frames per animation)
    # Frames 0-3, 4-7, etc. seem to be groups of 4 horizontal positions
    # Create sprite sheets by group
    for group_start in range(0, 44, 4):
        group = sprite_frames[group_start:group_start + 4]
        sheet = make_spritesheet([scale_image(img, SCALE) for img in group], cols=4)
        group_name = f"sprite_group_{group_start:02d}-{group_start+3:02d}"
        sheet.save(os.path.join(base_dir, 'sprites', f'{group_name}.png'))

    # Full sprite sheet
    all_sprites_sheet = make_spritesheet(
        [scale_image(img, SCALE) for img in sprite_frames], cols=8
    )
    all_sprites_sheet.save(os.path.join(base_dir, 'sprites', '_all_sprites_sheet.png'))
    print(f"  Extracted {len(sprite_frames)} sprite frames")

    # Also create colored versions of BLOB (first 16 frames are BLOB walk cycle)
    blob_colors = [
        ((255, 255, 255), "white"),
        ((0, 255, 0), "green"),
        ((0, 255, 255), "cyan"),
        ((255, 255, 0), "yellow"),
    ]
    for color_rgb, color_name in blob_colors:
        colored_frames = []
        for i in range(16):
            img = sprite_frames[i].copy()
            pixels = img.load()
            for y in range(img.height):
                for x in range(img.width):
                    r, g, b, a = pixels[x, y]
                    if a > 0:
                        pixels[x, y] = color_rgb + (255,)
            colored_frames.append(img)
        sheet = make_spritesheet([scale_image(img, SCALE) for img in colored_frames], cols=4)
        sheet.save(os.path.join(base_dir, 'sprites', f'blob_{color_name}.png'))
    print(f"  Created colored BLOB sheets")

    # === 3. Extract Font ===
    print("\n=== Extracting Font ===")

    # The font covers ASCII range. ZX Spectrum fonts typically start at space (32)
    # and cover 96 characters. Let's extract generously.
    font_chars = []
    for i in range(96):
        img = extract_font_char(mem, i)
        pixel_count = sum(1 for y in range(8) for x in range(8)
                         if img.getpixel((x, y))[3] > 0)
        if pixel_count > 0 or i < 64:  # Always include first 64
            font_chars.append(img)
            scaled = scale_image(img, SCALE)
            char_code = 32 + i
            safe_name = f"char_{i:03d}_0x{char_code:02X}"
            if 33 <= char_code <= 126:
                c = chr(char_code)
                if c.isalnum():
                    safe_name += f"_{c}"
            scaled.save(os.path.join(base_dir, 'font', f'{safe_name}.png'))

    # Font sheet
    if font_chars:
        sheet = make_spritesheet([scale_image(img, SCALE) for img in font_chars], cols=16)
        sheet.save(os.path.join(base_dir, 'font', '_font_sheet.png'))
    print(f"  Extracted {len(font_chars)} font characters")

    # === 4. Extract UI / Misc Graphics ===
    print("\n=== Extracting UI Graphics ===")

    # Border corners
    for i in range(8):  # Extract several
        img = extract_border_corner(mem, i)
        scaled = scale_image(img, SCALE)
        scaled.save(os.path.join(base_dir, 'ui', f'border_corner_{i}.png'))
    print(f"  Extracted border corners")

    # Laser sprite - it's at $E8B4, might be smaller than 8x8
    # Check several sizes
    for size_name, w, h in [("8x1", 1, 1), ("8x4", 1, 4), ("16x4", 2, 4)]:
        img = Image.new('RGBA', (w * 8, h), (0, 0, 0, 0))
        for row in range(h):
            for byte_idx in range(w):
                byte_val = mem[ADDR_LASER + row * w + byte_idx]
                for px in range(8):
                    bit = (byte_val >> (7 - px)) & 1
                    if bit:
                        img.putpixel((byte_idx * 8 + px, row), (255, 64, 64, 255))
        pixel_count = sum(1 for y in range(img.height) for x in range(img.width)
                         if img.getpixel((x, y))[3] > 0)
        if pixel_count > 0:
            scaled = scale_image(img, SCALE)
            scaled.save(os.path.join(base_dir, 'ui', f'laser_{size_name}.png'))
            print(f"  Laser {size_name}: {pixel_count} pixels")

    # === 5. Compose Big Platforms ===
    print("\n=== Composing Big Platforms ===")

    # Count valid big platforms
    num_big = 0
    for i in range(128):
        offset = ADDR_BIG_PLATFORMS + i * 4
        vals = [mem[offset + j] for j in range(4)]
        if all(v < num_tiles for v in vals):
            num_big = i + 1
        else:
            break

    print(f"  Total big platforms: {num_big}")

    bp_images = {}
    bp_list = []
    for i in range(num_big):
        img = compose_big_platform(mem, i, tile_images)
        bp_images[i] = img
        bp_list.append(img)
        scaled = scale_image(img, 2)
        scaled.save(os.path.join(base_dir, 'big_platforms', f'bigplat_{i:03d}.png'))

    # Big platform sheet
    if bp_list:
        sheet = make_spritesheet(bp_list, cols=10, bg_color=(20, 20, 30, 255))
        sheet.save(os.path.join(base_dir, 'big_platforms', '_all_big_platforms_sheet.png'))
    print(f"  Saved {num_big} big platform compositions")

    # === 6. Render Sample Rooms ===
    print("\n=== Rendering Sample Rooms (with correct per-room colors) ===")

    # Load room color table (extracted from fan-made gameplay map)
    room_colors = load_room_colors(base_dir)
    if room_colors:
        print(f"  Loaded room color table ({len(room_colors)} rooms)")
    else:
        print("  WARNING: room_colors.json not found, using default red")

    # Render first 64 rooms with correct runtime colors
    sample_rooms = list(range(min(64, 512)))
    for room_idx in sample_rooms:
        img = render_room_colored(mem, room_idx, room_colors)
        rx = room_idx % 16
        ry = room_idx // 16
        scaled = scale_image(img, 2)
        scaled.save(os.path.join(base_dir, 'rooms',
                                  f'room_{room_idx:03d}_x{rx:02d}_y{ry:02d}.png'))

    print(f"  Rendered {len(sample_rooms)} rooms")

    # Render a mini-map of all 512 rooms with correct colors
    print("\n=== Rendering Mini-Map ===")
    thumb_w, thumb_h = 64, 36  # Each room thumbnail size (256/4, 144/4)
    minimap = Image.new('RGBA', (thumb_w * 16, thumb_h * 32), (0, 0, 0, 255))
    for room_idx in range(512):
        img = render_room_colored(mem, room_idx, room_colors)
        thumb = img.resize((thumb_w, thumb_h), Image.NEAREST)
        rx = room_idx % 16
        ry = room_idx // 16
        minimap.paste(thumb, (rx * thumb_w, ry * thumb_h))

    minimap.save(os.path.join(base_dir, 'rooms', '_minimap_all_512.png'))
    print(f"  Saved 512-room minimap")

    # === Summary ===
    print("\n=== Extraction Complete ===")
    print(f"  Platform tiles: {sum(1 for v in tile_images.values() if v is not None)}")
    print(f"  Sprite frames:  {len(sprite_frames)}")
    print(f"  Font chars:     {len(font_chars)}")
    print(f"  Big platforms:  {num_big}")
    print(f"  Rooms rendered: {len(sample_rooms)} + minimap")
    print(f"\nOutput: {base_dir}/")


if __name__ == '__main__':
    main()
