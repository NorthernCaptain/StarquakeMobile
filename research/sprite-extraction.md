# Starquake - Sprite and Tile Extraction Guide

## Overview

No pre-existing Starquake sprite rips exist online. All graphic data must be extracted from the game ROM/snapshot. The data format is fully documented thanks to reverse engineering efforts.

## Source Files

Download the ZX Spectrum game file from:
- **Spectrum Computing**: https://spectrumcomputing.co.uk/entry/4873/ZX-Spectrum/Starquake
- Formats available: .TAP, .TZX, .Z80, .SNA

## ZX Spectrum Graphics Format

### Screen Memory (for reference -- NOT how game tiles are stored internally)

| Parameter | Value |
|-----------|-------|
| Resolution | 256 x 192 pixels |
| Pixel data | $4000-$57FF (6144 bytes), 1 bit per pixel |
| Attribute data | $5800-$5AFF (768 bytes), 1 byte per 8x8 cell |
| Memory layout | Non-linear (interleaved thirds) |

### Pixel Address Formula
```python
def pixel_address(x, y):
    third = y // 64           # 0, 1, or 2
    char_row = (y // 8) % 8   # 0-7 within the third
    pixel_line = y % 8        # 0-7 within the character
    col_byte = x // 8         # 0-31
    return 0x4000 + (third * 2048) + (pixel_line * 256) + (char_row * 32) + col_byte
```

### Attribute Byte Format
```
Bit 7: FLASH (0=off, 1=flashing)
Bit 6: BRIGHT (0=normal, 1=bright)  ** Also used as solidity flag in Starquake! **
Bits 5-3: PAPER (background color, 0-7)
Bits 2-0: INK (foreground color, 0-7)
```

### The 15-Color Palette
```
Index  Normal RGB        Bright RGB
0      (0,0,0)          (0,0,0)         Black
1      (0,0,205)        (0,0,255)       Blue
2      (205,0,0)        (255,0,0)       Red
3      (205,0,205)      (255,0,255)     Magenta
4      (0,205,0)        (0,255,0)       Green
5      (0,205,205)      (0,255,255)     Cyan
6      (205,205,0)      (255,255,0)     Yellow
7      (205,205,205)    (255,255,255)   White
```

## Starquake Internal Tile Format

**Important**: Game tiles are stored in a simpler linear format in ROM, NOT in the interleaved screen memory layout.

### Memory Map

| Address | Decimal | Content |
|---------|---------|---------|
| $7530 | 30000 | Room layout data (512 rooms, 12 bytes each) |
| $9740 | 38720 | Platform behavior/type data |
| $9840 | 38976 | Big platform definitions |
| $ADD4 | 44500 | Font data |
| $DF40 | 57152 | Blank graphic |
| $E074 | 57460 | BLOB and character sprites |
| $E8B4 | 59572 | Laser sprite |
| $EB23 | 60195 | Platform tile graphics |
| $6661 | 26209 | Border corner graphics |

### Platform Tile Format (at $EB23)

Each platform tile can be up to 8x6 character cells (64x48 pixels):

```
Bytes 0-5:  Bitmask (6 bytes = 48 bits)
            Each bit represents one 8x8 block position
            1 = block present in data, 0 = block absent (empty)
            Arranged as 8 columns x 6 rows

Then for each set bit (present block):
  8 bytes:  Pixel data (1 byte per row, MSB = leftmost pixel)
            This is straightforward linear bitmap data

Attribute data is at negative offsets from the tile base address.
```

### BLOB Sprite Format (at $E074)

```
Size:       24 x 16 pixels (3 x 2 character cells)
Frames:     16 total (8 left-facing, 8 right-facing)
Per frame:  48 bytes of pixel data
            (3 bytes per row x 16 rows = 48 bytes)
```

### Big Platform Format (at $9840)

```
4-byte records:
  Each byte references a small platform tile index
  Arranged as 2x2 grid, drawn bottom-right to top-left
```

### Room Format (at $7530)

```
12 bytes per room:
  4 columns x 3 rows of big platform indices
  Room coordinate: x = room# MOD 16, y = room# / 16
```

### Platform Behavior Encoding (at $9740)

```
Value < $50:  Attribute/color values
$5n:          Transport tubes
$6n:          Lethal/hazard zones
$7n:          Ray effects
$Cn:          Pickups (flying pod, etc.)
$Dn:          Teleporter zones
```

## Extraction Tools

### SkoolKit (Recommended)

```bash
pip install skoolkit

# Convert TZX tape to Z80 snapshot
tap2sna.py starquake.tzx starquake.z80

# Extract single 8x8 tile at address as PNG
sna2img.py -e '#UDG60195,56,4' starquake.z80 tile.png

# Extract sprite array (BLOB frames)
sna2img.py -e '#UDGARRAY3,56,4,1(57460-57507-8)(blob.png)' starquake.z80

# The #UDG macro format:
# #UDGaddr[,attr,scale,step,inc,flip,rotate,mask,tindex,alpha]

# The #UDGARRAY macro format:
# #UDGARRAYwidth[,attr,scale,step,inc,flip,rotate,mask,tindex,alpha](addr[-addr2[-step]])...(filename)
```

### Custom Python Script (Most Control)

