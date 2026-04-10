#!/usr/bin/env python3
"""
Render all 512 Starquake rooms from metadata.json and tile atlas.

Generates individual room images and a full map composite.
Output goes to research/assets/atarist/rooms/.
"""

import json
import os
import re
import sys
from PIL import Image, ImageDraw, ImageFont

# Paths relative to project root
SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
PROJECT_ROOT = os.path.dirname(SCRIPT_DIR)

METADATA_PATH = os.path.join(PROJECT_ROOT, "android", "assets", "metadata.json")
ATLAS_PATH = os.path.join(PROJECT_ROOT, "android", "assets", "atlases", "tiles.atlas")
TILES_PNG_PATH = os.path.join(PROJECT_ROOT, "android", "assets", "atlases", "tiles.png")
PALETTES_PATH = os.path.join(PROJECT_ROOT, "android", "assets", "palettes.png")
OUTPUT_DIR = os.path.join(PROJECT_ROOT, "research", "assets", "atarist", "rooms")

# Constants
TILE_W = 32
TILE_H = 24
ROOM_COLS = 8  # tiles per room horizontally
ROOM_ROWS = 6  # tiles per room vertically
ROOM_W = ROOM_COLS * TILE_W   # 256
ROOM_H = ROOM_ROWS * TILE_H   # 144
BP_COLS = 4    # big platforms per room horizontally
BP_ROWS = 3    # big platforms per room vertically
GRID_COLS = 16  # rooms in map horizontally
GRID_ROWS = 32  # rooms in map vertically
NUM_ROOMS = GRID_COLS * GRID_ROWS  # 512


def parse_atlas(atlas_path):
    """Parse a libGDX atlas file and return a dict of tile index -> (x, y, w, h)."""
    tiles = {}
    with open(atlas_path, "r") as f:
        lines = f.readlines()

    i = 0
    # Skip header (filename, size, format, filter, repeat lines)
    while i < len(lines):
        line = lines[i].strip()
        if line == "tile":
            break
        i += 1

    # Parse tile entries
    while i < len(lines):
        line = lines[i].strip()
        if line == "tile":
            # Read the tile's properties
            props = {}
            i += 1
            while i < len(lines) and lines[i].startswith("  "):
                prop_line = lines[i].strip()
                key, _, value = prop_line.partition(":")
                props[key.strip()] = value.strip()
                i += 1

            # Extract values
            xy = props.get("xy", "0, 0")
            size = props.get("size", "32, 24")
            index_str = props.get("index", "-1")

            x, y = [int(v.strip()) for v in xy.split(",")]
            w, h = [int(v.strip()) for v in size.split(",")]
            tile_index = int(index_str)

            if tile_index >= 0:
                # Store tile region. Only overwrite if this is 32x24 (prefer standard size).
                if tile_index not in tiles or (w == TILE_W and h == TILE_H):
                    tiles[tile_index] = (x, y, w, h)
        else:
            i += 1

    return tiles


def load_palettes(palettes_path):
    """Load palette image and return list of palette rows, each a list of (R,G,B,A) tuples."""
    palette_img = Image.open(palettes_path).convert("RGBA")
    palettes = []
    for row in range(palette_img.height):
        colors = []
        for col in range(palette_img.width):
            colors.append(palette_img.getpixel((col, row)))
        palettes.append(colors)
    return palettes


def colorize_tile(tile_gray, palette_colors):
    """
    Apply palette coloring to a grayscale tile image.

    tile_gray: PIL Image in RGBA mode (grayscale values in R channel)
    palette_colors: list of 16 (R,G,B,A) tuples

    Returns a new RGBA PIL Image with palette colors applied.
    """
    w, h = tile_gray.size
    result = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    src_data = tile_gray.load()
    dst_data = result.load()

    for py in range(h):
        for px in range(w):
            r, g, b, a = src_data[px, py]
            if a == 0:
                # Transparent pixel stays transparent
                dst_data[px, py] = (0, 0, 0, 0)
            else:
                # Map grayscale to palette index
                palette_idx = round(r / 17)
                palette_idx = min(palette_idx, len(palette_colors) - 1)
                pr, pg, pb, pa = palette_colors[palette_idx]
                # If palette index 0 (black/background), keep it opaque
                dst_data[px, py] = (pr, pg, pb, 255)

    return result


def extract_tile_region(tiles_img, tile_info):
    """
    Extract a 32x24 tile region from the atlas image.

    tile_info: (x, y, w, h) from atlas. Y is from top of image.
    Returns a 32x24 RGBA image.
    """
    x, y, w, h = tile_info
    # Crop from atlas (y is already from top in the atlas)
    region = tiles_img.crop((x, y, x + w, y + h))

    # If larger than 32x24, crop to top-left 32x24
    if w > TILE_W or h > TILE_H:
        region = region.crop((0, 0, min(w, TILE_W), min(h, TILE_H)))

    # If smaller, paste into a 32x24 canvas
    if region.size != (TILE_W, TILE_H):
        canvas = Image.new("RGBA", (TILE_W, TILE_H), (0, 0, 0, 0))
        canvas.paste(region, (0, 0))
        region = canvas

    return region


