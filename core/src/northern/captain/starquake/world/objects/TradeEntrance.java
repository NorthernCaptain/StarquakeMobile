package northern.captain.starquake.world.objects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import northern.captain.starquake.Assets;
import northern.captain.starquake.event.EventBus;
import northern.captain.starquake.event.GameEvent;
import northern.captain.starquake.world.Blob;
import northern.captain.starquake.world.Collidable;

/**
 * Trade/exchange entrance (tiles 37, 38).
 *
 * Tile 37: entrance from left — solid top 8px + right 4px, trigger on right, foreground right 16px.
 * Tile 38: entrance from right — mirrored (solid top 8px + left 4px, trigger on left, foreground left 16px).
 */
public class TradeEntrance extends GameObject {
    public static final int TILE_LEFT = 37;
    public static final int TILE_RIGHT = 38;

    private static final int FG_WIDTH = 16;

    private final boolean fromLeft; // true = tile 37, false = tile 38
    private final TextureRegion fgRegion;

    private TradeEntrance(Assets assets, int tileCol, int tileRow, boolean fromLeft, int tileId) {
        super(assets, tileCol, tileRow);
        this.fromLeft = fromLeft;

        TextureRegion full = assets.tileRegions.get(tileId);
        if (full != null) {
            int regionW = full.getRegionWidth();
            int regionH = full.getRegionHeight();
            if (fromLeft) {
                // Right 16px
                fgRegion = new TextureRegion(full, regionW - FG_WIDTH, 0, FG_WIDTH, regionH);
            } else {
                // Left 16px
                fgRegion = new TextureRegion(full, 0, 0, FG_WIDTH, regionH);
            }
        } else {
            fgRegion = null;
        }
    }

    public static TradeEntrance left(Assets assets, int tileCol, int tileRow) {
        return new TradeEntrance(assets, tileCol, tileRow, true, TILE_LEFT);
    }

    public static TradeEntrance right(Assets assets, int tileCol, int tileRow) {
        return new TradeEntrance(assets, tileCol, tileRow, false, TILE_RIGHT);
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
    public void onEnter(Collidable entity) {
        if (entity.getType() != Collidable.Type.BLOB) return;
        Blob blob = (Blob) entity;
        if (blob.state != Blob.State.IDLE && blob.state != Blob.State.WALK) return;

        // 6px trigger zone on the deep side
        if (fromLeft) {
            if (blob.x + Blob.SIZE > x + TILE_W - 6) {
                EventBus.get().post(GameEvent.ENTER_TRADE);
            }
        } else {
            if (blob.x < x + 6) {
                EventBus.get().post(GameEvent.ENTER_TRADE);
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
