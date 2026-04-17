package northern.captain.starquake.hud;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import northern.captain.starquake.Assets;
import northern.captain.starquake.audio.SoundManager;
import northern.captain.starquake.input.InputManager;

/**
 * Pause overlay: fade out game, slide panel from top with banner + Resume/Quit.
 * Resume slides panel up and fades game back in with 1s gameplay delay.
 */
public class PauseOverlay implements Overlay {
    private static final float VIEWPORT_W = 256;
    private static final float VIEWPORT_H = 168;

    private static final float FADE_TIME = 0.3f;
    private static final float SLIDE_TIME = 0.3f;

    // Banner
    private static final float BANNER_H = 48f * 256f / 320f; // ~38px
    private static final float BANNER_Y = VIEWPORT_H - BANNER_H - 4;

    // Buttons
    private static final float BTN_W = 80;
    private static final float BTN_H = 16;
    private static final float BTN_GAP = 12;
    private static final float BTN_TOP_Y = BANNER_Y - 42;

    private static final float RESUME_DELAY = 1.0f;

    // Reusable colors for button rendering
    private static final Color BTN_SEL_COLOR = new Color(0.3f, 0.3f, 0.5f, 0.9f);
    private static final Color BTN_COLOR = new Color(0.15f, 0.15f, 0.25f, 0.8f);
    private static final Color BORDER_COLOR = new Color(0.4f, 0.4f, 0.4f, 1f);

    enum Phase { FADE_OUT, SLIDE_IN, ACTIVE, SLIDE_OUT, FADE_IN, RESUME_WAIT, DONE }

    private Phase phase = Phase.FADE_OUT;
    private float timer;
    private int selected; // 0 = resume, 1 = quit
    private boolean quit;

    private final TextureRegion banner;
    private final TextureRegion pixel;
    private final BitmapFont font;

    public PauseOverlay(Assets assets) {
        this.banner = assets.bannerScreen;
        this.pixel = assets.whitePixel;
        this.font = assets.font;
    }

    public boolean isQuit() {
        return quit;
    }

    @Override
    public void update(float delta, InputManager input) {
        timer += delta;
        switch (phase) {
            case FADE_OUT:
                if (timer >= FADE_TIME) {
                    phase = Phase.SLIDE_IN;
                    timer = 0;
                }
                break;
            case SLIDE_IN:
                if (timer >= SLIDE_TIME) {
                    phase = Phase.ACTIVE;
                    timer = 0;
                }
                break;
            case ACTIVE:
                if (input.isJustPressed(InputManager.Action.UP)) {
                    selected = Math.max(0, selected - 1);
                    SoundManager.play(SoundManager.SoundType.UI_TEXT);
                }
                if (input.isJustPressed(InputManager.Action.DOWN)) {
                    selected = Math.min(1, selected + 1);
                    SoundManager.play(SoundManager.SoundType.UI_TEXT);
                }
                if (input.isJustPressed(InputManager.Action.ACTION_B)) {
                    // Back/ESC always resumes
                    SoundManager.play(SoundManager.SoundType.UI_TEXT);
                    phase = Phase.SLIDE_OUT;
                    timer = 0;
                } else if (input.isJustPressed(InputManager.Action.ACTION_A)) {
                    SoundManager.play(SoundManager.SoundType.UI_TEXT);
                    if (selected == 1) quit = true;
                    phase = Phase.SLIDE_OUT;
                    timer = 0;
                }
                break;
            case SLIDE_OUT:
                if (timer >= SLIDE_TIME) {
                    if (quit) {
                        phase = Phase.DONE;
                    } else {
                        phase = Phase.FADE_IN;
                        timer = 0;
                    }
                }
                break;
            case FADE_IN:
                if (timer >= FADE_TIME) {
                    phase = Phase.RESUME_WAIT;
                    timer = 0;
                }
                break;
            case RESUME_WAIT:
                if (timer >= RESUME_DELAY) {
                    phase = Phase.DONE;
                }
                break;
            case DONE:
                break;
        }
    }

