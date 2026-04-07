package northern.captain.starquake.world.objects;

import northern.captain.starquake.Assets;

/**
 * A tile with a custom collision box defined by insets from each side.
 *
 * For a full 32×24 tile with right=16, the solid area is 16×24 on the left.
 * Subclasses can extend this for additional behavior while inheriting
 * the inset-based collision.
 */
public class CollisionTile extends GameObject {
    private final float solidLeft, solidBottom, solidRight, solidTop;

    /**
     * @param insetLeft   empty pixels from the left edge
     * @param insetRight  empty pixels from the right edge
     * @param insetTop    empty pixels from the top edge
     * @param insetBottom empty pixels from the bottom edge
     */
    public CollisionTile(Assets assets, int tileCol, int tileRow,
                         int insetLeft, int insetRight, int insetTop, int insetBottom) {
        super(assets, tileCol, tileRow);
        this.solidLeft   = x + insetLeft;
        this.solidRight  = x + Math.max(insetLeft, TILE_W - insetRight);
        this.solidBottom = y + insetBottom;
        this.solidTop    = y + Math.max(insetBottom, TILE_H - insetTop);
    }

    @Override
    public boolean isSolidAt(float worldX, float worldY) {
        return worldX >= solidLeft && worldX < solidRight
            && worldY >= solidBottom && worldY < solidTop;
    }
}
