#!/usr/bin/env python3
"""Export Atari ST effects/enemies as palette-index sprites.

The output PNGs store palette index * 17 in RGB, matching the terrain tile
index-map convention. Alpha comes from the ST sprite mask, so palette index 0
can remain an opaque color when the original sprite uses it.
"""

import struct
from pathlib import Path

from PIL import Image

PROJECT_ROOT = Path(__file__).resolve().parents[1]
RAM_PATH = PROJECT_ROOT / "research/assets/atarist/st_ram_full.bin"
OUT_DIR = PROJECT_ROOT / "game_assets/sprites"

ADDR_EFFECTS = 0x35148
FRAME_SIZE = 160
FRAME_COUNT = 62
SPRITE_SIZE = 16


def decode_frame(ram, frame_index):
    addr = ADDR_EFFECTS + frame_index * FRAME_SIZE
    pixels = []
    for y in range(SPRITE_SIZE):
        row_off = addr + y * 10
        mask_word = struct.unpack_from(">H", ram, row_off)[0]
        planes = [
            struct.unpack_from(">H", ram, row_off + 2 + plane * 2)[0]
            for plane in range(4)
        ]
        for bit in range(SPRITE_SIZE):
            bit_mask = 1 << (15 - bit)
            if mask_word & bit_mask:
                pixels.append((0, 0, 0, 0))
                continue

            color_index = sum(
                (1 << plane) for plane in range(4) if planes[plane] & bit_mask
            )
            value = color_index * 17
            pixels.append((value, value, value, 255))
    return pixels


def main():
    ram = RAM_PATH.read_bytes()
    OUT_DIR.mkdir(parents=True, exist_ok=True)

    for frame_index in range(FRAME_COUNT):
        img = Image.new("RGBA", (SPRITE_SIZE, SPRITE_SIZE), (0, 0, 0, 0))
        img.putdata(decode_frame(ram, frame_index))

        if frame_index < 24:
            out_path = OUT_DIR / f"effect_{frame_index:02d}.png"
        else:
            out_path = OUT_DIR / f"enemy_{frame_index:02d}.png"
        img.save(out_path)

    print(f"Exported {FRAME_COUNT} indexed effect/enemy sprites to {OUT_DIR}")


if __name__ == "__main__":
    main()
