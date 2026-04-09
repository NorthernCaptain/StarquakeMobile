package northern.captain.starquake.hud;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import northern.captain.starquake.Assets;
import northern.captain.starquake.world.CoreAssembly;
import northern.captain.starquake.world.GameState;
import northern.captain.starquake.world.Inventory;
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

    // Vital icons: health, platforms, laser (top to bottom in source)
    private final TextureRegion iconHealth;
    private final TextureRegion iconPlatforms;
    private final TextureRegion iconLaser;
    private final TextureRegion iconHeart;

    // Cached item icon regions indexed by sprite index (0-34)
    private final TextureRegion[] itemIcons = new TextureRegion[35];

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

        for (int i = 0; i < itemIcons.length; i++) {
            itemIcons[i] = assets.itemsAtlas.findRegion("item", i);
        }
    }

    public void render(SpriteBatch batch, GameState state) {
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

        sb.setLength(0);
        int score = state.getScore();
        for (int i = 7; i >= 0; i--) {
            sb.append((char) ('0' + (score / (int) Math.pow(10, i)) % 10));
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

        // Right section: 4 inventory slots
        float slotX = HUD_W - 4 * 17 - 2;
        float slotY = HUD_Y + (HUD_H - 16) / 2f;
        for (int i = 0; i < Inventory.MAX_SLOTS; i++) {
            batch.setColor(BAR_BG_COLOR);
            batch.draw(pixel, slotX, slotY, 16, 16);
            ItemType item = inventory.getSlot(i);
            if (item != null) {
                TextureRegion icon = itemIcons[item.spriteIndex];
                if (icon != null) {
                    batch.setColor(Color.WHITE);
                    batch.draw(icon, slotX, slotY, 16, 16);
                }
            }
            slotX += 17;
        }

        // Red flash overlay on inventory when core assembly has no matches
        CoreAssembly core = CoreTrigger.getCoreAssembly();
        if (core != null && core.isInventoryFlashRed()) {
            batch.setColor(0.8f, 0.1f, 0.1f, 0.6f);
            float flashX = HUD_W - 4 * 17 - 4;
            float flashY = HUD_Y + (HUD_H - 20) / 2f;
            batch.draw(pixel, flashX, flashY, 4 * 17 + 4, 20);
        }

        batch.setColor(Color.WHITE);
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
