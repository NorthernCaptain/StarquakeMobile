package northern.captain.starquake.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import northern.captain.starquake.audio.SoundManager;
import northern.captain.starquake.StarquakeGame;
import northern.captain.starquake.input.InputManager;
import northern.captain.starquake.world.Blob;
import northern.captain.starquake.world.transitions.TeleportTransition;

/**
 * Title/start screen with starfield, banner, walking BLOB on terrain,
 * core assembly display, and navigation buttons.
 */
public class TitleScreen implements Screen {
    private static final int VIEWPORT_W = 256;
    private static final int VIEWPORT_H = 168;

    // Star field
    private static final int STAR_COUNT = 150;
    private static final float STAR_SPEED = 0.3f;

    // Banner
    private static final float BANNER_H = 48f * 256f / 320f; // ~38px, aspect preserved
    private static final float BANNER_TARGET_Y = VIEWPORT_H - BANNER_H - 4;
    private static final float BANNER_SLIDE_TIME = 0.5f;

    // Terrain: 2 big platforms (142) side by side = 4 tiles wide × 2 tall = 128×48
    private static final int BP_INDEX = 65;
    private static final int BP_COUNT = 2;
    private static final float TERRAIN_W = BP_COUNT * 64;
    private static final float TERRAIN_X = (VIEWPORT_W - TERRAIN_W) / 2f;
    private static final float TERRAIN_Y = 34;
    private static final float PALETTE_HOLD_TIME = 3f;
    private static final float PALETTE_FADE_TIME = 0.5f;
    private static final float PALETTE_CYCLE = PALETTE_HOLD_TIME + PALETTE_FADE_TIME;
    private static final int[] PALETTES = {14, 19, 18, 16, 10, 6, 15, 20};

    // Blob walk
    private static final float BLOB_Y = TERRAIN_Y + 48;
    private static final float BLOB_LEFT = TERRAIN_X + 4;
    private static final float BLOB_RIGHT = TERRAIN_X + TERRAIN_W - Blob.SIZE - 4;
    private static final float BLOB_SPEED = Blob.WALK_SPEED;
    private static final float FRAME_DURATION = 0.08f;

    private static final int[] WALK_RIGHT_CYCLE = {0, 1, 2, 3, 2, 1};
    private static final int[] WALK_LEFT_CYCLE = {7, 8, 9, 10, 9, 8};

    // Play text
    private static final float PLAY_Y = TERRAIN_Y + 24 + 20;

    // Core grid
    private static final float CORE_X = 204;
    private static final float CORE_Y = 2;
    private static final int CORE_CELL = 16;

    // Focus system
    private enum Focus { PLAY, ICONS, EXIT }
    private Focus focus = Focus.PLAY;
    private int iconIndex = 0; // 0=leaderboard, 1=achievements, 2=settings
    private float focusTimer;

    // Icon buttons rendered in screen coords — sized as % of screen height
    private static final float ICON_SIZE_FRAC = 0.12f; // 12% of screen height
    private static final float ICON_PADDING_FRAC = 0.02f; // 2% gap
    private static final float ICON_Y_FRAC = 0.08f; // 8% from bottom
    private static final float EXIT_Y_FRAC = 0.02f; // 2% from bottom

    private final StarquakeGame game;
    private final SpriteBatch batch = new SpriteBatch();
    private final FitViewport gameViewport = new FitViewport(VIEWPORT_W, VIEWPORT_H);
    private final ScreenViewport screenViewport = new ScreenViewport();
    private final InputManager inputManager = new InputManager();

    // Starfield
    private final float[] starX = new float[STAR_COUNT];
    private final float[] starY = new float[STAR_COUNT];
    private final float[] starZ = new float[STAR_COUNT];
    private final float[] starR = new float[STAR_COUNT];
    private final float[] starG = new float[STAR_COUNT];
    private final float[] starB = new float[STAR_COUNT];
    private float screenW, screenH;
    private final Vector2 touchPos = new Vector2();

    // Animation state
    private float timer;
    private float bannerY;

    // Blob
    private float blobX;
    private boolean blobRight = true;
    private boolean blobTurning;
    private float blobWalkTimer;
    private float blobTurnTimer;

    private final ShaderProgram paletteShader;

    // Start game transition
    private TeleportTransition transition;
    private float transitionTimer;
    private FrameBuffer titleFbo;
    private GameScreen pendingGameScreen;

    // Exit delay (play death sound before quitting)
    private float exitTimer = -1;

