package northern.captain.starquake.world.objects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import northern.captain.starquake.Assets;

/**
 * Walk-through passage or dead end. BLOB walks behind the tile graphic.
 *
 * The foreground is drawn at 70% opacity over BLOB using the palette shader.
 * Configurable solid edges (top, left, right) for passage vs dead-end variants.
 */
public class Passage extends CollisionTile {
    private static final int FG_INSET = 8; // foreground inset from sides and top

    private final TextureRegion fgRegion;
    private final int tileId;

    public Passage(Assets assets, int tileCol, int tileRow,
                   int insetLeft, int insetRight, int insetTop, int insetBottom,
                   int tileId) {
        super(assets, tileCol, tileRow, insetLeft, insetRight, insetTop, insetBottom);
        this.tileId = tileId;
        // Create a sub-region: 8px inset from sides and top, full bottom
        TextureRegion full = assets.tileRegions.get(tileId);
        if (full != null) {
            fgRegion = new TextureRegion(full,
                    FG_INSET, FG_INSET,                           // x, y offset in region (top-left)
                    full.getRegionWidth() - FG_INSET * 2,         // width
                    full.getRegionHeight() - FG_INSET);           // height (no bottom inset)
        } else {
            fgRegion = null;
        }
    }

    /** Tile 4: walk-through passage — top 8px solid. */
    public static Passage throughPassage(Assets assets, int tileCol, int tileRow) {
        return new Passage(assets, tileCol, tileRow, 0, 0, 0, 16, 4);
    }

    /** Tile 5: right dead end — top 8px and right 8px solid. */
    public static Passage rightDeadEnd(Assets assets, int tileCol, int tileRow) {
        return new Passage(assets, tileCol, tileRow, 0, 0, 0, 16, 5);
    }

    /** Tile 6: left dead end — top 8px and left 8px solid. */
    public static Passage leftDeadEnd(Assets assets, int tileCol, int tileRow) {
        return new Passage(assets, tileCol, tileRow, 0, 0, 0, 16, 6);
    }

    @Override
    public boolean isSolidAt(float worldX, float worldY) {
        if (worldX < x || worldX >= x + TILE_W) return false;
        if (worldY < y || worldY >= y + TILE_H) return false;

        // Top 8px always solid
        if (worldY >= y + TILE_H - 8) return true;

        // Side walls for dead ends
        if (tileId == 5 && worldX >= x + TILE_W - 8) return true;
        if (tileId == 6 && worldX < x + 8) return true;

        return false;
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

        // Draw inner area: 8px inset from sides and top, flush with bottom
        float fgW = TILE_W - FG_INSET * 2;
        float fgH = TILE_H - FG_INSET;
        batch.setColor(1, 1, 1, 0.7f);
        batch.draw(fgRegion, x + FG_INSET, y, fgW, fgH);
        batch.setColor(1, 1, 1, 1);

        batch.flush();
        batch.setShader(null);
    }
}
