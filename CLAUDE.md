# Starquake

libGDX clone of Starquake (1985) for mobile (Android/iOS) and desktop.

Package: `northern.captain.starquake`

## Build

```bash
./gradlew :desktop:run          # Desktop
./gradlew :android:installDebug  # Android
./gradlew :core:compileJava      # Quick compile check
```

Assets live in `android/assets/` (desktop symlinks to it). Game assets source in `game_assets/`, packed via `python tools/pack_atlases.py`.

## Architecture

### Rendering Pipeline

- **Viewport**: `FitViewport(256, 144)` — native Atari ST game resolution
- **Terrain**: Each Room owns a `FrameBuffer`. `RoomRenderer` draws indexed tiles through a palette shader into the FBO once, cached until room disposal.
- **Palette shader**: Tiles are grayscale index maps (pixel = palette index * 17). Fragment shader looks up color from a 16x26 palette texture at runtime.
- **Render order**: Terrain FBO → Game objects → Temp platforms → BLOB attachment → Transition effects → BLOB sprite → Debug text → Touch overlay

### Collision System

**Tile-based**, not pixel-perfect. Room stores an `int[6][8]` tile grid.

`Room.isSolidAt(worldX, worldY)` resolves collision in priority order:
1. **Game objects** — if the tile has a registered `GameObject`, it delegates entirely to `obj.isSolidAt()`. The object controls all collision for its tile.
2. **Non-solid tiles** — tiles in the `non_solid_tiles` metadata list pass through (empty, decorative, pickups).
3. **Solid terrain** — everything else is a full 32x24 collision box.
4. **Temp platforms** — checked as an overlay on top of the above.

`Blob.isSolidColumn()` probes 3 points (bottom, middle, top) along a vertical edge to avoid missing thin walls with the 24px flying hitbox.

### Game Object System

**`GameObject`** — abstract base class for interactive tiles. Placed in rooms by the registry during `Room.build()`.

Key methods:
- `isSolidAt(worldX, worldY)` — custom collision shape for this tile
- `onEnter(Collidable entity)` — called when an entity overlaps this tile
- `onAction(Collidable entity, Action action)` — called when entity presses a button while on this tile
- `render(batch, delta)` — draw on top of terrain

**`GameObjectRegistry`** — maps tile IDs to factory lambdas. `createDefault()` registers all known types. Adding a new tile type is one line.

**`Collidable`** — interface for entities that interact with game objects (BLOB, future enemies). Has `getType()` so objects can distinguish BLOB from enemies.

Concrete object types:
- `CollisionTile` — single-rect collision with insets from edges
- `MultiCollisionTile` — multiple solid rects within one tile
- `HoverStand` — hover platform stand with pillar/cradle collision, mount/dismount mechanics
- `ElectricShocker` — periodic lightning arc, kills BLOB on contact

### Collision Tile Registration

In `GameObjectRegistry.createDefault()`:
```java
register(25, (a, col, row) -> new CollisionTile(a, col, row, 0, 16, 0, 0));  // right 16px empty
register(32, HoverStand::new);   // hover platform stand
register(7, ElectricShocker::new); // electric shocker
```
Inset order: `(insetLeft, insetRight, insetTop, insetBottom)`.

### Event Bus

Synchronous event bus at `EventBus.get()` for decoupled communication.

```java
// Register
EventBus.get().register(GameEvent.Type.BLOB_DIED, e -> handleDeath(e));

// Post (use singletons for simple events)
EventBus.get().post(GameEvent.BLOB_DIED);

// Post with data (subclass GameEvent)
EventBus.get().post(new RoomChangedEvent(oldRoom, newRoom));
```

`GameEvent` is a class with a `Type` enum. Subclass to carry additional data. Static singletons (`GameEvent.BLOB_DIED`, etc.) avoid allocation for simple events.

Events: `BLOB_DIED`, `BLOB_SPAWNED`, `BLOB_MOUNTED_PLATFORM`, `BLOB_DISMOUNTED_PLATFORM`, `ROOM_CHANGED`.

`EventBus.clear()` is called on `GameScreen.dispose()`. Any class-level listeners (like `HoverStand.registerEvents()`) must be re-registered on new game.

### BLOB States

```
IDLE → WALK → TURNING → (back to IDLE/WALK)
     → FLYING (hover platform, 4-directional, taller hitbox)
     → TRANSITION (invisible, no physics — effects playing)
```

`TRANSITION` covers all visual effect sequences: death, birth, future teleport.

### Transition System

**`BlobTransition`** — interface for visual effects (explosion, assembly, suck-in, etc.).

**`BlobTransitionManager`** — runs a sequence of transitions:
```java
manager.start(blob, new BlobTransition[]{
    new ExplosionTransition(),
    new PauseTransition(2.0f),
    new AssemblyTransition()
}, onComplete);
```

BLOB enters `TRANSITION` state for the duration. Each step starts at BLOB's current center position. `repositionBlob()` allows mid-sequence position changes (for teleport).

Concrete transitions use libGDX `ParticleEffect` (`.p` files in `android/assets/effects/`).

### Temp Platforms

BLOB places 16x8 dissolving platforms on DOWN press. They last 2 seconds (1s solid + 1s dissolve). Dissolve uses a noise-based fragment shader (`dissolve.frag`) that snaps to game-pixel grid.

Room holds an `ArrayList<TempPlatform>` checked in `isSolidAt()` as an overlay.

### Shaders

All in `android/assets/shaders/`. Reuse `palette.vert` as vertex shader.

- `palette.frag` — indexed color lookup from palette texture
- `dissolve.frag` — noise-based pixel dissolve, `u_dissolve` (0-1), `u_pixelSize` for game-pixel snapping
- `lightning.frag` — animated electric arc between two points, layered sine waves, white→cyan→blue

Pattern for mid-batch shader switching:
```java
batch.flush();
batch.setShader(myShader);
myShader.setUniformf("u_foo", value);
batch.draw(...);
batch.flush();
batch.setShader(null);
```

### Asset Extraction

Original graphics extracted from Atari ST RAM dump and ZX Spectrum snapshot via Python scripts in `tools/`. Tiles are grayscale index maps; sprites are RGBA. Font from ZX Spectrum. Packed into libGDX texture atlases via `tools/pack_atlases.py`.

Key metadata in `game_assets/metadata.json`: rooms (512), big platforms (355), tile properties, non-solid tile list, palettes (26).
