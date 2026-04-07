package northern.captain.starquake.event;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;

/**
 * Simple synchronous event bus for decoupled communication between game systems.
 *
 * Events are dispatched immediately to all registered listeners for that event type.
 * Listeners receive the full {@link GameEvent} object which may carry additional data
 * in subclasses.
 *
 * Usage:
 *   EventBus.get().register(GameEvent.Type.BLOB_DIED, event -> handleDeath(event));
 *   EventBus.get().post(GameEvent.BLOB_DIED);
 *   EventBus.get().post(new RoomChangedEvent(oldRoom, newRoom));
 */
public class EventBus {
    private static final EventBus INSTANCE = new EventBus();

    public static EventBus get() {
        return INSTANCE;
    }

    public interface Listener {
        void onEvent(GameEvent event);
    }

    private final ObjectMap<GameEvent.Type, Array<Listener>> listeners = new ObjectMap<>();

    public void register(GameEvent.Type type, Listener listener) {
        Array<Listener> list = listeners.get(type);
        if (list == null) {
            list = new Array<>(4);
            listeners.put(type, list);
        }
        list.add(listener);
    }

    public void unregister(GameEvent.Type type, Listener listener) {
        Array<Listener> list = listeners.get(type);
        if (list != null) {
            list.removeValue(listener, true);
        }
    }

    public void post(GameEvent event) {
        Array<Listener> list = listeners.get(event.type);
        if (list == null) return;
        // Snapshot size to guard against modification during iteration
        Object[] items = list.items;
        for (int i = 0, n = list.size; i < n; i++) {
            ((Listener) items[i]).onEvent(event);
        }
    }

    public void clear() {
        listeners.clear();
    }
}