def render_room(room_data, big_platforms, tile_atlas, tiles_img, palettes):
    """
    Render a single room as a 256x144 RGBA image.

    room_data: dict with 'palette' and 'big_platforms' fields
    big_platforms: list of all big platform dicts
    tile_atlas: dict of tile index -> (x, y, w, h)
    tiles_img: PIL Image of the full atlas
    palettes: list of palette color arrays
    """
    room_img = Image.new("RGBA", (ROOM_W, ROOM_H), (0, 0, 0, 255))
    palette_idx = room_data["palette"]
    palette_colors = palettes[palette_idx] if palette_idx < len(palettes) else palettes[0]

    bp_indices = room_data["big_platforms"]

    for bp_row in range(BP_ROWS):
        for bp_col in range(BP_COLS):
            bp_list_idx = bp_row * BP_COLS + bp_col
            bp_index = bp_indices[bp_list_idx]

            if bp_index >= len(big_platforms):
                continue

            bp = big_platforms[bp_index]

            # Each big platform has 4 tiles in a 2x2 grid
            # tl=top-left, tr=top-right, bl=bottom-left, br=bottom-right
            quadrants = [
                ("tl", 0, 0),
                ("tr", 1, 0),
                ("bl", 0, 1),
                ("br", 1, 1),
            ]

            for quad_name, qcol, qrow in quadrants:
                tile_id = bp[quad_name]

                # Calculate position in room image
                # Big platform position: bp_col * 2 tiles + qcol, bp_row * 2 tiles + qrow
                tile_col = bp_col * 2 + qcol
                tile_row = bp_row * 2 + qrow
                dst_x = tile_col * TILE_W
                dst_y = tile_row * TILE_H

                if tile_id in tile_atlas:
                    tile_info = tile_atlas[tile_id]
                    tile_region = extract_tile_region(tiles_img, tile_info)
                    colored_tile = colorize_tile(tile_region, palette_colors)
                    room_img.paste(colored_tile, (dst_x, dst_y), colored_tile)
                # If tile not found, leave black

    return room_img


def render_full_map(room_images, output_path):
    """
    Create a full map image with all rooms placed in grid positions,
    with room numbers overlaid.
    """
    map_w = GRID_COLS * ROOM_W   # 4096
    map_h = GRID_ROWS * ROOM_H   # 4608
    full_map = Image.new("RGBA", (map_w, map_h), (0, 0, 0, 255))
    draw = ImageDraw.Draw(full_map)

    # Try to get a font for room numbers
    font = None
    font_size = 32
    try:
        # Try common system fonts
        for font_path in [
            "/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf",
            "/usr/share/fonts/truetype/liberation/LiberationSans-Bold.ttf",
            "/usr/share/fonts/TTF/DejaVuSans-Bold.ttf",
            "/usr/share/fonts/truetype/freefont/FreeSansBold.ttf",
            "/System/Library/Fonts/Helvetica.ttc",
        ]:
            if os.path.exists(font_path):
                font = ImageFont.truetype(font_path, font_size)
                break
    except Exception:
        pass

    if font is None:
        try:
            font = ImageFont.load_default(size=font_size)
        except TypeError:
            # Older Pillow without size parameter
            font = ImageFont.load_default()

    for room_idx, room_img in room_images.items():
        row = room_idx // GRID_COLS
        col = room_idx % GRID_COLS
        x = col * ROOM_W
        y = row * ROOM_H
        full_map.paste(room_img, (x, y))

        # Draw room number in center
        label = str(room_idx)
        bbox = draw.textbbox((0, 0), label, font=font)
        tw = bbox[2] - bbox[0]
        th = bbox[3] - bbox[1]
        tx = x + (ROOM_W - tw) // 2
        ty = y + (ROOM_H - th) // 2

        # Shadow text (+2,+2 black) then white on top
        draw.text((tx + 2, ty + 2), label, fill=(0, 0, 0, 255), font=font)
        draw.text((tx, ty), label, fill=(255, 255, 255, 255), font=font)

    full_map.save(output_path)
    print(f"Full map saved: {output_path} ({map_w}x{map_h})")


def main():
    # Load metadata
    print("Loading metadata...")
    with open(METADATA_PATH, "r") as f:
        metadata = json.load(f)

    rooms = metadata["rooms"]
    big_platforms = metadata["big_platforms"]

    print(f"  {len(rooms)} rooms, {len(big_platforms)} big platforms")

    # Parse tile atlas
    print("Parsing tile atlas...")
    tile_atlas = parse_atlas(ATLAS_PATH)
    print(f"  {len(tile_atlas)} tile regions parsed")

    # Load tile atlas image
    print("Loading tile atlas image...")
    tiles_img = Image.open(TILES_PNG_PATH).convert("RGBA")
    print(f"  Atlas size: {tiles_img.size}")

    # Load palettes
    print("Loading palettes...")
    palettes = load_palettes(PALETTES_PATH)
    print(f"  {len(palettes)} palettes loaded")

    # Create output directory
    os.makedirs(OUTPUT_DIR, exist_ok=True)
    print(f"Output directory: {OUTPUT_DIR}")

    # Render all rooms
    print(f"\nRendering {NUM_ROOMS} rooms...")
    room_images = {}
    errors = 0

    for room_data in rooms:
        room_idx = room_data["index"]
        col = room_data["x"]
        row = room_data["y"]

        try:
            room_img = render_room(room_data, big_platforms, tile_atlas, tiles_img, palettes)
            room_images[room_idx] = room_img

            filename = f"room_{room_idx:03d}_x{col:02d}_y{row:02d}.png"
            filepath = os.path.join(OUTPUT_DIR, filename)
            room_img.save(filepath)

            if (room_idx + 1) % 50 == 0:
                print(f"  Rendered {room_idx + 1}/{NUM_ROOMS} rooms...")

        except Exception as e:
            print(f"  ERROR rendering room {room_idx}: {e}", file=sys.stderr)
            errors += 1

    print(f"\nRoom rendering complete: {len(room_images)} rooms generated, {errors} errors")

    # Generate full map
    print("\nGenerating full map...")
    map_path = os.path.join(OUTPUT_DIR, "full_map.png")
    render_full_map(room_images, map_path)

    print(f"\nDone! Files written to {OUTPUT_DIR}")


if __name__ == "__main__":
    main()
