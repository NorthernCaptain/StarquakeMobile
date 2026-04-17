#!/usr/bin/env python3
"""
Pack game_assets/ into libGDX texture atlases.

Atlas groups:
  tiles.atlas     - terrain tile index maps (grayscale, palette index per pixel)
  palettes.atlas  - 16x1 palette textures (26 palettes)
  sprites.atlas   - BLOB, laser, effects, enemies (RGBA)
  items.atlas     - collectible item icons (RGBA)
  screens.atlas   - pre-rendered screen backgrounds (RGBA)
  font.atlas      - font characters (RGBA)

Each atlas uses max 2048x2048 pages, Nearest filtering.
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
    "pot": True,
    "flattenPaths": True,
}


def run_packer(input_dir, output_dir, atlas_name):
    """Run the libGDX TexturePacker on a directory."""
    settings_path = os.path.join(input_dir, 'pack.json')
    with open(settings_path, 'w') as f:
        json.dump(PACK_SETTINGS, f, indent=2)

    os.makedirs(output_dir, exist_ok=True)

    cmd = [
        'java', '-jar', PACKER_JAR,
        input_dir,
        output_dir,
        atlas_name
    ]

    result = subprocess.run(cmd, capture_output=True, text=True, timeout=60)
    if result.returncode != 0:
        print(f"    ERROR: {result.stderr.strip()}")
        return False

    atlas_file = os.path.join(output_dir, f'{atlas_name}.atlas')
    if os.path.exists(atlas_file):
        pages = len([f for f in os.listdir(output_dir)
                     if f.startswith(atlas_name) and f.endswith('.png')])
        size = sum(os.path.getsize(os.path.join(output_dir, f))
                   for f in os.listdir(output_dir)
                   if f.startswith(atlas_name))
        return pages, size
    return False


def stage_files(src_dir, staging_dir, filter_fn=None):
    """Copy matching PNGs to a staging directory. Returns count."""
    os.makedirs(staging_dir, exist_ok=True)
    count = 0
    if not os.path.isdir(src_dir):
        return 0
    for fname in os.listdir(src_dir):
        if fname.endswith('.png') and not fname.startswith('_'):
            if filter_fn is None or filter_fn(fname):
                shutil.copy2(os.path.join(src_dir, fname), staging_dir)
                count += 1
    return count


def main():
    if not os.path.exists(PACKER_JAR):
        print(f"Error: TexturePacker not found at {PACKER_JAR}")
        sys.exit(1)

    # Clean output
    if os.path.exists(ATLAS_OUTPUT):
        shutil.rmtree(ATLAS_OUTPUT)
    os.makedirs(ATLAS_OUTPUT)

    atlas_groups = [
        ('tiles',    os.path.join(ASSETS_DIR, 'tiles'),    None),
        ('palettes', os.path.join(ASSETS_DIR, 'palettes'), None),
        ('sprites',  os.path.join(ASSETS_DIR, 'sprites'),  None),
        ('items',    os.path.join(ASSETS_DIR, 'items'),     None),
        ('screens',  os.path.join(ASSETS_DIR, 'screens'),  None),
        ('font',     os.path.join(ASSETS_DIR, 'font'),     None),
        ('intro',    os.path.join(ASSETS_DIR, 'intro'),    None),
    ]

    total_atlases = 0
    total_pages = 0
    total_size = 0

    with tempfile.TemporaryDirectory() as tmpdir:
        for atlas_name, src_dir, filter_fn in atlas_groups:
            staging = os.path.join(tmpdir, atlas_name)
            count = stage_files(src_dir, staging, filter_fn)

            if count == 0:
                print(f"  {atlas_name}: no files, skipping")
                continue

            print(f"  {atlas_name}: {count} files ... ", end='', flush=True)
            result = run_packer(staging, ATLAS_OUTPUT, atlas_name)
            if result:
                pages, size = result
                print(f"{pages} page(s), {size//1024}KB")
                total_atlases += 1
                total_pages += pages
                total_size += size
            else:
                print("FAILED")

    print(f"\n=== Atlas packing complete ===")
    print(f"  Atlases: {total_atlases}")
    print(f"  Pages:   {total_pages}")
    print(f"  Size:    {total_size//1024}KB")
    print(f"  Output:  {ATLAS_OUTPUT}/")

    print(f"\nFiles:")
    for f in sorted(os.listdir(ATLAS_OUTPUT)):
        size = os.path.getsize(os.path.join(ATLAS_OUTPUT, f))
        print(f"  {f:35s} {size//1024:5d}KB")


if __name__ == '__main__':
    main()
