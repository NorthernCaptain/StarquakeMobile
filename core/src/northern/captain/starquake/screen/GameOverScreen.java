package northern.captain.starquake.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import northern.captain.starquake.StarquakeGame;
import northern.captain.starquake.audio.SoundManager;
import northern.captain.starquake.input.InputManager;
import northern.captain.starquake.world.GameState;
import northern.captain.starquake.world.transitions.TeleportTransition;

/**
 * Game Over screen. Shows room explode, then drops "GAME" / "OVER!" letters
 * one by one with scale animation and death cloud puffs.
 */
public class GameOverScreen implements Screen {
    private static final int VIEWPORT_W = 256;
    private static final int VIEWPORT_H = 168;
    private static final int LETTER_SIZE = 32;
    private static final int LETTER_GAP = 2;
    private static final int BANG_WIDTH = 16; // "!" sprite is 16px wide

    // Letter sprite indices: G=0, A=1, M=2, !=3, E=4, O=5, V=6, E=7, R=8
    private static final int[] ROW1_INDICES = {0, 1, 2, 4};       // GAME
    private static final int[] ROW2_INDICES = {5, 6, 7, 8, 3};    // OVER!
    private static final int TOTAL_LETTERS = 9;
    private static final int ROW1_COUNT = 4;
    private static final int ROW2_COUNT = 5;

    // Letter positions — centered with gaps, "!" uses actual width
    private static final float ROW1_WIDTH = ROW1_COUNT * LETTER_SIZE + (ROW1_COUNT - 1) * LETTER_GAP;
    private static final float ROW2_WIDTH = 4 * LETTER_SIZE + BANG_WIDTH + (ROW2_COUNT - 1) * LETTER_GAP;
    private static final float ROW1_BASE_X = (VIEWPORT_W - ROW1_WIDTH) / 2f;
    private static final float ROW2_BASE_X = (VIEWPORT_W - ROW2_WIDTH) / 2f;
    private static final float ROW1_Y = 100;
    private static final float ROW2_Y = 60;

    // Timing
    private static final float BLACK_PAUSE_TIME = 0.3f;
    private static final float DROP_FLIGHT_TIME = 0.4f;
    private static final float DROP_DELAY = 0f;
    private static final float CLOUD_FRAME_TIME = 0.05f;
    private static final int CLOUD_FRAMES = 12;
    private static final float CLOUD_DURATION = CLOUD_FRAMES * CLOUD_FRAME_TIME;
    private static final float FADE_TIME = 0.5f;
    private static final float START_SCALE = 8f;

    enum Phase { EXPLODE, BLACK_PAUSE, DROPPING, WAITING, FADE_OUT, FADE_IN }

    private final StarquakeGame game;
    private final SpriteBatch batch = new SpriteBatch();
    private final FitViewport gameViewport = new FitViewport(VIEWPORT_W, VIEWPORT_H);
    private final ScreenViewport screenViewport = new ScreenViewport();
    private final InputManager inputManager = new InputManager();

    private final TeleportTransition explodeTransition;
    private final FrameBuffer terrainCopy; // own copy of room terrain to survive GameScreen disposal
    private final TextureRegion[] letterSprites = new TextureRegion[TOTAL_LETTERS];
    private final TextureRegion[] cloudSprites = new TextureRegion[CLOUD_FRAMES];
    private final TextureRegion pixel;

    private Phase phase;
    private float timer;

    // Per-letter state: order is row1[0..3] then row2[0..4]
    private final float[] targetX = new float[TOTAL_LETTERS];
    private final float[] targetY = new float[TOTAL_LETTERS];
    private final float[] letterY = new float[TOTAL_LETTERS];
    private final float[] letterScale = new float[TOTAL_LETTERS];
    private final boolean[] landed = new boolean[TOTAL_LETTERS];
    private final float[] cloudTimer = new float[TOTAL_LETTERS];
    private int currentLetter;
    private float dropTimer; // timer for current letter's flight

    // Fade + next screen
    private final boolean win;
    private final GameState gameState;
    private Screen pendingScreen;

