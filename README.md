# Starquake

A mobile clone of [Starquake](https://en.wikipedia.org/wiki/Starquake_(video_game)) (1985, Stephen Crow) built with libGDX.

The original was a ZX Spectrum 48K arcade-adventure where BLOB (Bio-Logically Operated Being) descends into a destabilized planet to reassemble its core. The defining mechanic: BLOB cannot jump — instead, it lays temporary crumbling platforms to navigate a 512-room underground world.

## Game Overview

- **512 flip-screen rooms** in a 16x32 open-world grid
- **No jumping** — build temporary platforms to traverse gaps and reach higher ground
- **9 randomized core pieces** to collect and deliver each playthrough
- **4-item FIFO inventory** with Cheops Pyramid trading system
- **15 teleporters** with 5-character codes for fast travel
- **Hover pads** for flight sections, **vacuum tube lifts** for vertical movement
- **3 resource bars** (energy, ammo, platforms) and 4 lives
- Enemies scale in difficulty with depth; several instant-kill hazard types

## Project Structure

```
Starquake/
├── research/               # Game design docs and extracted reference assets
│   ├── game-overview.md          # Story, history, reception
│   ├── gameplay-mechanics.md     # Core loop, controls, resources, scoring
│   ├── enemies-and-hazards.md    # Enemy types, static hazards, difficulty scaling
│   ├── map-and-levels.md         # World structure, room types, tile hierarchy
│   ├── items-and-trading.md      # Items, inventory, Cheops Pyramid trading
│   ├── teleporter-codes.md       # All 15 codes per platform version
│   ├── platform-differences.md   # ZX Spectrum / C64 / Atari ST / etc.
│   ├── technical-reference.md    # Specs, memory map, data formats
│   ├── graphics-pipeline.md      # Asset enhancement strategy and tools
│   ├── sprite-extraction.md      # How to extract graphics from the ROM
│   ├── asset-catalog.md          # Full catalog of extracted assets + rendering guide
│   ├── resources.md              # External links, maps, disassemblies
│   └── assets/                   # Extracted graphics from ZX Spectrum ROM
│       ├── tiles/                    # ~120 platform tiles (4x scale)
│       ├── sprites/                  # 44 sprite frames + colored BLOB sheets
│       ├── font/                     # 88 font characters (8x8 native)
│       ├── ui/                       # Border corners, laser variants
│       ├── big_platforms/            # 128 composed 2x2 tile groups
│       ├── rooms/                    # 64 pre-rendered rooms + full minimap
│       ├── starquake.z80             # ZX Spectrum snapshot
│       └── starquake_memory.bin      # Raw 64KB memory dump
└── tools/
    └── extract_graphics.py       # Asset extraction script
```

## Asset Extraction

All graphics were extracted directly from the ZX Spectrum ROM. No pre-existing sprite rips exist online for Starquake.

```bash
python -m venv .venv
source .venv/bin/activate
pip install Pillow skoolkit
python tools/extract_graphics.py research/assets/starquake.z80
```

## Reference Versions

| Purpose | Version | Why |
|---------|---------|-----|
| Gameplay & data | ZX Spectrum | Original; most documented; disassemblies available |
| Visual inspiration | C64 / Atari ST | Best-looking 8-bit and 16-bit versions |
| Audio inspiration | C64 (SID) | Iconic music composed by Stephen Crow |

## Key References

- [Icemark data format analysis](https://www.icemark.com/dataformats/starquake/index.html) — room/tile data structure
- [Level7 ZX Spectrum disassembly](https://level7.org.uk/miscellany/starquake-disassembly.txt) — annotated Z80 assembly
- [BBC Micro disassembly (GitHub)](https://github.com/reubenscratton/starquake) — full buildable source
- [Complete ZX Spectrum map](https://maps.speccy.cz/map.php?id=Starquake) — 4096x4656 fan-made map
- [TileMap interactive viewer](https://simonowen.com/articles/tilemap/) — technical analysis of room system
