package northern.captain.starquake.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import northern.captain.starquake.StarquakeGame;
import northern.captain.starquake.audio.SoundManager;
import northern.captain.starquake.world.SaveManager;
import northern.captain.starquake.input.InputManager;
import northern.captain.starquake.world.GameState;
import northern.captain.starquake.world.ScoreManager;

/**
 * Win/congratulations screen. Shows infoscreen background with animated text
 * and assembled core in the black area.
 */
public class WinScreen implements Screen {
    private static final int VIEWPORT_W = 256;
    private static final int VIEWPORT_H = 168;

    // Black text area in viewport coords (mapped from 320x200 source)
    private static final float AREA_X = 40;
    private static final float AREA_Y = 34;
    private static final float AREA_W = 176;
    private static final float AREA_H = 100;

    // Core grid position — bottom right of black area
    private static final float CORE_X = AREA_X + AREA_W - 48 - 4; // 48px grid + 4px margin
    private static final float CORE_Y = AREA_Y + 4;
    private static final int CORE_CELL = 16;

    // Text positions — left side of area, typed line by line
    private static final float TEXT_X = AREA_X + 6;
    private static final float TEXT_TOP_Y = AREA_Y + AREA_H - 10;
    private static final float LINE_H = 12;
    private static final float TYPE_SPEED = 30f; // chars per second

    private static final float FADE_TIME = 0.5f;

    enum Phase { TYPING, WAITING, FADE_OUT, FADE_IN }

    private final StarquakeGame game;
    private final SpriteBatch batch = new SpriteBatch();
    private final FitViewport gameViewport = new FitViewport(VIEWPORT_W, VIEWPORT_H);
    private final ScreenViewport screenViewport = new ScreenViewport();
    private final InputManager inputManager = new InputManager();

    private final TextureRegion background;
    private final TextureRegion[] coreIcons = new TextureRegion[9];
    private final TextureRegion pixel;
    private final BitmapFont font;

    private final String[] lines;
    private final boolean[] lineCentered;

    private Phase phase = Phase.TYPING;
    private float timer;
    private float typeTimer; // separate timer for typing, never resets
    private TitleScreen pendingTitle;

    public WinScreen(StarquakeGame game, GameState gameState) {
        this.game = game;
        this.background = game.assets.infoScreen;
        this.pixel = game.assets.whitePixel;
        this.font = game.assets.font;

        for (int i = 0; i < 9; i++) {
            coreIcons[i] = game.assets.itemsAtlas.findRegion("item", i);
        }

        // Build text lines
        ScoreManager score = ScoreManager.get();
        int totalScore = gameState.getScore();
        int explorationScore = score != null ? score.getExplorationScore() : 0;
        int exploredPct = score != null ? score.getExplorationPercent() : 0;

        lines = new String[]{
                "CONGRATULATIONS",
                "",
                "YOU SAVED THE PLANET",
                "",
                "TOTAL SCORE:   " + totalScore,
                "EXPLORE SCORE: " + explorationScore,
                "EXPLORED:      " + exploredPct
        };
        lineCentered = new boolean[]{
                true,
                false,
                true,
                false,
                false,
                false,
                false
        };

        SoundManager.play(SoundManager.SoundType.GAME_OVER);
        if (SaveManager.get() != null) SaveManager.get().clearAll();

        Gdx.input.setInputProcessor(new InputMultiplexer(inputManager.getKeyboardListener()));
        inputManager.connectControllers();
    }

    @Override
    public void render(float delta) {
        delta = Math.min(delta, 1 / 15f);
        timer += delta;
        typeTimer += delta;

        update();

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        gameViewport.apply();
        batch.setProjectionMatrix(gameViewport.getCamera().combined);
        batch.begin();

        // Background
        if (background != null) {
            batch.draw(background, 0, 0, VIEWPORT_W, VIEWPORT_H);
        }

        // Typed text
        renderText();

        // Fade overlay
        if (phase == Phase.FADE_OUT) {
            float a = Math.min(timer / FADE_TIME, 1f);
            batch.setColor(0, 0, 0, a);
            batch.draw(pixel, 0, 0, VIEWPORT_W, VIEWPORT_H);
            batch.setColor(Color.WHITE);
        }

        batch.end();

        // Fade-in title
        if (phase == Phase.FADE_IN && pendingTitle != null) {
            pendingTitle.render(delta);
            screenViewport.apply();
            batch.begin();
            float a = 1f - Math.min(timer / FADE_TIME, 1f);
            batch.setColor(0, 0, 0, a);
            batch.draw(pixel, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            batch.setColor(Color.WHITE);
            batch.end();
        }

        inputManager.update();
    }

    private void update() {
        switch (phase) {
            case TYPING:
                // Check if all text typed
                int totalChars = 0;
                for (String line : lines) totalChars += line.length();
                int charsShown = (int) (typeTimer * TYPE_SPEED);
                if (charsShown >= totalChars) {
                    phase = Phase.WAITING;
                    timer = 0;
                }
                break;
            case WAITING:
                if (Gdx.input.justTouched()
                        || inputManager.isJustPressed(InputManager.Action.ACTION_A)
                        || inputManager.isJustPressed(InputManager.Action.ACTION_B)
                        || inputManager.isJustPressed(InputManager.Action.UP)
                        || inputManager.isJustPressed(InputManager.Action.DOWN)
                        || inputManager.isJustPressed(InputManager.Action.LEFT)
                        || inputManager.isJustPressed(InputManager.Action.RIGHT)) {
                    SoundManager.play(SoundManager.SoundType.UI_TEXT);
                    phase = Phase.FADE_OUT;
                    timer = 0;
                }
                break;
            case FADE_OUT:
                if (timer >= FADE_TIME) {
                    pendingTitle = new TitleScreen(game);
                    pendingTitle.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
                    phase = Phase.FADE_IN;
                    timer = 0;
                }
                break;
            case FADE_IN:
                if (timer >= FADE_TIME) {
                    game.setScreen(pendingTitle);
                }
                break;
        }
    }

    private void renderCore() {
        for (int i = 0; i < 9; i++) {
            if (coreIcons[i] == null) continue;
            int col = i % 3;
            int row = i / 3;
            float x = CORE_X + col * CORE_CELL;
            float y = CORE_Y + (2 - row) * CORE_CELL;
            batch.draw(coreIcons[i], x, y, CORE_CELL, CORE_CELL);
        }
    }

    private void renderText() {
        int charsShown = (int) (typeTimer * TYPE_SPEED);
        int charsUsed = 0;

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            int remaining = charsShown - charsUsed;
            if (remaining <= 0) break;

            String visible = line.substring(0, Math.min(remaining, line.length()));
            float y = TEXT_TOP_Y - i * LINE_H;

            font.setColor(i == 2 ? Color.YELLOW : Color.WHITE);

            if (lineCentered[i] && visible.length() > 0) {
                float textW = visible.length() * 8;
                float cx = AREA_X + (AREA_W - textW) / 2f;
                font.draw(batch, visible, cx, y);
            } else {
                font.draw(batch, visible, TEXT_X, y);
            }

            charsUsed += line.length();
        }
        font.setColor(Color.WHITE);
    }

    @Override
    public void resize(int width, int height) {
        gameViewport.update(width, height, true);
        screenViewport.update(width, height, true);
    }

    @Override public void show() {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        batch.dispose();
        inputManager.disconnectControllers();
    }
}
