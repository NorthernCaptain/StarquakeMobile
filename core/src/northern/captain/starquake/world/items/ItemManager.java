package northern.captain.starquake.world.items;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;
import northern.captain.starquake.Assets;
import northern.captain.starquake.world.Room;

import java.util.ArrayList;
import java.util.Random;

/**
 * Global item state. Responsible for initial placement, room population,
 * collection tracking, FIFO drops, and boost respawn.
 */
public class ItemManager {
    private static final float BOOST_RESPAWN_TIME = 60f;

    private final Assets assets;
    private final IntMap<Array<ItemPlacement>> placements = new IntMap<>();
    private final ArrayList<RespawnEntry> respawnQueue = new ArrayList<>();
    private Room activeRoom;

    public ItemManager(Assets assets) {
        this.assets = assets;
    }

    // ---- Initialization ----

    /** Scatter all items across the map for a new game. */
    public void initializeGame(long seed) {
        placements.clear();
        respawnQueue.clear();
        Random rng = new Random(seed);

        // Access Card: 1 item, random room from candidates
        int[] cardRooms = {8, 40, 168, 182};
        placeItem(ItemType.ACCESS_CARD, cardRooms[rng.nextInt(cardRooms.length)], rng);

        // Key: 1 item, random room from candidates
        int[] keyRooms = {150, 198, 200, 246};
        placeItem(ItemType.KEY, keyRooms[rng.nextInt(keyRooms.length)], rng);

        // DEBUG: access card in room 176 for space lock testing
        placeItem(ItemType.ACCESS_CARD, 176, rng);

        // Required core elements (7 entries with 2 candidate rooms each)
        int[][] coreRoomPairs = {
            {436, 422}, {236, 222}, {52, 16}, {502, 504},
            {296, 314}, {72, 106}, {310, 278}
        };

        // Choose 5 random core part types for the missing core slots
        ItemType[] partPool = getPartPool();
        shuffle(partPool, rng);
        // First 5 of the shuffled pool are "required" core parts
        // Entries 0-4 use required parts, entries 5-6 use random parts
        for (int i = 0; i < coreRoomPairs.length; i++) {
            int[] pair = coreRoomPairs[i];
            int room = pair[rng.nextInt(2)];
            ItemType part = partPool[i % partPool.length];
            placeItem(part, room, rng);
        }

        // Extra core elements (11 entries, random sprites — decoys/trade material)
        int[][] extraRoomPairs = {
            {56, 42}, {416, 352}, {140, 14}, {266, 316}, {476, 482},
            {84, 86}, {478, 62}, {80, 82}, {226, 194}, {114, 116}, {466, 372}
        };
        for (int[] pair : extraRoomPairs) {
            int room = pair[rng.nextInt(2)];
            ItemType part = partPool[rng.nextInt(partPool.length)];
            placeItem(part, room, rng);
        }

        // Boost items: 224 + 32 pyramids = 256 total, distributed randomly
        distributeBoosts(rng);
    }

    private ItemType[] getPartPool() {
        // All 15 collectible core part types
        return new ItemType[]{
            ItemType.PART_A0, ItemType.PART_A1, ItemType.PART_A2,
            ItemType.PART_A3, ItemType.PART_A4, ItemType.PART_A5,
            ItemType.PART_B0, ItemType.PART_B1, ItemType.PART_B2,
            ItemType.PART_B3, ItemType.PART_B4, ItemType.PART_B5,
            ItemType.PART_B6, ItemType.PART_B7, ItemType.PART_B8
        };
    }

