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
 * Lift entrance and tube tiles (0-3).
 *
 * Entrance tiles activate the lift when BLOB walks to the center.
 * Tube tiles continue lifting. All draw foreground at 70% opacity.
 */
public class Lift extends GameObject {
    public static final int TILE_TUBE = 0;
    public static final int TILE_ENTRANCE_BOTH = 1;
    public static final int TILE_ENTRANCE_LEFT = 2;
    public static final int TILE_ENTRANCE_RIGHT = 3;

    private static final float CENTER_TOLERANCE = 2;
    private static final int FG_INSET = 8;

    private final int tileId;
    private final TextureRegion fgRegion;

    /** Solid rects: array of {left, bottom, right, top} in world coords. */
    private final float[][] solidRects;

    private Lift(Assets assets, int tileCol, int tileRow, int tileId, float[][] solidRects) {
        super(assets, tileCol, tileRow);
        this.tileId = tileId;
        this.solidRects = solidRects;

        TextureRegion full = assets.tileRegions.get(tileId);
        if (full != null) {
            fgRegion = new TextureRegion(full,
                    FG_INSET, 0,
                    full.getRegionWidth() - FG_INSET * 2,
                    full.getRegionHeight());
        } else {
            fgRegion = null;
        }
    }

    /** Tile 0: tube — 4px solid walls on each side. */
    public static Lift tube(Assets assets, int tileCol, int tileRow) {
        float x = tileCol * TILE_W;
        float y = (5 - tileRow) * TILE_H;
        return new Lift(assets, tileCol, tileRow, TILE_TUBE, new float[][]{
                {x, y, x + 4, y + TILE_H},           // left wall
                {x + TILE_W - 4, y, x + TILE_W, y + TILE_H}  // right wall
        });
    }

    /** Tile 1: entrance both sides — 8×8 solid top-left and top-right. */
    public static Lift entranceBoth(Assets assets, int tileCol, int tileRow) {
        float x = tileCol * TILE_W;
        float y = (5 - tileRow) * TILE_H;
        return new Lift(assets, tileCol, tileRow, TILE_ENTRANCE_BOTH, new float[][]{
                {x, y + TILE_H - 8, x + 8, y + TILE_H},           // top-left
                {x + TILE_W - 8, y + TILE_H - 8, x + TILE_W, y + TILE_H}  // top-right
        });
    }

    /** Tile 2: left entrance — 8×8 top-left, 8×24 right wall. */
    public static Lift entranceLeft(Assets assets, int tileCol, int tileRow) {
        float x = tileCol * TILE_W;
        float y = (5 - tileRow) * TILE_H;
        return new Lift(assets, tileCol, tileRow, TILE_ENTRANCE_LEFT, new float[][]{
                {x, y + TILE_H - 8, x + 8, y + TILE_H},   // top-left
                {x + TILE_W - 8, y, x + TILE_W, y + TILE_H} // right wall full height
        });
    }

    /** Tile 3: right entrance — 8×8 top-right, 8×24 left wall. */
    public static Lift entranceRight(Assets assets, int tileCol, int tileRow) {
        float x = tileCol * TILE_W;
        float y = (5 - tileRow) * TILE_H;
        return new Lift(assets, tileCol, tileRow, TILE_ENTRANCE_RIGHT, new float[][]{
                {x, y, x + 8, y + TILE_H},                           // left wall full height
                {x + TILE_W - 8, y + TILE_H - 8, x + TILE_W, y + TILE_H} // top-right
        });
    }

    public int getTileId() {
        return tileId;
    }

    public boolean isEntrance() {
        return tileId != TILE_TUBE;
    }

    @Override
    public boolean isSolidAt(float worldX, float worldY) {
        if (worldX < x || worldX >= x + TILE_W) return false;
        if (worldY < y || worldY >= y + TILE_H) return false;
        // 2px solid cap at top of every lift tile — prevents falling back in
        if (worldY >= y + TILE_H - 2) return true;
        // Custom solid rects (walls, corners)
        for (float[] r : solidRects) {
            if (worldX >= r[0] && worldX < r[2] && worldY >= r[1] && worldY < r[3]) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onEnter(Collidable entity) {
        if (entity.getType() != Collidable.Type.BLOB) return;
        if (!isEntrance()) return;

        Blob blob = (Blob) entity;
        if (blob.state != Blob.State.IDLE && blob.state != Blob.State.WALK) return;

        // Check BLOB is centered
        float tileCenterX = x + TILE_W / 2f;
        float blobCenterX = blob.x + Blob.SIZE / 2f;
        if (Math.abs(blobCenterX - tileCenterX) > CENTER_TOLERANCE) return;

        // Center blob exactly and start lifting
        blob.x = tileCenterX - Blob.SIZE / 2f;
        blob.startLifting();
        EventBus.get().post(GameEvent.LIFT_STARTED);
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

        float fgW = TILE_W - FG_INSET * 2;
        batch.setColor(1, 1, 1, 0.7f);
        batch.draw(fgRegion, x + FG_INSET, y, fgW, TILE_H);
        batch.setColor(1, 1, 1, 1);

        batch.flush();
        batch.setShader(null);
    }
}
