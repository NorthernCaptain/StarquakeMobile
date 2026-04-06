#!/usr/bin/env python3
"""
Pack game_assets/ into libGDX texture atlases grouped by purpose/color.

Atlas groups:
  terrain_red.atlas      - red terrain tiles
  terrain_magenta.atlas  - magenta terrain tiles
  terrain_cyan.atlas     - cyan terrain tiles
  terrain_yellow.atlas   - yellow terrain tiles
  terrain_green.atlas    - green terrain tiles
  terrain_white.atlas    - white terrain tiles
  fixed.atlas            - fixed-color tiles (tubes, teleporters, hazards)
  sprites.atlas          - BLOB + enemies
  font.atlas             - font characters

Each atlas uses max 2048x2048 pages, Nearest filtering, no padding bleed.
Output goes to game_assets/atlases/ ready for libGDX AssetManager.

Usage:
  python tools/pack_atlases.py
"""

import os
import sys
import json
import shutil
import subprocess
import tempfile

PROJECT_ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ASSETS_DIR = os.path.join(PROJECT_ROOT, 'game_assets')
ATLAS_OUTPUT = os.path.join(ASSETS_DIR, 'atlases')
PACKER_JAR = os.path.join(PROJECT_ROOT, 'tools', 'runnable-texturepacker.jar')

# libGDX TexturePacker settings (written as pack.json in each input dir)
PACK_SETTINGS = {
    "maxWidth": 2048,
    "maxHeight": 2048,
    "paddingX": 2,
    "paddingY": 2,
    "edgePadding": True,
    "duplicatePadding": True,
    "filterMin": "Nearest",
    "filterMag": "Nearest",
    "format": "RGBA8888",
    "stripWhitespaceX": False,
    "stripWhitespaceY": False,
    "pot": True,           # power of two
    "flattenPaths": True,  # no subdirs in region names
}


def run_packer(input_dir, output_dir, atlas_name):
    """Run the libGDX TexturePacker on a directory."""
    # Write pack.json settings
    settings_path = os.path.join(input_dir, 'pack.json')
    with open(settings_path, 'w') as f:
        json.dump(PACK_SETTINGS, f, indent=2)

    os.makedirs(output_dir, exist_ok=True)

    cmd = [
        'java', '-jar', PACKER_JAR,
        input_dir,     # input directory
        output_dir,    # output directory
        atlas_name     # atlas file name (without .atlas)
    ]

    result = subprocess.run(cmd, capture_output=True, text=True, timeout=60)
    if result.returncode != 0:
        print(f"    ERROR: {result.stderr.strip()}")
        return False

    # Count output files
    atlas_file = os.path.join(output_dir, f'{atlas_name}.atlas')
    if os.path.exists(atlas_file):
        # Count pages
        pages = len([f for f in os.listdir(output_dir)
                     if f.startswith(atlas_name) and f.endswith('.png')])
        size = sum(os.path.getsize(os.path.join(output_dir, f))
                   for f in os.listdir(output_dir)
                   if f.startswith(atlas_name))
        return pages, size
    return False


def stage_files(src_patterns, staging_dir):
    """Copy matching files to a staging directory. Returns count."""
    os.makedirs(staging_dir, exist_ok=True)
    count = 0
    for src_dir, pattern_fn in src_patterns:
        if not os.path.isdir(src_dir):
            continue
        for fname in os.listdir(src_dir):
            if fname.endswith('.png') and pattern_fn(fname):
                shutil.copy2(os.path.join(src_dir, fname), staging_dir)
                count += 1
    return count


def main():
    if not os.path.exists(PACKER_JAR):
        print(f"Error: TexturePacker not found at {PACKER_JAR}")
        sys.exit(1)

    tiles_dir = os.path.join(ASSETS_DIR, 'tiles')
    sprites_dir = os.path.join(ASSETS_DIR, 'sprites')
    font_dir = os.path.join(ASSETS_DIR, 'font')

    # Clean output
    if os.path.exists(ATLAS_OUTPUT):
        shutil.rmtree(ATLAS_OUTPUT)
    os.makedirs(ATLAS_OUTPUT)

    # Define atlas groups
    terrain_colors = ['red', 'magenta', 'cyan', 'yellow', 'green', 'white']

    with tempfile.TemporaryDirectory() as tmpdir:
        total_atlases = 0
        total_pages = 0
        total_size = 0

        # --- Terrain atlases (one per color) ---
        for color in terrain_colors:
            staging = os.path.join(tmpdir, f'terrain_{color}')
            count = stage_files([
                (tiles_dir, lambda f, c=color: f.endswith(f'_{c}.png'))
            ], staging)

            if count == 0:
                continue

            print(f"  terrain_{color}: {count} tiles ... ", end='', flush=True)
            result = run_packer(staging, ATLAS_OUTPUT, f'terrain_{color}')
            if result:
                pages, size = result
                print(f"{pages} page(s), {size//1024}KB")
                total_atlases += 1
                total_pages += pages
                total_size += size
            else:
                print("FAILED")

        # --- Fixed tiles atlas ---
        staging = os.path.join(tmpdir, 'fixed')
        # Fixed tiles have no color suffix (just tile_NNN.png)
        count = stage_files([
            (tiles_dir, lambda f: f.startswith('tile_') and f.count('_') == 1)
        ], staging)

        if count > 0:
            print(f"  fixed: {count} tiles ... ", end='', flush=True)
            result = run_packer(staging, ATLAS_OUTPUT, 'fixed')
            if result:
                pages, size = result
                print(f"{pages} page(s), {size//1024}KB")
                total_atlases += 1
                total_pages += pages
                total_size += size

        # --- Sprites atlas ---
        staging = os.path.join(tmpdir, 'sprites')
        count = stage_files([
            (sprites_dir, lambda f: f.endswith('.png'))
        ], staging)

        if count > 0:
            print(f"  sprites: {count} images ... ", end='', flush=True)
            result = run_packer(staging, ATLAS_OUTPUT, 'sprites')
            if result:
                pages, size = result
                print(f"{pages} page(s), {size//1024}KB")
                total_atlases += 1
                total_pages += pages
                total_size += size

        # --- Font atlas ---
        staging = os.path.join(tmpdir, 'font')
        count = stage_files([
            (font_dir, lambda f: f.endswith('.png'))
        ], staging)

        if count > 0:
            print(f"  font: {count} chars ... ", end='', flush=True)
            result = run_packer(staging, ATLAS_OUTPUT, 'font')
            if result:
                pages, size = result
                print(f"{pages} page(s), {size//1024}KB")
                total_atlases += 1
                total_pages += pages
                total_size += size

    # --- Summary ---
    print(f"\n=== Atlas packing complete ===")
    print(f"  Atlases: {total_atlases}")
    print(f"  Pages:   {total_pages}")
    print(f"  Size:    {total_size//1024}KB")
    print(f"  Output:  {ATLAS_OUTPUT}/")

    # List output files
    print(f"\nFiles:")
    for f in sorted(os.listdir(ATLAS_OUTPUT)):
        size = os.path.getsize(os.path.join(ATLAS_OUTPUT, f))
        print(f"  {f:35s} {size//1024:5d}KB")


if __name__ == '__main__':
    main()
