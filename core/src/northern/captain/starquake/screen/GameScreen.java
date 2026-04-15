package northern.captain.starquake.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectSet;
import com.badlogic.gdx.utils.viewport.FitViewport;

import northern.captain.starquake.StarquakeGame;
import northern.captain.starquake.input.InputManager;
import northern.captain.starquake.input.InputManager.Action;
import northern.captain.starquake.input.TouchControls;
import northern.captain.starquake.hud.Hud;
import northern.captain.starquake.world.Blob;
import northern.captain.starquake.world.Inventory;
import northern.captain.starquake.world.items.ItemManager;
import northern.captain.starquake.world.items.ItemPickup;
import northern.captain.starquake.world.BlobRenderer;
import northern.captain.starquake.world.GameState;
import northern.captain.starquake.world.LiftController;
import northern.captain.starquake.world.TunnelController;
import northern.captain.starquake.world.PlatformRenderer;
import northern.captain.starquake.world.Room;
import northern.captain.starquake.world.RoomRenderer;
import northern.captain.starquake.world.TempPlatform;
import northern.captain.starquake.event.EventBus;
import northern.captain.starquake.event.GameEvent;
import northern.captain.starquake.world.objects.GameObject;
import northern.captain.starquake.world.objects.BreakableFloor;
import northern.captain.starquake.world.objects.CoreTrigger;
import northern.captain.starquake.world.objects.GameObjectRegistry;
import northern.captain.starquake.world.objects.HoverStand;
import northern.captain.starquake.event.EnterTeleportEvent;
import northern.captain.starquake.event.GameOverEvent;
import northern.captain.starquake.event.EnterTradeEvent;
import northern.captain.starquake.event.RoomChangedEvent;
import northern.captain.starquake.hud.Overlay;
import northern.captain.starquake.hud.TeleportOverlay;
import northern.captain.starquake.hud.TradingOverlay;
import northern.captain.starquake.audio.MusicManager;
import northern.captain.starquake.audio.SoundManager;
import static northern.captain.starquake.audio.SoundManager.SoundType;
import northern.captain.starquake.world.CoreAssembly;
import northern.captain.starquake.world.ProjectileManager;
import northern.captain.starquake.world.ScoreManager;
import northern.captain.starquake.services.AchievementManager;
import northern.captain.starquake.world.TeleportRegistry;
import northern.captain.starquake.world.items.ItemType;
import northern.captain.starquake.world.objects.Teleporter;
import northern.captain.starquake.world.transitions.TeleportTransition;
import northern.captain.starquake.world.transitions.AssemblyTransition;
import northern.captain.starquake.world.transitions.BlobTransition;
import northern.captain.starquake.world.transitions.BlobTransitionManager;
import northern.captain.starquake.world.transitions.ExplosionTransition;
import northern.captain.starquake.world.transitions.PauseTransition;

import java.util.ArrayList;
import java.util.Iterator;

public class GameScreen implements Screen {
    public static final int VIEWPORT_W = Room.WIDTH;       // 256
    public static final int VIEWPORT_H = Room.HEIGHT + 24; // 168 (144 room + 24 HUD)
    private static final float TRANSITION_DURATION = 0.4f;

    private final StarquakeGame game;
    private final SpriteBatch batch = new SpriteBatch();
    private final FitViewport gameViewport = new FitViewport(VIEWPORT_W, VIEWPORT_H);

    private final InputManager inputManager;
    private final TouchControls touchControls;
    private final RoomRenderer roomRenderer;
    private final BlobRenderer blobRenderer;
    private final PlatformRenderer platformRenderer;
    private final GameObjectRegistry objectRegistry;
    private final BlobTransitionManager transitionManager = new BlobTransitionManager();
    private final GameState gameState = new GameState();
    private final Inventory inventory = new Inventory();
    private final ItemManager itemManager;
    private final ProjectileManager projectileManager;
    private final Hud hud;
    private final LiftController liftController = new LiftController();
    private final TunnelController tunnelController;

    private Room room;
    private Blob blob;
    private final ArrayList<TempPlatform> platforms = new ArrayList<>();

