package northern.captain.starquake.world.objects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import northern.captain.starquake.Assets;
import northern.captain.starquake.input.InputManager;
import northern.captain.starquake.world.Blob;
import northern.captain.starquake.world.Collidable;
import northern.captain.starquake.world.Inventory;
import northern.captain.starquake.world.items.ItemPickup;
import northern.captain.starquake.world.items.ItemType;

/**
 * Space lock (tiles 37, 38). Within-room teleporter requiring Access Card.
 *
 * Tile 37: entrance from left — solid wall on right, partner expected to the right.
 * Tile 38: entrance from right — solid wall on left, partner expected to the left.
 *
 * Press UP near the solid wall: if BLOB has Access Card, teleport to partner.
 * If no card, blink card icon above the lock. If no partner exists, do nothing.
 */
public class SpaceLock extends GameObject {
    public static final int TILE_LEFT = 37;
    public static final int TILE_RIGHT = 38;

    private static final int FG_WIDTH = 16;
    private static final float HINT_DURATION = 2f;
    private static final float HINT_FREQ = 5f;

    private final boolean fromLeft; // true = tile 37, false = tile 38
    private final TextureRegion fgRegion;
    private final TextureRegion cardIcon;
    private float hintTimer;

    private SpaceLock(Assets assets, int tileCol, int tileRow, boolean fromLeft, int tileId) {
        super(assets, tileCol, tileRow);
        this.fromLeft = fromLeft;
        this.cardIcon = assets.itemsAtlas.findRegion("item", ItemType.ACCESS_CARD.spriteIndex);

        TextureRegion full = assets.tileRegions.get(tileId);
        if (full != null) {
            int regionW = full.getRegionWidth();
            int regionH = full.getRegionHeight();
            if (fromLeft) {
                fgRegion = new TextureRegion(full, regionW - FG_WIDTH, 0, FG_WIDTH, regionH);
            } else {
                fgRegion = new TextureRegion(full, 0, 0, FG_WIDTH, regionH);
            }
        } else {
            fgRegion = null;
        }
    }

    public static SpaceLock left(Assets assets, int tileCol, int tileRow) {
        return new SpaceLock(assets, tileCol, tileRow, true, TILE_LEFT);
    }

    public static SpaceLock right(Assets assets, int tileCol, int tileRow) {
        return new SpaceLock(assets, tileCol, tileRow, false, TILE_RIGHT);
    }

    @Override
    public boolean isSolidAt(float worldX, float worldY) {
        if (worldX < x || worldX >= x + TILE_W) return false;
        if (worldY < y || worldY >= y + TILE_H) return false;
        // Top 8px solid
        if (worldY >= y + TILE_H - 8) return true;
        // 4px solid wall on the deep side
        if (fromLeft && worldX >= x + TILE_W - 4) return true;
        if (!fromLeft && worldX < x + 4) return true;
        return false;
    }

    @Override
    public void update(float delta) {
        if (hintTimer > 0) hintTimer -= delta;
    }

    @Override
    public boolean onAction(Collidable entity, InputManager.Action action) {
        if (action != InputManager.Action.UP) return false;
        if (entity.getType() != Collidable.Type.BLOB) return false;
        Blob blob = (Blob) entity;
        if (blob.state != Blob.State.IDLE && blob.state != Blob.State.WALK) return false;

        // BLOB must be near the solid wall. The wall pushes BLOB out, so check
        // if BLOB's edge is within 2px outside the wall.
        if (fromLeft) {
            // Wall on right at x+TILE_W-4. BLOB approaching from left.
            float blobRight = blob.x + Blob.SIZE;
            if (blobRight < x + TILE_W - 4 - 2 || blobRight > x + TILE_W) return false;
        } else {
            // Wall on left at x+4. BLOB approaching from right.
            if (blob.x > x + 4 + 2 || blob.x + Blob.SIZE < x) return false;
        }

        // Find partner space lock
        SpaceLock partner = findPartner();
        if (partner == null) return false;

        Inventory inventory = ItemPickup.inventory();
        if (inventory == null || !inventory.contains(ItemType.ACCESS_CARD)) {
            // No card — blink card icon
            hintTimer = HINT_DURATION;
            return true;
        }

        // Teleport BLOB to partner's open area
        float targetX;
        if (partner.fromLeft) {
            // Partner is tile 37 (wall on right) — place BLOB on the left/open side
            targetX = partner.x + 4;
        } else {
            // Partner is tile 38 (wall on left) — place BLOB on the right/open side
            targetX = partner.x + TILE_W - 4 - Blob.SIZE;
        }
        blob.x = targetX;
        blob.y = partner.y;
        return true;
    }

    /** Find the partner SpaceLock in the same room. Tile 37 looks right, tile 38 looks left. */
    private SpaceLock findPartner() {
        if (room == null) return null;
        int partnerCol = fromLeft ? tileCol + 1 : tileCol - 1;
        if (partnerCol < 0 || partnerCol >= 8) return null;

        Array<GameObject> objs = room.getObjectsAt(
                partnerCol * TILE_W + TILE_W / 2f,
                y + TILE_H / 2f);
        if (objs == null) return null;

        for (int i = 0; i < objs.size; i++) {
            if (objs.get(i) instanceof SpaceLock) {
                SpaceLock other = (SpaceLock) objs.get(i);
                if (other != this) return other;
            }
        }
        return null;
    }

    @Override
    public void render(SpriteBatch batch, float delta) {
        // Blink card icon above the lock when player tried without card
        if (hintTimer > 0 && cardIcon != null) {
            boolean visible = ((int) (hintTimer * HINT_FREQ * 2)) % 2 == 0;
            if (visible) {
                float iconX = x + (TILE_W - 16) / 2f;
                float iconY = y + TILE_H + 2;
                batch.setColor(Color.WHITE);
                batch.draw(cardIcon, iconX, iconY, 16, 16);
            }
        }
    }

    @Override
    public void renderForeground(SpriteBatch batch, float delta) {
        if (fgRegion == null || room == null) return;

        batch.flush();
        batch.setShader(assets.paletteShader);
        assets.paletteTexture.bind(1);
        Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);
        assets.paletteShader.setUniformi("u_palette", 1);
        assets.paletteShader.setUniformf("u_paletteRow", room.paletteIndex);

        if (fromLeft) {
            batch.draw(fgRegion, x + TILE_W - FG_WIDTH, y, FG_WIDTH, TILE_H);
        } else {
            batch.draw(fgRegion, x, y, FG_WIDTH, TILE_H);
        }

        batch.flush();
        batch.setShader(null);
    }
}
