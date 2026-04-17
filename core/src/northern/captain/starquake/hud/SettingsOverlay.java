package northern.captain.starquake.hud;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import northern.captain.starquake.AppSettings;
import northern.captain.starquake.Assets;
import northern.captain.starquake.audio.MusicManager;
import northern.captain.starquake.audio.SoundManager;
import northern.captain.starquake.input.InputManager;

/**
 * Settings overlay: slides up from bottom over infoscreen background.
 * 3 rows: Music ON/OFF, Sound ON/OFF, D-pad RIGHT/LEFT.
 * D-pad up/down selects row, left/right toggles value, fire exits.
 * Touch: tap value area to toggle, tap outside black area to close.
 * All changes persist immediately via AppSettings.
 */
public class SettingsOverlay implements Overlay {
    private static final float VIEWPORT_W = 256;
    private static final float VIEWPORT_H = 168;
    private static final float SLIDE_TIME = 0.3f;

    // Black text area in infoscreen (mapped from 320x200 source)
    private static final float AREA_X = 40;
    private static final float AREA_Y = 34;
    private static final float AREA_W = 176;
    private static final float AREA_H = 100;

    // Layout inside black area
    private static final float TEXT_X = AREA_X + 8;
    private static final float TITLE_Y = AREA_Y + AREA_H - 12;
    private static final float ROW_START_Y = TITLE_Y - 22;
    private static final float ROW_H = 16;
    private static final float VALUE_X = AREA_X + AREA_W - 48;

    private static final int ROW_COUNT = 3;

    private static final Color GREEN = new Color(0.2f, 0.9f, 0.2f, 1f);
    private static final Color GRAY = new Color(0.5f, 0.5f, 0.5f, 1f);
    private static final Color YELLOW = new Color(1f, 0.9f, 0.2f, 1f);
    private static final Color HIGHLIGHT_BG = new Color(0.2f, 0.2f, 0.35f, 0.6f);

    private static final String[] LABELS = {"MUSIC", "SOUND", "D-PAD"};

    enum Phase { SLIDE_IN, ACTIVE, SLIDE_OUT, DONE }

    private Phase phase = Phase.SLIDE_IN;
    private float timer;
    private int selected; // 0=music, 1=sound, 2=dpad

    private final TextureRegion background;
    private final TextureRegion pixel;
    private final BitmapFont font;

    public SettingsOverlay(Assets assets) {
        this.background = assets.infoScreen;
        this.pixel = assets.whitePixel;
        this.font = assets.font;
    }

    @Override
    public void update(float delta, InputManager input) {
        timer += delta;
        switch (phase) {
            case SLIDE_IN:
                if (timer >= SLIDE_TIME) {
                    phase = Phase.ACTIVE;
                    timer = 0;
                }
                break;
            case ACTIVE:
                if (input.isJustPressed(InputManager.Action.UP)) {
                    selected = (selected - 1 + ROW_COUNT) % ROW_COUNT;
                    SoundManager.play(SoundManager.SoundType.UI_TEXT);
                }
                if (input.isJustPressed(InputManager.Action.DOWN)) {
                    selected = (selected + 1) % ROW_COUNT;
                    SoundManager.play(SoundManager.SoundType.UI_TEXT);
                }
                if (input.isJustPressed(InputManager.Action.LEFT)
                        || input.isJustPressed(InputManager.Action.RIGHT)) {
                    toggleSelected();
                }
                if (input.isJustPressed(InputManager.Action.ACTION_A)
                        || input.isJustPressed(InputManager.Action.ACTION_B)) {
                    SoundManager.play(SoundManager.SoundType.UI_TEXT);
                    phase = Phase.SLIDE_OUT;
                    timer = 0;
                }
                break;
            case SLIDE_OUT:
                if (timer >= SLIDE_TIME) {
                    phase = Phase.DONE;
                }
                break;
            case DONE:
                break;
        }
    }

