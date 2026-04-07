package com.starquake.game.world;

/**
 * A temporary platform placed by BLOB.
 *
 * Sits at a fixed position in the room. After STABLE_TIME seconds it begins
 * dissolving (DISSOLVE_TIME seconds), then is removed. During dissolve the
 * collision stays active — it only vanishes when fully dissolved.
 *
 * Position uses libGDX convention: (x, y) is bottom-left, y=0 at room bottom.
 */
public class TempPlatform {
    public static final int WIDTH = 16;
    public static final int HEIGHT = 8;
    public static final float STABLE_TIME = 1.0f;
    public static final float DISSOLVE_TIME = 1.0f;
    public static final float TOTAL_TIME = STABLE_TIME + DISSOLVE_TIME;

    public final float x, y;
    public float age;

    public TempPlatform(float x, float y) {
        this.x = x;
        this.y = y;
        this.age = 0;
    }

    /** 0 = fully solid, 1 = fully dissolved. */
    public float getDissolve() {
        if (age < STABLE_TIME) return 0;
        return Math.min((age - STABLE_TIME) / DISSOLVE_TIME, 1f);
    }

    public boolean isExpired() {
        return age >= TOTAL_TIME;
    }

    public void update(float delta) {
        age += delta;
    }
}
