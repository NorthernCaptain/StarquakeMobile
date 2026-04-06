# Starquake - Enemies and Hazards

## Enemy Overview

- Up to **4 enemies per room**
- Enemy **intelligence increases** the deeper BLOB goes (closer to planet center)
- Enemies respawn when re-entering a room

## Enemy Types

### Standard Aliens
- **Behavior**: Move around the room in patterns; some track BLOB's position
- **Damage**: Drain energy on contact (not instant kill)
- **Points**: 80 - 320 points when killed (varies by type/location)
- **Killable**: Yes, with laser shots
- **Intelligence**: Varies by depth -- surface aliens move simply, deep aliens track aggressively
- **Visual**: Various alien sprite designs per area

### Gyroscopes (Spinning Tops) -- INSTANT KILL
- **Behavior**: Rotate in place or move unpredictably
- **Damage**: **Instant death** on contact (lose a life regardless of energy)
- **Key danger**: Can **materialize unpredictably** among normal aliens -- you may not notice them until it's too late
- **Killable**: No (or extremely difficult)
- **Visual**: Spinning top / gyroscope shape
- **Strategy**: Learn to visually identify them instantly and avoid at all costs

### Chain-Driven Tanks -- INSTANT KILL
- **Behavior**: Move along set paths with electrical discharge
- **Damage**: **Instant death** on contact
- **Visual**: Tank-like enemies with visible energy discharge/electricity
- **Killable**: No
- **Strategy**: Time movement to pass when they're at the far end of their patrol

### Hedgehog Plants -- INSTANT KILL
- **Behavior**: Static or semi-static hazard
- **Damage**: **Instant death** on contact
- **Visual**: Spiky plant-like objects
- **Killable**: No
- **Strategy**: Navigate around them carefully

## Static Hazards

### Spikes
- Fixed position floor/wall hazards
- **Instant death** on contact
- Always visible -- memorize their locations

### Energy Fields
- Barrier-like hazards blocking passages
- **Instant death** on contact
- Some may be deactivatable; others are permanent obstacles

### Broken-Bottle Objects
- Static hazards resembling broken glass/bottles
- **Instant death** on contact
- Found on floors and ledges

### Electrodes (Rhythmic)
- **Behavior**: Discharge electricity in a **rhythmic pattern** -- on/off cycles
- **Damage**: Lethal during discharge phase
- **Safe windows**: Can be passed during the off-phase of their cycle
- **Strategy**: Watch the timing pattern, then move through during the safe window
- One of the more skill-based hazards -- requires patience and timing

### Mobile Mines
- **Behavior**: Move along set paths
- **Damage**: Lethal on contact
- **Pattern**: Predictable movement -- learn the path and time your crossing
- Somewhere between static hazards and enemies

## Breakable Floors
- Horizontal barriers/platforms that **break when BLOB lands on them** from sufficient height
- Can be used strategically to access areas below
- Also a hazard -- you might fall through unexpectedly into a dangerous area
- Distinct visual appearance from solid platforms

## Hazard Difficulty Scaling

The game increases difficulty as BLOB descends deeper into the planet:

| Depth | Enemy Behavior |
|-------|---------------|
| Surface (top rows) | Simple movement patterns, fewer hazards |
| Mid-depth | More enemies, some tracking behavior |
| Deep (near center) | Aggressive tracking AI, more instant-kill enemies, denser hazard placement |

## Combat Tips for Clone Design

Key design considerations:
1. **Laser is limited** -- ammo management matters
2. **Not all enemies can be killed** -- avoidance is often the only option
3. **Instant-kill enemies create tension** -- the player must always be alert
4. **Rhythmic hazards add skill-based gameplay** -- timing challenges break up the exploration
5. **Enemy respawn on room re-entry** means no room is ever permanently "cleared"
6. **Gyroscope unpredictability** is a standout mechanic -- they keep the player on edge
