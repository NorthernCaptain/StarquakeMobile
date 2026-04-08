package northern.captain.starquake.world.objects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import northern.captain.starquake.Assets;
import northern.captain.starquake.event.EnterTeleportEvent;
import northern.captain.starquake.event.EventBus;
import northern.captain.starquake.world.Blob;
import northern.captain.starquake.world.Collidable;

/**
 * Teleport entrance (tile 36).
 *
 * Solid: top 8px, right 4px.
 * Trigger zone: 6px wide strip on the right side.
 * Foreground: right 12px, full height, opaque (hides BLOB entering).
 *
 * When BLOB enters the trigger zone, posts ENTER_TELEPORT event.
 */
public class Teleporter extends GameObject {
    public static final int TILE_ID = 36;

    private static final int FG_WIDTH = 16;
    private final TextureRegion fgRegion;

    public Teleporter(Assets assets, int tileCol, int tileRow) {
        super(assets, tileCol, tileRow);

        TextureRegion full = assets.tileRegions.get(TILE_ID);
        if (full != null) {
            // Right 12px of the tile, full height
            int regionW = full.getRegionWidth();
            int regionH = full.getRegionHeight();
            fgRegion = new TextureRegion(full,
                    regionW - FG_WIDTH, 0,
                    FG_WIDTH, regionH);
        } else {
            fgRegion = null;
        }
    }

    @Override
    public boolean isSolidAt(float worldX, float worldY) {
        if (worldX < x || worldX >= x + TILE_W) return false;
        if (worldY < y || worldY >= y + TILE_H) return false;
        // Top 8px solid
        if (worldY >= y + TILE_H - 8) return true;
        // Right 4px solid
        if (worldX >= x + TILE_W - 4) return true;
        return false;
    }

    @Override
    public void onEnter(Collidable entity) {
        if (entity.getType() != Collidable.Type.BLOB) return;
        Blob blob = (Blob) entity;
        if (blob.state != Blob.State.IDLE && blob.state != Blob.State.WALK) return;

        // Trigger zone: rightmost 6px of the tile
        float triggerLeft = x + TILE_W - 6;
        if (blob.x + Blob.SIZE > triggerLeft) {
            EventBus.get().post(new EnterTeleportEvent(room.roomIndex));
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

        batch.draw(fgRegion, x + TILE_W - FG_WIDTH, y, FG_WIDTH, TILE_H);

        batch.flush();
        batch.setShader(null);
    }
}
