package northern.captain.starquake.world;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import northern.captain.starquake.Assets;
import northern.captain.starquake.audio.SoundManager;
import northern.captain.starquake.event.EventBus;
import northern.captain.starquake.event.GameEvent;
import northern.captain.starquake.services.AchievementManager;
import java.util.ArrayList;

/**
 * Manages enemy generation, update, rendering, projectile collision, and 2-room cache.
 * Enemies are generated deterministically per room (seeded by roomIndex).
 */
public class EnemyManager {
    private static final int CORE_ROOM = CoreAssembly.CORE_ROOM;

    // Sprite regions: shocker=effect 6-8, bolt=effect 12-14,
    // standard=effect 15-23 (types 0-2) + enemy 24-59 (types 3-14)
    private final TextureRegion[][] shockerFrames = new TextureRegion[1][3];
    private final TextureRegion[][] boltFrames = new TextureRegion[1][3];
    private final TextureRegion[][] standardFrames = new TextureRegion[15][3];

    // 2-room cache
    private int currentRoomIndex = -1;
    private int cachedRoomIndex = -1;
    private final ArrayList<Enemy> currentEnemies = new ArrayList<>();
    private final ArrayList<Enemy> cachedEnemies = new ArrayList<>();

    private final Assets assets;
    private float lastBlobX, lastBlobY;

    // Current room depth params (for respawning with random type)
    private int curMinType, curMaxType, curMinDamage, curMaxDamage;
    private float curMinSpeed, curMaxSpeed;

    public EnemyManager(Assets assets) {
        this.assets = assets;
        // Load sprite frames
        for (int f = 0; f < 3; f++) {
            shockerFrames[0][f] = assets.spritesAtlas.findRegion("effect", 6 + f);
            boltFrames[0][f] = assets.spritesAtlas.findRegion("effect", 12 + f);
        }
        // Standard: effect 15-23 (types 0-2), enemy 24-59 (types 3-14)
        for (int t = 0; t < 3; t++) {
            for (int f = 0; f < 3; f++) {
                standardFrames[t][f] = assets.spritesAtlas.findRegion("effect", 15 + t * 3 + f);
            }
        }
        for (int t = 0; t < 12; t++) {
            for (int f = 0; f < 3; f++) {
                standardFrames[3 + t][f] = assets.spritesAtlas.findRegion("enemy", 24 + t * 3 + f);
            }
        }
    }

    // ---- Room transitions ----

    public void onRoomChanged(Room oldRoom, Room newRoom) {
        int newIdx = newRoom.roomIndex;

        if (newIdx == cachedRoomIndex) {
            // Going back to cached room — swap
            ArrayList<Enemy> temp = new ArrayList<>(currentEnemies);
            int tempIdx = currentRoomIndex;
            currentEnemies.clear();
            currentEnemies.addAll(cachedEnemies);
            currentRoomIndex = cachedRoomIndex;
            cachedEnemies.clear();
            cachedEnemies.addAll(temp);
            cachedRoomIndex = tempIdx;
        } else {
            // New room — cache current, generate new
            cachedEnemies.clear();
            cachedEnemies.addAll(currentEnemies);
            cachedRoomIndex = currentRoomIndex;
            currentEnemies.clear();
            currentRoomIndex = newIdx;
            generateEnemies(newRoom);
        }
    }

    public void generateForRoom(Room room) {
        currentRoomIndex = room.roomIndex;
        currentEnemies.clear();
        generateEnemies(room);
    }

    // ---- Generation ----

