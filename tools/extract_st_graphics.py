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

# === Memory addresses (from RAM analysis) ===
ADDR_BIGPLAT_TABLE = 0x211B0    # 128 entries x 4 bytes
ADDR_ROOM_LAYOUT   = 0x213B0    # 512 rooms x 12 bytes
ADDR_TILE_PTRS     = 0x2474C    # 120 x 32-bit pointers
ADDR_PALETTE_TABLE = 0x3AE9A    # 27 palettes x 32 bytes
ADDR_ROOM_PAL_IDX  = 0x3B1FA    # 512 bytes, palette index per room
ADDR_SCREEN        = 0xF8000    # 32000 bytes screen buffer

NUM_TILES = 120
NUM_BIGPLATS = 128
NUM_ROOMS = 512
NUM_PALETTES = 27

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
    mode = 'RGBA' if transparent_bg else 'RGB'
    img = Image.new(mode, (w * scale, h * scale), (0, 0, 0, 0) if transparent_bg else (0, 0, 0))
    for y in range(h):
        for x in range(w):
            ci = pixels[y][x]
            if transparent_bg and ci == 0:
                continue
            color = palette_rgb[ci % 16]
            if transparent_bg:
                color = color + (255,)
            for dy in range(scale):
                for dx in range(scale):
                    img.putpixel((x * scale + dx, y * scale + dy), color)
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
    default_pal = palettes[min(8, len(palettes) - 1)]
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


