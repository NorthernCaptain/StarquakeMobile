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
        // Tile  0: lift tube — 4px solid walls
        r.register(Lift.TILE_TUBE, Lift::tube);
        // Tile  1: lift entrance both sides
        r.register(Lift.TILE_ENTRANCE_BOTH, Lift::entranceBoth);
        // Tile  2: lift entrance left
        r.register(Lift.TILE_ENTRANCE_LEFT, Lift::entranceLeft);
        // Tile  3: lift entrance right
        r.register(Lift.TILE_ENTRANCE_RIGHT, Lift::entranceRight);
        // Tile  4: walk-through passage — top 8px solid, foreground at 70%
        r.register(4, Passage::throughPassage);
        // Tile  5: right dead end — top 8px + right 8px solid, foreground at 70%
        r.register(5, Passage::rightDeadEnd);
        // Tile  6: left dead end — top 8px + left 8px solid, foreground at 70%
        r.register(6, Passage::leftDeadEnd);
        // Tile  7: electric shocker — hanging arc between two balls
        r.register(ElectricShocker.TILE_ID_7, ElectricShocker::hanging);
        // Tile 25: right 16px empty
        r.register(25, (a, col, row) -> new CollisionTile(a, col, row, 0, 16, 0, 0));
        // Tile 26: 8px solid sides, center empty
        r.register(26, (a, col, row) -> new MultiCollisionTile(a, col, row,
                new int[][]{{0, 0, 8, 24}, {24, 0, 8, 24}}));
        // Tile 27: lethal — left 16px empty corridor, right solid with kill zone
        r.register(27, (a, col, row) -> new KillerTile(a, col, row,
                16, 0, 0, 0,
                16, 8, 16, 16));
        // Tile 30: 8px inset both sides
        r.register(30, (a, col, row) -> new CollisionTile(a, col, row, 8, 8, 0, 0));
        // Tile 31: 8px inset left
        r.register(31, (a, col, row) -> new CollisionTile(a, col, row, 8, 0, 0, 0));
        // Tile 32: hover platform stand
        r.register(HoverStand.TILE_ID, HoverStand::new);
        // Tile 33: 8x8 solid bottom-right corner
        r.register(33, (a, col, row) -> new CollisionTile(a, col, row, 24, 0, 16, 0));
        // Tile 34: 8x8 solid bottom-left corner
        r.register(34, (a, col, row) -> new CollisionTile(a, col, row, 0, 24, 16, 0));
        // Tile 35: two 8x8 solid bottom corners
        r.register(35, (a, col, row) -> new MultiCollisionTile(a, col, row,
                new int[][]{{0, 0, 8, 8}, {24, 0, 8, 8}}));
        // Tile 36: teleport entrance
        r.register(Teleporter.TILE_ID, Teleporter::new);
        // Tile 37: space lock from left (within-room teleport, requires Access Card)
        r.register(SpaceLock.TILE_LEFT, SpaceLock::left);
        // Tile 38: space lock from right
        r.register(SpaceLock.TILE_RIGHT, SpaceLock::right);
        // Tile 39: breakable floor — solid top 8px, breaks when BLOB lands on it
        r.register(BreakableFloor.TILE_ID, BreakableFloor::new);
        // Tile 40: electric shocker — horizontal arc between side poles
        r.register(ElectricShocker.TILE_ID_40, ElectricShocker::sidePoles);
        // Tile 43: tunnel teleporter going right
        r.register(TunnelTeleporter.TILE_RIGHT, TunnelTeleporter::right);
        // Tile 44: tunnel teleporter going left
        r.register(TunnelTeleporter.TILE_LEFT, TunnelTeleporter::left);
        // Tile 45: locked door — solid 8px column
        r.register(Door.TILE_ID, Door::new);
        // Tile 46: lethal — 8px inset sides and top, kill zone matches solid area
        r.register(46, (a, col, row) -> new KillerTile(a, col, row,
                8, 8, 8, 0,
                6, 0, 20, 16));
        // Tile 48: solid top 8px only
        // 48 is an empty tile! exclude
        //r.register(48, (a, col, row) -> new CollisionTile(a, col, row, 0, 0, 0, 16));
        // Tile 58: solid bottom 16px
        r.register(58, (a, col, row) -> new CollisionTile(a, col, row, 0, 0, 8, 0));
        // Tile 59: solid left 24px
        r.register(59, (a, col, row) -> new CollisionTile(a, col, row, 0, 8, 0, 0));
        // Tile 85: 8px empty sides + top
        r.register(85, (a, col, row) -> new CollisionTile(a, col, row, 8, 8, 8, 0));
        // Tile 83: 8px empty each side
        r.register(83, (a, col, row) -> new CollisionTile(a, col, row, 8, 8, 0, 0));
        // Tile 82: lethal — 8px inset sides and top, kills from top only
        r.register(82, (a, col, row) -> new KillerTile(a, col, row,
                8, 8, 8, 0,
                10, 0, 12, 16));
        // Tile 122: core room trigger — invisible, starts assembly animation
        r.register(CoreTrigger.TILE_ID, CoreTrigger::new);
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
