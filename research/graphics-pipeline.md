# Starquake Clone - Graphics Enhancement Pipeline

## The Challenge

The original ZX Spectrum graphics are:
- 8x8 pixel tiles, max 2 colors per tile (1 ink + 1 paper per 8x8 cell)
- 15-color palette total (8 colors x 2 brightness, black counted once)
- 24x16 pixel character sprites
- "Molecular model kit" tessellating aesthetic

We want: **same pixel art style, but richer** -- more colors, shading, depth. Still pixelated, not smooth.

## Strategy: Extract -> Enhance -> Generate

### Phase 0: Extract Original Assets

No pre-existing sprite rips of Starquake exist anywhere online. We must extract them ourselves.

**Best tool: SkoolKit** (Python, open source)
```bash
pip install skoolkit
```

Known graphic addresses in ZX Spectrum memory:
| Address | Content |
|---------|---------|
| $E074 (57460) | BLOB sprites (24x16 px, 16 frames) |
| $EB23 (60195) | Platform/tile graphics |
| $9840 (38976) | Big platform definitions (2x2 tile compositions) |
| $9740 (38720) | Platform behavior/type data |
| $E8B4 | Laser sprite |
| $ADD4 | Font data |
| $7530 (30000) | Room layout data (512 rooms) |

**Extraction commands:**
```bash
# Get a .z80 snapshot from spectrumcomputing.co.uk
# Extract BLOB sprite
sna2img.py -e '#UDG57460,56,4' starquake.z80 blob.png

# Extract array of platform tiles
sna2img.py -e '#UDGARRAY8(60195-60259-8)(platforms.png)' starquake.z80
```

**Alternative: Custom Python script** using Pillow, parsing the Icemark data format directly. This gives the most control -- can batch-export every tile as individual PNG files.

**Tile data format** (from Icemark analysis):
- Each platform tile: first 6 bytes = bitmask (which of 48 possible 8x8 blocks exist)
- Then 8 bytes per present block (1 byte per pixel row, MSB = leftmost)
- Attribute bytes at negative offsets from tile data
- Bright bit (bit 6) of attribute = solidity flag (game mechanic encoded in color!)

### Phase 1: Upscale from 8x8 to 16x16 or 32x32

Target: **16x16 tiles** (2x original). This is the sweet spot for mobile -- readable on phone screens while staying authentically retro.

**AI Pixel Art Upscalers:**

| Tool | Price | How It Works |
|------|-------|-------------|
| **Retro Diffusion** | $65 one-time | Runs locally as Aseprite plugin. Custom AI model. Has "Neural Resize" for upscaling and "RD-Detail" for adding detail. Pixel-perfect output. Best for batch work. |
| **Upsampler.com** | Free tier available | Web-based. 2x/4x/8x upscale. Sprite-sheet-aware. Good for quick tests. |
| **PixelLab** | $9-22/mo | Cloud-based. Style-consistent generation, animation support, tileset-specific features (Wang tiles, dual-grid). Best for generating new tiles from reference. |

**Recommended: Retro Diffusion** -- one-time cost, unlimited local use, Aseprite integration, designed exactly for this use case.

### Phase 2: Recolor and Add Depth

The original has 2 colors per tile. We want 8-16+ colors per tile with shading and depth.

**Approach A: AI-assisted recoloring**
- Feed extracted tiles to Retro Diffusion or PixelLab as reference images
- AI generates enhanced versions maintaining the shape but adding color depth, shading, highlights
- Retro Diffusion's "Palettize" feature can apply a richer palette to existing tiles

**Approach B: Programmatic enhancement**
- Take the monochrome tile shapes as masks
- Apply gradient fills, edge highlights, ambient occlusion
- Add per-tile palettes (8-16 colors each) instead of the 2-color Spectrum limit
- Can be scripted in Python with Pillow

**Approach C: Hybrid (recommended)**
- AI-regenerate the ~30 most important/visible tiles (ground, walls, key structures)
- Batch-enhance the rest programmatically
- Hand-tweak anything that looks off in Aseprite

### Phase 3: Character and Enemy Sprites

**BLOB (player character):**
- Most visible sprite in the game -- worth investing in quality
- Option 1: Commission on Fiverr (~$50-100 for a 16-frame sprite sheet)
- Option 2: Use PixelLab to generate from the original as reference
- Option 3: Retro Diffusion with the original sprite as input + "RD-Detail" enhancement
- Need: 16 frames (8 left, 8 right), walk cycle animation, at 48x32 pixels (2x upscale)

**Enemies:**
- Multiple types with simpler animation needs
- AI-generate using PixelLab with text prompts describing each enemy type
- Use original sprites as style reference to maintain visual consistency

### Phase 4: Runtime Enhancement (Code-Side)

Things that don't need new art assets:

| Enhancement | Implementation |
|-------------|---------------|
| **Per-room color themes** | Palette-swap shaders in libGDX; original already varies colors per room |
| **Lighting/atmosphere** | Gradient overlays, vignette, subtle glow around energy sources |
| **Particle effects** | Sparks, dust, energy trails -- libGDX particle system |
| **Background layers** | Parallax starfield or cave texture behind the tile layer |
| **Screen transitions** | Fade/dissolve instead of hard flip-screen cuts |
| **CRT/retro shader** | Optional scanline + slight curvature filter for nostalgia |

## Tool Costs

| Tool | Cost | Purpose |
|------|------|---------|
| Aseprite | $20 (Steam) | Pixel art editor, required for Retro Diffusion |
| Retro Diffusion | $65 one-time | AI upscaling, detail enhancement, palette tools |
| PixelLab | $9/mo | AI tile/sprite generation from reference |
| Fiverr commission | ~$50-100 | BLOB character sprite sheet (optional) |
| **Total minimum** | **$85** | Aseprite + Retro Diffusion |
| **Recommended** | **~$160** | Above + PixelLab for a month |

## Pre-Made Asset Packs (Supplementary)

itch.io has sci-fi/cave/alien pixel art tilesets that could supplement:
- Use as reference for style consistency
- Borrow UI elements (health bars, inventory frames)
- Starquake's unique "molecular" aesthetic means most won't be direct replacements

## Practical Workflow Summary

```
1. Download Starquake .z80 snapshot
2. Extract all tiles + sprites with SkoolKit or custom Python script
3. Catalog: identify each tile's role (ground, wall, hazard, decoration, etc.)
4. Upscale 2x with Retro Diffusion Neural Resize (8x8 -> 16x16)
5. Recolor key tiles with Retro Diffusion RD-Detail / PixelLab
6. Generate BLOB sprite sheet (commission or AI)
7. Generate enemy sprites with PixelLab from descriptions + original refs
8. Build tileset atlas for libGDX
9. Add runtime shaders for lighting, palette variation, particles
```
