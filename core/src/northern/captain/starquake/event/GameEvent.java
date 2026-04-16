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
        LIFT_STARTED,
        ENTER_TELEPORT,
        ENTER_TRADE,
        TUNNEL_TELEPORT,
        ROOM_CHANGED,
        GAME_OVER,
        ITEM_COLLECTED,
        TRADE_COMPLETED,
        DOOR_OPENED,
        CORE_DELIVERED,
        FLOOR_BROKEN,
        ENEMY_KILLED
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
    public static final GameEvent LIFT_STARTED = new GameEvent(Type.LIFT_STARTED);
    // ENTER_TELEPORT uses EnterTeleportEvent subclass (carries roomIndex)
    // ENTER_TRADE uses EnterTradeEvent subclass (carries pyramid, offeredItem, slotIndex)
    // ITEM_COLLECTED uses ItemCollectedEvent subclass
    // TRADE_COMPLETED uses TradeCompletedEvent subclass
    // ROOM_CHANGED uses RoomChangedEvent subclass
    public static final GameEvent DOOR_OPENED = new GameEvent(Type.DOOR_OPENED);
    // CORE_DELIVERED uses CoreDeliveredEvent subclass (carries totalDelivered)
    public static final GameEvent FLOOR_BROKEN = new GameEvent(Type.FLOOR_BROKEN);
    public static final GameEvent ENEMY_KILLED = new GameEvent(Type.ENEMY_KILLED);
}
