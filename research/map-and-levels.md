# Starquake - Map and Level Design

## World Structure

### Grid Layout
- **512 rooms** arranged in a **16-column x 32-row** grid
- Room coordinate formula: `x = room_number MOD 16`, `y = INT(room_number / 16)`
- **Flip-screen** transitions (no scrolling) -- moving off one screen edge instantly shows the adjacent room
- Stephen Crow wanted scrolling but ZX Spectrum hardware couldn't support it

### Open World Design
- **Single connected open world** -- not separate levels
- **Surface** at the top rows of the grid
- **Underground caverns** comprise the vast majority of the 512 screens
- Difficulty **increases toward the planet's center** (deeper = harder)
- **Fixed map layout** every playthrough
- Only **object placement and core piece selection** are randomized

## Room Types

### Surface Areas
- Top rows of the map grid
- BLOB's starting area
- Relatively open and less dangerous
- Entry point to the underground

### Underground Caverns
- Bulk of the 512 screens
- Varied cave systems with **different color schemes per room**
- Multiple visual themes creating variety despite the Spectrum's limitations

### Teleporter Rooms
- 15 rooms containing teleporter booths
- Each booth has a fixed 5-character code
- Distributed across the map for fast travel coverage

### Core Room
- Where core pieces must be delivered
- Accessible via the "QUAKE" teleporter (Spectrum) or "Chasm" (C64)
- Route: teleport to QUAKE, then navigate one screen up and two right
- Visit early to learn which 9 pieces are needed

### Hover Pad Docking Areas
- Stations where BLOB can mount/dismount hover pads
- Hover pad provides full flight capability within connected areas
- Must find another docking station to dismount

### Cheops Pyramid Rooms
- Contain the pyramid trading structures
- BLOB can exchange items here (requires Access Card)
- Strategically placed to create trade routes

### Security Door Corridors
- Passages gated by locked doors
- Require Keys, Numbered Chips, Access Card, or Flexible Whatsit
- Guard access to important areas

## Room Internal Structure

### Hierarchical Tile System (from ZX Spectrum data format)

```
Room Level:     4 wide x 3 tall grid of "big platforms" (12 bytes per room)
                     |
Big Platform:   2x2 arrangement of "small platforms" (4-byte records)
                     |
Small Platform: Drawn from 8x8 pixel character cell blocks
                Max dimensions: 64x48 pixels (8x6 character cells)
                     |
Base Tile:      8x8 pixels (1 ZX Spectrum character cell)
```

- Each room is composed of a **4x3 grid** of big platforms
- Each big platform references a **2x2 grid** of small platforms
- Small platforms are built from **8x8 pixel tiles**
- This hierarchical system allows 512 rooms in only ~6KB of room data

### Collision System
- Collision buffer uses **4x2 pixel cells**
- The **brightness bit** in tile color attributes doubles as the collision/solidity flag
- Bright tiles = solid; dark tiles = passthrough (clever memory optimization)

### Platform Behavior Types
Encoded as hex nibbles in the tile data:

| Code | Behavior |
|------|----------|
| 0x5n | Transport tubes |
| 0x6n | Lethal zones |
| 0x7n | Ray effects |
| 0xCn | Pickups |
| 0xDn | Teleporters |

## Navigation Systems

### Teleporters
- 15 booths with 5-character codes
- Instant travel between fixed locations
- Essential for efficient exploration of the 512-room world
- Codes must be memorized or noted down (no in-game map)

### Hover Pads
- Mounted at docking stations
- Free flight within accessible areas
- Must dock to dismount
- Cannot interact with items/teleporters while flying

### Vacuum Tube Lifts
- One-way upward transport
- Vertical tubes BLOB enters at the bottom and exits at the top
- Connect different vertical layers

### Secret Passages
- Rectangular gaps in walls
- Shortcut to adjacent screens
- Not always obvious -- reward exploration

## Visual Design

### Color and Art Style
- **Black backgrounds** with colorful platforms and structures
- Each room uses a **different color scheme** creating visual variety
- Tile textures described as having a **"molecular model kit" aesthetic** -- tessellating circular/spherical patterns
- "Colorful," "slightly wacky," and "cute" visual style
- **Zero attribute clash** -- a major technical achievement on ZX Spectrum

### Title Screen
- Features border art with blinking corner eyes (random blink timing)

## Map Resources

### Fan-Made Complete Map
- **Speccy.cz**: https://maps.speccy.cz/map.php?id=Starquake
  - Direct image: https://maps.speccy.cz/maps/Starquake.png
  - Dimensions: 4096 x 4656 pixels, PNG, 819 KB
  - Created by Arttu Ilmari Ylarakkola (2002), rated 8.7/10

### Interactive Map Viewer
- **Simon Owen's TileMap**: https://github.com/simonowen/tilemap
  - Windows application that renders all 512 rooms
  - Seamless transitions between rooms
  - Technical article: https://simonowen.com/articles/tilemap/

### MSX Version Map
- Available at MSX Resource Center: https://www.msx.org/forum/msx-talk/software-and-gaming/msx-solutions-starquake-map