    public GameOverScreen(StarquakeGame game, TextureRegion roomTerrain, boolean win, GameState gameState) {
        this.win = win;
        this.gameState = gameState;
        this.game = game;
        this.pixel = game.assets.whitePixel;

        // Load sprites
        for (int i = 0; i < TOTAL_LETTERS; i++) {
            letterSprites[i] = game.assets.spritesAtlas.findRegion("game_over", i);
        }
        for (int i = 0; i < CLOUD_FRAMES; i++) {
            cloudSprites[i] = game.assets.spritesAtlas.findRegion("blob_death_cloud", i);
        }

        // Compute target positions with gaps
        for (int i = 0; i < ROW1_COUNT; i++) {
            targetX[i] = ROW1_BASE_X + i * (LETTER_SIZE + LETTER_GAP);
            targetY[i] = ROW1_Y;
        }
        // Row 2: O V E R are 32px each, ! is 16px
        float rx = ROW2_BASE_X;
        for (int i = 0; i < ROW2_COUNT; i++) {
            targetX[ROW1_COUNT + i] = rx;
            targetY[ROW1_COUNT + i] = ROW2_Y;
            int w = (i == ROW2_COUNT - 1) ? BANG_WIDTH : LETTER_SIZE; // last is "!"
            rx += w + LETTER_GAP;
        }

        // Init letter state
        for (int i = 0; i < TOTAL_LETTERS; i++) {
            letterScale[i] = START_SCALE;
            letterY[i] = VIEWPORT_H + 50;
            cloudTimer[i] = -1;
        }

        // Copy room terrain to own FBO (original will be disposed with GameScreen)
        terrainCopy = new FrameBuffer(Pixmap.Format.RGBA8888, 256, 144, false);
        terrainCopy.getColorBufferTexture().setFilter(
                Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        OrthographicCamera fboCam = new OrthographicCamera();
        fboCam.setToOrtho(false, 256, 144);
        terrainCopy.begin();
        batch.setProjectionMatrix(fboCam.combined);
        batch.begin();
        batch.draw(roomTerrain, 0, 0, 256, 144);
        batch.end();
        terrainCopy.end();
        TextureRegion terrainRegion = new TextureRegion(terrainCopy.getColorBufferTexture());
        terrainRegion.flip(false, true);

        // Start room explode
        explodeTransition = new TeleportTransition();
        explodeTransition.start(terrainRegion);
        phase = Phase.EXPLODE;
        timer = 0;

        // Input
        Gdx.input.setInputProcessor(new InputMultiplexer(inputManager.getKeyboardListener()));
        inputManager.connectControllers();
    }

    @Override
    public void render(float delta) {
        delta = Math.min(delta, 1 / 15f);
        timer += delta;

        update(delta);

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        gameViewport.apply();
        batch.setProjectionMatrix(gameViewport.getCamera().combined);
        batch.begin();

        switch (phase) {
            case EXPLODE:
                explodeTransition.render(batch);
                break;
            case BLACK_PAUSE:
                // pure black
                break;
            case DROPPING:
            case WAITING:
                renderLetters();
                renderClouds();
                break;
            case FADE_OUT:
                renderLetters();
                renderClouds();
                // Black overlay fading in
                float fadeAlpha = Math.min(timer / FADE_TIME, 1f);
                batch.setColor(0, 0, 0, fadeAlpha);
                batch.draw(pixel, 0, 0, VIEWPORT_W, VIEWPORT_H);
                batch.setColor(Color.WHITE);
                break;
            case FADE_IN:
                // Title screen renders itself, we draw black overlay fading out
                break;
        }

        batch.end();

        // Fade-in: let title render, then overlay fading black over full screen
        if (phase == Phase.FADE_IN && pendingScreen != null) {
            pendingScreen.render(delta);
            float alpha = 1f - Math.min(timer / FADE_TIME, 1f);
            screenViewport.apply();
            batch.setProjectionMatrix(screenViewport.getCamera().combined);
            batch.begin();
            batch.setColor(0, 0, 0, alpha);
            batch.draw(pixel, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            batch.setColor(Color.WHITE);
            batch.end();
        }

        inputManager.update();
    }

    private void update(float delta) {
        switch (phase) {
            case EXPLODE:
                explodeTransition.update(delta);
                // Only use disintegrate — skip reassemble by checking disintegrate done
                if (explodeTransition.getPhase() == TeleportTransition.Phase.BLACK_PAUSE
                        || explodeTransition.isDone()) {
                    phase = Phase.BLACK_PAUSE;
                    timer = 0;
                }
                break;

            case BLACK_PAUSE:
                if (timer >= BLACK_PAUSE_TIME) {
                    SoundManager.play(SoundManager.SoundType.GAME_OVER);
                    phase = Phase.DROPPING;
                    timer = 0;
                    currentLetter = 0;
                    dropTimer = 0;
                }
                break;

            case DROPPING:
                updateDropping(delta);
                updateClouds(delta);
                break;

            case WAITING:
                updateClouds(delta);
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
                    if (win) {
                        pendingScreen = new WinScreen(game, gameState);
                    } else {
                        pendingScreen = new TitleScreen(game);
                    }
                    pendingScreen.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
                    phase = Phase.FADE_IN;
                    timer = 0;
                }
                break;

            case FADE_IN:
                if (timer >= FADE_TIME) {
                    game.setScreen(pendingScreen);
                }
                break;
        }
    }

    private void updateDropping(float delta) {
        if (currentLetter >= TOTAL_LETTERS) {
            // All letters dropped — check if all clouds finished
            boolean allDone = true;
            for (int i = 0; i < TOTAL_LETTERS; i++) {
                if (cloudTimer[i] >= 0 && cloudTimer[i] < CLOUD_DURATION) {
                    allDone = false;
                    break;
                }
            }
            if (allDone) {
                phase = Phase.WAITING;
                timer = 0;
            }
            return;
        }

        dropTimer += delta;

        if (!landed[currentLetter]) {
            float t = Math.min(dropTimer / DROP_FLIGHT_TIME, 1f);
            float ease = Interpolation.pow2In.apply(t); // accelerate downward

            letterY[currentLetter] = (VIEWPORT_H + 50) + (targetY[currentLetter] - (VIEWPORT_H + 50)) * ease;
            letterScale[currentLetter] = START_SCALE + (1f - START_SCALE) * ease;

            if (t >= 1f) {
                letterY[currentLetter] = targetY[currentLetter];
                letterScale[currentLetter] = 1f;
                landed[currentLetter] = true;
                cloudTimer[currentLetter] = 0; // start cloud
                SoundManager.play(SoundManager.SoundType.EXPLOSION);
            }
        } else {
            // Letter landed, wait for delay then advance to next
            float timeSinceLand = dropTimer - DROP_FLIGHT_TIME;
            if (timeSinceLand >= DROP_DELAY) {
                currentLetter++;
                dropTimer = 0;
            }
        }
    }

    private void updateClouds(float delta) {
        for (int i = 0; i < TOTAL_LETTERS; i++) {
            if (cloudTimer[i] >= 0 && cloudTimer[i] < CLOUD_DURATION) {
                cloudTimer[i] += delta;
            }
        }
    }

    private void renderLetters() {
        for (int i = 0; i < TOTAL_LETTERS; i++) {
            if (!landed[i] && i != currentLetter) continue;

            // Map from sequential index to sprite index
            int spriteIdx = getSpriteIndex(i);
            TextureRegion sprite = letterSprites[spriteIdx];
            if (sprite == null) continue;

            float scale = letterScale[i];
            float drawSize = LETTER_SIZE * scale;
            float cx = targetX[i] + LETTER_SIZE / 2f;
            float cy = letterY[i] + LETTER_SIZE / 2f;

            batch.draw(sprite,
                    cx - drawSize / 2f, cy - drawSize / 2f,
                    drawSize, drawSize);
        }
    }

    private void renderClouds() {
        for (int i = 0; i < TOTAL_LETTERS; i++) {
            if (cloudTimer[i] < 0 || cloudTimer[i] >= CLOUD_DURATION) continue;

            int frame = Math.min((int) (cloudTimer[i] / CLOUD_FRAME_TIME), CLOUD_FRAMES - 1);
            TextureRegion sprite = cloudSprites[frame];
            if (sprite == null) continue;

            // Cloud centered at the bottom of the letter
            float cx = targetX[i] + LETTER_SIZE / 2f;
            float cy = targetY[i];
            batch.draw(sprite, cx - 16, cy - 16, 32, 32);
        }
    }

    /** Map sequential letter index (0-8) to sprite index. */
    private int getSpriteIndex(int seqIdx) {
        if (seqIdx < 4) return ROW1_INDICES[seqIdx];
        return ROW2_INDICES[seqIdx - 4];
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
        terrainCopy.dispose();
        inputManager.disconnectControllers();
    }
}
