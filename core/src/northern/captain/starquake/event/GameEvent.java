package northern.captain.starquake.event;

/**
 * Base event object dispatched through {@link EventBus}.
 *
 * Subclass to carry additional data:
 *   class RoomChangedEvent extends GameEvent {
 *       public final int oldRoom, newRoom;
 *       RoomChangedEvent(int oldRoom, int newRoom) { super(Type.ROOM_CHANGED); ... }
 *   }
 */
public class GameEvent {
    public enum Type {
        BLOB_DIED,
        BLOB_SPAWNED,
        BLOB_MOUNTED_PLATFORM,
        BLOB_DISMOUNTED_PLATFORM,
        ROOM_CHANGED
    }

    public final Type type;

    public GameEvent(Type type) {
        this.type = type;
    }

    /** Convenience: reusable singleton events for types that carry no extra data. */
    public static final GameEvent BLOB_DIED = new GameEvent(Type.BLOB_DIED);
    public static final GameEvent BLOB_SPAWNED = new GameEvent(Type.BLOB_SPAWNED);
    public static final GameEvent BLOB_MOUNTED_PLATFORM = new GameEvent(Type.BLOB_MOUNTED_PLATFORM);
    public static final GameEvent BLOB_DISMOUNTED_PLATFORM = new GameEvent(Type.BLOB_DISMOUNTED_PLATFORM);
}
