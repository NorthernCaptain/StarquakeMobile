package com.starquake.game.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.starquake.game.StarquakeGame;
import com.starquake.game.world.Room;
import com.starquake.game.world.RoomRenderer;

public class GameScreen implements Screen {
    public static final int VIEWPORT_W = 1536;
    public static final int VIEWPORT_H = 1152;

    private final StarquakeGame game;
    private final SpriteBatch batch = new SpriteBatch();

    // Letterboxed 4:3 game area
    private final FitViewport gameViewport = new FitViewport(VIEWPORT_W, VIEWPORT_H);
    // Full-screen overlay for touch controls (Phase 2+)
    private final ScreenViewport overlayViewport = new ScreenViewport();

    private final JsonValue metadata;
    private Room room;
    private final RoomRenderer roomRenderer;

    public GameScreen(StarquakeGame game, int startRoom) {
        this.game    = game;
        metadata     = new JsonReader().parse(Gdx.files.internal("metadata.json"));
        room         = Room.build(metadata, startRoom);
        roomRenderer = new RoomRenderer(game.assets, metadata);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        gameViewport.apply();
        batch.setProjectionMatrix(gameViewport.getCamera().combined);
        batch.begin();
        roomRenderer.render(batch, room);
        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        gameViewport.update(width, height, true);
        overlayViewport.update(width, height, true);
    }

    @Override public void show() {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        batch.dispose();
    }
}
