# Starquake — Items, Trading, and Doors

## Item Catalog (Atari ST Sprite Indices)

Our item sprites are extracted from the Atari ST version (35 items, 0-34). Numbering differs from BBC Micro (30 items, 0-29).

### Core Display Pieces (items 0-8) — CORE_DISPLAY

The 3×3 visual grid shown in the core room. Not collectible — these are the "assembled" appearance of each core slot.

| Index | Grid Position |
|-------|---------------|
| 0 | Top-left |
| 1 | Top-center |
| 2 | Top-right |
| 3 | Middle-left |
| 4 | Middle-center |
| 5 | Middle-right |
| 6 | Bottom-left |
| 7 | Bottom-center |
| 8 | Bottom-right |

Palette: indices [10,11,12,13,14,15] — green/cyan mechanical parts.

### Collectible Core Parts — Set A (items 9-14) — CORE_PART

Findable parts for the core assembly. Same visual style as Set B. Together they form a pool of 15 possible parts; 5 are randomly chosen as "missing" each game.

Palette: [1,2,3,13,14,15] — red/gray tones. BBC equivalents: items 20-25.

### Access Card (item 15) — ACCESS_CARD

- Opens **space locks** (tiles 37-38) — within-room teleport
- Enables **Cheops Pyramid** trading (item 25)
- Does **NOT** open doors (tile 45) — that's the Key's job
- **Reusable** — NOT consumed on use
- When pushed out of FIFO, placed back in current room like any other item (no self-destruct)
- Only 1 placed at game start — protect from FIFO pushout
- BBC equivalent: item 9 (ITEM_PASS)

### Key (item 16) — KEY

- Opens **doors** (tile 45)
- Does **NOT** open space locks — that's the Access Card's job
- **Reusable** — NOT consumed (BBC disassembly confirms at &1D72)
- When pushed out of FIFO, placed back in current room like any other item
- Only **1 key** placed in the world (enough for all 5 doors)
- BBC equivalent: item 10 (ITEM_KEY)

### Resource Pickups (items 17-24) — PICKUP

Consumed **instantly on contact** — never enter inventory.

| Index | Name | Effect |
|-------|------|--------|
| 17 | Health boost (small) | Restores partial health |
| 18 | Health boost (full) | Restores health to 100% |
| 19 | Universal boost | Fills lowest vital to max; if all full → +1 life |
| 20 | Platform recharge (small) | Restores partial platforms |
| 21 | Laser recharge (small) | Restores partial laser energy |
| 22 | Laser recharge (full) | Restores laser to 100% |
| 23 | Platform recharge (full) | Restores platforms to 100% |
| 24 | Extra life | +1 life |

### Cheops Pyramid (item 25) — PYRAMID

- **Placed randomly in rooms** (32 instances at game start)
- NOT a tile — it's an item object sitting on the ground
- Interaction: walk up + press UP while carrying the Access Card
- After 1 trade, the pyramid is **consumed** (removed from world)
- 32 pyramids = 32 max trades per game
- BBC equivalent: item 7 (PYRAMID), also an item not a tile

### Collectible Core Parts — Set B (items 26-34) — CORE_PART

Same category as Set A. Items 30-34 are Atari ST additions (BBC only had 30 items).

Palette: mixed [1,2,3,12,13,14,15] — red/gray/white tones. BBC equivalents: items 26-29 + AT-only 30-34.

---

## Tile Mechanics

### Tiles 37-38: Space Locks (within-room teleport, requires Access Card)

**BBC**: TILE_SPACE_LOCK_LEFT (0x23) / TILE_SPACE_LOCK_RIGHT (0x24)

Space locks are paired archway tiles within a single room. When BLOB enters one, he's teleported to the other side of the room.

- **Requires Access Card** (item 15) — without it, BLOB is blocked
- Always come in left/right pairs (tile 37 = left entrance, tile 38 = right entrance)
- Flash effect on teleport (background cycles black/white)
- NOT consumed — both space locks remain after use
- The Access Card is NOT consumed

BBC code at &1E5D-&1ED1:
1. Check if BLOB overlaps a space lock tile
2. Check ITEM_PASS in pockets (`CMP #$09`)
3. Teleport to the paired space lock position in same room
4. Flip BLOB facing direction

Room &16A (362 decimal) has space locks disabled — it uses passages instead.

**Current implementation**: `TradeEntrance.java` — needs refactoring to `SpaceLock.java`.

### Tiles 43-44: Tunnel Teleporters (cross-room, NO card required)

BBC equivalent: passage system (data-driven, not tile-based). Cross-room horizontal teleport between adjacent rooms. No item required.

Our implementation is correct as-is.

### Tile 45: Door/Gate (requires Key only)

- Solid vertical barrier blocking a passage
- Opened by **KEY (item 16)** only — Access Card does NOT work on doors
- Key is NOT consumed — reusable for all 5 doors
- Once opened, stays open for rest of session (persistent flag)
- BBC: TILE_HORIZONTAL_GATE (0x0A), `gate_table` at &61C3, checks `CMP #&0A` (ITEM_KEY only)

**5 doors in the entire game:**

| Room | Grid position |
|------|--------------|
| 190 | row 11, col 14 |
| 252 | row 15, col 12 |
| 452 | row 28, col 4 |
| 482 | row 30, col 2 |
| 486 | row 30, col 6 |

---

## Cheops Pyramid Trading

### How It Works

