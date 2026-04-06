# Starquake - Gameplay Mechanics

## Core Loop

1. Visit the **core room** to learn which 9 objects are needed
2. **Explore** the 512-room underground world
3. **Collect** items (max 4 in inventory at a time)
4. **Trade** items at Cheops Pyramids to obtain core pieces
5. **Deliver** core pieces to the core room (10,000 pts each)
6. Repeat until all 9 pieces are delivered

## BLOB - Player Character

BLOB (Bio-Logically Operated Being) is the player character. Key characteristic: **BLOB cannot jump**. This is the game's defining design decision.

### Movement

| Action | Description |
|--------|-------------|
| **Walk left/right** | Standard horizontal movement |
| **Fall** | BLOB falls under gravity; no fall damage |
| **Build platform** | Creates a temporary platform at BLOB's feet; it crumbles after a few seconds |
| **Shoot laser** | Fires horizontally; consumes ammunition |
| **Enter secret passage** | Rectangular gaps in walls that shortcut to adjacent screens |

### Platform-Laying (Signature Mechanic)

This is what makes Starquake unique:

- BLOB carries a **finite supply of platforms** (displayed on HUD)
- Pressing the platform button creates a small platform at BLOB's current position
- Platforms **crumble and disappear** after a few seconds
- Used to cross gaps, reach higher areas, and navigate the world
- Platform supply can be replenished by collecting platform pickups
- Strategic platform placement is the core skill of the game

### Hover Pad

- BLOB can mount **hover pads** found at docking stations throughout the world
- While on a hover pad, BLOB has **full directional flight**
- **Restrictions while hovering**:
  - Cannot collect items
  - Cannot use teleporters
  - Cannot trade at Cheops Pyramids
  - Cannot interact with most objects
- Must **dock** at a hover pad station to dismount
- Hover pads are essential for reaching certain areas

### Vacuum Tube Lifts

- Vertical tubes that transport BLOB upward
- **One-way only** (upward)
- Used to move between vertical sections of the map

### Teleporters

- **15 teleporter booths** distributed across the map
- Each has a **5-character code** (varies by platform version)
- Enter a code to instantly teleport to that booth's location
- Critical for efficient navigation across the 512-room world
- The core room is accessible via specific teleporter (e.g., "QUAKE" on Spectrum, "Chasm" on C64)

## Resource System

BLOB manages three separate resource bars displayed on the HUD:

| Resource | Description | Depleted By | Refilled By |
|----------|-------------|-------------|-------------|
| **Energy** | Health bar | Alien contact | Energy pickups |
| **Ammunition** | Laser shots | Shooting | Ammo pickups |
| **Platforms** | Building material | Laying platforms | Platform pickups |

- When **energy reaches zero**, BLOB loses a life
- Various pickup sizes exist for each resource (small/medium/large)
- **Multi-purpose Cylinder**: Special item that tops up whichever resource is lowest, or grants an extra life if all are full

## Lives System

- BLOB starts with **4 lives**
- Extra lives obtained from:
  - **Joystick icons** (collectible extra life pickups)
  - **Multi-purpose Cylinder** when all resources are full
- Game over when all lives are lost

## Inventory System

- BLOB can carry **maximum 4 items** at a time
- Inventory operates as **FIFO** (First In, First Out)
  - Picking up a 5th item drops the oldest item
- Items are either carried for delivery (core pieces) or used/traded
- Must manage inventory carefully -- dropping a core piece by accident means backtracking

## Scoring

| Action | Points |
|--------|--------|
| Entering a new screen | 250 |
| Killing a standard alien | 80 - 320 (varies by type) |
| Delivering a core piece | 10,000 |

## Security System

### Security Doors
- Locked doors blocking certain passages
- Opened by:
  - **Numbered Chips** (specific chip for specific door)
  - **Access Card** (opens any door)
  - **Flexible Whatsit** (one-time use, opens any door)
  - **Keys** (basic door opener)

### Access Floors
- Certain floor sections require the Access Card to traverse

## Game Completion

- Deliver all **9 core pieces** to the core room
- Core pieces are **randomized each playthrough** from the pool of collectible objects
- Some core pieces can only be obtained through **trading** at Cheops Pyramids
- The ending famously mocks the player for completing such a difficult game