def extract_sprites(ram, outdir, palettes):
    """Extract sprite data — BLOB, enemies, items."""
    spritedir = os.path.join(outdir, "sprites")
    os.makedirs(spritedir, exist_ok=True)

    default_pal = palettes[min(8, len(palettes) - 1)]
    default_rgb = [st_color_to_rgb(c) for c in default_pal]

    sprite_info = []

    # Known sprites from tile pointer table
    # Entry 58: BLOB walk frame (width_param=16 -> 64px wide, 24px tall)
    # Entry 86: Full BLOB sprite (64x48)
    known_sprites = {58: "blob_walk", 86: "blob_full"}

    for idx, name in known_sprites.items():
        ptr_off = ADDR_TILE_PTRS + idx * 4
        ptr = struct.unpack_from('>I', ram, ptr_off)[0]
        if ptr == 0 or ptr + 4 >= len(ram):
            continue

        height = struct.unpack_from('>H', ram, ptr)[0]
        width_param = struct.unpack_from('>H', ram, ptr + 2)[0]
        width_groups = width_param // 4
        width_px = width_groups * 16

        pixels = decode_bitplane_block(ram, ptr + 4, width_groups, height)
        img = render_pixels(pixels, default_rgb, scale=3, transparent_bg=True)
        img.save(os.path.join(spritedir, f"{name}_{width_px}x{height}.png"))

        sprite_info.append({
            "name": name, "index": idx, "ptr": f"0x{ptr:05X}",
            "width": width_px, "height": height
        })
        print(f"  Sprite '{name}': {width_px}x{height} at 0x{ptr:05X}")

    # Search for additional sprites outside the tile data region
    # Look for valid sprite headers (sensible height/width) followed by bitplane data
    tile_region_start = 0x28D98
    tile_region_end = 0x34978

    search_regions = [
        (0x34978, 0x3AE9A, "post_tiles"),
        (0x3B3FA, 0x50000, "mid_ram"),
        (0x60000, 0x80000, "upper_ram"),
    ]

    found_sprites = []
    for region_start, region_end, region_name in search_regions:
        for addr in range(region_start, min(region_end, len(ram) - 8), 2):
            h = struct.unpack_from('>H', ram, addr)[0]
            w = struct.unpack_from('>H', ram, addr + 2)[0]

            if h not in range(8, 49) or w not in [4, 8, 12, 16]:
                continue

            wg = w // 4
            data_size = h * wg * 8
            if addr + 4 + data_size > len(ram):
                continue

            block = ram[addr + 4:addr + 4 + data_size]
            nz = sum(1 for b in block if b != 0)
            total = len(block)

            # Must have meaningful data (not empty, not code)
            if nz < total * 0.05 or nz > total * 0.95:
                continue

            # Check it doesn't overlap with an already-found sprite
            overlap = False
            for fa, fh, fw in found_sprites:
                fs = fh * (fw // 4) * 8 + 4
                if fa <= addr < fa + fs or addr <= fa < addr + data_size + 4:
                    overlap = True
                    break
            if overlap:
                continue

            wpx = wg * 16
            found_sprites.append((addr, h, w))

            pixels = decode_bitplane_block(ram, addr + 4, wg, h)
            img = render_pixels(pixels, default_rgb, scale=3, transparent_bg=True)
            img.save(os.path.join(spritedir, f"sprite_0x{addr:05X}_{wpx}x{h}.png"))

            sprite_info.append({
                "name": f"sprite_0x{addr:05X}",
                "ptr": f"0x{addr:05X}",
                "width": wpx, "height": h,
                "region": region_name,
            })

    print(f"  Found {len(found_sprites)} additional sprites outside tile region")

    with open(os.path.join(spritedir, "sprite_info.json"), "w") as f:
        json.dump(sprite_info, f, indent=2)

    return sprite_info


def extract_big_platforms(ram, outdir, tile_info, palettes):
    """Extract big platform compositions (2x2 tile grids)."""
    platdir = os.path.join(outdir, "platforms")
    os.makedirs(platdir, exist_ok=True)

    default_pal = palettes[min(8, len(palettes) - 1)]
    default_rgb = [st_color_to_rgb(c) for c in default_pal]

    plat_info = []

    for i in range(NUM_BIGPLATS):
        off = ADDR_BIGPLAT_TABLE + i * 4
        tl, tr, bl, br = ram[off], ram[off + 1], ram[off + 2], ram[off + 3]
        plat_info.append({"index": i, "tiles": [tl, tr, bl, br]})

        # Compose from tiles — each tile is 32x24 (standard)
        # Big platform = 64x48
        img = Image.new('RGBA', (64 * 2, 48 * 2), (0, 0, 0, 0))

        for slot, (tile_idx, dx, dy) in enumerate([
            (tl, 0, 0), (tr, 32, 0), (bl, 0, 24), (br, 32, 24)
        ]):
            if tile_idx >= NUM_TILES:
                continue

            ptr_off = ADDR_TILE_PTRS + tile_idx * 4
            ptr = struct.unpack_from('>I', ram, ptr_off)[0]
            if ptr == 0 or ptr + 4 >= len(ram):
                continue

            height = struct.unpack_from('>H', ram, ptr)[0]
            width_param = struct.unpack_from('>H', ram, ptr + 2)[0]
            if height == 0 or width_param == 0:
                continue

            width_groups = width_param // 4
            pixels = decode_bitplane_block(ram, ptr + 4, width_groups, height)
            tile_img = render_pixels(pixels, default_rgb, scale=2, transparent_bg=True)
            img.paste(tile_img, (dx * 2, dy * 2), tile_img)

        # Check if entirely empty
        has_content = any(img.getpixel((x, y))[3] > 0
                         for x in range(0, img.width, 8)
                         for y in range(0, img.height, 8))
        if has_content:
            img.save(os.path.join(platdir, f"bigplat_{i:03d}.png"))

    with open(os.path.join(platdir, "bigplat_info.json"), "w") as f:
        json.dump(plat_info, f, indent=2)

    non_empty = sum(1 for i in range(NUM_BIGPLATS)
                    if os.path.exists(os.path.join(platdir, f"bigplat_{i:03d}.png")))
    print(f"  Extracted {non_empty}/{NUM_BIGPLATS} non-empty big platforms")
    return plat_info


def extract_rooms(ram, outdir, palettes, room_pal_map, count=64):
    """Extract room screenshots. Does first `count` rooms by default."""
    roomdir = os.path.join(outdir, "rooms")
    os.makedirs(roomdir, exist_ok=True)

    rendered = 0
    for room_idx in range(min(count, NUM_ROOMS)):
        x = room_idx % 16
        y = room_idx // 16

        # Get room's palette
        pal_idx = room_pal_map[room_idx] if room_idx < len(room_pal_map) else 0
        pal = palettes[min(pal_idx, len(palettes) - 1)]
        pal_rgb = [st_color_to_rgb(c) for c in pal]

        # Read room layout: 12 big platform indices (4 cols x 3 rows)
        room_off = ADDR_ROOM_LAYOUT + room_idx * 12
        bp_indices = list(ram[room_off:room_off + 12])

        # Compose room: 4 cols x 3 rows of big platforms
        # Each big plat = 64x48 native, render at 2x = 128x96
        img = Image.new('RGBA', (256 * 2, 144 * 2), (0, 0, 0, 255))

        for row in range(3):
            for col in range(4):
                bp_idx = bp_indices[row * 4 + col]
                if bp_idx >= NUM_BIGPLATS:
                    continue

                bp_off = ADDR_BIGPLAT_TABLE + bp_idx * 4
                tl, tr, bl, br = ram[bp_off], ram[bp_off + 1], ram[bp_off + 2], ram[bp_off + 3]

                for tile_idx, tdx, tdy in [
                    (tl, 0, 0), (tr, 32, 0), (bl, 0, 24), (br, 32, 24)
                ]:
                    if tile_idx >= NUM_TILES:
                        continue
                    ptr_off = ADDR_TILE_PTRS + tile_idx * 4
                    ptr = struct.unpack_from('>I', ram, ptr_off)[0]
                    if ptr == 0 or ptr + 4 >= len(ram):
                        continue

                    height = struct.unpack_from('>H', ram, ptr)[0]
                    width_param = struct.unpack_from('>H', ram, ptr + 2)[0]
                    if height == 0 or width_param == 0:
                        continue

                    width_groups = width_param // 4
                    pixels = decode_bitplane_block(ram, ptr + 4, width_groups, height)
                    tile_img = render_pixels(pixels, pal_rgb, scale=2, transparent_bg=True)

                    px = (col * 64 + tdx) * 2
                    py = (row * 48 + tdy) * 2
                    img.paste(tile_img, (px, py), tile_img)

        # Convert to RGB for saving
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

    print("\n4. Extracting big platforms...")
    plat_info = extract_big_platforms(ram, outdir, tile_info, palettes)

    print("\n5. Extracting rooms (first 64)...")
    extract_rooms(ram, outdir, palettes, room_pal_map, count=64)

    print("\n6. Extracting current screen...")
    extract_screen(ram, outdir, palettes, room_pal_map)

    print(f"\nDone! Output in {outdir}")


if __name__ == "__main__":
    main()