    @Override
    public void render(SpriteBatch batch) {
        switch (phase) {
            case FADE_OUT: {
                float a = Math.min(timer / FADE_TIME, 1f) * 0.7f;
                batch.setColor(0, 0, 0, a);
                batch.draw(pixel, 0, 0, VIEWPORT_W, VIEWPORT_H);
                break;
            }
            case SLIDE_IN: {
                // Dimmed background
                batch.setColor(0, 0, 0, 0.7f);
                batch.draw(pixel, 0, 0, VIEWPORT_W, VIEWPORT_H);
                // Panel slides from top
                float t = Interpolation.pow2Out.apply(Math.min(timer / SLIDE_TIME, 1f));
                renderPanel(batch, VIEWPORT_H * (1f - t));
                break;
            }
            case ACTIVE: {
                batch.setColor(0, 0, 0, 0.7f);
                batch.draw(pixel, 0, 0, VIEWPORT_W, VIEWPORT_H);
                renderPanel(batch, 0);
                break;
            }
            case SLIDE_OUT: {
                batch.setColor(0, 0, 0, 0.7f);
                batch.draw(pixel, 0, 0, VIEWPORT_W, VIEWPORT_H);
                float t = Interpolation.pow2In.apply(Math.min(timer / SLIDE_TIME, 1f));
                renderPanel(batch, VIEWPORT_H * t);
                break;
            }
            case FADE_IN: {
                float a = (1f - Math.min(timer / FADE_TIME, 1f)) * 0.7f;
                batch.setColor(0, 0, 0, a);
                batch.draw(pixel, 0, 0, VIEWPORT_W, VIEWPORT_H);
                break;
            }
            case RESUME_WAIT:
            case DONE:
                break;
        }
        batch.setColor(Color.WHITE);
    }

    private void renderPanel(SpriteBatch batch, float offsetY) {
        // Banner
        if (banner != null) {
            batch.setColor(Color.WHITE);
            batch.draw(banner, 0, BANNER_Y + offsetY, VIEWPORT_W, BANNER_H);
        }

        // Buttons
        String[] labels = {"RESUME", "QUIT"};
        for (int i = 0; i < 2; i++) {
            float bx = (VIEWPORT_W - BTN_W) / 2f;
            float by = BTN_TOP_Y - i * (BTN_H + BTN_GAP) + offsetY;

            // Button background
            boolean sel = (i == selected);
            batch.setColor(sel ? BTN_SEL_COLOR : BTN_COLOR);
            batch.draw(pixel, bx, by, BTN_W, BTN_H);

            // Border
            Color borderColor = sel ? Color.YELLOW : BORDER_COLOR;
            batch.setColor(borderColor);
            batch.draw(pixel, bx, by, BTN_W, 1);
            batch.draw(pixel, bx, by + BTN_H - 1, BTN_W, 1);
            batch.draw(pixel, bx, by, 1, BTN_H);
            batch.draw(pixel, bx + BTN_W - 1, by, 1, BTN_H);

            // Label centered
            font.setColor(sel ? Color.YELLOW : Color.WHITE);
            float textW = labels[i].length() * 8;
            font.draw(batch, labels[i], bx + (BTN_W - textW) / 2f, by + BTN_H - 4);
        }
        font.setColor(Color.WHITE);
        batch.setColor(Color.WHITE);
    }

    /** Check if a viewport-space tap hit one of the buttons. */
    public void checkTap(float vx, float vy) {
        if (phase != Phase.ACTIVE) return;
        for (int i = 0; i < 2; i++) {
            float bx = (VIEWPORT_W - BTN_W) / 2f;
            float by = BTN_TOP_Y - i * (BTN_H + BTN_GAP);
            if (vx >= bx && vx <= bx + BTN_W && vy >= by && vy <= by + BTN_H) {
                selected = i;
                SoundManager.play(SoundManager.SoundType.UI_TEXT);
                if (i == 1) quit = true;
                phase = Phase.SLIDE_OUT;
                timer = 0;
            }
        }
    }

    @Override
    public boolean isDone() {
        return phase == Phase.DONE;
    }
}
