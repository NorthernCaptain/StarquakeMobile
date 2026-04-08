# Starquake

libGDX clone of Starquake (1985) for mobile (Android/iOS) and desktop.

Package: `northern.captain.starquake`

## Build

```bash
./gradlew :desktop:run          # Desktop
./gradlew :android:installDebug  # Android
./gradlew :core:compileJava      # Quick compile check
```

Assets in `android/assets/` (desktop symlinks to it). Source assets in `game_assets/`, packed via `python tools/pack_atlases.py`.

## Development Principles

### OOP Game Objects
Every special tile is a `GameObject` subclass registered in `GameObjectRegistry.createDefault()`. The tile object owns its collision, rendering, and interaction logic. GameScreen knows nothing about specific tile types — it just iterates objects and calls generic methods.

### Event-Driven Communication
Use `EventBus.get()` for decoupled communication between systems. Game objects post events, controllers/managers subscribe and react. No direct references between unrelated systems.

`GameEvent` is a class (not just enum) — subclass to carry data. Use static singletons for simple events to avoid allocation.

### Controllers Own Their Process
Complex multi-step behaviors (lift, tunnel teleport) are owned by dedicated controller classes. GameScreen creates the controller and provides a room-transition callback. The controller manages the full sequence: animation, room switch, blob state.

### Collidable Interface
All entities that interact with game objects implement `Collidable` (BLOB, future enemies). Game objects receive `Collidable` in callbacks and check `getType()` to decide behavior. Cast to concrete type only when needed.

### Transitions for Visual Effects
`BlobTransition` interface for any visual effect that takes control of BLOB. Composable via `BlobTransitionManager` sequences with pauses. Use `endTransition(false)` to skip immunity when not needed (e.g. tunnel teleport).

### CPU Particles over Shaders for Pixel Movement
When pixels need to MOVE (suck-in, blow-out), use CPU particle systems with TextureRegion sub-regions of the source sprite. No Pixmap readback — just subdivide the atlas region into 1px sub-regions. Shaders can only discard/color pixels, not move them.

### Collision Design
- Tile-based, not pixel-perfect. Room stores `int[6][8]` tile grid.
- Game objects override `isSolidAt()` for custom shapes.
- Kill zones should extend 2px beyond solid edges for side-approach detection.
- Use `blobBottom <= killTop` (inclusive) for top-approach kills.
- `Blob.wouldCollide()` checks before placing temp platforms.

### Foreground Layer
Game objects can draw in front of BLOB via `renderForeground()`. Used for passages (70% opacity), electric shockers (tile drawn over BLOB), lift tubes. Uses palette shader to render indexed tile sub-regions.

### Shader Patterns
All shaders in `android/assets/shaders/`, reuse `palette.vert`. Shared shaders loaded in `Assets` for Android lifecycle safety. Mid-batch switching: `flush() → setShader → draw → flush → setShader(null)`.

### Asset Lookup
`Assets.getTileIdAt(roomIndex, col, row)` — lightweight tile ID lookup from JSON metadata without building a Room. Used by controllers to find destination tiles in adjacent rooms.

## Key Constants
- Room: 256×144 pixels, 8×6 tiles (32×24 each)
- BLOB: 16×16 pixels, walk 48px/s, fly 64px/s, gravity 180px/s²
- Platform height: 8px (hover platform below BLOB)
- Room grid: 16 columns × 32 rows = 512 rooms