    private void distributeBoosts(Random rng) {
        // Build the boost type list: 96 health + 32 laser + 32 platform + 32 life + 32 universal + 32 pyramid
        // BBC had 3 energy variants (32 each). We split as: 32 small, 32 full, 32 small (alternating)
        ItemType[] boostTypes = new ItemType[256];
        int idx = 0;
        for (int i = 0; i < 48; i++) boostTypes[idx++] = ItemType.HEALTH_SMALL;
        for (int i = 0; i < 48; i++) boostTypes[idx++] = ItemType.HEALTH_FULL;
        for (int i = 0; i < 16; i++) boostTypes[idx++] = ItemType.LASER_SMALL;
        for (int i = 0; i < 16; i++) boostTypes[idx++] = ItemType.LASER_FULL;
        for (int i = 0; i < 16; i++) boostTypes[idx++] = ItemType.PLATFORM_SMALL;
        for (int i = 0; i < 16; i++) boostTypes[idx++] = ItemType.PLATFORM_FULL;
        for (int i = 0; i < 32; i++) boostTypes[idx++] = ItemType.EXTRA_LIFE;
        for (int i = 0; i < 32; i++) boostTypes[idx++] = ItemType.UNIVERSAL_BOOST;
        for (int i = 0; i < 32; i++) boostTypes[idx++] = ItemType.PYRAMID;
        shuffle(boostTypes, rng);

        // Distribute across rooms (1 per room max, skip rooms that already have items)
        boolean[] used = new boolean[512];
        // Mark rooms already used by core elements / key / card
        for (IntMap.Entry<Array<ItemPlacement>> entry : placements) {
            used[entry.key] = true;
        }

        int boostIdx = 0;
        for (int attempt = 0; attempt < 4000 && boostIdx < boostTypes.length; attempt++) {
            int room = rng.nextInt(512);
            if (used[room]) continue;
            int tilePos = findFloorTile(room, rng);
            if (tilePos < 0) continue;

            used[room] = true;
            int col = tilePos % 8;
            int row = tilePos / 8;
            addPlacement(room, new ItemPlacement(boostTypes[boostIdx], col, row));
            boostIdx++;
        }

        if (boostIdx < boostTypes.length) {
            Gdx.app.log("ItemManager", "Warning: placed " + boostIdx + "/" + boostTypes.length + " boost items");
        }
    }

    /** Find a non-solid tile above a solid tile (floor position) in the given room. */
    private int findFloorTile(int roomIndex, Random rng) {
        // Scan columns in random order, find a tile that's empty above solid
        int startCol = rng.nextInt(8);
        for (int dc = 0; dc < 8; dc++) {
            int col = (startCol + dc) % 8;
            for (int row = 4; row >= 1; row--) { // row 0=top, 5=bottom; check rows 1-4
                int tileAbove = assets.getTileIdAt(roomIndex, col, row);
                int tileBelow = assets.getTileIdAt(roomIndex, col, row + 1);
                if (tileAbove >= 0 && assets.isTileNonSolid(tileAbove)
                    && tileBelow >= 0 && !assets.isTileNonSolid(tileBelow)) {
                    return row * 8 + col;
                }
            }
        }
        return -1;
    }

    private void placeItem(ItemType type, int roomIndex, Random rng) {
        int tilePos = findFloorTile(roomIndex, rng);
        if (tilePos < 0) {
            // Fallback: place at center bottom
            tilePos = 4 * 8 + 4; // row 4, col 4
        }
        int col = tilePos % 8;
        int row = tilePos / 8;
        addPlacement(roomIndex, new ItemPlacement(type, col, row));
    }

    private void addPlacement(int roomIndex, ItemPlacement p) {
        Array<ItemPlacement> list = placements.get(roomIndex);
        if (list == null) {
            list = new Array<>(4);
            placements.put(roomIndex, list);
        }
        list.add(p);
    }

    // ---- Room lifecycle ----

    /** Create ItemPickup game objects for all items in the given room. */
    public void populateRoom(Room room) {
        this.activeRoom = room;
        Array<ItemPlacement> list = placements.get(room.roomIndex);
        if (list == null) return;
        for (int i = 0; i < list.size; i++) {
            ItemPlacement p = list.get(i);
            ItemPickup pickup = createPickup(p.type, p.tileCol, p.tileRow);
            if (pickup != null) {
                room.addObject(pickup);
            }
        }
    }