```python
"""
Skeleton for extracting Starquake tiles from a .z80 snapshot.
Uses Pillow for image output and the Icemark data format specification.
"""
from PIL import Image
import struct

# ZX Spectrum color palette (normal and bright variants)
PALETTE = {
    (0, 0): (0, 0, 0),        # Black normal
    (0, 1): (0, 0, 0),        # Black bright
    (1, 0): (0, 0, 205),      # Blue normal
    (1, 1): (0, 0, 255),      # Blue bright
    (2, 0): (205, 0, 0),      # Red normal
    (2, 1): (255, 0, 0),      # Red bright
    (3, 0): (205, 0, 205),    # Magenta normal
    (3, 1): (255, 0, 255),    # Magenta bright
    (4, 0): (0, 205, 0),      # Green normal
    (4, 1): (0, 255, 0),      # Green bright
    (5, 0): (0, 205, 205),    # Cyan normal
    (5, 1): (0, 255, 255),    # Cyan bright
    (6, 0): (205, 205, 0),    # Yellow normal
    (6, 1): (255, 255, 0),    # Yellow bright
    (7, 0): (205, 205, 205),  # White normal
    (7, 1): (255, 255, 255),  # White bright
}

def load_z80_snapshot(filepath):
    """Load a .z80 snapshot and return the 48K memory as bytes."""
    # .z80 format has a header followed by (possibly compressed) memory
    # Implementation depends on .z80 version (v1, v2, v3)
    # See: https://worldofspectrum.org/faq/reference/z80format.htm
    pass  # TODO: implement

def extract_tile(memory, addr, bitmask_bytes=6):
    """
    Extract a platform tile from Starquake's compressed format.
    
    Args:
        memory: 48K byte array
        addr: start address of tile data
        bitmask_bytes: number of bitmask bytes (6 for 8x6 grid)
    
    Returns:
        List of (x, y, pixel_data) for each present 8x8 block
    """
    bitmask = memory[addr:addr + bitmask_bytes]
    blocks = []
    data_offset = addr + bitmask_bytes
    
    for byte_idx in range(bitmask_bytes):
        for bit_idx in range(7, -1, -1):  # MSB first
            block_num = byte_idx * 8 + (7 - bit_idx)
            col = block_num % 8
            row = block_num // 8
            
            if bitmask[byte_idx] & (1 << bit_idx):
                pixel_data = memory[data_offset:data_offset + 8]
                blocks.append((col, row, pixel_data))
                data_offset += 8
    
    return blocks

def render_tile(blocks, attr_ink=7, attr_paper=0, bright=False):
    """Render extracted blocks to a PIL Image."""
    # Find bounds
    if not blocks:
        return None
    max_col = max(b[0] for b in blocks) + 1
    max_row = max(b[1] for b in blocks) + 1
    
    img = Image.new('RGBA', (max_col * 8, max_row * 8), (0, 0, 0, 0))
    ink_rgb = PALETTE[(attr_ink, int(bright))]
    paper_rgb = PALETTE[(attr_paper, int(bright))]
    
    for col, row, pixel_data in blocks:
        for py in range(8):
            byte = pixel_data[py]
            for px in range(8):
                bit = (byte >> (7 - px)) & 1
                color = ink_rgb if bit else paper_rgb
                img.putpixel((col * 8 + px, row * 8 + py), color + (255,))
    
    return img

# Usage:
# memory = load_z80_snapshot('starquake.z80')
# blocks = extract_tile(memory, 0xEB23)
# img = render_tile(blocks, attr_ink=6, attr_paper=0, bright=True)
# img.save('tile_0.png')
```

### zxED (Visual Exploration)

- GUI tool for browsing ZX Spectrum snapshot memory as bitmaps
- Download: https://sourceforge.net/projects/zxed/
- Load .z80 snapshot, navigate to known addresses, visually identify sprites
- Export selections as PNG
- Good for exploration; less good for batch extraction

### scrconv (Screenshot Converter)

```bash
# Convert .SCR (raw screen dump) files to PNG
# Useful for capturing full room screenshots from an emulator
go install github.com/mrcook/scrconv@latest
scrconv starquake_screen.scr output.png
```

## Extraction Checklist

- [ ] Download Starquake .z80 snapshot
- [ ] Extract all platform tiles from $EB23 region
- [ ] Extract BLOB sprite frames from $E074 (16 frames, 24x16 each)
- [ ] Extract enemy sprites (addresses TBD from disassembly)
- [ ] Extract object/item sprites
- [ ] Extract laser sprite from $E8B4
- [ ] Extract font from $ADD4
- [ ] Extract border/UI graphics from $6661
- [ ] Catalog each tile with its role (ground, wall, hazard, decoration, etc.)
- [ ] Export as individual PNGs + combined sprite sheet atlas
- [ ] Document tile-to-room mapping for level reconstruction

## Key References

| Resource | URL |
|----------|-----|
| Icemark Data Format (primary) | https://www.icemark.com/dataformats/starquake/index.html |
| SkoolKit | https://github.com/skoolkid/skoolkit |
| SkoolKit Image Macros | https://skoolkit.ca/docs/skoolkit/skool-macros.html |
| BBC Micro Disassembly | https://github.com/reubenscratton/starquake |
| ZX Spectrum Screen Layout | http://www.breakintoprogram.co.uk/hardware/computers/zx-spectrum/screen-memory-layout |
| .z80 File Format Spec | https://worldofspectrum.org/faq/reference/z80format.htm |
| Spectrum Computing (game files) | https://spectrumcomputing.co.uk/entry/4873/ZX-Spectrum/Starquake |
