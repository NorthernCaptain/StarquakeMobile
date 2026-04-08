package northern.captain.starquake.world;

import com.badlogic.gdx.utils.Array;
import northern.captain.starquake.world.objects.GameObject;
import northern.captain.starquake.world.objects.Lift;

/**
 * Controls BLOB movement through lift tubes.
 *
 * When activated, moves BLOB upward at constant speed through connected
 * lift tiles, handling room transitions. Stops when BLOB is fully pushed
 * out of the last lift tile.
 */
public class LiftController {
    private static final float LIFT_SPEED = Blob.FLY_SPEED;

    private Blob blob;
    private boolean active;
    private Room room;

    /** Callback for when a room transition is needed during lift. */
    public interface RoomTransitionHandler {
        Room transitionUp();
    }

    private RoomTransitionHandler transitionHandler;

    public void setTransitionHandler(RoomTransitionHandler handler) {
        this.transitionHandler = handler;
    }

    public boolean isActive() {
        return active;
    }

    public void start(Blob blob, Room room) {
        this.blob = blob;
        this.room = room;
        this.active = true;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public void update(float delta) {
        if (!active) return;

        blob.y += LIFT_SPEED * delta;

        // Check if BLOB exited the top of the room
        if (blob.y >= Room.HEIGHT) {
            if (transitionHandler != null) {
                Room newRoom = transitionHandler.transitionUp();
                if (newRoom != null) {
                    room = newRoom;
                    blob.y = 0;
                    // Check if the tile at blob position in new room is a lift tile
                    if (!isLiftTileAt(blob.x + Blob.SIZE / 2f, blob.y + 1)) {
                        stop();
                    }
                    return;
                }
            }
            // No room above or transition failed — stop
            blob.y = Room.HEIGHT - Blob.SIZE;
            stop();
            return;
        }

        // Check if BLOB's feet have left the current lift tile
        float checkX = blob.x + Blob.SIZE / 2f;

        // Check tile at feet — if it's still a lift tile, keep going
        if (isLiftTileAt(checkX, blob.y)) return;

        // Feet are outside a lift tile. Check if head is in a lift tile above.
        if (isLiftTileAt(checkX, blob.y + Blob.SIZE - 1)) return;

        // Fully outside all lift tiles — stop
        stop();
    }

    private boolean isLiftTileAt(float worldX, float worldY) {
        if (worldX < 0 || worldY < 0 || worldX >= Room.WIDTH || worldY >= Room.HEIGHT) return false;
        int col = (int) (worldX / Room.TILE_W);
        int row = (Room.TILE_ROWS - 1) - (int) (worldY / Room.TILE_H);
        if (col < 0 || col >= Room.TILE_COLS || row < 0 || row >= Room.TILE_ROWS) return false;

        Array<GameObject> objs = room.getObjectsAt(worldX, worldY);
        if (objs != null) {
            for (int i = 0, n = objs.size; i < n; i++) {
                if (objs.get(i) instanceof Lift) return true;
            }
        }
        return false;
    }

    private void stop() {
        active = false;
        blob.stopLifting();
    }
}
