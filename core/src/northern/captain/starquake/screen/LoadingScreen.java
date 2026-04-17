package northern.captain.starquake.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import northern.captain.starquake.StarquakeGame;
import northern.captain.starquake.audio.MusicManager;
import northern.captain.starquake.audio.SoundManager;
import northern.captain.starquake.world.SaveManager;

public class LoadingScreen implements Screen {
    private static final float FADE_IN_TIME = 2.0f;
    private static final float FADE_OUT_TIME = 0.7f;
    private static final float HOLD_TIME = 0.2f;
    private static final float DESIGN_HEIGHT = 880f;

    enum Phase { FADE_IN, WAIT, FADE_OUT, DONE }

    private final StarquakeGame game;
    private final SpriteBatch batch = new SpriteBatch();
    private final ScreenViewport viewport = new ScreenViewport();

    private TextureAtlas introAtlas;
    private TextureRegion background;
    private TextureRegion logo;

    private Phase phase = Phase.FADE_IN;
    private float timer;
    private boolean assetsReady;
    private boolean firstFrameRendered;

    public LoadingScreen(StarquakeGame game) {
        this.game = game;

        // Load intro atlas synchronously
        introAtlas = new TextureAtlas(Gdx.files.internal("atlases/intro.atlas"));
        for (Texture t : introAtlas.getTextures()) {
            t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        }
        background = introAtlas.findRegion("background");
        logo = introAtlas.findRegion("v3_intro");
    }

    @Override
    public void render(float delta) {
        delta = Math.min(delta, 1 / 15f);

        // Don't advance timer until first frame has been rendered (atlas visible)
        if (firstFrameRendered) {
            timer += delta;
        }

        // Start loading game assets only after fade in (matches Quadronia pattern)
        if (!assetsReady && phase != Phase.FADE_IN && game.assets.update()) {
            assetsReady = true;
            SoundManager.init();
            MusicManager.init();
            SaveManager.init();
        }

        // Phase transitions
        switch (phase) {
            case FADE_IN:
                if (timer >= FADE_IN_TIME) {
                    phase = Phase.WAIT;
                    timer = 0;
                }
                break;
            case WAIT:
                if (timer >= HOLD_TIME && assetsReady) {
                    phase = Phase.FADE_OUT;
                    timer = 0;
                }
                break;
            case FADE_OUT:
                if (timer >= FADE_OUT_TIME) {
                    phase = Phase.DONE;
                    MusicManager.get().setEnabled(true);
                    game.setScreen(new TitleScreen(game));
                    return;
                }
                break;
            case DONE:
                break;
        }

        float w = viewport.getWorldWidth();
        float h = viewport.getWorldHeight();

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        float alpha;
        switch (phase) {
            case FADE_IN:  alpha = Math.min(timer / FADE_IN_TIME, 1f); break;
            case FADE_OUT: alpha = 1f - Math.min(timer / FADE_OUT_TIME, 1f); break;
            default:       alpha = 1f; break;
        }

        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();
        batch.setColor(1, 1, 1, alpha);

        // Tile background across entire screen
        if (background != null) {
            float bgW = background.getRegionWidth();
            float bgH = background.getRegionHeight();
            for (float bx = 0; bx < w; bx += bgW) {
                for (float by = 0; by < h; by += bgH) {
                    batch.draw(background, bx, by, bgW, bgH);
                }
            }
        }

        // Center logo at design size
        if (logo != null) {
            float lw = logo.getRegionWidth();
            float lh = logo.getRegionHeight();
            batch.draw(logo, (w - lw) / 2f, (h - lh) / 2f, lw, lh);
        }

        batch.setColor(Color.WHITE);
        batch.end();

        firstFrameRendered = true;
    }

    @Override
    public void show() {
        updateViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    @Override
    public void resize(int w, int h) {
        updateViewport(w, h);
    }

    private void updateViewport(int w, int h) {
        viewport.setUnitsPerPixel(DESIGN_HEIGHT / Math.max(1, h));
        viewport.update(w, h, true);
    }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        batch.dispose();
        if (introAtlas != null) {
            introAtlas.dispose();
            introAtlas = null;
            background = null;
            logo = null;
        }
    }
}
