package northern.captain.starquake.world.objects;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import northern.captain.starquake.Assets;
import northern.captain.starquake.input.InputManager;
import northern.captain.starquake.world.Collidable;
import northern.captain.starquake.world.Room;

/**
 * Base class for interactive game objects placed in rooms.
 *
 * Each object occupies a tile-grid cell (32×24 pixels) and is created
 * by {@link GameObjectRegistry} when the room is built and a matching
 * tile ID is found.
 */
public abstract class GameObject {
    /** Tile-grid column (0-7) and row (0-5), row 0 = top of room. */
    public final int tileCol, tileRow;
    /** World-pixel position (bottom-left, libGDX convention y=0 at bottom). */
    public final float x, y;
    /** Tile dimensions. */
    public static final int TILE_W = 32, TILE_H = 24;

    protected final Assets assets;
    protected Room room;

    protected GameObject(Assets assets, int tileCol, int tileRow) {
        this.assets = assets;
        this.tileCol = tileCol;
        this.tileRow = tileRow;
        this.x = tileCol * TILE_W;
        // Row 0 = top of room → y = (5 - tileRow) * TILE_H for libGDX bottom-up
        this.y = (5 - tileRow) * TILE_H;
    }

    /** Called by Room after the object is added. */
    public void onAddedToRoom(Room room) {
        this.room = room;
    }

    /** Called every frame. */
    public void update(float delta) {}

    /** Draw on top of terrain, before BLOB. Batch is already begun. */
    public void render(SpriteBatch batch, float delta) {}

    /** Draw in front of BLOB (foreground layer). Batch is already begun. */
    public void renderForeground(SpriteBatch batch, float delta) {}

    /**
     * Collision check for this object's tile. Called by Room.isSolidAt()
     * instead of the default solid-box when this tile has a game object.
     * Default: entire tile is solid.
     */
    public boolean isSolidAt(float worldX, float worldY) {
        return worldX >= x && worldX < x + TILE_W
            && worldY >= y && worldY < y + TILE_H;
    }

    /** Called when a collidable entity overlaps this object's tile. */
    public void onEnter(Collidable entity) {}

    /** Called when a collidable entity is on this tile and triggers an action. Returns true if consumed. */
    public boolean onAction(Collidable entity, InputManager.Action action) { return false; }

    /** Multimap key for tile-coordinate lookup. */
    public static int tileKey(int col, int row) {
        return row * 8 + col;
    }

    public int getTileKey() {
        return tileKey(tileCol, tileRow);
    }
}