    // Room transition state
    private Room prevRoom;
    private float transitionTime;
    private int transitionDx, transitionDy;

    private Overlay activeOverlay;

    // Walk step sound timer
    private float stepTimer;

    // Teleport system
    private final TeleportRegistry teleportRegistry = new TeleportRegistry();
    private TeleportTransition teleportTransition;
    private int teleportTargetRoom = -1;

    public GameScreen(StarquakeGame game, int startRoom) {
        this.game = game;
        roomRenderer = new RoomRenderer(game.assets);
        blobRenderer = new BlobRenderer(game.assets);
        platformRenderer = new PlatformRenderer(game.assets);

        objectRegistry = GameObjectRegistry.createDefault();
        tunnelController = new TunnelController(game.assets);
        itemManager = new ItemManager(game.assets, objectRegistry);
        projectileManager = new ProjectileManager(game.assets);
        ItemPickup.init(gameState, inventory, itemManager);
        hud = new Hud(game.assets, inventory);

        inputManager = new InputManager();
        touchControls = new TouchControls(inputManager, false);
        inputManager.connectControllers();
        Gdx.input.setInputProcessor(new InputMultiplexer(inputManager.getKeyboardListener()));

        room = Room.build(game.assets, startRoom, objectRegistry);
        long seed = System.currentTimeMillis();
        CoreTrigger.initCoreAssembly(game.assets, seed, itemManager.getPartPool());
        teleportRegistry.initialize(seed);
        itemManager.initializeGame(seed);
        itemManager.populateRoom(room);
        blob = new Blob(Room.WIDTH / 2f - Blob.SIZE / 2f, 40);

        game.assets.font.getData().setScale(1f);
        game.assets.font.setUseIntegerPositions(true);

        // Set up tunnel controller
        tunnelController.setBlob(blob);
        tunnelController.setRoom(room);
        tunnelController.setTransitionHandler(dx -> {
            int next = Room.adjacentIndex(room.roomIndex, dx, 0);
            if (next < 0) return null;
            room.clearTempPlatforms();
            platforms.clear();
            projectileManager.clear();
            int oldRoom = room.roomIndex;
            prevRoom = room;
            room = Room.build(game.assets, next, objectRegistry);
            itemManager.populateRoom(room);
            tunnelController.setRoom(room);
            EventBus.get().post(new RoomChangedEvent(oldRoom, next));
            transitionDx = dx;
            transitionDy = 0;
            transitionTime = 0;
            return room;
        });

        // Register event listeners
        gameState.registerEvents();
        EventBus.get().register(GameEvent.Type.BLOB_DIED, e -> {
            SoundManager.play(SoundType.DEATH);
            triggerDeath();
        });
        EventBus.get().register(GameEvent.Type.LIFT_STARTED, e -> startLift());
        EventBus.get().register(GameEvent.Type.ENTER_TRADE, e -> startTrading((EnterTradeEvent) e));
        EventBus.get().register(GameEvent.Type.ENTER_TELEPORT, e -> startTeleport((EnterTeleportEvent) e));
        EventBus.get().register(GameEvent.Type.BLOB_SPAWNED, e -> SoundManager.play(SoundType.SPAWN));
        EventBus.get().register(GameEvent.Type.GAME_OVER, e -> {
            GameOverEvent go = (GameOverEvent) e;
            TextureRegion roomTerrain = roomRenderer.getTerrainTexture(room);
            game.setScreen(new GameOverScreen(game, roomTerrain, go.win, gameState));
        });
        HoverStand.registerEvents();
        ScoreManager.init(gameState);
        AchievementManager.init();
        AchievementManager.get().startNewGame();
        AchievementManager.get().setTeleportRegistry(teleportRegistry);
        AchievementManager.get().registerEvents();
        AchievementManager.get().onResume();
        // Mark starting room as visited
        EventBus.get().post(new RoomChangedEvent(-1, startRoom));

        // Birth effect on initial spawn
        triggerSpawn();
    }

    private boolean isRoomTransitioning() {
        return prevRoom != null;
    }

    private void triggerSpawn() {
        transitionManager.start(blob, new BlobTransition[]{
                new AssemblyTransition()
        }, () -> EventBus.get().post(GameEvent.BLOB_SPAWNED));
    }