    // Sprites
    private Array<TextureAtlas.AtlasRegion> blobFrames;
    // Big platform 142 quad tiles: tl, tr, bl, br
    private TextureRegion bpTL, bpTR, bpBL, bpBR;
    private TextureRegion[] coreIcons = new TextureRegion[9];
    private TextureRegion[] buttonIcons = new TextureRegion[4]; // leaderboard, achievements, settings, exit

    public TitleScreen(StarquakeGame game) {
        this.game = game;
        bannerY = VIEWPORT_H; // start off-top
        blobX = (BLOB_LEFT + BLOB_RIGHT) / 2f;

        // Own palette shader instance (shared one may have stale uniform state)
        ShaderProgram.pedantic = false;
        paletteShader = new ShaderProgram(
                Gdx.files.internal("shaders/palette.vert"),
                Gdx.files.internal("shaders/palette.frag"));
        if (!paletteShader.isCompiled())
            Gdx.app.error("TitleScreen", "Shader error:\n" + paletteShader.getLog());

        // Load sprites
        blobFrames = game.assets.spritesAtlas.findRegions("blob");
        com.badlogic.gdx.utils.JsonValue bp = game.assets.getBigPlatform(BP_INDEX);
        if (bp != null) {
            bpTL = game.assets.tileRegions.get(bp.getInt("tl"));
            bpTR = game.assets.tileRegions.get(bp.getInt("tr"));
            bpBL = game.assets.tileRegions.get(bp.getInt("bl"));
            bpBR = game.assets.tileRegions.get(bp.getInt("br"));
        }
        for (int i = 0; i < 9; i++) {
            coreIcons[i] = game.assets.itemsAtlas.findRegion("item", i);
        }
        buttonIcons[0] = game.assets.spritesAtlas.findRegion("leaderboards");
        buttonIcons[1] = game.assets.spritesAtlas.findRegion("achivements");
        buttonIcons[2] = game.assets.spritesAtlas.findRegion("settings");
        buttonIcons[3] = game.assets.spritesAtlas.findRegion("exit");

        // Init starfield
        screenW = Gdx.graphics.getWidth();
        screenH = Gdx.graphics.getHeight();
        for (int i = 0; i < STAR_COUNT; i++) {
            resetStar(i, true);
        }

        // Input
        Gdx.input.setInputProcessor(new InputMultiplexer(inputManager.getKeyboardListener()));
        inputManager.connectControllers();
    }

    private void resetStar(int i, boolean randomZ) {
        float cx = screenW / 2f;
        float cy = screenH / 2f;
        starX[i] = cx + MathUtils.random(-screenW * 0.1f, screenW * 0.1f);
        starY[i] = cy + MathUtils.random(-screenH * 0.1f, screenH * 0.1f);
        starZ[i] = randomZ ? MathUtils.random(0f, 1f) : MathUtils.random(0f, 0.1f);

        // Random star color
        int colorType = MathUtils.random(3);
        switch (colorType) {
            case 0: starR[i] = 1f;   starG[i] = 1f;   starB[i] = 1f;   break; // white
            case 1: starR[i] = 1f;   starG[i] = 0.95f; starB[i] = 0.6f; break; // yellow
            case 2: starR[i] = 1f;   starG[i] = 0.5f;  starB[i] = 0.4f; break; // red
            case 3: starR[i] = 0.5f; starG[i] = 0.7f;  starB[i] = 1f;   break; // blue
        }
    }

    @Override
    public void render(float delta) {
        delta = Math.min(delta, 1 / 15f);

        if (exitTimer >= 0) {
            exitTimer += delta;
            if (exitTimer >= 1.0f) Gdx.app.exit();
            return;
        }

        timer += delta;
        focusTimer += delta;

        // Clear
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (transition != null) {
            transitionTimer += delta;
            updateTransition(delta);
            if (transition == null) return; // switched to GameScreen

            // Stars fade out during first half of disintegrate, hidden after
            if (transition.getPhase() == TeleportTransition.Phase.DISINTEGRATE && transitionTimer < 0.5f) {
                float starFade = 1f - transitionTimer / 0.5f;
                updateStars(delta);
                screenViewport.apply();
                batch.setProjectionMatrix(screenViewport.getCamera().combined);
                batch.begin();
                renderStarsFaded(starFade);
                batch.end();
            }

            // Transition particles in game viewport
            gameViewport.apply();
            batch.setProjectionMatrix(gameViewport.getCamera().combined);
            batch.begin();
            transition.render(batch);
            batch.end();
            inputManager.update();
            return;
        }

        updateStars(delta);
        updateBanner(delta);
        updateBlob(delta);
        updateInput();

        // 1. Starfield + icon buttons in screen coords
        screenViewport.apply();
        batch.setProjectionMatrix(screenViewport.getCamera().combined);
        batch.begin();
        renderStars();
        renderButtonsScreen();
        batch.end();

        // 2. Game elements in 256x168 viewport
        gameViewport.apply();
        batch.setProjectionMatrix(gameViewport.getCamera().combined);
        batch.begin();
        renderBanner();
        renderTerrain();
        renderBlob();
        renderPlayText();
        renderCore();
        batch.end();

        inputManager.update();
    }

