# Starquake - Platform Differences

## Platform Comparison

### ZX Spectrum 48K (1985) -- Original
- **Developer**: Stephen Crow
- **Graphics**: Bright, colorful backgrounds; monochrome sprites (single color per 8x8 cell avoids attribute clash)
- **Sound**: Beeper-only (no dedicated sound chip); won CRASH Best Sound FX despite this limitation
- **Resolution**: 256 x 192 pixels
- **Color**: Attribute-based, 15 unique colors (8 colors x 2 brightness)
- **Map**: 512 rooms (16x32 grid)
- **Significance**: The original and historically most important version
- **Technical achievement**: Zero attribute clash -- remarkable for Spectrum

### Commodore 64 (1985) -- Best 8-bit Version
- **Developer**: Stephen Crow
- **Graphics**: Best 8-bit visuals -- colorful, non-chunky sprites with hardware sprite support
- **Sound**: SID chip music composed by Stephen Crow himself; described as "Jean-Michel Jarre-like"
- **Resolution**: 320 x 200 pixels (hi-res mode) or 160 x 200 (multicolor)
- **Map**: 512 rooms
- **Teleporter codes**: Different from Spectrum (e.g., "Chasm" instead of "QUAKE" for core)
- **Considered**: The definitive 8-bit experience for both graphics and audio

### Amstrad CPC (1985)
- **Graphics**: Subdued color palette compared to other versions
- **Sound**: AY-3-8910 sound chip (3 channels)
- **Assessment**: Weakest 8-bit version visually
- **Map**: 512 rooms (same structure)

### BBC Micro (1985)
- **Map**: Possibly **256 rooms** in a **16x16 grid** (half the Spectrum version)
- **Full disassembly available**: https://github.com/reubenscratton/starquake
- **Annotated disassembly**: https://level7.org.uk/miscellany/starquake-disassembly.txt

### MSX (1986)
- **Similar to ZX Spectrum** in capabilities
- **Map available**: at MSX Resource Center

### Atari 8-bit (1986)
- **Developer**: Nick Strange (port, not Stephen Crow)
- **Graphics**: Lacks the color vibrancy of other versions
- **Issues**: Collision detection problems reported
- **Assessment**: Inferior port; not recommended as reference

### Atari ST (1988) -- Best Visual Version
- **Developer**: Stephen Crow (returned to port it himself)
- **Graphics**: Best-looking version overall; 16-bit hardware capabilities
- **Sound**: Music by Jason C. Brooke -- described as "superb" and "fantastic"
- **Difficulty**: Notably **harder** than 8-bit versions
- **Resolution**: 320 x 200 pixels (16 colors from 512 palette)
- **Assessment**: The premium version of the game

### DOS (~1988)
- **Teleporter codes**: Unique set (Moria, Rubia, Abyss, etc.)
- **Less documented** than other versions

## Recommended Reference Versions for Clone Development

| Purpose | Version | Reason |
|---------|---------|--------|
| **Gameplay mechanics** | ZX Spectrum | Original design intent; most documented; disassemblies available |
| **Visual reference** | C64 or Atari ST | Best-looking versions for art style inspiration |
| **Audio reference** | C64 (SID) | Most iconic music; Atari ST also excellent |
| **Technical data** | ZX Spectrum | Most thoroughly reverse-engineered; Icemark data format docs |
| **Map reference** | ZX Spectrum | Complete 4096x4656 pixel fan map available |

## Key Differences Between Versions

### Teleporter Codes (all vary by platform)
See `teleporter-codes.md` for complete tables.

### Difficulty
- Atari ST is notably harder than 8-bit versions
- Enemy AI and placement may differ slightly between versions

### Map Size
- Most versions: 512 rooms (16x32)
- BBC Micro: possibly 256 rooms (16x16)

### Visual Style
All versions share the same core aesthetic but differ in color depth and sprite quality:
- ZX Spectrum: Monochrome sprites on colorful backgrounds
- C64: Full-color sprites
- Atari ST: Enhanced 16-bit color palette
