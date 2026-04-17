package northern.captain.starquake.world;

import java.util.Random;

/**
 * Assigns unique 5-letter names to the 15 teleporters and tracks
 * which ones the player has discovered by visiting.
 */
public class TeleportRegistry {
    public static final int[] TELEPORT_ROOMS = {
        31, 40, 66, 150, 162, 213, 289, 343, 380, 433, 457, 461, 470, 499, 506
    };
    public static final int COUNT = TELEPORT_ROOMS.length;

    private final String[] names = new String[COUNT];
    private final boolean[] visited = new boolean[COUNT];

    /** Shuffle word pool and assign first 15 to teleporters. */
    public void initialize(long seed) {
        Random rng = new Random(seed);
        String[] pool = TeleportWords.WORDS.clone();
        for (int i = pool.length - 1; i > 0; i--) {
            int j = rng.nextInt(i + 1);
            String tmp = pool[i]; pool[i] = pool[j]; pool[j] = tmp;
        }
        for (int i = 0; i < COUNT; i++) {
            names[i] = pool[i];
            visited[i] = false;
        }
    }

    /** Returns the teleporter index (0-14) for a room, or -1 if not a teleporter. */
    public int indexOf(int roomIndex) {
        for (int i = 0; i < COUNT; i++) {
            if (TELEPORT_ROOMS[i] == roomIndex) return i;
        }
        return -1;
    }

    public void markVisited(int roomIndex) {
        int idx = indexOf(roomIndex);
        if (idx >= 0) {
            visited[idx] = true;
            if (SaveManager.get() != null) SaveManager.get().saveTeleportRegistry(this);
        }
    }

    public boolean isVisited(int index) {
        return index >= 0 && index < COUNT && visited[index];
    }

    public String getName(int index) {
        return (index >= 0 && index < COUNT) ? names[index] : null;
    }

    public void loadState(String[] loadedNames, boolean[] loadedVisited) {
        for (int i = 0; i < COUNT; i++) {
            names[i] = loadedNames[i];
            visited[i] = loadedVisited[i];
        }
    }

    public int getRoomForIndex(int index) {
        return (index >= 0 && index < COUNT) ? TELEPORT_ROOMS[index] : -1;
    }
}