    private void generateEnemies(Room room) {
        int idx = room.roomIndex;
        // Skip special rooms
        if (idx == CORE_ROOM) return;
        if (isSpecialRoom(idx)) return;

        int roomY = idx / Room.GRID_COLS;
        int depthFromCenter = Math.abs(roomY - 16);

        int count;
        int minDamage, maxDamage;
        float minSpeed, maxSpeed;
        int minType, maxType;

        if (depthFromCenter <= 3) {
            count = 3 + MathUtils.random(2);
            minDamage = 10; maxDamage = 13;
            minSpeed = 45; maxSpeed = 50;
            minType = 12; maxType = 14;
        } else if (depthFromCenter <= 7) {
            count = 2 + MathUtils.random(2);
            minDamage = 8; maxDamage = 10;
            minSpeed = 38; maxSpeed = 45;
            minType = 8; maxType = 14;
        } else if (depthFromCenter <= 11) {
            count = 2 + MathUtils.random(1);
            minDamage = 5; maxDamage = 8;
            minSpeed = 30; maxSpeed = 38;
            minType = 4; maxType = 10;
        } else {
            count = 2;
            minDamage = 3; maxDamage = 5;
            minSpeed = 25; maxSpeed = 30;
            minType = 0; maxType = 4;
        }

        // Store for respawn randomization
        curMinType = minType; curMaxType = maxType;
        curMinDamage = minDamage; curMaxDamage = maxDamage;
        curMinSpeed = minSpeed; curMaxSpeed = maxSpeed;

        // Possibly add a special enemy (depth 0-8)
        if (depthFromCenter <= 8 && MathUtils.random() < 0.3f) {
            if (MathUtils.randomBoolean()) {
                addShocker(room);
            } else {
                addBolt(room);
            }
        }

        // Standard enemies — random type/speed/damage within range
        for (int i = 0; i < count; i++) {
            int typeIdx = MathUtils.random(minType, Math.min(maxType, 14));
            float spd = minSpeed + MathUtils.random() * (maxSpeed - minSpeed);
            int dmg = MathUtils.random(minDamage, maxDamage);
            float[] pos = findRandomPosition(room);
            currentEnemies.add(new Enemy(Enemy.Category.STANDARD, typeIdx, pos[0], pos[1], spd, dmg));
        }
    }

    private void addShocker(Room room) {
        // Find a flat ground position — empty tile above solid tile
        int startCol = MathUtils.random(Room.TILE_COLS - 1);
        for (int dc = 0; dc < Room.TILE_COLS; dc++) {
            int col = (startCol + dc) % Room.TILE_COLS;
            for (int row = Room.TILE_ROWS - 2; row >= 0; row--) {
                // row in tile grid (0=top), convert to world Y (libGDX Y-up)
                float tileTopY = (Room.TILE_ROWS - 1 - row) * Room.TILE_H;
                float groundTopY = tileTopY; // top of the empty tile = where shocker stands
                float belowY = tileTopY - 1; // just below this tile = should be solid
                float checkX = col * Room.TILE_W + Room.TILE_W / 2f;
                if (!room.isSolidAt(checkX, tileTopY + 1) && room.isSolidAt(checkX, belowY)) {
                    currentEnemies.add(new Enemy(Enemy.Category.SHOCKER, 0,
                            col * Room.TILE_W + 8, tileTopY, 30, 0));
                    return;
                }
            }
        }
    }

    private void addBolt(Room room) {
        float[] pos = findRandomPosition(room);
        currentEnemies.add(new Enemy(Enemy.Category.BOLT, 0, pos[0], pos[1], 120, 0));
    }

    private float[] findRandomPosition(Room room) {
        for (int attempt = 0; attempt < 50; attempt++) {
            float px = 16f + MathUtils.random(Room.WIDTH - 48f);
            float py = 16f + MathUtils.random(Room.HEIGHT - 48f);
            if (!room.isSolidAt(px + 8, py + 8)) {
                return new float[]{px, py};
            }
        }
        return new float[]{Room.WIDTH / 2f, Room.HEIGHT / 2f};
    }

    private static final float MIN_RESPAWN_DIST = 60f;

