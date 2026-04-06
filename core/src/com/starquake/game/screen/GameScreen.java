package com.starquake.game.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.starquake.game.StarquakeGame;
import com.starquake.game.input.InputManager;
import com.starquake.game.input.InputManager.Action;
import com.starquake.game.input.TouchControls;
import com.starquake.game.world.Room;
import com.starquake.game.world.RoomRenderer;

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

    private Room room;

    // Transition state
    private Room prevRoom;          // room sliding out (null when idle)
    private float transitionTime;   // 0 → TRANSITION_DURATION
    private int transitionDx;       // slide direction: -1, 0, +1
    private int transitionDy;

    public GameScreen(StarquakeGame game, int startRoom) {
        this.game    = game;
        roomRenderer = new RoomRenderer(game.assets);

        inputManager = new InputManager();
        touchControls = new TouchControls(inputManager, false);
        inputManager.connectControllers();

        Gdx.input.setInputProcessor(new InputMultiplexer(inputManager.getKeyboardListener()));

        room = Room.build(game.assets, startRoom);

        game.assets.font.getData().setScale(1f);
        game.assets.font.setUseIntegerPositions(true);
    }

    private boolean isTransitioning() {
        return prevRoom != null;
    }

    @Override
    public void render(float delta) {
        // --- Input ---
        touchControls.poll();

        // --- Room navigation (blocked during transition) ---
        if (!isTransitioning()) {
            // D-pad: isJustPressed for initial tap, isPressed for auto-repeat after transition
            int dx = 0, dy = 0;
            if (inputManager.isJustPressed(Action.RIGHT) || inputManager.isPressed(Action.RIGHT)) dx = 1;
            if (inputManager.isJustPressed(Action.LEFT)  || inputManager.isPressed(Action.LEFT))  dx = -1;
            if (inputManager.isJustPressed(Action.DOWN)  || inputManager.isPressed(Action.DOWN))  dy = 1;
            if (inputManager.isJustPressed(Action.UP)    || inputManager.isPressed(Action.UP))    dy = -1;

            if (dx != 0 || dy != 0) {
                int next = Room.adjacentIndex(room.roomIndex, dx, dy);
                if (next >= 0) {
                    prevRoom = room;
                    room = Room.build(game.assets, next);
                    transitionDx = dx;
                    transitionDy = dy;
                    transitionTime = 0;
                }
            }
        }

        // --- Update transition ---
        if (isTransitioning()) {
            transitionTime += delta;
            if (transitionTime >= TRANSITION_DURATION) {
                prevRoom.dispose();
                prevRoom = null;
            }
        }

        // --- Render FBOs (before applying game viewport) ---
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

            // Slide offset: new room slides in from the direction of travel
            float offsetX = VIEWPORT_W * transitionDx * (1f - t);
            float offsetY = VIEWPORT_H * -transitionDy * (1f - t);

            // Previous room slides out in the opposite direction
            float prevOffsetX = -VIEWPORT_W * transitionDx * t;
            float prevOffsetY = VIEWPORT_H * transitionDy * t;

            batch.draw(terrainPrev, prevOffsetX, prevOffsetY, VIEWPORT_W, VIEWPORT_H);
            batch.draw(terrainCur, offsetX, offsetY, VIEWPORT_W, VIEWPORT_H);
        } else {
            batch.draw(terrainCur, 0, 0, VIEWPORT_W, VIEWPORT_H);
        }

        // Debug text (on current room, always at fixed position)
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
        touchControls.dispose();
        inputManager.disconnectControllers();
        if (prevRoom != null) prevRoom.dispose();
        room.dispose();
    }
}
