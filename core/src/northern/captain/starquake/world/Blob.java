package northern.captain.starquake.world;

import northern.captain.starquake.event.EventBus;
import northern.captain.starquake.event.GameEvent;

/**
 * BLOB player character — position, velocity, physics, collision.
 *
 * All units are game-world pixels (room = 256×144).
 * Position (x, y) is the bottom-left corner of the hitbox.
 * Y=0 is the bottom of the room (libGDX convention).
 *
 * Animation state machine handles turning: BLOB must complete a rotation
 * animation before moving in the new direction.
 *
 * FLYING state: BLOB rides a hover platform. The platform adds 8px below,
 * making the total height 24px. No gravity, 4-directional movement,
 * no walk animation (idle frame + rotate only).
 */
public class Blob implements Collidable {
    public static final float SIZE = 16;
    public static final float WALK_SPEED = 48;    // units/sec
    public static final float FLY_SPEED = 64;     // units/sec
    public static final float GRAVITY = 180;      // units/sec²
    public static final float TERMINAL_VEL = 120; // units/sec (downward)
    public static final float PLATFORM_HEIGHT = 8; // hover platform below BLOB

    public enum Exit {
        NONE(0, 0), LEFT(-1, 0), RIGHT(1, 0), UP(0, -1), DOWN(0, 1);
        public final int dx, dy;
        Exit(int dx, int dy) { this.dx = dx; this.dy = dy; }
    }

    /**
     * Animation state:
     * - IDLE: standing still, showing idle frame for current facing
     * - WALK: moving, cycling walk frames for current facing
     * - TURNING: rotating from one facing to the other, no horizontal movement
     * - FLYING: riding hover platform, no gravity, 4-directional, rotate only
     */
    public enum State { IDLE, WALK, TURNING, FLYING, TRANSITION, LIFTING }

    /** Rendered under BLOB when flying. Set by the object that grants flight. */
    public Renderable attachment;

    private static final float IMMUNITY_DURATION = 2.0f;
    private float immunityTimer;

    public float x, y;
    public float vx, vy;
    public boolean onGround;
    public boolean blockedH;
    public boolean facingRight = true;
    public State state = State.IDLE;
    public Exit exit = Exit.NONE;

    public Blob(float x, float y) {
        this.x = x;
        this.y = y;
        this.state = State.TRANSITION; // birth effect plays first
    }

    @Override public Collidable.Type getType() { return Collidable.Type.BLOB; }
    @Override public float getX() { return x; }
    @Override public float getY() { return y; }
    @Override public float getWidth() { return SIZE; }
    @Override public float getHeight() { return getCollisionHeight(); }

    public float getCollisionHeight() {
        return state == State.FLYING ? SIZE + PLATFORM_HEIGHT : SIZE;
    }

    @Override
    public float getBottom() {
        return state == State.FLYING ? y - PLATFORM_HEIGHT : y;
    }

    public void startFlying() {
        state = State.FLYING;
        vy = 0;
        vx = 0;
    }

    public void stopFlying() {
        state = State.IDLE;
        vx = 0;
        vy = 0;
        attachment = null;
    }

    public void startTransition() {
        state = State.TRANSITION;
        vx = 0;
        vy = 0;
    }

    public void endTransition() {
        endTransition(true);
    }

    public void endTransition(boolean grantImmunity) {
        state = State.IDLE;
        if (grantImmunity) immunityTimer = IMMUNITY_DURATION;
    }

    /** Called when something kills BLOB. Posts BLOB_DIED event. */
    public void die() {
        if (state == State.TRANSITION) return;
        if (immunityTimer > 0) return;
        attachment = null;
        startTransition();
        EventBus.get().post(GameEvent.BLOB_DIED);
    }

    public boolean isImmune() {
        return immunityTimer > 0;
    }

    /** Returns current opacity (0-1) accounting for immunity flash. */
    public float getAlpha() {
        if (immunityTimer <= 0) return 1f;
        // Flash: sine wave between 0.2 and 0.8, ~5 cycles per second
        return 0.5f + 0.3f * (float) Math.sin(immunityTimer * Math.PI * 10);
    }

    public boolean isInTransition() {
        return state == State.TRANSITION;
    }

    public void startLifting() {
        state = State.LIFTING;
        vx = 0;
        vy = 0;
    }

    public void stopLifting() {
        state = State.IDLE;
    }

    public void applyInput(boolean wantLeft, boolean wantRight, boolean wantUp, boolean wantDown) {
        applyInput(wantLeft, wantRight, wantUp, wantDown, 0, 0, false);
    }

    public void applyInput(boolean wantLeft, boolean wantRight, boolean wantUp, boolean wantDown,
                           float analogX, float analogY, boolean analogActive) {
        if (state == State.TRANSITION || state == State.LIFTING) return;
        if (state == State.FLYING) {
            if (analogActive) {
                applyAnalogFlyingInput(analogX, analogY);
            } else {
                applyFlyingInput(wantLeft, wantRight, wantUp, wantDown);
            }
            return;
        }

        if (state == State.TURNING) return;

        if (wantRight && !wantLeft) {
            if (!facingRight) {
                state = State.TURNING;
            } else {
                state = State.WALK;
                vx = WALK_SPEED;
            }
        } else if (wantLeft && !wantRight) {
            if (facingRight) {
                state = State.TURNING;
            } else {
                state = State.WALK;
                vx = -WALK_SPEED;
            }
        } else {
            state = State.IDLE;
            vx = 0;
        }
    }

