package northern.captain.starquake.world;

import northern.captain.starquake.event.EventBus;
import northern.captain.starquake.event.GameEvent;

/**
 * Tracks player progression: lives, score, resources, inventory.
 *
 * Subscribes to game events to update automatically.
 * Read by Hud for display.
 */
public class GameState {
    public static final int MAX_LIVES = 10;
    public static final int MAX_HEALTH = 100;
    public static final int MAX_PLATFORMS = 20;
    public static final int MAX_LASER = 100;

    private int lives = MAX_LIVES / 2;
    private int score;
    private static final float HEALTH_REGEN_RATE = 3f;    // units per second
    private static final float PLATFORM_REGEN_RATE = 1f;  // units per second
    private static final float LASER_REGEN_RATE = 6f;    // units per second

    private int health = MAX_HEALTH;
    private int platforms = MAX_PLATFORMS;
    private int laserEnergy = MAX_LASER;

    private float healthAccum;
    private float platformAccum;
    private float laserAccum;

    /** Register event listeners. Call once at startup. */
    public void registerEvents() {
        EventBus.get().register(GameEvent.Type.BLOB_DIED, e -> loseLife());
    }

    public void update(float delta) {
        if (health < MAX_HEALTH) {
            healthAccum += HEALTH_REGEN_RATE * delta;
            int gain = (int) healthAccum;
            if (gain > 0) { health = Math.min(health + gain, MAX_HEALTH); healthAccum -= gain; }
        }
        if (platforms < MAX_PLATFORMS) {
            platformAccum += PLATFORM_REGEN_RATE * delta;
            int gain = (int) platformAccum;
            if (gain > 0) { platforms = Math.min(platforms + gain, MAX_PLATFORMS); platformAccum -= gain; }
        }
        if (laserEnergy < MAX_LASER) {
            laserAccum += LASER_REGEN_RATE * delta;
            int gain = (int) laserAccum;
            if (gain > 0) { laserEnergy = Math.min(laserEnergy + gain, MAX_LASER); laserAccum -= gain; }
        }
    }

    // Getters
    public int getLives() { return lives; }
    public int getScore() { return score; }
    public int getHealth() { return health; }
    public int getPlatforms() { return platforms; }
    public int getLaserEnergy() { return laserEnergy; }

    public void setLives(int v) { lives = Math.min(v, MAX_LIVES); }
    public void setScore(int v) { score = v; }
    public void setHealth(int v) { health = Math.min(v, MAX_HEALTH); }
    public void setPlatforms(int v) { platforms = Math.min(v, MAX_PLATFORMS); }
    public void setLaserEnergy(int v) { laserEnergy = Math.min(v, MAX_LASER); }

    // Normalized 0-1 for progress bars
    public float getHealthFraction() { return (float) health / MAX_HEALTH; }
    public float getPlatformsFraction() { return (float) platforms / MAX_PLATFORMS; }
    public float getLaserFraction() { return (float) laserEnergy / MAX_LASER; }

    public void addScore(int points) { score += points; }

    public void loseLife() {
        if (lives > 0) lives--;
    }

    public boolean isGameOver() { return lives <= 0; }

    public boolean usePlatform() {
        if (platforms <= 0) return false;
        platforms--;
        return true;
    }

    public void addPlatforms(int count) {
        platforms = Math.min(platforms + count, MAX_PLATFORMS);
    }

    public void damage(int amount) {
        health = Math.max(0, health - amount);
    }

    public void heal(int amount) {
        health = Math.min(health + amount, MAX_HEALTH);
    }

    public void useLaser(int cost) {
        laserEnergy = Math.max(0, laserEnergy - cost);
    }

    public void rechargeLaser(int amount) {
        laserEnergy = Math.min(laserEnergy + amount, MAX_LASER);
    }

    public void addLife() {
        if (lives < MAX_LIVES) lives++;
    }

    /** Fill the lowest vital to max. If all full, grant an extra life. */
    public void universalBoost() {
        float hFrac = getHealthFraction();
        float pFrac = getPlatformsFraction();
        float lFrac = getLaserFraction();

        if (hFrac <= pFrac && hFrac <= lFrac && hFrac < 1f) {
            health = MAX_HEALTH;
        } else if (pFrac <= lFrac && pFrac < 1f) {
            platforms = MAX_PLATFORMS;
        } else if (lFrac < 1f) {
            laserEnergy = MAX_LASER;
        } else {
            addLife();
        }
    }

    public void reset() {
        lives = MAX_LIVES;
        score = 0;
        health = MAX_HEALTH;
        platforms = MAX_PLATFORMS;
        laserEnergy = MAX_LASER;
    }
}