    // ---- Collection & drops ----

    /** Called when an item is collected. Removes from placements, schedules respawn if boost. */
    public void onItemCollected(ItemPickup item) {
        if (activeRoom == null) return;
        int roomIndex = activeRoom.roomIndex;
        Array<ItemPlacement> list = placements.get(roomIndex);
        if (list == null) return;

        for (int i = list.size - 1; i >= 0; i--) {
            ItemPlacement p = list.get(i);
            if (p.type == item.itemType && p.tileCol == item.tileCol && p.tileRow == item.tileRow) {
                list.removeIndex(i);

                // Schedule respawn for boost items
                if (item.itemType.isBoost()) {
                    respawnQueue.add(new RespawnEntry(
                        item.itemType, roomIndex, item.tileCol, item.tileRow, BOOST_RESPAWN_TIME));
                }
                break;
            }
        }
    }

    /** Drop an item into a room (FIFO overflow). */
    public void dropItem(ItemType type, int roomIndex, int tileCol, int tileRow) {
        ItemPlacement p = new ItemPlacement(type, tileCol, tileRow);
        addPlacement(roomIndex, p);

        // If player is in this room, create the pickup object immediately
        if (activeRoom != null && activeRoom.roomIndex == roomIndex) {
            ItemPickup pickup = createPickup(type, tileCol, tileRow);
            if (pickup != null) {
                activeRoom.addObject(pickup);
            }
        }
    }

    // ---- Respawn ----

    /** Tick respawn timers. Call each frame from GameScreen. */
    public void update(float delta) {
        for (int i = respawnQueue.size() - 1; i >= 0; i--) {
            RespawnEntry entry = respawnQueue.get(i);
            entry.timer -= delta;
            if (entry.timer <= 0) {
                respawnQueue.remove(i);
                // Re-add placement
                addPlacement(entry.roomIndex, new ItemPlacement(entry.type, entry.tileCol, entry.tileRow));

                // If player is currently in this room, create the pickup
                if (activeRoom != null && activeRoom.roomIndex == entry.roomIndex) {
                    ItemPickup pickup = createPickup(entry.type, entry.tileCol, entry.tileRow);
                    if (pickup != null) {
                        activeRoom.addObject(pickup);
                    }
                }
            }
        }
    }

    // ---- Factory ----

    private ItemPickup createPickup(ItemType type, int tileCol, int tileRow) {
        if (type.isBoost())           return new BoostPickup(assets, tileCol, tileRow, type);
        if (type.isPyramid())         return new CheopsPyramid(assets, tileCol, tileRow);
        if (type == ItemType.ACCESS_CARD) return new AccessCardPickup(assets, tileCol, tileRow);
        if (type == ItemType.KEY)     return new KeyPickup(assets, tileCol, tileRow);
        if (type.isCorePart())        return new CorePartPickup(assets, tileCol, tileRow, type);
        return null;
    }

    // ---- Helper ----

    private static <T> void shuffle(T[] array, Random rng) {
        for (int i = array.length - 1; i > 0; i--) {
            int j = rng.nextInt(i + 1);
            T tmp = array[i];
            array[i] = array[j];
            array[j] = tmp;
        }
    }

    // ---- Data classes ----

    public static class ItemPlacement {
        public final ItemType type;
        public final int tileCol, tileRow;

        public ItemPlacement(ItemType type, int tileCol, int tileRow) {
            this.type = type;
            this.tileCol = tileCol;
            this.tileRow = tileRow;
        }
    }

    private static class RespawnEntry {
        final ItemType type;
        final int roomIndex, tileCol, tileRow;
        float timer;

        RespawnEntry(ItemType type, int roomIndex, int tileCol, int tileRow, float timer) {
            this.type = type;
            this.roomIndex = roomIndex;
            this.tileCol = tileCol;
            this.tileRow = tileRow;
            this.timer = timer;
        }
    }
}