    private void updateTransition(float delta) {
        transition.update(delta);

        if (transition.needsTarget()) {
            // Create GameScreen and get its first room terrain
            pendingGameScreen = new GameScreen(game, 435);
            pendingGameScreen.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            // Render the room FBO
            TextureRegion roomTerrain = pendingGameScreen.getRoomTerrain();
            transition.setTarget(roomTerrain);
        }

        if (transition.isDone()) {
            transition = null;
            if (titleFbo != null) { titleFbo.dispose(); titleFbo = null; }
            game.setScreen(pendingGameScreen);
        }
    }

    // ---- Stars ----

    private void updateStars(float delta) {
        float cx = screenW / 2f;
        float cy = screenH / 2f;
        for (int i = 0; i < STAR_COUNT; i++) {
            starZ[i] += STAR_SPEED * delta;
            // Drift outward from center proportional to z
            float dx = starX[i] - cx;
            float dy = starY[i] - cy;
            float z2 = starZ[i] * starZ[i];
            starX[i] += dx * z2 * delta * 2f;
            starY[i] += dy * z2 * delta * 2f;

            if (starZ[i] > 1f || starX[i] < -10 || starX[i] > screenW + 10
                    || starY[i] < -10 || starY[i] > screenH + 10) {
                resetStar(i, false);
            }
        }
    }

    private void renderStars() {
        renderStarsFaded(1f);
    }

    private void renderStarsFaded(float fade) {
        TextureRegion pixel = game.assets.whitePixel;
        for (int i = 0; i < STAR_COUNT; i++) {
            float size = 1f + starZ[i] * 3f;
            float alpha = (0.3f + starZ[i] * 0.7f) * fade;
            batch.setColor(starR[i], starG[i], starB[i], alpha);
            batch.draw(pixel, starX[i], starY[i], size, size);
        }
        batch.setColor(Color.WHITE);
    }

    // ---- Banner ----

    private void updateBanner(float delta) {
        float t = Math.min(timer / BANNER_SLIDE_TIME, 1f);
        bannerY = VIEWPORT_H + (BANNER_TARGET_Y - VIEWPORT_H) * Interpolation.pow2Out.apply(t);
    }

    private void renderBanner() {
        if (game.assets.bannerScreen != null) {
            batch.draw(game.assets.bannerScreen, 0, bannerY, VIEWPORT_W, BANNER_H);
        }
    }

    // ---- Terrain ----

    private void renderTerrain() {
        if (bpTL == null) return;

        int numPal = PALETTES.length;
        float cyclePos = timer % (PALETTE_CYCLE * numPal);
        int palSlot = (int) (cyclePos / PALETTE_CYCLE);
        float slotTime = cyclePos - palSlot * PALETTE_CYCLE;

        float curPalette = PALETTES[palSlot % numPal];
        float nextPalette = PALETTES[(palSlot + 1) % numPal];

        // During hold: draw current only
        // During fade: draw current at full, overlay next with increasing alpha
        drawTerrainWithPalette(curPalette);

        if (slotTime > PALETTE_HOLD_TIME) {
            float fadeAlpha = (slotTime - PALETTE_HOLD_TIME) / PALETTE_FADE_TIME;
            batch.setColor(1, 1, 1, fadeAlpha);
            drawTerrainWithPalette(nextPalette);
            batch.setColor(Color.WHITE);
        }
    }

    private void drawTerrainWithPalette(float paletteRow) {
        // End current batch, switch shader, set up textures from scratch
        batch.end();

        batch.setShader(paletteShader);
        game.assets.paletteTexture.bind(1);
        Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);

        batch.begin();
        paletteShader.setUniformi("u_palette", 1);
        paletteShader.setUniformf("u_paletteRow", paletteRow);

        for (int bp = 0; bp < BP_COUNT; bp++) {
            float bx = TERRAIN_X + bp * 64;
            if (bpTL != null) batch.draw(bpTL, bx,      TERRAIN_Y + 24, 32, 24);
            if (bpTR != null) batch.draw(bpTR, bx + 32, TERRAIN_Y + 24, 32, 24);
            if (bpBL != null) batch.draw(bpBL, bx,      TERRAIN_Y,      32, 24);
            if (bpBR != null) batch.draw(bpBR, bx + 32, TERRAIN_Y,      32, 24);
        }

