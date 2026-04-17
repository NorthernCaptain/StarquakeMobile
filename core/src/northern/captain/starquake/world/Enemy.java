package northern.captain.starquake.world;

import com.badlogic.gdx.math.MathUtils;

/**
 * A single enemy entity. Three categories with different behaviors:
 * SHOCKER (ground, instant-kill, 2HP), BOLT (freeze-move, instant-kill, 1HP),
 * STANDARD (flying, health drain, 1HP, 5 movement patterns).
 */
public class Enemy implements Collidable {
    public enum Category { SHOCKER, BOLT, STANDARD }

    // Movement patterns for standard enemies
    public enum Pattern { SINE_WAVE, ZIGZAG, SPIRAL, LURKER, WEAVER }

    public static final float SIZE = 16;
    private static final float ANIM_FRAME_TIME = 0.1f;
    private static final float CONTACT_COOLDOWN = 0.5f;
    private static final float RESPAWN_TIME = 5f;
    private static final float BOLT_DASH_SPEED = 120f;

    public final Category category;
    public final Pattern pattern;
    public final int typeIndex;      // 0-14 for standard
    public final int damage;         // health drain per contact (standard only)
    public final float speed;

    public float x, y;
    public float vx, vy;
    public boolean alive = true;
    public int hp;
    public float respawnTimer;
    public float contactCooldown;
    public float animTimer;

    // Bolt-specific
    public float freezeTimer;
    public boolean frozen;
    public float targetX, targetY;

    // Pattern-specific timers
    public float patternTimer;       // general-purpose timer for pattern state
    public float patternPhase;       // phase offset for sine/spiral

    public Enemy(Category category, int typeIndex, float x, float y, float speed, int damage) {
        this.category = category;
        this.typeIndex = typeIndex;
        this.x = x;
        this.y = y;
        this.speed = speed;
        this.damage = damage;

        switch (category) {
            case SHOCKER:
                hp = 2;
                pattern = Pattern.SINE_WAVE; // unused for shocker
                vx = speed * (MathUtils.randomBoolean() ? 1 : -1);
                vy = 0;
                break;
            case BOLT:
                hp = 1;
                pattern = Pattern.SINE_WAVE; // unused for bolt
                frozen = true;
                freezeTimer = MathUtils.random(1.5f, 2.5f);
                break;
            default:
                hp = 1;
                pattern = Pattern.values()[typeIndex % 5];
                patternPhase = MathUtils.random(MathUtils.PI2);
                patternTimer = 0;
                // Initial velocity — random direction
                float angle = MathUtils.random(MathUtils.PI2);
                vx = MathUtils.cos(angle) * speed;
                vy = MathUtils.sin(angle) * speed;
                break;
        }
    }

    public void update(float delta, Room room, Blob blob) {
        if (!alive) {
            respawnTimer -= delta;
            return;
        }

        animTimer += delta;
        contactCooldown = Math.max(0, contactCooldown - delta);

        switch (category) {
            case SHOCKER: updateShocker(delta, room); break;
            case BOLT:    updateBolt(delta, room); break;
            case STANDARD: updateStandard(delta, room, blob); break;
        }
    }

    // ---- Shocker: ground slide ----

    private void updateShocker(float delta, Room room) {
        float newX = x + vx * delta;

        // Check wall ahead
        float checkX = vx > 0 ? newX + SIZE : newX;
        if (room.isSolidAt(checkX, y + SIZE / 2f)) {
            vx = -vx;
            return;
        }

        // Check floor below — reverse if edge (no ground ahead)
        float floorCheckX = vx > 0 ? newX + SIZE - 2 : newX + 2;
        if (!room.isSolidAt(floorCheckX, y - 1)) {
            vx = -vx;
            return;
        }

        x = newX;
    }

    // ---- Bolt: freeze-move ----

    private void updateBolt(float delta, Room room) {
        if (frozen) {
            freezeTimer -= delta;
            if (freezeTimer <= 0) {
                // Pick random non-solid target
                for (int attempt = 0; attempt < 20; attempt++) {
                    targetX = MathUtils.random(16f, Room.WIDTH - 16f);
                    targetY = MathUtils.random(16f, Room.HEIGHT - 16f);
                    if (!room.isSolidAt(targetX + 8, targetY + 8)) break;
                }
                float dx = targetX - x;
                float dy = targetY - y;
                float dist = (float) Math.sqrt(dx * dx + dy * dy);
                if (dist > 1) {
                    vx = (dx / dist) * BOLT_DASH_SPEED;
                    vy = (dy / dist) * BOLT_DASH_SPEED;
                }
                frozen = false;
            }
            return;
        }

        // Dashing toward target
        float dx = targetX - x;
        float dy = targetY - y;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);