    private void startLift() {
        liftController.start(blob, room);
        liftController.setTransitionHandler(() -> {
            int next = Room.adjacentIndex(room.roomIndex, 0, -1); // up in grid
            if (next < 0) return null;
            room.clearTempPlatforms();
            platforms.clear();
            projectileManager.clear();
            int oldRoom = room.roomIndex;
            prevRoom = room;
            room = Room.build(game.assets, next, objectRegistry);
            itemManager.populateRoom(room);
            liftController.setRoom(room);
            EventBus.get().post(new RoomChangedEvent(oldRoom, next));
            // No slide transition for lift — instant room switch
            prevRoom.dispose();
            prevRoom = null;
            return room;
        });
    }

    private void startTrading(EnterTradeEvent e) {
        // Pick 4 random parts from full pool, excluding the offered item
        ItemType[] pool = itemManager.getPartPool();
        ItemType[] shuffled = pool.clone();
        for (int i = shuffled.length - 1; i > 0; i--) {
            int j = (int) (Math.random() * (i + 1));
            ItemType tmp = shuffled[i]; shuffled[i] = shuffled[j]; shuffled[j] = tmp;
        }
        ItemType[] options = new ItemType[4];
        int count = 0;
        for (ItemType part : shuffled) {
            if (count >= 4) break;
            if (part != e.offeredItem) options[count++] = part;
        }
        activeOverlay = new TradingOverlay(game.assets, gameViewport, inventory, e.pyramid,
                e.offeredItem, e.slotIndex, options);
        blob.startLifting();
    }