        batch.end();
        batch.setShader(null);
        batch.begin();
    }

    // ---- Blob ----

    private void updateBlob(float delta) {
        if (blobTurning) {
            blobTurnTimer += delta;
            if (blobTurnTimer >= FRAME_DURATION * 3) {
                blobTurning = false;
                blobRight = !blobRight;
            }
            return;
        }

        blobWalkTimer += delta;
        float dx = BLOB_SPEED * delta * (blobRight ? 1 : -1);
        blobX += dx;

        if (blobX >= BLOB_RIGHT) {
            blobX = BLOB_RIGHT;
            blobTurning = true;
            blobTurnTimer = 0;
        } else if (blobX <= BLOB_LEFT) {
            blobX = BLOB_LEFT;
            blobTurning = true;
            blobTurnTimer = 0;
        }
    }

    private void renderBlob() {
        TextureRegion frame;
        if (blobTurning) {
            int turnFrame = Math.min((int) (blobTurnTimer / FRAME_DURATION), 2);
            if (blobRight) {
                frame = blobFrames.get(4 + turnFrame); // right→left: 4,5,6
            } else {
                frame = blobFrames.get(6 - turnFrame); // left→right: 6,5,4
            }
        } else if (blobRight) {
            int step = (int) (blobWalkTimer / FRAME_DURATION);
            frame = blobFrames.get(WALK_RIGHT_CYCLE[step % WALK_RIGHT_CYCLE.length]);
        } else {
            int step = (int) (blobWalkTimer / FRAME_DURATION);
            frame = blobFrames.get(WALK_LEFT_CYCLE[step % WALK_LEFT_CYCLE.length]);
        }
        batch.draw(frame, blobX, BLOB_Y, Blob.SIZE, Blob.SIZE);
    }

    // ---- Play text ----

    private void renderPlayText() {
        BitmapFont font = game.assets.font;
        float alpha = (focus == Focus.PLAY)
                ? 0.5f + 0.5f * MathUtils.sin(focusTimer * MathUtils.PI * 3)
                : 1f;
        font.setColor(1, 1, 1, alpha);
        // Center "PLAY" text
        float textW = 4 * 8; // 4 chars * 8px
        font.draw(batch, "PLAY", (VIEWPORT_W - textW) / 2f, PLAY_Y);
        font.setColor(Color.WHITE);
    }

    // ---- Core grid ----

    private void renderCore() {
        for (int i = 0; i < 9; i++) {
            if (coreIcons[i] == null) continue;
            int col = i % 3;
            int row = i / 3;
            float x = CORE_X + col * CORE_CELL;
            float y = CORE_Y + (2 - row) * CORE_CELL; // Y-up
            batch.draw(coreIcons[i], x, y, CORE_CELL, CORE_CELL);
        }
    }

    // ---- Buttons (rendered in screen coords) ----

    private void renderButtonsScreen() {
        float iconSize = screenH * ICON_SIZE_FRAC;
        float iconPad = screenH * ICON_PADDING_FRAC;
        float iconY = screenH * ICON_Y_FRAC;
        float totalW = 3 * iconSize + 2 * iconPad;
        float startX = (screenW - totalW) / 2f;

        for (int i = 0; i < 3; i++) {
            if (buttonIcons[i] == null) continue;
            float alpha = (focus == Focus.ICONS && iconIndex == i)
                    ? 0.5f + 0.5f * MathUtils.sin(focusTimer * MathUtils.PI * 3)
                    : 1f;
            float ix = startX + i * (iconSize + iconPad);
            batch.setColor(1, 1, 1, alpha);
            batch.draw(buttonIcons[i], ix, iconY, iconSize, iconSize);
        }

        // Exit button — aligned to bottom-left of game viewport area
        if (buttonIcons[3] != null) {
            float exitH = iconSize * 0.6f;
            float exitW = exitH * 2f;
            float exitX = gameViewport.getScreenX();
            float exitY = gameViewport.getScreenY();
            float alpha = (focus == Focus.EXIT)
                    ? 0.5f + 0.5f * MathUtils.sin(focusTimer * MathUtils.PI * 3)
                    : 1f;
            batch.setColor(1, 1, 1, alpha);
            batch.draw(buttonIcons[3], exitX, exitY, exitW, exitH);
        }
        batch.setColor(Color.WHITE);
    }

    /** Get icon button rect in screen coords for hit testing. */
    private boolean hitIconButton(int index, float tx, float ty) {
        float iconSize = screenH * ICON_SIZE_FRAC;
        float iconPad = screenH * ICON_PADDING_FRAC;
        float iconY = screenH * ICON_Y_FRAC;
        float totalW = 3 * iconSize + 2 * iconPad;
        float startX = (screenW - totalW) / 2f;
        float ix = startX + index * (iconSize + iconPad);
        return tx >= ix && tx <= ix + iconSize && ty >= iconY && ty <= iconY + iconSize;
    }

    private boolean hitExitButton(float tx, float ty) {
        float iconSize = screenH * ICON_SIZE_FRAC;
        float exitH = iconSize * 0.6f;
        float exitW = exitH * 2f;
        float exitX = gameViewport.getScreenX();
        float exitY = gameViewport.getScreenY();
        return tx >= exitX && tx <= exitX + exitW && ty >= exitY && ty <= exitY + exitH;
    }

    // ---- Input ----

    private void updateInput() {
        // Controller/keyboard navigation
        if (inputManager.isJustPressed(InputManager.Action.DOWN)) {
            if (focus == Focus.PLAY) { focus = Focus.ICONS; focusTimer = 0; }
            else if (focus == Focus.ICONS) { focus = Focus.EXIT; focusTimer = 0; }
        }
        if (inputManager.isJustPressed(InputManager.Action.UP)) {
            if (focus == Focus.EXIT) { focus = Focus.ICONS; focusTimer = 0; }
            else if (focus == Focus.ICONS) { focus = Focus.PLAY; focusTimer = 0; }
        }
        if (inputManager.isJustPressed(InputManager.Action.LEFT) && focus == Focus.ICONS) {
            iconIndex = (iconIndex - 1 + 3) % 3; focusTimer = 0;
        }
        if (inputManager.isJustPressed(InputManager.Action.RIGHT) && focus == Focus.ICONS) {
            iconIndex = (iconIndex + 1) % 3; focusTimer = 0;
        }
        if (inputManager.isJustPressed(InputManager.Action.ACTION_A)) {
            activateFocus();
        }

        // Touch input
        if (Gdx.input.justTouched()) {
            // Check play area in game viewport coords
            touchPos.set(Gdx.input.getX(), Gdx.input.getY());
            gameViewport.unproject(touchPos);
            if (touchPos.x >= TERRAIN_X && touchPos.x <= TERRAIN_X + TERRAIN_W
                    && touchPos.y >= TERRAIN_Y + 24 && touchPos.y <= PLAY_Y + 10) {
                startGame();
                return;
            }

            // Check icon buttons in screen coords
            float sx = Gdx.input.getX();
            float sy = Gdx.graphics.getHeight() - Gdx.input.getY(); // flip Y to bottom-left
            for (int i = 0; i < 3; i++) {
                if (hitIconButton(i, sx, sy)) {
                    focus = Focus.ICONS;
                    iconIndex = i;
                    focusTimer = 0;
                    activateFocus();
                    return;
                }
            }
            if (hitExitButton(sx, sy)) {
                triggerExit();
            }
        }
    }

    private void activateFocus() {
        switch (focus) {
            case PLAY:
                startGame();
                break;
            case ICONS:
                // TODO: leaderboard, achievements, settings
                break;
            case EXIT:
                triggerExit();
                break;
        }
    }

    private void triggerExit() {
        if (exitTimer >= 0) return;
        SoundManager.play(SoundManager.SoundType.DEATH);
        exitTimer = 0;
    }

    private void startGame() {
        if (transition != null) return; // already starting

        // Render game elements (no stars) to an FBO
        titleFbo = new FrameBuffer(Pixmap.Format.RGBA8888, VIEWPORT_W, VIEWPORT_H, false);
        titleFbo.getColorBufferTexture().setFilter(
                Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        titleFbo.begin();
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        OrthographicCamera fboCam = new OrthographicCamera();
        fboCam.setToOrtho(false, VIEWPORT_W, VIEWPORT_H);
        batch.setProjectionMatrix(fboCam.combined);
        batch.begin();
        renderBanner();
        renderTerrain();
        renderBlob();
        renderPlayText();
        renderCore();
        batch.end();
        titleFbo.end();

        // Start disintegrate from the captured title screen
        TextureRegion titleRegion = new TextureRegion(titleFbo.getColorBufferTexture());
        titleRegion.flip(false, true);
        transition = new TeleportTransition();
        transition.start(titleRegion);
        SoundManager.play(SoundManager.SoundType.TELEPORT_ENTER);
    }

    @Override
    public void resize(int width, int height) {
        screenW = width;
        screenH = height;
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
        paletteShader.dispose();
        if (titleFbo != null) titleFbo.dispose();
        inputManager.disconnectControllers();
    }
}