        if (dist < 4f || room.isSolidAt(x + SIZE / 2f + vx * delta, y + SIZE / 2f + vy * delta)) {
            // Arrived or hit wall — freeze again
            frozen = true;
            freezeTimer = MathUtils.random(1.5f, 2.5f);
            vx = 0;
            vy = 0;
        } else {
            x += vx * delta;
            y += vy * delta;
        }
    }

    // ---- Standard: blob-aware movement patterns ----

    private void updateStandard(float delta, Room room, Blob blob) {
        patternTimer += delta;

        float blobCX = blob.x + Blob.SIZE / 2f;
        float blobCY = blob.y + Blob.SIZE / 2f;
        float toDirX = blobCX - (x + SIZE / 2f);
        float toDirY = blobCY - (y + SIZE / 2f);
        float toDist = (float) Math.sqrt(toDirX * toDirX + toDirY * toDirY);
        if (toDist > 1) { toDirX /= toDist; toDirY /= toDist; }

        switch (pattern) {
            case SINE_WAVE:  updateSineWave(delta, toDirX, toDirY); break;
            case ZIGZAG:     updateZigzag(delta, toDirX, toDirY); break;
            case SPIRAL:     updateSpiral(delta, toDirX, toDirY); break;
            case LURKER:     updateLurker(delta, toDirX, toDirY); break;
            case WEAVER:     updateWeaver(delta, toDirX, toDirY); break;
        }

        // Apply velocity with wall bounce
        float newX = x + vx * delta;
        float newY = y + vy * delta;

        boolean hitX = room.isSolidAt(vx > 0 ? newX + SIZE : newX, y + SIZE / 2f);
        boolean hitY = room.isSolidAt(x + SIZE / 2f, vy > 0 ? newY + SIZE : newY);

        if (hitX) { vx = -vx; newX = x; }
        if (hitY) { vy = -vy; newY = y; }

        // Clamp to room bounds
        newX = Math.max(0, Math.min(newX, Room.WIDTH - SIZE));
        newY = Math.max(0, Math.min(newY, Room.HEIGHT - SIZE));

        x = newX;
        y = newY;
    }

    private void updateSineWave(float delta, float dirX, float dirY) {
        // Mostly random movement with gentle sine wave, slight blob bias
        float perpX = -dirY;
        float perpY = dirX;
        float wave = MathUtils.sin(patternTimer * 3f + patternPhase) * 0.9f;
        float bias = 0.15f; // only 15% toward blob
        vx = (dirX * bias + perpX * wave + MathUtils.sin(patternTimer * 1.7f) * 0.5f) * speed;
        vy = (dirY * bias + perpY * wave + MathUtils.cos(patternTimer * 1.3f) * 0.5f) * speed;
    }

    private void updateZigzag(float delta, float dirX, float dirY) {
        // Random dashes, only slightly biased toward blob
        if (patternTimer > 0.5f + patternPhase * 0.05f) {
            patternTimer = 0;
            // Mostly random angle, with 20% pull toward blob
            float randAngle = MathUtils.random(MathUtils.PI2);
            float blobAngle = MathUtils.atan2(dirY, dirX);
            float angle = randAngle + (blobAngle - randAngle) * 0.2f;
            vx = MathUtils.cos(angle) * speed;
            vy = MathUtils.sin(angle) * speed;
        }
    }

    private void updateSpiral(float delta, float dirX, float dirY) {
        // Circle with very gentle drift toward blob
        float circleAngle = patternTimer * 2.5f + patternPhase;
        float circleVX = MathUtils.cos(circleAngle) * speed * 0.85f;
        float circleVY = MathUtils.sin(circleAngle) * speed * 0.85f;
        vx = circleVX + dirX * speed * 0.1f;
        vy = circleVY + dirY * speed * 0.1f;
    }

    private void updateLurker(float delta, float dirX, float dirY) {
        // Mostly idle with random wandering, occasional random burst
        float burstCycle = patternTimer % 3.5f;
        if (burstCycle < 0.2f) {
            // Burst: random direction, only slightly toward blob
            float angle = MathUtils.random(MathUtils.PI2);
            float blobAngle = MathUtils.atan2(dirY, dirX);
            angle = angle + (blobAngle - angle) * 0.3f;
            vx = MathUtils.cos(angle) * speed * 2f;
            vy = MathUtils.sin(angle) * speed * 2f;
        } else {
            // Random wander with tiny blob pull
            vx = (MathUtils.sin(patternTimer * 1.1f + patternPhase) * 0.8f + dirX * 0.1f) * speed * 0.3f;
            vy = (MathUtils.cos(patternTimer * 0.9f + patternPhase) * 0.8f + dirY * 0.1f) * speed * 0.3f;
        }
    }

    private void updateWeaver(float delta, float dirX, float dirY) {
        // Alternate between random direction and perpendicular, slight blob pull
        float cycle = patternTimer % 1.5f;
        if (cycle < 0.7f) {
            // Random direction with slight blob bias
            vx = (MathUtils.sin(patternTimer * 2f + patternPhase) * 0.7f + dirX * 0.15f) * speed;
            vy = (MathUtils.cos(patternTimer * 1.5f + patternPhase) * 0.7f + dirY * 0.15f) * speed;
        } else {
            // Perpendicular weave
            float side = MathUtils.sin(patternPhase) > 0 ? 1 : -1;
            vx = -dirY * side * speed * 0.8f;
            vy = dirX * side * speed * 0.8f;
        }
    }

    // ---- Animation ----

    public int getAnimFrame() {
        return ((int) (animTimer / ANIM_FRAME_TIME)) % 3;
    }

    // ---- Collidable ----

    @Override public Collidable.Type getType() { return Collidable.Type.ENEMY; }
    @Override public float getX() { return x; }
    @Override public float getY() { return y; }
    @Override public float getWidth() { return SIZE; }
    @Override public float getHeight() { return SIZE; }
    @Override public float getBottom() { return y; }

    public boolean overlapsBlob(Blob blob) {
        float bx = blob.x, by = blob.getBottom();
        float bw = Blob.SIZE, bh = blob.getHeight();
        return x < bx + bw && x + SIZE > bx && y < by + bh && y + SIZE > by;
    }

    public boolean overlapsRect(float rx, float ry, float rw, float rh) {
        return x < rx + rw && x + SIZE > rx && y < ry + rh && y + SIZE > ry;
    }

    public boolean canRespawn() {
        return category == Category.STANDARD && !alive && respawnTimer <= 0;
    }

    public void kill() {
        alive = false;
        if (category == Category.STANDARD) {
            respawnTimer = RESPAWN_TIME;
        }
    }

    public void startContactCooldown() {
        contactCooldown = CONTACT_COOLDOWN;
    }
}
