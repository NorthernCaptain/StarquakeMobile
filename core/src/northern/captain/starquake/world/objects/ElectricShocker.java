package northern.captain.starquake.world.objects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import northern.captain.starquake.Assets;
import northern.captain.starquake.world.Blob;
import northern.captain.starquake.world.Collidable;

/**
 * Electric shocker — periodic lightning arc that kills BLOB on contact.
 *
 * Configurable collision, kill zone, and arc position via factory methods.
 *
 * Cycles: IDLE (2s) → CHARGING (0.3s) → ACTIVE (1s) → repeat.
 */
public class ElectricShocker extends GameObject {
    public static final int TILE_ID_7 = 7;
    public static final int TILE_ID_40 = 40;

    private enum Phase { IDLE, CHARGING, ACTIVE }
    private static final float IDLE_TIME = 2.0f;
    private static final float CHARGE_TIME = 0.3f;
    private static final float ACTIVE_TIME = 1.0f;

    /** Solid collision: bottom strip height, and side pole width. */
    private final float solidHeight;
    private final float solidSideWidth;

    /** Kill zone (world coords). */
    private final float killLeft, killRight, killBottom, killTop;

    /** Lightning arc quad (world coords). */
    private final float arcX, arcY, arcW, arcH;

    private final boolean drawForeground;
    private final TextureRegion tileRegion;

    private Phase phase = Phase.IDLE;
    private float phaseTimer = (float) (Math.random() * IDLE_TIME);
    private float totalTime = phaseTimer;

    private ElectricShocker(Assets assets, int tileCol, int tileRow, Config cfg) {
        super(assets, tileCol, tileRow);
        this.solidHeight = cfg.solidHeight;
        this.solidSideWidth = cfg.solidSideWidth;
        this.killLeft   = x + cfg.killInsetX;
        this.killRight  = x + TILE_W - cfg.killInsetX;
        this.killBottom = y + cfg.killInsetBottom;
        this.killTop    = y + TILE_H - cfg.killInsetTop;
        this.arcX = x + cfg.arcInsetX;
        this.arcY = y + cfg.arcInsetBottom;
        this.arcW = TILE_W - cfg.arcInsetX * 2;
        this.arcH = TILE_H - cfg.arcInsetBottom - cfg.arcInsetTop;
        this.drawForeground = cfg.drawForeground;
        this.tileRegion = cfg.drawForeground ? assets.tileRegions.get(cfg.tileId) : null;
    }

    private static class Config {
        int solidHeight;        // solid block at bottom of tile (0 = none)
        int solidSideWidth;     // solid poles on left/right edges (0 = none)
        int killInsetX;         // kill zone horizontal inset from tile edges
        int killInsetBottom;    // kill zone inset from bottom
        int killInsetTop;       // kill zone inset from top
        int arcInsetX;          // arc quad horizontal inset
        int arcInsetBottom;     // arc quad inset from bottom
        int arcInsetTop;        // arc quad inset from top
        boolean drawForeground; // draw tile in front of BLOB
        int tileId;
    }

    /** Tile 7: hanging arc between two balls on legs. Solid base at bottom 8px. */
    public static ElectricShocker hanging(Assets assets, int tileCol, int tileRow) {
        Config c = new Config();
        c.solidHeight = 8;
        c.killInsetX = 8;
        c.killInsetBottom = 8;
        c.killInsetTop = 2;
        c.arcInsetX = 8;
        c.arcInsetBottom = 16;
        c.arcInsetTop = 2;
        c.drawForeground = true;
        c.tileId = TILE_ID_7;
        return new ElectricShocker(assets, tileCol, tileRow, c);
    }

    /** Tile 40: horizontal arc between two vertical poles. No solid base. */
    public static ElectricShocker sidePoles(Assets assets, int tileCol, int tileRow) {
        Config c = new Config();
        c.solidHeight = 0;
        c.solidSideWidth = 8;
        c.killInsetX = 8;
        c.killInsetBottom = 8;
        c.killInsetTop = 8;
        c.arcInsetX = 8;
        c.arcInsetBottom = 8;
        c.arcInsetTop = 8;
        c.drawForeground = false;
        c.tileId = TILE_ID_40;
        return new ElectricShocker(assets, tileCol, tileRow, c);
    }

    @Override
    public boolean isSolidAt(float worldX, float worldY) {
        if (worldX < x || worldX >= x + TILE_W) return false;
        if (worldY < y || worldY >= y + TILE_H) return false;
        // Solid base at bottom
        if (solidHeight > 0 && worldY < y + solidHeight) return true;
        // Solid poles on sides
        if (solidSideWidth > 0 && (worldX < x + solidSideWidth || worldX >= x + TILE_W - solidSideWidth)) return true;
        return false;
    }

    @Override
    public void update(float delta) {
        phaseTimer += delta;
        totalTime += delta;

        switch (phase) {
            case IDLE:
                if (phaseTimer >= IDLE_TIME) { phase = Phase.CHARGING; phaseTimer = 0; }
                break;
            case CHARGING:
                if (phaseTimer >= CHARGE_TIME) { phase = Phase.ACTIVE; phaseTimer = 0; }
                break;
            case ACTIVE:
                if (phaseTimer >= ACTIVE_TIME) { phase = Phase.IDLE; phaseTimer = 0; }
                break;
        }
    }

    @Override
    public void onEnter(Collidable entity) {
        if (entity.getType() != Collidable.Type.BLOB) return;
        if (phase != Phase.ACTIVE) return;

        Blob blob = (Blob) entity;
        if (blob.isInTransition() || blob.isImmune()) return;

        float blobBottom = blob.getBottom();
        float blobTop = blob.y + Blob.SIZE;

        if (blobTop > killBottom && blobBottom < killTop
                && blob.x + Blob.SIZE > killLeft && blob.x < killRight) {
            blob.die();
        }
    }

    @Override
    public void render(SpriteBatch batch, float delta) {
        if (phase != Phase.ACTIVE && phase != Phase.CHARGING) return;

        float alpha = (phase == Phase.CHARGING) ? phaseTimer / CHARGE_TIME * 0.5f : 1.0f;

        batch.flush();
        batch.setShader(assets.lightningShader);
        assets.lightningShader.setUniformf("u_time", totalTime);
        float pixelSize = Gdx.graphics.getHeight() / 144f;
        assets.lightningShader.setUniformf("u_pixelSize", pixelSize);

        batch.setColor(1, 1, 1, alpha);
        batch.draw(assets.whitePixel, arcX, arcY, arcW, arcH);
        batch.setColor(1, 1, 1, 1);

        batch.flush();
        batch.setShader(null);
    }

    @Override
    public void renderForeground(SpriteBatch batch, float delta) {
        if (!drawForeground || tileRegion == null || room == null) return;

        batch.flush();
        batch.setShader(assets.paletteShader);
        assets.paletteTexture.bind(1);
        Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);
        assets.paletteShader.setUniformi("u_palette", 1);
        assets.paletteShader.setUniformf("u_paletteRow", room.paletteIndex);

        batch.draw(tileRegion, x, y, TILE_W, TILE_H);

        batch.flush();
        batch.setShader(null);
    }
}
