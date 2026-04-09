package northern.captain.starquake.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import northern.captain.starquake.StarquakeGame;

public class LoadingScreen implements Screen {
    private final StarquakeGame game;
    private final ShapeRenderer shapes = new ShapeRenderer();

    public LoadingScreen(StarquakeGame game) {
        this.game = game;
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (game.assets.update()) {
            // setScreen calls hide() then dispose() on this screen — no manual dispose needed
            game.setScreen(new GameScreen(game, 176));
            return;
        }

        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();
        float barW = w * 0.5f;
        float barH = 20;
        float progress = game.assets.getProgress();

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.25f, 0.25f, 0.25f, 1);
        shapes.rect(w * 0.25f, h * 0.5f - barH * 0.5f, barW, barH);
        shapes.setColor(1, 1, 1, 1);
        shapes.rect(w * 0.25f, h * 0.5f - barH * 0.5f, barW * progress, barH);
        shapes.end();
    }

    @Override public void show() {}
    @Override public void resize(int w, int h) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        shapes.dispose();
    }
}
