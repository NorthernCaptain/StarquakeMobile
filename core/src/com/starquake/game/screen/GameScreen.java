package com.starquake.game.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.starquake.game.StarquakeGame;
import com.starquake.game.world.Room;
import com.starquake.game.world.RoomRenderer;

public class GameScreen implements Screen {
    public static final int VIEWPORT_W = Room.WIDTH;
    public static final int VIEWPORT_H = Room.HEIGHT;

    private final StarquakeGame game;
    private final SpriteBatch batch = new SpriteBatch();
    private final FitViewport gameViewport = new FitViewport(VIEWPORT_W, VIEWPORT_H);
    private final RoomRenderer roomRenderer;

    private Room room;

    public GameScreen(StarquakeGame game, int startRoom) {
        this.game    = game;
        roomRenderer = new RoomRenderer(game.assets);
        room         = Room.build(game.assets, startRoom);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        gameViewport.apply();
        batch.setProjectionMatrix(gameViewport.getCamera().combined);

        TextureRegion terrain = roomRenderer.getTerrainTexture(room);

        batch.begin();
        batch.draw(terrain, 0, 0, VIEWPORT_W, VIEWPORT_H);
        // TODO: sprites, items, HUD drawn here on top
        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        gameViewport.update(width, height, true);
    }

    @Override public void show() {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        batch.dispose();
        roomRenderer.dispose();
        room.dispose();
    }
}