    private void applyFlyingInput(boolean wantLeft, boolean wantRight, boolean wantUp, boolean wantDown) {
        if (wantRight && !wantLeft) {
            facingRight = true;
            vx = FLY_SPEED;
        } else if (wantLeft && !wantRight) {
            facingRight = false;
            vx = -FLY_SPEED;
        } else {
            vx = 0;
        }

        if (wantUp && !wantDown) {
            vy = FLY_SPEED;
        } else if (wantDown && !wantUp) {
            vy = -FLY_SPEED;
        } else {
            vy = 0;
        }
    }

    private void applyAnalogFlyingInput(float analogX, float analogY) {
        float len = (float) Math.sqrt(analogX * analogX + analogY * analogY);
        if (len < 0.15f) {
            vx = 0;
            vy = 0;
            return;
        }
        // Always fly at max speed, direction only
        vx = (analogX / len) * FLY_SPEED;
        vy = (analogY / len) * FLY_SPEED;
        if (analogX > 0.1f) facingRight = true;
        else if (analogX < -0.1f) facingRight = false;
    }

    /** Called by BlobRenderer when the turn animation completes. */
    public void onTurnComplete() {
        facingRight = !facingRight;
        state = State.IDLE;
    }

    public void update(float delta, Room room) {
        if (state == State.TRANSITION || state == State.LIFTING) return;
        if (immunityTimer > 0) immunityTimer -= delta;

        float bottom = getBottom();
        float top = y + SIZE;
        boolean flying = (state == State.FLYING);

        exit = Exit.NONE;

        if (!flying) {
            if (state == State.TURNING) vx = 0;
            vy -= GRAVITY * delta;
            if (vy < -TERMINAL_VEL) vy = -TERMINAL_VEL;
        }

        // --- Horizontal collision ---
        blockedH = false;
        if (vx > 0) {
            float probeX = x + vx * delta;
            if (isSolidColumn(room, probeX + SIZE - 1, bottom, top)) {
                int ix = (int) (probeX + SIZE - 1);
                while (ix > x + SIZE && isSolidColumn(room, ix, bottom, top)) ix--;
                x = ix - SIZE + 1;
                vx = 0;
                blockedH = true;
                if (!flying) state = State.IDLE;
            } else {
                x = probeX;
            }
        } else if (vx < 0) {
            float probeX = x + vx * delta;
            if (isSolidColumn(room, probeX, bottom, top)) {
                int ix = (int) probeX;
                while (ix < x && isSolidColumn(room, ix, bottom, top)) ix++;
                x = ix;
                vx = 0;
                blockedH = true;
                if (!flying) state = State.IDLE;
            } else {
                x = probeX;
            }
        }

        // --- Vertical collision ---
        float newY = y + vy * delta;
        float newBottom = flying ? newY - PLATFORM_HEIGHT : newY;
        onGround = false;

        if (vy <= 0) {
            float footL = x + 2;
            float footR = x + SIZE - 3;
            if (room.isSolidAt(footL, newBottom) || room.isSolidAt(footR, newBottom)) {
                int iy = (int) newBottom;
                while (iy < bottom + 2 && (room.isSolidAt(footL, iy) || room.isSolidAt(footR, iy))) {
                    iy++;
                }
                newY = flying ? iy + PLATFORM_HEIGHT : iy;
                vy = 0;
                onGround = true;
            }
        } else {
            float headL = x + 2;
            float headR = x + SIZE - 3;
            float headY = newY + SIZE;
            if (room.isSolidAt(headL, headY) || room.isSolidAt(headR, headY)) {
                int iy = (int) headY;
                while (iy > top - 2 && (room.isSolidAt(headL, iy) || room.isSolidAt(headR, iy))) {
                    iy--;
                }
                newY = iy - SIZE;
                vy = 0;
            }
        }
        y = newY;

        // --- Edge exit ---
        if (x + SIZE <= 0)         exit = Exit.LEFT;
        else if (x >= Room.WIDTH)  exit = Exit.RIGHT;
        else if (y + SIZE <= 0)    exit = Exit.DOWN;
        else if (y >= Room.HEIGHT) exit = Exit.UP;
    }

    /** Check if BLOB would collide with anything at the given Y position. */
    public boolean wouldCollide(float testY, Room room) {
        float bottom = state == State.FLYING ? testY - PLATFORM_HEIGHT : testY;
        float top = testY + SIZE;
        // Check both vertical edges and full height
        return isSolidColumn(room, x + 2, bottom, top)
            || isSolidColumn(room, x + SIZE - 3, bottom, top);
    }

    /**
     * Check if any point along a vertical column is solid.
     * Tests bottom, middle, and top probe points (inset by 2px).
     */
    private boolean isSolidColumn(Room room, float worldX, float bottom, float top) {
        float yLo = bottom + 2;
        float yHi = top - 3;
        float yMid = (yLo + yHi) * 0.5f;
        return room.isSolidAt(worldX, yLo)
            || room.isSolidAt(worldX, yMid)
            || room.isSolidAt(worldX, yHi);
    }
}
