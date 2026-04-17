package northern.captain.starquake.hud;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import northern.captain.starquake.Assets;
import northern.captain.starquake.world.CoreAssembly;
import northern.captain.starquake.world.GameState;
import northern.captain.starquake.world.Inventory;
import northern.captain.starquake.world.ScoreManager;
import northern.captain.starquake.world.Room;
import northern.captain.starquake.world.items.ItemType;
import northern.captain.starquake.world.objects.CoreTrigger;

/**
 * Renders the HUD bar at the top of the screen (256×24).
 *
 * Layout (y=144 to y=168 in viewport, visually at top):
 * - Left: lives count + score
 * - Center: 3 stacked progress bars (health, platforms, laser)
 * - Right: collected item icons (TODO)
 */
public class Hud {
    private static final float HUD_Y = Room.HEIGHT; // 144
    private static final float HUD_H = 24;
    private static final float HUD_W = Room.WIDTH;  // 256

    // Bar dimensions — icons are 17px wide + 2px gap, positioned left of bars
    private static final float BAR_X = 90;
    private static final float BAR_W = 60;
    private static final float BAR_H = 6;
    private static final float BAR_GAP = 1;

    // Colors
    private static final Color BG_COLOR = new Color(0.08f, 0.08f, 0.12f, 0.9f);
    private static final Color HEALTH_COLOR = new Color(0.2f, 0.9f, 0.2f, 1);
    private static final Color PLATFORM_COLOR = new Color(0.3f, 0.5f, 1f, 1);
    private static final Color LASER_COLOR = new Color(1f, 0.9f, 0.2f, 1);
    private static final Color BAR_BG_COLOR = new Color(0.2f, 0.2f, 0.25f, 1);

    private static final int ICON_W = 17;
    private static final int ICON_H = 7;

    private final TextureRegion pixel;
    private final BitmapFont font;
    private final Inventory inventory;
    private final StringBuilder sb = new StringBuilder(16);
    private int debugRoomIndex;
    private int displayScore;
    private int scoreFrom;
    private int scoreTo;
    private float scoreTimer;
    private static final float SCORE_ANIM_TIME = 0.4f;

    public void setDebugRoomIndex(int index) { debugRoomIndex = index; }

    // Vital icons: health, platforms, laser (top to bottom in source)
    private final TextureRegion iconHealth;
    private final TextureRegion iconPlatforms;
    private final TextureRegion iconLaser;
    private final TextureRegion iconHeart;
    private final TextureRegion iconInventory;
    private final TextureRegion iconPause;

    // Cached item icon regions indexed by sprite index (0-34)
    private final TextureRegion[] itemIcons = new TextureRegion[35];

    /** Set to true when pause icon is tapped (polled by GameScreen). */
    private boolean pauseRequested;

    public Hud(Assets assets, Inventory inventory) {
        this.pixel = assets.whitePixel;
        this.font = assets.font;
        this.inventory = inventory;

        TextureRegion vitals = assets.spritesAtlas.findRegion("hud_vitals");
        if (vitals != null) {
            iconHealth = new TextureRegion(vitals, 0, 0, ICON_W, ICON_H);
            iconPlatforms = new TextureRegion(vitals, 0, ICON_H, ICON_W, ICON_H);
            iconLaser = new TextureRegion(vitals, 0, ICON_H * 2, ICON_W, ICON_H + 1);
        } else {
            iconHealth = iconPlatforms = iconLaser = null;
        }
        iconHeart = assets.spritesAtlas.findRegion("hud_heart");
        iconInventory = assets.spritesAtlas.findRegion("inventory_icon");
        iconPause = assets.spritesAtlas.findRegion("pause");

        for (int i = 0; i < itemIcons.length; i++) {
            itemIcons[i] = assets.itemsAtlas.findRegion("item", i);
        }
    }