1. BLOB walks over a Cheops Pyramid item (item 25) and presses UP
2. Game checks for Access Card (item 15) in inventory — if absent, nothing happens
3. Trading screen appears (uses `circuitScreen` background)
4. Game picks one random non-card item from BLOB's 4-slot inventory
5. Game offers **5 exchange options**:
   - 4 drawn randomly from unrestored core elements
   - Option 5 = keep what you already have (no swap)
6. Player selects 1-5
7. Selected option replaces the inventory item; old item is destroyed
8. The pyramid item is **consumed** (removed from world)

### Trading Screen Layout
```
┌─────────────────────────────┐
│     CHEOPS PYRAMID          │
│                             │
│  EXCHANGE [item] FOR        │
│                             │
│  1. [part]  2. [part]       │
│  3. [part]  4. [part]       │
│  5. [keep current]          │
│                             │
│  ACME CHEAP BOARD V.7       │
└─────────────────────────────┘
```

### Why Trading Matters
- All core parts ARE findable as room pickups (none are trade-only)
- But trading is a shortcut when parts are in hard-to-reach rooms
- Also useful to swap unwanted parts for needed ones
- With only 4 inventory slots and 32 pyramids, planning trade routes matters

---

## Inventory System

### FIFO (First-In, First-Out)
- **4 slots** maximum
- When picking up a 5th item, the **oldest** item is pushed out (dropped in current room)
- Items that enter inventory: CORE_PART (9-14, 26-34), ACCESS_CARD (15), KEY (16)
- PICKUP items (17-24) and PYRAMID items (25) are consumed on contact, never enter inventory

### Protecting the Access Card
The Access Card is the most important item — without it, no trading and no space locks. If pushed out of the FIFO, it's placed back in the current room (no self-destruct — BBC code treats all dropped items identically). But leaving it behind means backtracking. Strategy: deliver core parts to core ASAP to free inventory space.

---

## Core Assembly

### Initialization (BBC &11EB-&1221)
1. 9 core slots initialized with structural display pieces (items 0-8)
2. **5 of the 9** are randomly replaced with CORE_PART IDs from the pool of 15
3. Remaining **4 slots** keep their structural pieces (already "correct")
4. The 5 replaced slots define what BLOB must find and deliver

### Delivery
- BLOB enters core room with a matching CORE_PART in inventory
- Part consumed from inventory → slot restored to CORE_DISPLAY piece
- +10,000 points per delivery
- All 9 restored = **game won**

---

## Item Placement at Game Start

### Core Elements & Special Items (20 entries from `possible_core_element_locations`)

**Access Card (1 placed, from 4 candidate rooms):**
Rooms: 8, 40, 168, 182

**Key (1 placed, from 4 candidate rooms):**
Rooms: 150, 198, 200, 246

**Required core elements (7 entries, specific sprites from core_elements_required[2..8]):**
Each entry has 2 candidate rooms (1 chosen randomly):

| Entry | Room A | Room B |
|-------|--------|--------|
| 4 | 436 | 422 |
| 5 | 236 | 222 |
| 6 | 52 | 16 |
| 7 | 502 | 504 |
| 8 | 296 | 314 |
| 9 | 72 | 106 |
| 10 | 310 | 278 |

**Extra core elements (11 entries, random sprites — decoys/trade material):**

| Entry | Room A | Room B |
|-------|--------|--------|
| 11 | 56 | 42 |
| 12 | 416 | 352 |
| 13 | 140 | 14 |
| 14 | 266 | 316 |
| 15 | 476 | 482 |
| 16 | 84 | 86 |
| 17 | 478 | 62 |
| 18 | 80 | 82 |
| 19 | 226 | 194 |
| 20 | 114 | 116 |
| 21 | 466 | 372 |

### Boost Items (256 total, randomly distributed 1-per-room)

| Count | AT Items | Type |
|-------|----------|------|
| 96 | 17-18 variants | Health boost (3 visual variants, 32 each) |
| 32 | 21-22 | Laser recharge |
| 32 | 20, 23 | Platform recharge |
| 32 | 24 | Extra life |
| 32 | 19 | Universal boost |
| 32 | 25 | Cheops pyramid (consumable trade point) |

---

## Cross-Reference: BBC Micro → Atari ST

| BBC Index | BBC Name | AT Index | AT Name |
|-----------|----------|----------|---------|
| 0-2 | ENERGY (×3 variants) | 17-18 | Health boost (small/full) |
| 3 | AMMO | 21-22 | Laser recharge (small/full) |
| 4 | FUEL | 20, 23 | Platform recharge (small/full) |
| 5 | LIFE | 24 | Extra life |
| 6 | RANDOM | 19 | Universal boost |
| 7 | PYRAMID | 25 | Cheops pyramid |
| 8 | EMPTY | — | (no equivalent, use blank) |
| 9 | PASS | 15 | Access card |
| 10 | KEY | 16 | Key |
| 11-19 | CORE structural | 0-8 | Core display pieces |
| 20-29 | CORE non-structural | 9-14, 26-29 | Collectible core parts |
| — | (BBC: 30 items) | 30-34 | AT-only additional core parts |

## Sources

- BBC Micro source: github.com/reubenscratton/starquake (main.asm)
- BBC Micro disassembly: level7.org.uk/miscellany/starquake-disassembly.txt
- C64 Wiki: c64-wiki.com/wiki/Starquake
- CRASH Magazine Issue 22 review