    private void toggleSelected() {
        AppSettings settings = AppSettings.get();
        SoundManager.play(SoundManager.SoundType.UI_TEXT);
        switch (selected) {
            case 0:
                boolean musicOn = !settings.isMusicEnabled();
                settings.setMusicEnabled(musicOn);
                if (MusicManager.get() != null) MusicManager.get().setEnabled(musicOn);
                break;
            case 1:
                boolean soundOn = !settings.isSoundEnabled();
                settings.setSoundEnabled(soundOn);
                if (SoundManager.get() != null) SoundManager.get().setEnabled(soundOn);
                break;
            case 2:
                boolean dpadRight = !settings.isDpadRight();
                settings.setDpadRight(dpadRight);
                break;
        }
    }

    @Override
    public void render(SpriteBatch batch) {
        float offsetY;
        switch (phase) {
            case SLIDE_IN:
                offsetY = -VIEWPORT_H * (1f - Interpolation.pow2Out.apply(Math.min(timer / SLIDE_TIME, 1f)));
                break;
            case SLIDE_OUT:
                offsetY = -VIEWPORT_H * Interpolation.pow2In.apply(Math.min(timer / SLIDE_TIME, 1f));
                break;
            default:
                offsetY = 0;
                break;
        }

        // Background
        if (background != null) {
            batch.setColor(Color.WHITE);
            batch.draw(background, 0, offsetY, VIEWPORT_W, VIEWPORT_H);
        }

        // Title
        font.setColor(Color.WHITE);
        font.draw(batch, "SETTINGS", TEXT_X, TITLE_Y + offsetY);

        // Rows
        AppSettings settings = AppSettings.get();
        boolean[] on = {settings.isMusicEnabled(), settings.isSoundEnabled(), settings.isDpadRight()};
        String[] values = {
                on[0] ? "ON" : "OFF",
                on[1] ? "ON" : "OFF",
                on[2] ? "RIGHT" : "LEFT"
        };

        for (int i = 0; i < ROW_COUNT; i++) {
            float rowY = ROW_START_Y - i * ROW_H + offsetY;

            // Highlight selected row
            if (i == selected && phase == Phase.ACTIVE) {
                batch.setColor(HIGHLIGHT_BG);
                batch.draw(pixel, AREA_X + 2, rowY - 11, AREA_W - 4, ROW_H);
            }

            // Label
            font.setColor(i == selected ? Color.YELLOW : Color.WHITE);
            font.draw(batch, LABELS[i], TEXT_X, rowY);

            // Value color: music/sound = green/gray, dpad = green/yellow
            Color vc;
            if (i < 2) {
                vc = on[i] ? GREEN : GRAY;
            } else {
                vc = on[i] ? GREEN : YELLOW;
            }
            font.setColor(vc);
            font.draw(batch, values[i], VALUE_X, rowY);
        }

        font.setColor(Color.WHITE);
        batch.setColor(Color.WHITE);
    }

    /** Check if a viewport-space tap hit a value or outside the black area. */
    public void checkTap(float vx, float vy) {
        if (phase != Phase.ACTIVE) return;

        // Tap outside black area → close
        if (vx < AREA_X || vx > AREA_X + AREA_W || vy < AREA_Y || vy > AREA_Y + AREA_H) {
            SoundManager.play(SoundManager.SoundType.UI_TEXT);
            phase = Phase.SLIDE_OUT;
            timer = 0;
            return;
        }

        // Check if tap is on a value area (right side of a row)
        if (vx >= VALUE_X - 4) {
            for (int i = 0; i < ROW_COUNT; i++) {
                float rowTop = ROW_START_Y - i * ROW_H + 2;
                float rowBot = rowTop - ROW_H;
                if (vy <= rowTop && vy >= rowBot) {
                    selected = i;
                    toggleSelected();
                    return;
                }
            }
        }
    }

    @Override
    public boolean isDone() {
        return phase == Phase.DONE;
    }
}
