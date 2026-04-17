package northern.captain.starquake.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;
import northern.captain.starquake.world.items.ItemManager;
import northern.captain.starquake.world.items.ItemType;
import northern.captain.starquake.world.objects.BreakableFloor;

import java.util.HashSet;
import java.util.Set;

/**
 * Singleton save/restore manager. Persists game state across app restarts
 * using multiple small libGDX Preferences files (one per category).
 *
 * Usage: SaveManager.init(); SaveManager.get().saveState(...);
 */
public class SaveManager {
    private static SaveManager instance;

    private static final int SAVE_VERSION = 1;

    private final Preferences metaPrefs;
    private final Preferences statePrefs;
    private final Preferences inventoryPrefs;
    private final Preferences itemsPrefs;
    private final Preferences corePrefs;
    private final Preferences teleportPrefs;
    private final Preferences doorsPrefs;
    private final Preferences breakablePrefs;
    private final Preferences scorePrefs;

    public static void init() {
        instance = new SaveManager();
    }

    public static SaveManager get() {
        return instance;
    }

    private SaveManager() {
        metaPrefs = Gdx.app.getPreferences("save_meta");
        statePrefs = Gdx.app.getPreferences("save_state");
        inventoryPrefs = Gdx.app.getPreferences("save_inventory");
        itemsPrefs = Gdx.app.getPreferences("save_items");
        corePrefs = Gdx.app.getPreferences("save_core");
        teleportPrefs = Gdx.app.getPreferences("save_teleport");
        doorsPrefs = Gdx.app.getPreferences("save_doors");
        breakablePrefs = Gdx.app.getPreferences("save_breakable");
        scorePrefs = Gdx.app.getPreferences("save_score");
    }

    // ---- Meta ----

    public boolean hasSave() {
        return metaPrefs.getBoolean("exists", false);
    }

    public void createSaveMeta() {
        metaPrefs.putBoolean("exists", true);
        metaPrefs.putInteger("version", SAVE_VERSION);
        metaPrefs.flush();
    }

    /** Wipe all save_* prefs. Does NOT touch app_* prefs. */
    public void clearAll() {
        metaPrefs.clear(); metaPrefs.flush();
        statePrefs.clear(); statePrefs.flush();
        inventoryPrefs.clear(); inventoryPrefs.flush();
        itemsPrefs.clear(); itemsPrefs.flush();
        corePrefs.clear(); corePrefs.flush();
        teleportPrefs.clear(); teleportPrefs.flush();
        doorsPrefs.clear(); doorsPrefs.flush();
        breakablePrefs.clear(); breakablePrefs.flush();
        scorePrefs.clear(); scorePrefs.flush();
    }

    // ---- State (blob + vitals) ----

    public void saveState(int roomIndex, Blob blob, GameState gameState) {
        statePrefs.putInteger("room", roomIndex);
        statePrefs.putFloat("blob_x", blob.x);
        statePrefs.putFloat("blob_y", blob.y);
        statePrefs.putBoolean("blob_facing", blob.facingRight);
        statePrefs.putBoolean("blob_flying", blob.state == Blob.State.FLYING);
        statePrefs.putInteger("lives", gameState.getLives());
        statePrefs.putInteger("score", gameState.getScore());
        statePrefs.putInteger("health", gameState.getHealth());
        statePrefs.putInteger("platforms", gameState.getPlatforms());
        statePrefs.putInteger("laser", gameState.getLaserEnergy());
        statePrefs.flush();
    }

    public int getSavedRoom() {
        return statePrefs.getInteger("room", 435);
    }

    public void loadState(Blob blob, GameState gameState) {
        blob.x = statePrefs.getFloat("blob_x", Room.WIDTH / 2f);
        blob.y = statePrefs.getFloat("blob_y", 40);
        blob.facingRight = statePrefs.getBoolean("blob_facing", true);
        if (statePrefs.getBoolean("blob_flying", false)) {
            blob.startFlying();
        }
        gameState.setLives(statePrefs.getInteger("lives", 5));
        gameState.setScore(statePrefs.getInteger("score", 0));
        gameState.setHealth(statePrefs.getInteger("health", 100));
        gameState.setPlatforms(statePrefs.getInteger("platforms", 20));
        gameState.setLaserEnergy(statePrefs.getInteger("laser", 100));
    }

    // ---- Inventory ----

    public void saveInventory(Inventory inventory) {
        inventoryPrefs.putInteger("count", inventory.getCount());
        for (int i = 0; i < Inventory.MAX_SLOTS; i++) {
            ItemType item = inventory.getSlot(i);
            inventoryPrefs.putString("slot_" + i, item != null ? item.name() : "");
        }
        inventoryPrefs.flush();
    }