    private void executeTeleportRoomSwitch() {
        // Build new room
        room.clearTempPlatforms();
        platforms.clear();
        projectileManager.clear();
        int oldRoom = room.roomIndex;
        room.dispose();
        room = Room.build(game.assets, teleportTargetRoom, objectRegistry);
        itemManager.populateRoom(room);
        tunnelController.setRoom(room);
        EventBus.get().post(new RoomChangedEvent(oldRoom, teleportTargetRoom));

        // Position BLOB at teleporter tile in the new room
        findTeleporter:
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 8; col++) {
                int tileId = game.assets.getTileIdAt(teleportTargetRoom, col, row);
                if (tileId == Teleporter.TILE_ID) {
                    blob.x = col * 32 + 8;
                    blob.y = (5 - row) * 24;
                    break findTeleporter;
                }
            }
        }

        Teleporter.suppressUntilExit = true;

        // Provide new room's terrain to the transition
        TextureRegion targetTerrain = roomRenderer.getTerrainTexture(room);
        teleportTransition.setTarget(targetTerrain);
        teleportTargetRoom = -1;
    }

    private void startTeleport(EnterTeleportEvent e) {
        teleportRegistry.markVisited(e.roomIndex);
        activeOverlay = new TeleportOverlay(game.assets, gameViewport, teleportRegistry, e.roomIndex);
        blob.startLifting();
        SoundManager.play(SoundType.TELEPORT_ENTER);
    }

    private void triggerDeath() {
        transitionManager.start(blob, new BlobTransition[]{
                new ExplosionTransition(),
                new PauseTransition(1.0f)
        }, () -> {
            if (gameState.isGameOver()) {
                EventBus.get().post(new GameOverEvent(false));
            } else {
                transitionManager.start(blob, new BlobTransition[]{
                        new AssemblyTransition()
                }, () -> EventBus.get().post(GameEvent.BLOB_SPAWNED));
            }
        });
    }

    @Override
    public void render(float delta) {
        delta = Math.min(delta, 1 / 15f);

        touchControls.setWalkMode(blob.state != Blob.State.FLYING);
        touchControls.poll();
        if (AchievementManager.get() != null) AchievementManager.get().update(delta);
        updateWorld(delta);
        renderWorld(delta);
        inputManager.update();
        if (MusicManager.get() != null) MusicManager.get().update();
    }

    private void updateWorld(float delta) {
        // Teleport room transition (disintegrate/reassemble)
        if (teleportTransition != null) {
            teleportTransition.update(delta);
            if (teleportTransition.needsTarget()) {
                executeTeleportRoomSwitch();
            }
            if (teleportTransition.isDone()) {
                teleportTransition = null;
                blob.facingRight = false;
                blob.x -= 8;
                blob.stopLifting();
            }
            return;
        }

        if (activeOverlay != null) {
            activeOverlay.update(delta, inputManager);
            if (activeOverlay.isDone()) {
                if (activeOverlay instanceof TeleportOverlay) {
                    int target = ((TeleportOverlay) activeOverlay).getTargetRoom();
                    activeOverlay = null;
                    if (target >= 0) {
                        teleportTargetRoom = target;
                        TextureRegion sourceTerrain = roomRenderer.getTerrainTexture(room);
                        teleportTransition = new TeleportTransition();
                        teleportTransition.start(sourceTerrain);
                        SoundManager.play(SoundType.TELEPORT);
                        return;
                    }
                    // Cancelled — suppress re-trigger until BLOB exits the booth
                    Teleporter.suppressUntilExit = true;
                    blob.facingRight = false;
                    blob.x -= 8;
                }
                blob.stopLifting();
                activeOverlay = null;
            }
            return;
        }

        transitionManager.update(delta);
        tunnelController.update(delta);
        itemManager.update(delta);

        if (isRoomTransitioning()) {
            updateRoomTransition(delta);
        } else if (blob.state == Blob.State.LIFTING) {
            updateLifting(delta);
        } else if (!blob.isInTransition()) {
            updateGameplay(delta);
        } else {
            updateObjectsOnly(delta);
        }
    }

    private void updateRoomTransition(float delta) {
        transitionTime += delta;
        if (transitionTime >= TRANSITION_DURATION) {
            prevRoom.dispose();
            prevRoom = null;
        }
    }

    private void updateLifting(float delta) {
        liftController.update(delta);
        for (GameObject obj : room.getObjects()) {
            obj.update(delta);
        }
    }

    private void updateGameplay(float delta) {
        blob.applyInput(
                inputManager.isPressed(Action.LEFT),
                inputManager.isPressed(Action.RIGHT),
                inputManager.isPressed(Action.UP),
                inputManager.isPressed(Action.DOWN),
                inputManager.getAnalogX(),
                inputManager.getAnalogY(),
                inputManager.isAnalogActive());

        // Walk step sound — play immediately on first step, then every 0.42s
        if (blob.state == Blob.State.WALK) {
            if (stepTimer <= 0) {
                SoundManager.play(SoundType.STEP);
                stepTimer = 0.48f;
            }
            stepTimer -= delta;
        } else {
            stepTimer = 0;
        }

        updateTempPlatforms(delta);

        if (blob.attachment != null) {
            blob.attachment.update(delta);
        }

        // Save prev ground state BEFORE blob.update changes onGround,
        // then objects can detect landing (onGround && !prevOnGround)
        BreakableFloor.postUpdateBlobTracking();
        BreakableFloor.updateBlobTracking(blob);
        blob.update(delta, room);

        for (GameObject obj : room.getObjects()) {
            obj.update(delta);
        }

        gameState.update(delta);

        // Shooting
        if (inputManager.isJustPressed(Action.ACTION_A)) {
            if (blob.state == Blob.State.WALK || blob.state == Blob.State.IDLE) {
                if (gameState.getLaserEnergy() >= ProjectileManager.WALK_COST) {
                    gameState.useLaser(ProjectileManager.WALK_COST);
                    projectileManager.fireWalk(blob.x, blob.y, blob.facingRight);
                    SoundManager.play(SoundType.FIRE_WALK);
                }
            } else if (blob.state == Blob.State.FLYING) {
                if (gameState.getLaserEnergy() >= ProjectileManager.FLY_COST) {
                    gameState.useLaser(ProjectileManager.FLY_COST);
                    float dx, dy;
                    if (inputManager.isAnalogActive()) {
                        dx = inputManager.getAnalogX();
                        dy = inputManager.getAnalogY();
                    } else {
                        dx = blob.vx;
                        dy = blob.vy;
                    }
                    if (dx == 0 && dy == 0) dx = blob.facingRight ? 1 : -1;
                    projectileManager.fireFly(blob.x, blob.y, dx, dy);
                    SoundManager.play(SoundType.FIRE_FLY);
                }
            }
        }
        projectileManager.update(delta, room);

        Array<GameObject> overlapping = getOverlappingObjects();
        dispatchObjectActions(overlapping);
        checkObjectCollisions(overlapping);

        checkRoomExit();
    }

    private void updateTempPlatforms(float delta) {
        if (blob.state != Blob.State.FLYING && inputManager.isJustPressed(Action.DOWN)) {
            float px = blob.x;
            float py = blob.y;
            float newBlobY = py + TempPlatform.HEIGHT;
            if (gameState.getPlatforms() > 0 && !blob.wouldCollide(newBlobY, room)) {
                gameState.usePlatform();
                SoundManager.play(SoundType.PLATFORM);
                TempPlatform plat = new TempPlatform(px, py);
                platforms.add(plat);
                room.addTempPlatform(plat);
                blob.y = newBlobY;
                blob.vy = 0;
            }
        }

        Iterator<TempPlatform> it = platforms.iterator();
        while (it.hasNext()) {
            TempPlatform p = it.next();
            p.update(delta);
            if (p.isExpired()) {
                room.removeTempPlatform(p);
                it.remove();
            }
        }
    }

    private void checkRoomExit() {
        if (blob.exit == Blob.Exit.NONE) return;
        Blob.Exit exit = blob.exit;
        int next = Room.adjacentIndex(room.roomIndex, exit.dx, exit.dy);
        if (next >= 0) {
            room.clearTempPlatforms();
            platforms.clear();
            projectileManager.clear();

            int oldRoom = room.roomIndex;
            prevRoom = room;
            room = Room.build(game.assets, next, objectRegistry);
            itemManager.populateRoom(room);
            EventBus.get().post(new RoomChangedEvent(oldRoom, next));
            transitionDx = exit.dx;
            transitionDy = exit.dy;
            transitionTime = 0;

            if (exit.dx != 0) {
                blob.x = exit.dx > 0 ? 0 : Room.WIDTH - Blob.SIZE;
                // Clamp Y to room bounds and stop vertical momentum
                blob.y = Math.max(0, Math.min(blob.y, Room.HEIGHT - Blob.SIZE));
                blob.vy = 0;
            }
            if (exit.dy != 0) {
                blob.y = exit.dy > 0 ? Room.HEIGHT - Blob.SIZE : 0;
                blob.x = Math.max(0, Math.min(blob.x, Room.WIDTH - Blob.SIZE));
            }
        } else {
            blob.x = Math.max(0, Math.min(blob.x, Room.WIDTH - Blob.SIZE));
            blob.y = Math.max(0, Math.min(blob.y, Room.HEIGHT - Blob.SIZE));
            blob.exit = Blob.Exit.NONE;
        }
    }

    private void updateObjectsOnly(float delta) {
        for (GameObject obj : room.getObjects()) {
            obj.update(delta);
        }
    }

    private void renderWorld(float delta) {
        TextureRegion terrainCur = roomRenderer.getTerrainTexture(room);
        TextureRegion terrainPrev = isRoomTransitioning() ? roomRenderer.getTerrainTexture(prevRoom) : null;

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        gameViewport.apply();
        batch.setProjectionMatrix(gameViewport.getCamera().combined);
        batch.begin();

        if (teleportTransition != null) {
            teleportTransition.render(batch);
        } else if (isRoomTransitioning()) {
            renderRoomTransition(terrainCur, terrainPrev);
        } else {
            renderRoom(terrainCur, delta);
        }

        hud.setDebugRoomIndex(room.roomIndex);
        hud.render(batch, gameState, delta);

        // Core delivery flying item renders on top of HUD
        CoreAssembly core = CoreTrigger.getCoreAssembly();
        if (core != null) {
            core.renderOverlay(batch);
        }

        if (activeOverlay != null) {
            activeOverlay.render(batch);
        }

        batch.end();

        touchControls.render();
    }

    private void renderRoomTransition(TextureRegion terrainCur, TextureRegion terrainPrev) {
        float t = Interpolation.pow2.apply(
                Math.min(transitionTime / TRANSITION_DURATION, 1f));
        float roomW = Room.WIDTH;
        float roomH = Room.HEIGHT;
        batch.draw(terrainPrev,
                -roomW * transitionDx * t,
                roomH * transitionDy * t, roomW, roomH);
        batch.draw(terrainCur,
                roomW * transitionDx * (1f - t),
                roomH * -transitionDy * (1f - t), roomW, roomH);
    }

    private void renderRoom(TextureRegion terrain, float delta) {
        batch.draw(terrain, 0, 0, Room.WIDTH, Room.HEIGHT);

        for (GameObject obj : room.getObjects()) {
            obj.render(batch, delta);
        }
        for (TempPlatform p : platforms) {
            platformRenderer.render(batch, p);
        }
        if (blob.attachment != null && !blob.isInTransition()) {
            blob.attachment.render(batch, blob.x, blob.y - Blob.PLATFORM_HEIGHT);
        }

        transitionManager.render(batch);
        tunnelController.render(batch);
        blobRenderer.render(batch, blob, delta);
        projectileManager.render(batch);

        for (GameObject obj : room.getObjects()) {
            obj.renderForeground(batch, delta);
        }
    }

    private static final InputManager.Action[] ACTIONS = InputManager.Action.values();

    private void dispatchObjectActions(Array<GameObject> objs) {
        if (objs == null) return;
        for (InputManager.Action action : ACTIONS) {
            if (inputManager.isJustPressed(action)) {
                for (GameObject obj : objs) {
                    if (obj.onAction(blob, action)) break;
                }
            }
        }
    }

    private void checkObjectCollisions(Array<GameObject> objs) {
        if (objs == null) return;
        for (GameObject obj : objs) {
            obj.onEnter(blob);
        }
    }

    private final Array<GameObject> overlappingList = new Array<>();
    private final ObjectSet<GameObject> overlappingSet = new ObjectSet<>();

    private Array<GameObject> getOverlappingObjects() {
        overlappingList.clear();
        overlappingSet.clear();
        float bottom = blob.getBottom();
        float top = blob.y + Blob.SIZE;
        float left = blob.x;
        float right = blob.x + Blob.SIZE;

        collectAt(left, bottom);
        collectAt(right - 1, bottom);
        collectAt(left, top - 1);
        collectAt(right - 1, top - 1);

        if (blob.onGround) {
            collectAt(blob.x + Blob.SIZE / 2f, bottom - 1);
        }

        return overlappingList.size > 0 ? overlappingList : null;
    }

    private void collectAt(float worldX, float worldY) {
        Array<GameObject> objs = room.getObjectsAt(worldX, worldY);
        if (objs == null) return;
        for (int i = 0, n = objs.size; i < n; i++) {
            GameObject obj = objs.get(i);
            if (overlappingSet.add(obj)) {
                overlappingList.add(obj);
            }
        }
    }

    /** Get the current room's rendered terrain texture. Ensures FBO is rendered. */
    public TextureRegion getRoomTerrain() {
        return roomRenderer.getTerrainTexture(room);
    }

    @Override
    public void resize(int width, int height) {
        gameViewport.update(width, height, true);
        touchControls.resize(width, height);
    }

    @Override
    public void show() {
    }

    @Override
    public void pause() {
        if (AchievementManager.get() != null) AchievementManager.get().onPause();
        if (MusicManager.get() != null) MusicManager.get().pause();
    }

    @Override
    public void resume() {
        if (AchievementManager.get() != null) AchievementManager.get().onResume();
        if (MusicManager.get() != null) MusicManager.get().resume();
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        EventBus.get().clear();
        ScoreManager.dispose();
        AchievementManager.dispose();
        Teleporter.suppressUntilExit = false;
        batch.dispose();
        roomRenderer.dispose();
        platformRenderer.dispose();
        transitionManager.dispose();
        touchControls.dispose();
        inputManager.disconnectControllers();
        if (prevRoom != null) prevRoom.dispose();
        room.dispose();
    }
}
