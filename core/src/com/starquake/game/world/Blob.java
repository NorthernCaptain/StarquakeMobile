package com.starquake.game.world;

/**
 * BLOB player character — position, velocity, physics, collision.
 *
 * All units are game-world pixels (room = 256×144).
 * Position (x, y) is the bottom-left corner of the 16×16 hitbox.
 * Y=0 is the bottom of the room (libGDX convention).
 *
 * Animation state machine handles turning: BLOB must complete a rotation
 * animation before moving in the new direction.
 */
public class Blob {
    public static final float SIZE = 16;
    public static final float WALK_SPEED = 48;    // units/sec
    public static final float GRAVITY = 180;      // units/sec²
    public static final float TERMINAL_VEL = 120; // units/sec (downward)

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
     */
    public enum State { IDLE, WALK, TURNING }

    public float x, y;
    public float vx, vy;
    public boolean onGround;
    public boolean blockedH;     // true if horizontal movement was blocked by a wall
    public boolean facingRight = true;
    public State state = State.IDLE;
    public Exit exit = Exit.NONE;

    public Blob(float x, float y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Apply input intent. Call before update().
     * Returns true if BLOB will move this frame (not turning).
     */
    public void applyInput(boolean wantLeft, boolean wantRight) {
        if (state == State.TURNING) {
            // Don't accept new input while turning — animation drives the state
            return;
        }

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

    /** Called by BlobRenderer when the turn animation completes. */
    public void onTurnComplete() {
        facingRight = !facingRight;
        state = State.IDLE;
    }

    public void update(float delta, Room room) {
        exit = Exit.NONE;

        // No horizontal movement while turning
        if (state == State.TURNING) vx = 0;

        // Gravity
        vy -= GRAVITY * delta;
        if (vy < -TERMINAL_VEL) vy = -TERMINAL_VEL;

        // Horizontal movement with pixel-perfect collision
        blockedH = false;
        if (vx > 0) {
            float probeX = x + vx * delta;
            float checkY1 = y + 2;
            float checkY2 = y + SIZE - 3;
            if (room.isSolidAt(probeX + SIZE - 1, checkY1) || room.isSolidAt(probeX + SIZE - 1, checkY2)) {
                // Find exact wall position
                int ix = (int) (probeX + SIZE - 1);
                while (ix > x + SIZE && (room.isSolidAt(ix, checkY1) || room.isSolidAt(ix, checkY2))) {
                    ix--;
                }
                x = ix - SIZE + 1;
                vx = 0;
                blockedH = true;
                state = State.IDLE;
            } else {
                x = probeX;
            }
        } else if (vx < 0) {
            float probeX = x + vx * delta;
            float checkY1 = y + 2;
            float checkY2 = y + SIZE - 3;
            if (room.isSolidAt(probeX, checkY1) || room.isSolidAt(probeX, checkY2)) {
                int ix = (int) probeX;
                while (ix < x && (room.isSolidAt(ix, checkY1) || room.isSolidAt(ix, checkY2))) {
                    ix++;
                }
                x = ix;
                vx = 0;
                blockedH = true;
                state = State.IDLE;
            } else {
                x = probeX;
            }
        }

        // Vertical movement
        float newY = y + vy * delta;
        onGround = false;
        if (vy <= 0) {
            // Falling/standing: check feet
            float footL = x + 2;
            float footR = x + SIZE - 3;
            if (room.isSolidAt(footL, newY) || room.isSolidAt(footR, newY)) {
                // Find the exact surface: scan up from newY to find first non-solid pixel
                int iy = (int) newY;
                while (iy < y + 2 && (room.isSolidAt(footL, iy) || room.isSolidAt(footR, iy))) {
                    iy++;
                }
                newY = iy;
                vy = 0;
                onGround = true;
            }
        } else {
            // Rising: check head
            float headL = x + 2;
            float headR = x + SIZE - 3;
            if (room.isSolidAt(headL, newY + SIZE) || room.isSolidAt(headR, newY + SIZE)) {
                // Find exact ceiling
                int iy = (int) (newY + SIZE);
                while (iy > y + SIZE - 2 && (room.isSolidAt(headL, iy) || room.isSolidAt(headR, iy))) {
                    iy--;
                }
                newY = iy - SIZE;
                vy = 0;
            }
        }
        y = newY;

        // Edge exit detection
        if (x + SIZE <= 0)         exit = Exit.LEFT;
        else if (x >= Room.WIDTH)  exit = Exit.RIGHT;
        else if (y + SIZE <= 0)    exit = Exit.DOWN;
        else if (y >= Room.HEIGHT) exit = Exit.UP;
    }

}
