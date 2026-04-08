package northern.captain.starquake.world.objects;

import northern.captain.starquake.Assets;
import northern.captain.starquake.world.Collidable;

/**
 * Locked door (tile 45). Solid until opened with a key card.
 *
 * TODO: implement key card interaction — onAction with ACTION_A checks inventory,
 * opens door (clears collision, plays animation).
 */
public class Door extends CollisionTile {
    public static final int TILE_ID = 45;

    public Door(Assets assets, int tileCol, int tileRow) {
        super(assets, tileCol, tileRow, 8, 16, 0, 0);
    }
}
