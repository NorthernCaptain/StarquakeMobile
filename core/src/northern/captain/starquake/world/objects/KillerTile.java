package northern.captain.starquake.world.objects;

import northern.captain.starquake.Assets;
import northern.captain.starquake.world.Blob;
import northern.captain.starquake.world.Collidable;

/**
 * A tile with custom collision (like CollisionTile) plus a kill zone
 * that instantly kills BLOB on contact.
 */
public class KillerTile extends CollisionTile {
    private final float killLeft, killBottom, killRight, killTop;

    /**
     * @param killX      kill zone x offset from tile left
     * @param killY      kill zone y offset from tile bottom
     * @param killW      kill zone width
     * @param killH      kill zone height
     */
    public KillerTile(Assets assets, int tileCol, int tileRow,
                      int insetLeft, int insetRight, int insetTop, int insetBottom,
                      int killX, int killY, int killW, int killH) {
        super(assets, tileCol, tileRow, insetLeft, insetRight, insetTop, insetBottom);
        this.killLeft   = x + killX;
        this.killBottom = y + killY;
        this.killRight  = x + killX + killW;
        this.killTop    = y + killY + killH;
    }

    @Override
    public void onEnter(Collidable entity) {
        if (entity.getType() != Collidable.Type.BLOB) return;
        Blob blob = (Blob) entity;
        if (blob.isInTransition() || blob.isImmune()) return;

        float blobBottom = blob.getBottom();
        float blobTop = blob.y + Blob.SIZE;

        if (blobTop > killBottom && blobBottom <= killTop
                && blob.x + Blob.SIZE > killLeft && blob.x < killRight) {
            blob.die();
        }
    }
}
