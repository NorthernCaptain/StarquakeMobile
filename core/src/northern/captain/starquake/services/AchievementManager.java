package northern.captain.starquake.services;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import northern.captain.starquake.event.CoreDeliveredEvent;
import northern.captain.starquake.event.EventBus;
import northern.captain.starquake.event.GameEvent;
import northern.captain.starquake.event.GameOverEvent;
import northern.captain.starquake.event.RoomChangedEvent;
import northern.captain.starquake.world.CoreAssembly;
import northern.captain.starquake.world.TeleportRegistry;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

/**
 * Tracks achievements and per-game stats. Event-driven singleton.
 * Achievements persist forever in app_achievements prefs.
 * Game stats persist per-game in app_game_stats prefs (reset on new game).
 */
public class AchievementManager {
    private static AchievementManager instance;

    private final Preferences achievementPrefs;
    private final Preferences statsPrefs;
    private final Set<AchievementDef> unlocked = EnumSet.noneOf(AchievementDef.class);

    private TeleportRegistry teleportRegistry;

    // Per-game tracking
    private final HashSet<Integer> visitedRooms = new HashSet<>();
    private int enemiesKilled;
    private int deathCount;
    private float playTime;
    private boolean playing;

    public static void init() {
        instance = new AchievementManager();
    }

    public static AchievementManager get() {
        return instance;
    }

    public static void dispose() {
        if (instance != null) instance.saveStats();
        instance = null;
    }

    private AchievementManager() {
        achievementPrefs = Gdx.app.getPreferences("app_achievements");
        statsPrefs = Gdx.app.getPreferences("app_game_stats");
        loadUnlocked();
        loadStats();
    }

    // ---- Achievement unlocking ----

    public void tryUnlock(AchievementDef def) {
        if (unlocked.contains(def)) return;
        unlocked.add(def);
        achievementPrefs.putBoolean(def.name(), true);
        achievementPrefs.flush();
        GameServicesFactory.get().getProcessor().unlockAchievement(def);
    }

    public boolean isUnlocked(AchievementDef def) {
        return unlocked.contains(def);
    }

    public void submitScore(LeaderboardDef board, int score) {
        GameServicesFactory.get().getProcessor().submitScore(board, score);
    }

    // ---- Event registration ----

    public void registerEvents() {
        EventBus bus = EventBus.get();

        bus.register(GameEvent.Type.ROOM_CHANGED, e -> {
            RoomChangedEvent rc = (RoomChangedEvent) e;
            visitedRooms.add(rc.newRoom);
            int count = visitedRooms.size();
            if (count >= 2) tryUnlock(AchievementDef.FIRST_STEPS);
            if (count >= 100) tryUnlock(AchievementDef.EXPLORER);
            if (count >= 256) tryUnlock(AchievementDef.CARTOGRAPHER);
            if (count >= 512) tryUnlock(AchievementDef.FULL_MAP);
            if (rc.newRoom == CoreAssembly.CORE_ROOM) tryUnlock(AchievementDef.CORE_DISCOVERY);
        });

        bus.register(GameEvent.Type.ENTER_TELEPORT, e -> {
            tryUnlock(AchievementDef.BEAM_ME_UP);
            // Check if all 15 teleporters discovered via TeleportRegistry
            if (teleportRegistry != null) {
                boolean allVisited = true;
                for (int i = 0; i < TeleportRegistry.COUNT; i++) {
                    if (!teleportRegistry.isVisited(i)) { allVisited = false; break; }
                }
                if (allVisited) tryUnlock(AchievementDef.FREQUENT_FLYER);
            }
        });

        bus.register(GameEvent.Type.TUNNEL_TELEPORT, e -> {
            tryUnlock(AchievementDef.TUNNEL_VISION);
        });

        bus.register(GameEvent.Type.TRADE_COMPLETED, e -> {
            tryUnlock(AchievementDef.TRADER);
        });

        bus.register(GameEvent.Type.DOOR_OPENED, e -> {
            tryUnlock(AchievementDef.KEY_MASTER);
        });

        bus.register(GameEvent.Type.CORE_DELIVERED, e -> {
            CoreDeliveredEvent cd = (CoreDeliveredEvent) e;
            if (cd.totalDelivered >= 1) tryUnlock(AchievementDef.FIRST_DELIVERY);
            if (cd.totalDelivered >= 5) tryUnlock(AchievementDef.HALF_WAY_THERE);
        });

        bus.register(GameEvent.Type.BLOB_MOUNTED_PLATFORM, e -> {
            tryUnlock(AchievementDef.LIFT_OFF);
        });

        bus.register(GameEvent.Type.BLOB_DIED, e -> {
            deathCount++;
            saveStats();
        });

        bus.register(GameEvent.Type.GAME_OVER, e -> {
            GameOverEvent go = (GameOverEvent) e;
            if (go.win) {
                tryUnlock(AchievementDef.PLANET_SAVIOR);
                if (playTime < 30 * 60) tryUnlock(AchievementDef.SPEED_DEMON);
                if (deathCount == 0) tryUnlock(AchievementDef.NO_DEATH_RUN);
            }
        });
    }

    // ---- Per-game stats ----

    public void startNewGame() {
        visitedRooms.clear();
        enemiesKilled = 0;
        deathCount = 0;
        playTime = 0;
        playing = false;
        statsPrefs.clear();
        statsPrefs.flush();
    }

    public void update(float delta) {
        if (playing) playTime += delta;
    }

    public void setTeleportRegistry(TeleportRegistry registry) { this.teleportRegistry = registry; }

    public void onPause() { playing = false; saveStats(); }
    public void onResume() { playing = true; }

    public void saveStats() {
        StringBuilder sb = new StringBuilder();
        for (int room : visitedRooms) {
            if (sb.length() > 0) sb.append(',');
            sb.append(room);
        }
        statsPrefs.putString("rooms_visited", sb.toString());
        statsPrefs.putInteger("enemies_killed", enemiesKilled);
        statsPrefs.putInteger("death_count", deathCount);
        statsPrefs.putFloat("play_time", playTime);
        statsPrefs.flush();
    }

    public void loadStats() {
        String roomsStr = statsPrefs.getString("rooms_visited", "");
        visitedRooms.clear();
        if (!roomsStr.isEmpty()) {
            for (String s : roomsStr.split(",")) {
                try { visitedRooms.add(Integer.parseInt(s.trim())); }
                catch (NumberFormatException ignored) {}
            }
        }
        enemiesKilled = statsPrefs.getInteger("enemies_killed", 0);
        deathCount = statsPrefs.getInteger("death_count", 0);
        playTime = statsPrefs.getFloat("play_time", 0);
    }

    private void loadUnlocked() {
        for (AchievementDef def : AchievementDef.values()) {
            if (achievementPrefs.getBoolean(def.name(), false)) {
                unlocked.add(def);
            }
        }
    }

    // ---- Getters ----

    public int getRoomsVisited() { return visitedRooms.size(); }
    public int getEnemiesKilled() { return enemiesKilled; }
    public int getDeathCount() { return deathCount; }
    public float getPlayTime() { return playTime; }

    /** Called when an enemy is killed (future enemy system). */
    public void onEnemyKilled() {
        enemiesKilled++;
        if (enemiesKilled >= 20) tryUnlock(AchievementDef.SHARPSHOOTER);
    }
}
