package northern.captain.starquake.world;

import northern.captain.starquake.event.EnterTeleportEvent;
import northern.captain.starquake.event.EventBus;
import northern.captain.starquake.event.GameEvent;
import northern.captain.starquake.event.ItemCollectedEvent;
import northern.captain.starquake.event.RoomChangedEvent;
import northern.captain.starquake.event.TunnelTeleportEvent;

import java.util.HashSet;

/**
 * Tracks game score and exploration statistics.
 * Singleton, listens to EventBus for all scoring events.
 *
 * Usage: ScoreManager.init(gameState); ... ScoreManager.get().getExplorationPercent();
 */
public class ScoreManager {
    private static ScoreManager instance;

    // Score values
    private static final int SCORE_ROOM_VISITED = 7;
    private static final int SCORE_BOOST_COLLECTED = 19;
    private static final int SCORE_ITEM_COLLECTED = 174;
    private static final int SCORE_TUNNEL_USED = 227;
    private static final int SCORE_TELEPORTER_DISCOVERED = 449;
    private static final int SCORE_TRADE_COMPLETED = 521;
    private static final int SCORE_DOOR_OPENED = 947;
    private static final int SCORE_FLOOR_BROKEN = 81;
    private static final int SCORE_CORE_DELIVERED = 9741;

    // Exploration score weights
    private static final int EXPLORE_ROOM = 12;
    private static final int EXPLORE_TELEPORTER = 10;
    private static final int EXPLORE_TRADE = 8;
    private static final int EXPLORE_TUNNEL = 6;
    private static final int EXPLORE_DOOR = 5;

    private final GameState gameState;

    private final HashSet<Integer> visitedRooms = new HashSet<>();
    private final HashSet<Long> usedTunnels = new HashSet<>();
    private final HashSet<Integer> collectedItemTypes = new HashSet<>();
    private final HashSet<Integer> discoveredTeleporters = new HashSet<>();
    private int tradesCompleted;
    private int doorsOpened;
    private int corePartsDelivered;
    private int deathCount;

    public static void init(GameState gameState) {
        instance = new ScoreManager(gameState);
    }

    public static void dispose() {
        instance = null;
    }

    public static ScoreManager get() {
        return instance;
    }

    private ScoreManager(GameState gameState) {
        this.gameState = gameState;
        registerEvents();
    }

    private void registerEvents() {
        EventBus bus = EventBus.get();

        bus.register(GameEvent.Type.ROOM_CHANGED, e -> {
            RoomChangedEvent rc = (RoomChangedEvent) e;
            if (visitedRooms.add(rc.newRoom) && rc.oldRoom >= 0) {
                // Don't score the initial spawn room (oldRoom == -1)
                gameState.addScore(SCORE_ROOM_VISITED);
            }
        });

        bus.register(GameEvent.Type.ITEM_COLLECTED, e -> {
            ItemCollectedEvent ic = (ItemCollectedEvent) e;
            if (ic.itemType.isBoost()) {
                // Boosts always score (they respawn, can be collected multiple times)
                gameState.addScore(SCORE_BOOST_COLLECTED);
            } else if (collectedItemTypes.add(ic.itemType.ordinal())) {
                // Inventory items score only once per type (drop+re-pick doesn't re-score)
                gameState.addScore(SCORE_ITEM_COLLECTED);
            }
        });

        bus.register(GameEvent.Type.ENTER_TELEPORT, e -> {
            EnterTeleportEvent te = (EnterTeleportEvent) e;
            if (discoveredTeleporters.add(te.roomIndex)) {
                gameState.addScore(SCORE_TELEPORTER_DISCOVERED);
            }
        });

        bus.register(GameEvent.Type.TUNNEL_TELEPORT, e -> {
            TunnelTeleportEvent tt = (TunnelTeleportEvent) e;
            long key = (long) tt.roomIndex * 2 + (tt.goingRight ? 1 : 0);
            if (usedTunnels.add(key)) {
                gameState.addScore(SCORE_TUNNEL_USED);
            }
        });

        bus.register(GameEvent.Type.TRADE_COMPLETED, e -> {
            tradesCompleted++;
            gameState.addScore(SCORE_TRADE_COMPLETED);
        });

        bus.register(GameEvent.Type.DOOR_OPENED, e -> {
            doorsOpened++;
            gameState.addScore(SCORE_DOOR_OPENED);
        });

        bus.register(GameEvent.Type.CORE_DELIVERED, e -> {
            corePartsDelivered++;
            gameState.addScore(SCORE_CORE_DELIVERED);
        });

        bus.register(GameEvent.Type.FLOOR_BROKEN, e -> {
            gameState.addScore(SCORE_FLOOR_BROKEN);
        });

        bus.register(GameEvent.Type.BLOB_DIED, e -> {
            deathCount++;
        });
    }

    /** Room-based exploration percentage (0-100). */
    public int getExplorationPercent() {
        return visitedRooms.size() * 100 / 512;
    }

    /** Weighted exploration score for leaderboard. */
    public int getExplorationScore() {
        return visitedRooms.size() * EXPLORE_ROOM
             + discoveredTeleporters.size() * EXPLORE_TELEPORTER
             + tradesCompleted * EXPLORE_TRADE
             + usedTunnels.size() * EXPLORE_TUNNEL
             + doorsOpened * EXPLORE_DOOR;
    }

    public int getRoomsVisited() { return visitedRooms.size(); }
    public int getDeathCount() { return deathCount; }
    public int getTradesCompleted() { return tradesCompleted; }
    public int getCorePartsDelivered() { return corePartsDelivered; }
}