    public void loadInventory(Inventory inventory) {
        inventory.clear();
        int count = inventoryPrefs.getInteger("count", 0);
        // Load in reverse order since add() prepends (FIFO)
        for (int i = count - 1; i >= 0; i--) {
            String name = inventoryPrefs.getString("slot_" + i, "");
            if (!name.isEmpty()) {
                try {
                    inventory.add(ItemType.valueOf(name));
                } catch (IllegalArgumentException ignored) {}
            }
        }
    }

    // ---- Items ----

    public void saveItemPlacements(ItemManager itemManager) {
        // Save full placements as serialized string
        StringBuilder sb = new StringBuilder();
        IntMap<Array<ItemManager.ItemPlacement>> placements = itemManager.getPlacements();
        for (IntMap.Entry<Array<ItemManager.ItemPlacement>> entry : placements) {
            for (int i = 0; i < entry.value.size; i++) {
                ItemManager.ItemPlacement p = entry.value.get(i);
                if (sb.length() > 0) sb.append(';');
                sb.append(entry.key).append(',')
                  .append(p.type.name()).append(',')
                  .append(p.tileCol).append(',')
                  .append(p.tileRow);
            }
        }
        itemsPrefs.putString("placements", sb.toString());
        itemsPrefs.putString("collected", "");
        itemsPrefs.flush();
    }

    public void saveItemCollected(int roomIndex, int col, int row) {
        String current = itemsPrefs.getString("collected", "");
        String key = roomIndex + ":" + col + ":" + row;
        // Dedup: don't append if already recorded
        if ((";" + current + ";").contains(";" + key + ";") || current.equals(key)) return;
        if (current.isEmpty()) {
            itemsPrefs.putString("collected", key);
        } else {
            itemsPrefs.putString("collected", current + ";" + key);
        }
        itemsPrefs.flush();
    }

    public void loadItems(ItemManager itemManager) {
        // Parse placements
        String placementsStr = itemsPrefs.getString("placements", "");
        if (placementsStr.isEmpty()) return;

        // Build collected set
        Set<String> collected = new HashSet<>();
        String collectedStr = itemsPrefs.getString("collected", "");
        if (!collectedStr.isEmpty()) {
            for (String key : collectedStr.split(";")) {
                collected.add(key.trim());
            }
        }

        // Reconstruct placements minus collected
        for (String entry : placementsStr.split(";")) {
            String[] parts = entry.split(",");
            if (parts.length != 4) continue;
            try {
                int room = Integer.parseInt(parts[0]);
                ItemType type = ItemType.valueOf(parts[1]);
                int col = Integer.parseInt(parts[2]);
                int row = Integer.parseInt(parts[3]);
                String key = room + ":" + col + ":" + row;
                if (!collected.contains(key)) {
                    itemManager.addPlacementDirect(room, type, col, row);
                }
            } catch (Exception ignored) {}
        }
    }

    // ---- Core Assembly ----

    public void saveCoreAssembly(CoreAssembly core) {
        for (int i = 0; i < 9; i++) {
            ItemType req = core.getRequiredPart(i);
            corePrefs.putString("required_" + i, req != null ? req.name() : "");
            corePrefs.putBoolean("restored_" + i, core.isRestored(i));
        }
        corePrefs.putInteger("restored_count", core.getRestoredCount());
        corePrefs.flush();
    }

    public void loadCoreAssembly(CoreAssembly core) {
        ItemType[] required = new ItemType[9];
        boolean[] restored = new boolean[9];
        int restoredCount = corePrefs.getInteger("restored_count", 0);
        for (int i = 0; i < 9; i++) {
            String name = corePrefs.getString("required_" + i, "");
            required[i] = name.isEmpty() ? null : ItemType.valueOf(name);
            restored[i] = corePrefs.getBoolean("restored_" + i, true);
        }
        core.loadState(required, restored, restoredCount);
    }

    // ---- Teleport ----

    public void saveTeleportRegistry(TeleportRegistry registry) {
        for (int i = 0; i < TeleportRegistry.COUNT; i++) {
            teleportPrefs.putString("name_" + i, registry.getName(i));
            teleportPrefs.putBoolean("visited_" + i, registry.isVisited(i));
        }
        teleportPrefs.flush();
    }