    private Enemy respawnEnemy(Room room) {
        // Create a brand new random enemy with current room's depth params
        int typeIdx = MathUtils.random(curMinType, Math.min(curMaxType, 14));
        float spd = curMinSpeed + MathUtils.random() * (curMaxSpeed - curMinSpeed);
        int dmg = MathUtils.random(curMinDamage, curMaxDamage);

        // Find position at least 60px from blob
        float px = Room.WIDTH / 2f, py = Room.HEIGHT / 2f;
        for (int attempt = 0; attempt < 50; attempt++) {
            float tx = 16f + MathUtils.random(Room.WIDTH - 48f);
            float ty = 16f + MathUtils.random(Room.HEIGHT - 48f);
            if (!room.isSolidAt(tx + 8, ty + 8)) {
                float dx = tx - lastBlobX;
                float dy = ty - lastBlobY;
                if (dx * dx + dy * dy >= MIN_RESPAWN_DIST * MIN_RESPAWN_DIST) {
                    px = tx;
                    py = ty;
                    break;
                }
            }
        }

        return new Enemy(Enemy.Category.STANDARD, typeIdx, px, py, spd, dmg);
    }

    private boolean isSpecialRoom(int roomIndex) {
        // Skip teleporter rooms and rooms with lifts
        for (int tr : TeleportRegistry.TELEPORT_ROOMS) {
            if (tr == roomIndex) return true;
        }
        return false;
    }

    // ---- Update ----

    public void update(float delta, Room room, Blob blob, GameState gameState) {
        lastBlobX = blob.x;
        lastBlobY = blob.y;
        for (int i = 0; i < currentEnemies.size(); i++) {
            Enemy e = currentEnemies.get(i);

            if (e.canRespawn()) {
                currentEnemies.set(i, respawnEnemy(room));
                continue;
            }

            e.update(delta, room, blob);

            // Blob collision — skip if immune, in transition, or lifting
            if (e.alive && e.overlapsBlob(blob) && !blob.isImmune()
                    && blob.state != Blob.State.TRANSITION && blob.state != Blob.State.LIFTING) {
                if (e.category == Enemy.Category.SHOCKER || e.category == Enemy.Category.BOLT) {
                    blob.die();
                } else if (e.contactCooldown <= 0) {
                    gameState.damage(e.damage);
                    e.startContactCooldown();
                    if (gameState.getHealth() <= 0) {
                        blob.die();
                    }
                }
            }
        }
    }

    // ---- Projectile collision ----

    /**
     * Check if a projectile at the given bounds hits any enemy.
     * Returns the hit enemy (for score/effects), or null.
     */
    public Enemy checkProjectileHit(float px, float py, float pw, float ph) {
        for (Enemy e : currentEnemies) {
            if (!e.alive) continue;
            if (e.overlapsRect(px, py, pw, ph)) {
                e.hp--;
                if (e.hp <= 0) {
                    e.kill();
                    SoundManager.play(SoundManager.SoundType.EXPLOSION);
                    EventBus.get().post(GameEvent.ENEMY_KILLED);
                    AchievementManager am = AchievementManager.get();
                    if (am != null) am.onEnemyKilled();
                }
                return e;
            }
        }
        return null;
    }

    // ---- Render ----

    public void render(SpriteBatch batch, float delta) {
        for (Enemy e : currentEnemies) {
            if (!e.alive) continue;

            TextureRegion frame = getFrame(e);
            if (frame != null) {
                batch.draw(frame, e.x, e.y, Enemy.SIZE, Enemy.SIZE);
            }
        }
    }

    private TextureRegion getFrame(Enemy e) {
        int f = e.getAnimFrame();
        switch (e.category) {
            case SHOCKER: return shockerFrames[0][f];
            case BOLT:    return boltFrames[0][f];
            case STANDARD:
                if (e.typeIndex >= 0 && e.typeIndex < standardFrames.length) {
                    return standardFrames[e.typeIndex][f];
                }
                return null;
            default: return null;
        }
    }

    public void clear() {
        currentEnemies.clear();
        cachedEnemies.clear();
        currentRoomIndex = -1;
        cachedRoomIndex = -1;
    }
}
