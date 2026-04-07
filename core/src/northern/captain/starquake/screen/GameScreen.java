package northern.captain.starquake.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.utils.viewport.FitViewport;
import northern.captain.starquake.StarquakeGame;
import northern.captain.starquake.input.InputManager;
import northern.captain.starquake.input.InputManager.Action;
import northern.captain.starquake.input.TouchControls;
import northern.captain.starquake.world.Blob;
import northern.captain.starquake.world.BlobRenderer;
import northern.captain.starquake.world.PlatformRenderer;
import northern.captain.starquake.world.Room;
import northern.captain.starquake.world.RoomRenderer;
import northern.captain.starquake.world.TempPlatform;

import java.util.ArrayList;
import java.util.Iterator;

public class GameScreen implements Screen {
    public static final int VIEWPORT_W = Room.WIDTH;
    public static final int VIEWPORT_H = Room.HEIGHT;
    private static final float TRANSITION_DURATION = 0.4f;

    private final StarquakeGame game;
    private final SpriteBatch batch = new SpriteBatch();
    private final FitViewport gameViewport = new FitViewport(VIEWPORT_W, VIEWPORT_H);

    private final InputManager inputManager;
    private final TouchControls touchControls;
    private final RoomRenderer roomRenderer;
    private final BlobRenderer blobRenderer;
    private final PlatformRenderer platformRenderer;

    private Room room;
    private Blob blob;
    private final ArrayList<TempPlatform> platforms = new ArrayList<>();

    // Transition state
    private Room prevRoom;
    private float transitionTime;
    private int transitionDx, transitionDy;

    public GameScreen(StarquakeGame game, int startRoom) {
        this.game    = game;
        roomRenderer = new RoomRenderer(game.assets);
        blobRenderer = new BlobRenderer(game.assets);
        platformRenderer = new PlatformRenderer(game.assets);

        inputManager = new InputManager();
        touchControls = new TouchControls(inputManager, false);
        inputManager.connectControllers();
        Gdx.input.setInputProcessor(new InputMultiplexer(inputManager.getKeyboardListener()));

        room = Room.build(game.assets, startRoom);
        blob = new Blob(Room.WIDTH / 2f - Blob.SIZE / 2f, Room.HEIGHT - Blob.SIZE);

        game.assets.font.getData().setScale(1f);
        game.assets.font.setUseIntegerPositions(true);
    }

    private boolean isTransitioning() {
        return prevRoom != null;
    }

    @Override
    public void render(float delta) {
        delta = Math.min(delta, 1 / 15f); // clamp to avoid physics explosion on lag spikes

        // --- Input ---
        touchControls.poll();

        // --- Update ---
        if (isTransitioning()) {
            transitionTime += delta;
            if (transitionTime >= TRANSITION_DURATION) {
                prevRoom.dispose();
                prevRoom = null;
            }
        } else {
            // BLOB movement from input
            blob.applyInput(
                    inputManager.isPressed(Action.LEFT),
                    inputManager.isPressed(Action.RIGHT));

            // Place platform on DOWN press — platform bottom at BLOB's feet, BLOB rides up
            if (inputManager.isJustPressed(Action.DOWN)) {
                float px = blob.x;
                float py = blob.y;  // platform bottom = where BLOB was standing
                TempPlatform plat = new TempPlatform(px, py);
                platforms.add(plat);
                room.setCollisionRect((int) px, (int) py, TempPlatform.WIDTH, TempPlatform.HEIGHT, true);
                blob.y += TempPlatform.HEIGHT;  // push BLOB on top
                blob.vy = 0;
            }

            // Update platforms — remove expired ones and clear their collision
            Iterator<TempPlatform> it = platforms.iterator();
            while (it.hasNext()) {
                TempPlatform p = it.next();
                p.update(delta);
                if (p.isExpired()) {
                    room.setCollisionRect((int) p.x, (int) p.y, TempPlatform.WIDTH, TempPlatform.HEIGHT, false);
                    it.remove();
                }
            }

            blob.update(delta, room);

            // Room transition on edge exit
            if (blob.exit != Blob.Exit.NONE) {
                Blob.Exit exit = blob.exit;
                int next = Room.adjacentIndex(room.roomIndex, exit.dx, exit.dy);
                if (next >= 0) {
                    // Clear platforms from old room
                    for (TempPlatform p : platforms) {
                        room.setCollisionRect((int) p.x, (int) p.y,
                                TempPlatform.WIDTH, TempPlatform.HEIGHT, false);
                    }
                    platforms.clear();

                    prevRoom = room;
                    room = Room.build(game.assets, next);
                    transitionDx = exit.dx;
                    transitionDy = exit.dy;
                    transitionTime = 0;

                    // Reposition BLOB at opposite edge
                    if (exit.dx != 0) blob.x = exit.dx > 0 ? 0 : Room.WIDTH - Blob.SIZE;
                    if (exit.dy != 0) blob.y = exit.dy > 0 ? Room.HEIGHT - Blob.SIZE : 0;
                } else {
                    // World boundary: push BLOB back in
                    blob.x = Math.max(0, Math.min(blob.x, Room.WIDTH - Blob.SIZE));
                    blob.y = Math.max(0, Math.min(blob.y, Room.HEIGHT - Blob.SIZE));
                    blob.exit = Blob.Exit.NONE;
                }
            }
        }

        // --- Render FBOs ---
        TextureRegion terrainCur = roomRenderer.getTerrainTexture(room);
        TextureRegion terrainPrev = isTransitioning() ? roomRenderer.getTerrainTexture(prevRoom) : null;

        // --- Draw ---
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        gameViewport.apply();
        batch.setProjectionMatrix(gameViewport.getCamera().combined);
        batch.begin();

        if (isTransitioning()) {
            float t = Interpolation.pow2.apply(
                    Math.min(transitionTime / TRANSITION_DURATION, 1f));
            float offsetX = VIEWPORT_W * transitionDx * (1f - t);
            float offsetY = VIEWPORT_H * -transitionDy * (1f - t);
            float prevOffsetX = -VIEWPORT_W * transitionDx * t;
            float prevOffsetY = VIEWPORT_H * transitionDy * t;
            batch.draw(terrainPrev, prevOffsetX, prevOffsetY, VIEWPORT_W, VIEWPORT_H);
            batch.draw(terrainCur, offsetX, offsetY, VIEWPORT_W, VIEWPORT_H);
        } else {
            batch.draw(terrainCur, 0, 0, VIEWPORT_W, VIEWPORT_H);
        }

        // Platforms + BLOB (drawn on top of terrain, only when not transitioning)
        if (!isTransitioning()) {
            for (TempPlatform p : platforms) {
                platformRenderer.render(batch, p);
            }
            blobRenderer.render(batch, blob, delta);
        }

        // Debug text
        game.assets.font.draw(batch, "ROOM " + room.roomIndex
                + " X" + room.getX() + " Y" + room.getY(), 2, VIEWPORT_H - 2);
        batch.end();

        // Touch controls overlay
        touchControls.render();

        // --- End of frame ---
        inputManager.update();
    }

    @Override
    public void resize(int width, int height) {
        gameViewport.update(width, height, true);
        touchControls.resize(width, height);
    }

    @Override public void show() {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        batch.dispose();
        roomRenderer.dispose();
        platformRenderer.dispose();
        touchControls.dispose();
        inputManager.disconnectControllers();
        if (prevRoom != null) prevRoom.dispose();
        room.dispose();
    }
}