    public void loadTeleportRegistry(TeleportRegistry registry) {
        String[] names = new String[TeleportRegistry.COUNT];
        boolean[] visited = new boolean[TeleportRegistry.COUNT];
        for (int i = 0; i < TeleportRegistry.COUNT; i++) {
            names[i] = teleportPrefs.getString("name_" + i, "?????");
            visited[i] = teleportPrefs.getBoolean("visited_" + i, false);
        }
        registry.loadState(names, visited);
    }

    // ---- Doors ----

    public void saveDoorOpened(int roomIndex) {
        String current = doorsPrefs.getString("opened", "");
        String roomStr = String.valueOf(roomIndex);
        // Avoid duplicates
        if (current.isEmpty()) {
            doorsPrefs.putString("opened", roomStr);
        } else if (!("," + current + ",").contains("," + roomStr + ",")) {
            doorsPrefs.putString("opened", current + "," + roomStr);
        }
        doorsPrefs.flush();
    }

    public Set<Integer> loadOpenedDoors() {
        Set<Integer> doors = new HashSet<>();
        String str = doorsPrefs.getString("opened", "");
        if (!str.isEmpty()) {
            for (String s : str.split(",")) {
                try { doors.add(Integer.parseInt(s.trim())); }
                catch (NumberFormatException ignored) {}
            }
        }
        return doors;
    }

    // ---- Breakable Floors ----

    public void saveBreakableFloors() {
        StringBuilder sb = new StringBuilder();
        for (Long key : BreakableFloor.getBrokenTiles()) {
            if (sb.length() > 0) sb.append(',');
            sb.append(key);
        }
        breakablePrefs.putString("broken", sb.toString());
        breakablePrefs.flush();
    }

    public void loadBreakableFloors() {
        String str = breakablePrefs.getString("broken", "");
        if (str.isEmpty()) return;
        for (String s : str.split(",")) {
            try { BreakableFloor.addBrokenTile(Long.parseLong(s.trim())); }
            catch (NumberFormatException ignored) {}
        }
    }

    // ---- Score Manager ----

    public void saveScoreManager(ScoreManager score) {
        scorePrefs.putString("visited_rooms", intSetToCSV(score.getVisitedRoomSet()));
        scorePrefs.putString("used_tunnels", longSetToCSV(score.getUsedTunnelSet()));
        scorePrefs.putString("collected_types", intSetToCSV(score.getCollectedItemTypeSet()));
        scorePrefs.putString("discovered_teleporters", intSetToCSV(score.getDiscoveredTeleporterSet()));
        scorePrefs.putInteger("trades", score.getTradesCompleted());
        scorePrefs.putInteger("doors", score.getDoorsOpened());
        scorePrefs.putInteger("cores", score.getCorePartsDelivered());
        scorePrefs.putInteger("deaths", score.getDeathCount());
        scorePrefs.flush();
    }

    public void loadScoreManager(ScoreManager score) {
        score.loadState(
            csvToIntSet(scorePrefs.getString("visited_rooms", "")),
            csvToLongSet(scorePrefs.getString("used_tunnels", "")),
            csvToIntSet(scorePrefs.getString("collected_types", "")),
            csvToIntSet(scorePrefs.getString("discovered_teleporters", "")),
            scorePrefs.getInteger("trades", 0),
            scorePrefs.getInteger("doors", 0),
            scorePrefs.getInteger("cores", 0),
            scorePrefs.getInteger("deaths", 0)
        );
    }

    // ---- Helpers ----

    private String intSetToCSV(Set<Integer> set) {
        StringBuilder sb = new StringBuilder();
        for (int v : set) {
            if (sb.length() > 0) sb.append(',');
            sb.append(v);
        }
        return sb.toString();
    }

    private String longSetToCSV(Set<Long> set) {
        StringBuilder sb = new StringBuilder();
        for (long v : set) {
            if (sb.length() > 0) sb.append(',');
            sb.append(v);
        }
        return sb.toString();
    }

    private Set<Integer> csvToIntSet(String csv) {
        Set<Integer> set = new HashSet<>();
        if (csv != null && !csv.isEmpty()) {
            for (String s : csv.split(",")) {
                try { set.add(Integer.parseInt(s.trim())); }
                catch (NumberFormatException ignored) {}
            }
        }
        return set;
    }

    private Set<Long> csvToLongSet(String csv) {
        Set<Long> set = new HashSet<>();
        if (csv != null && !csv.isEmpty()) {
            for (String s : csv.split(",")) {
                try { set.add(Long.parseLong(s.trim())); }
                catch (NumberFormatException ignored) {}
            }
        }
        return set;
    }
}
