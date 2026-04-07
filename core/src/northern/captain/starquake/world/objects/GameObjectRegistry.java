package northern.captain.starquake.world.objects;

import com.badlogic.gdx.utils.IntMap;
import northern.captain.starquake.Assets;

/**
 * Maps tile IDs to GameObject factories.
 *
 * Register all special tile types once at startup. When a room is built,
 * it queries this registry for each tile — if a factory exists, a new
 * GameObject is created for that tile position.
 */
public class GameObjectRegistry {

    @FunctionalInterface
    public interface Factory {
        GameObject create(Assets assets, int tileCol, int tileRow);
    }

    private final IntMap<Factory> factories = new IntMap<>();

    public void register(int tileId, Factory factory) {
        factories.put(tileId, factory);
    }

    /** Register all known game object types. */
    public static GameObjectRegistry createDefault() {
        GameObjectRegistry r = new GameObjectRegistry();
        r.register(HoverStand.TILE_ID, HoverStand::new);
        r.register(ElectricShocker.TILE_ID_7, ElectricShocker::hanging);
        r.register(ElectricShocker.TILE_ID_40, ElectricShocker::sidePoles);
        r.register(25, (a, col, row) -> new CollisionTile(a, col, row, 0, 16, 0, 0));
        r.register(30, (a, col, row) -> new CollisionTile(a, col, row, 8, 8, 0, 0));
        r.register(31, (a, col, row) -> new CollisionTile(a, col, row, 8, 0, 0, 0));
        r.register(33, (a, col, row) -> new CollisionTile(a, col, row, 24, 0, 16, 0));
        r.register(34, (a, col, row) -> new CollisionTile(a, col, row, 0, 24, 16, 0));
        r.register(35, (a, col, row) -> new MultiCollisionTile(a, col, row,
                new int[][]{{0, 0, 8, 8}, {24, 0, 8, 8}}));
        r.register(39, (a, col, row) -> new CollisionTile(a, col, row, 0, 0, 0, 16));
        r.register(43, (a, col, row) -> new MultiCollisionTile(a, col, row,
                new int[][]{{0, 16, 32, 8}, {24, 0, 8, 24}}));
        r.register(44, (a, col, row) -> new MultiCollisionTile(a, col, row,
                new int[][]{{0, 16, 32, 8}, {0, 0, 8, 24}}));
        return r;
    }

    /** Returns a new GameObject for this tile, or null if not a special tile. */
    public GameObject create(int tileId, Assets assets, int tileCol, int tileRow) {
        Factory f = factories.get(tileId);
        if (f == null) return null;
        return f.create(assets, tileCol, tileRow);
    }

    public boolean isRegistered(int tileId) {
        return factories.containsKey(tileId);
    }
}