    public void render(SpriteBatch batch, GameState state, float delta) {
        // Background bar
        batch.setColor(BG_COLOR);
        batch.draw(pixel, 0, HUD_Y, HUD_W, HUD_H);
        batch.setColor(Color.WHITE);

        // Left section: heart icon + lives count + score
        if (iconHeart != null) {
            batch.setColor(Color.WHITE);
            batch.draw(iconHeart, 2, HUD_Y + HUD_H - 9, 8, 8);
        }
        sb.setLength(0);
        int lives = state.getLives();
        sb.append((char) ('0' + lives / 10));
        sb.append((char) ('0' + lives % 10));
        font.draw(batch, sb, 12, HUD_Y + HUD_H - 1);

        // Debug: room number next to lives
        sb.setLength(0);
        sb.append('R');
        if (debugRoomIndex < 100) sb.append('0');
        if (debugRoomIndex < 10) sb.append('0');
        sb.append(debugRoomIndex);
        font.draw(batch, sb, 34, HUD_Y + HUD_H - 1);

        // Exploration %
        ScoreManager scoreMgr = ScoreManager.get();
        if (scoreMgr != null) {
            sb.setLength(0);
            int pct = scoreMgr.getExplorationPercent();
            if (pct < 10) sb.append(' ');
            sb.append(pct);
            sb.append('%');
            font.draw(batch, sb, 68, HUD_Y + HUD_H - 1);
        }

        // Animated score display
        int realScore = state.getScore();
        if (realScore != scoreTo) {
            scoreFrom = displayScore;
            scoreTo = realScore;
            scoreTimer = 0;
        }
        if (displayScore != scoreTo) {
            scoreTimer += delta;
            float t = Math.min(scoreTimer / SCORE_ANIM_TIME, 1f);
            displayScore = scoreFrom + (int) ((scoreTo - scoreFrom) * t);
            if (t >= 1f) displayScore = scoreTo;
        }
        sb.setLength(0);
        for (int i = 7; i >= 0; i--) {
            sb.append((char) ('0' + (displayScore / (int) Math.pow(10, i)) % 10));
        }
        font.draw(batch, sb, 2, HUD_Y + HUD_H - 11);

        // Center: 3 progress bars stacked with icons
        float barBaseY = HUD_Y + 2;
        float iconX = BAR_X - ICON_W - 2;
        float barY2 = barBaseY + (BAR_H + BAR_GAP) * 2;
        float barY1 = barBaseY + (BAR_H + BAR_GAP);
        float barY0 = barBaseY;

        drawIcon(batch, iconHealth, iconX, barY2 - 1);
        drawBar(batch, BAR_X, barY2, BAR_W, BAR_H, state.getHealthFraction(), HEALTH_COLOR);
        drawIcon(batch, iconPlatforms, iconX, barY1 - 1);
        drawBar(batch, BAR_X, barY1, BAR_W, BAR_H, state.getPlatformsFraction(), PLATFORM_COLOR);
        drawIcon(batch, iconLaser, iconX, barY0 - 1);
        drawBar(batch, BAR_X, barY0, BAR_W, BAR_H, state.getLaserFraction(), LASER_COLOR);

        // Pause icon (far right)
        if (iconPause != null) {
            batch.setColor(Color.WHITE);
            batch.draw(iconPause, HUD_W - 18, HUD_Y + (HUD_H - 16) / 2f, 16, 16);
        }

        // Inventory icon + 4 inventory slots (shifted 16px left for pause icon)
        float slotX = HUD_W - 4 * 17 - 2 - 16;
        if (iconInventory != null) {
            batch.setColor(Color.WHITE);
            batch.draw(iconInventory, slotX - 18, HUD_Y + (HUD_H - 16) / 2f, 16, 16);
        }
        float slotY = HUD_Y + (HUD_H - 16) / 2f;
        CoreAssembly coreAnim = CoreTrigger.getCoreAssembly();
        int hiddenSlot = (coreAnim != null) ? coreAnim.getDeliveringSlot() : -1;
        for (int i = 0; i < Inventory.MAX_SLOTS; i++) {
            batch.setColor(BAR_BG_COLOR);
            batch.draw(pixel, slotX, slotY, 16, 16);
            if (i != hiddenSlot) {
                ItemType item = inventory.getSlot(i);
                if (item != null) {
                    TextureRegion icon = itemIcons[item.spriteIndex];
                    if (icon != null) {
                        batch.setColor(Color.WHITE);
                        batch.draw(icon, slotX, slotY, 16, 16);
                    }
                }
            }
            slotX += 17;
        }

        // Red flash overlay on inventory when core assembly has no matches
        CoreAssembly core = CoreTrigger.getCoreAssembly();
        if (core != null && core.isInventoryFlashRed()) {
            batch.setColor(0.8f, 0.1f, 0.1f, 0.6f);
            float flashX = HUD_W - 4 * 17 - 4 - 16;
            float flashY = HUD_Y + (HUD_H - 20) / 2f;
            batch.draw(pixel, flashX, flashY, 4 * 17 + 4, 20);
        }

        batch.setColor(Color.WHITE);
    }

    /**
     * Check if a viewport-space tap hit the pause icon area.
     * Call with coords already unprojected to game viewport.
     */
    public void checkPauseTap(float vx, float vy) {
        if (vx >= HUD_W - 20 && vy >= HUD_Y && vy <= HUD_Y + HUD_H) {
            pauseRequested = true;
        }
    }

    public boolean isPauseRequested() {
        boolean r = pauseRequested;
        pauseRequested = false;
        return r;
    }

    private void drawIcon(SpriteBatch batch, TextureRegion icon, float x, float y) {
        if (icon != null) {
            batch.setColor(Color.WHITE);
            batch.draw(icon, x, y, ICON_W, ICON_H);
        }
    }

    private void drawBar(SpriteBatch batch, float x, float y, float w, float h,
                          float fraction, Color fillColor) {
        // Background
        batch.setColor(BAR_BG_COLOR);
        batch.draw(pixel, x, y, w, h);

        // Fill
        if (fraction > 0) {
            batch.setColor(fillColor);
            batch.draw(pixel, x, y, w * fraction, h);
        }

        // Border
        batch.setColor(0.4f, 0.4f, 0.4f, 1f);
        batch.draw(pixel, x, y, w, 1);
        batch.draw(pixel, x, y + h - 1, w, 1);
        batch.draw(pixel, x, y, 1, h);
        batch.draw(pixel, x + w - 1, y, 1, h);

        batch.setColor(Color.WHITE);
    }
}
