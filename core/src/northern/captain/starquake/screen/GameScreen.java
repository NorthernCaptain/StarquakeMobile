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
import northern.captain.starquake.world.objects.CoreTrigger;
import northern.captain.starquake.world.objects.GameObjectRegistry;
import northern.captain.starquake.world.objects.HoverStand;
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

    public GameScreen(StarquakeGame game, int startRoom) {
        this.game = game;
        roomRenderer = new RoomRenderer(game.assets);
        blobRenderer = new BlobRenderer(game.assets);
        platformRenderer = new PlatformRenderer(game.assets);

        objectRegistry = GameObjectRegistry.createDefault();
        tunnelController = new TunnelController(game.assets);
        itemManager = new ItemManager(game.assets);
        ItemPickup.init(gameState, inventory, itemManager);
        hud = new Hud(game.assets, inventory);

        inputManager = new InputManager();
        touchControls = new TouchControls(inputManager, false);
        inputManager.connectControllers();
        Gdx.input.setInputProcessor(new InputMultiplexer(inputManager.getKeyboardListener()));

        room = Room.build(game.assets, startRoom, objectRegistry);
        long seed = System.currentTimeMillis();
        CoreTrigger.initCoreAssembly(game.assets, seed, itemManager.getPartPool());
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
            prevRoom = room;
            room = Room.build(game.assets, next, objectRegistry);
            itemManager.populateRoom(room);
            tunnelController.setRoom(room);
            transitionDx = dx;
            transitionDy = 0;
            transitionTime = 0;
            return room;
        });

        // Register event listeners
        gameState.registerEvents();
        EventBus.get().register(GameEvent.Type.BLOB_DIED, e -> triggerDeath());
        EventBus.get().register(GameEvent.Type.LIFT_STARTED, e -> startLift());
        HoverStand.registerEvents();

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
            prevRoom = room;
            room = Room.build(game.assets, next, objectRegistry);
            itemManager.populateRoom(room);
            liftController.setRoom(room);
            // No slide transition for lift — instant room switch
            prevRoom.dispose();
            prevRoom = null;
            return room;
        });
    }

    private void triggerDeath() {
        transitionManager.start(blob, new BlobTransition[]{
                new ExplosionTransition(),
                new PauseTransition(2.0f),
                new AssemblyTransition()
        }, () -> EventBus.get().post(GameEvent.BLOB_SPAWNED));
    }

    @Override
    public void render(float delta) {
        delta = Math.min(delta, 1 / 15f);

        touchControls.poll();
        updateWorld(delta);
        renderWorld(delta);
        inputManager.update();
    }

    private void updateWorld(float delta) {
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
                inputManager.isPressed(Action.DOWN));

        updateTempPlatforms(delta);

        for (GameObject obj : room.getObjects()) {
            obj.update(delta);
        }
        if (blob.attachment != null) {
            blob.attachment.update(delta);
        }

        blob.update(delta, room);
        gameState.update(delta);

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

            prevRoom = room;
            room = Room.build(game.assets, next, objectRegistry);
            itemManager.populateRoom(room);
            transitionDx = exit.dx;
            transitionDy = exit.dy;
            transitionTime = 0;

            if (exit.dx != 0) blob.x = exit.dx > 0 ? 0 : Room.WIDTH - Blob.SIZE;
            if (exit.dy != 0) blob.y = exit.dy > 0 ? Room.HEIGHT - Blob.SIZE : 0;
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

        if (isRoomTransitioning()) {
            renderRoomTransition(terrainCur, terrainPrev);
        } else {
            renderRoom(terrainCur, delta);
        }

        hud.render(batch, gameState);
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
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        EventBus.get().clear();
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
