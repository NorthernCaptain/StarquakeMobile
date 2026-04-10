#!/usr/bin/env python3
"""Find sealed rooms (all perimeter tiles solid) in Starquake map.

The "core room" is a special room where the planet's core is assembled.
It should be completely sealed — all perimeter tiles are solid (no entrances).

Room grid: 16 columns x 32 rows = 512 rooms.
Room index = row * 16 + column.
Each room: 8 x 6 tiles (big_platforms: 4 cols x 3 rows of 2x2 tile blocks).

A tile is solid if its tile ID is NOT in the non_solid_tiles list.
Non-solid tile IDs: [20, 33, 34, 41, 42, 48, 53, 54, 55, 56]

Perimeter = left column (col 0, rows 0-5) + right column (col 7, rows 0-5)
          + top row (row 0, cols 0-7) + bottom row (row 5, cols 0-7)
          (corners counted once)
Total perimeter tiles: 24 (8 + 8 + 4 + 4)
"""

import json
import math
import os
import sys


def main():
    metadata_path = os.path.join(
        os.path.dirname(__file__), "..", "android", "assets", "metadata.json"
    )
    with open(metadata_path, "r") as f:
        meta = json.load(f)

    rooms = meta["rooms"]
    big_platforms = meta["big_platforms"]
    non_solid_tiles = set(meta["non_solid_tiles"])

    # Build lookup for big_platforms by index
    bp_lookup = {}
    for bp in big_platforms:
        bp_lookup[bp["index"]] = bp

    # Map center (approximate)
    center_row = 16
    center_col = 8

    KEYS = ["tl", "tr", "bl", "br"]

    def get_tile_id(room, tile_col, tile_row):
        """Get the tile ID at (tile_col, tile_row) within a room."""
        bp_col = tile_col // 2
        bp_row = tile_row // 2
        bp_ref = room["big_platforms"][bp_row * 4 + bp_col]
        bp = bp_lookup[bp_ref]
        qi = (tile_row % 2) * 2 + (tile_col % 2)
        return bp[KEYS[qi]]

    def is_solid(tile_id):
        return tile_id not in non_solid_tiles

    print(f"Starquake Core Room Finder")
    print(f"=========================")
    print(f"Total rooms: {len(rooms)}")
    print(f"Non-solid tile IDs: {sorted(non_solid_tiles)}")
    print(f"Map center: row={center_row}, col={center_col} (room {center_row * 16 + center_col})")
    print()

    # Analyze each room
    results = []
    for room in rooms:
        room_idx = room["index"]
        row = room["y"]
        col = room["x"]

        open_edges = []

        # Perimeter positions with their edge direction
        # Top row (row 0): all 8 columns
        for tc in range(8):
            tid = get_tile_id(room, tc, 0)
            if not is_solid(tid):
                open_edges.append((tc, 0, tid))

        # Bottom row (row 5): all 8 columns
        for tc in range(8):
            tid = get_tile_id(room, tc, 5)
            if not is_solid(tid):
                open_edges.append((tc, 5, tid))

        # Left column (col 0): rows 1-4 (corners already counted)
        for tr in range(1, 5):
            tid = get_tile_id(room, 0, tr)
            if not is_solid(tid):
                open_edges.append((0, tr, tid))

        # Right column (col 7): rows 1-4 (corners already counted)
        for tr in range(1, 5):
            tid = get_tile_id(room, 7, tr)
            if not is_solid(tid):
                open_edges.append((7, tr, tid))

        dist = math.sqrt((row - center_row) ** 2 + (col - center_col) ** 2)
        results.append((room_idx, row, col, dist, len(open_edges), open_edges))

    results.sort(key=lambda x: (x[4], x[3]))

    # Print completely sealed rooms
    sealed = [r for r in results if r[4] == 0]
    print(f"COMPLETELY SEALED ROOMS (0 open perimeter tiles): {len(sealed)}")
    if sealed:
        for room_idx, row, col, dist, n, _ in sealed:
            print(f"  Room {room_idx:>3} at grid ({col}, {row})  dist={dist:.1f}")
    else:
        print("  (none found)")

    # Print nearly sealed rooms
    print()
    print(f"NEARLY SEALED ROOMS (1-4 open perimeter tiles):")
    print(f"  {'Room':>4}  {'Grid':>10}  {'Dist':>5}  {'Open':>4}  Open tile positions")
    print(f"  {'----':>4}  {'----------':>10}  {'-----':>5}  {'----':>4}  -------------------")
    for room_idx, row, col, dist, n, open_edges in results:
        if 1 <= n <= 4:
            details = ", ".join(f"(c{tc},r{tr})=tile{tid}" for tc, tr, tid in open_edges)
            print(f"  {room_idx:>4}  ({col:>2},{row:>2})     {dist:>5.1f}  {n:>4}  {details}")

    # Print detailed grid for top 5 candidates
    print()
    print("DETAILED TILE GRIDS (top 5 candidates):")
    for room_idx, row, col, dist, n, open_edges in results[:5]:
        room = rooms[room_idx]
        print(f"\n  Room {room_idx} at grid ({col}, {row}), palette={room['palette']}, "
              f"dist={dist:.1f}, open={n}")
        print(f"  Big platforms: {room['big_platforms']}")
        for tr in range(6):
            tiles = []
            for tc in range(8):
                tid = get_tile_id(room, tc, tr)
                marker = "*" if not is_solid(tid) else " "
                tiles.append(f"{tid:>3}{marker}")
            label = ""
            if tr == 0: label = " <- top edge"
            if tr == 5: label = " <- bottom edge"
            print(f"    r{tr}: {'  '.join(tiles)}{label}")
        print(f"         c0L   c1    c2    c3    c4    c5    c6   c7R")
        print(f"    (* = non-solid, L/R = left/right edge)")

    # Print rooms near center
    print()
    print("ROOMS NEAR MAP CENTER (dist < 5, open <= 6):")
    near_center = [(r, row, col, dist, n, oe)
                   for r, row, col, dist, n, oe in results
                   if dist < 5 and n <= 6]
    near_center.sort(key=lambda x: (x[4], x[3]))
    for room_idx, row, col, dist, n, open_edges in near_center:
        details = ", ".join(f"(c{tc},r{tr})=t{tid}" for tc, tr, tid in open_edges)
        print(f"  Room {room_idx:>3} ({col:>2},{row:>2}) dist={dist:.1f} open={n}: {details}")


if __name__ == "__main__":
    main()
