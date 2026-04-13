package northern.captain.starquake.world;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import northern.captain.starquake.Assets;
import northern.captain.starquake.audio.SoundManager;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Manages active projectiles: spawning, movement, wall collision,
 * reflection (fly shot), hit effects, and rendering.
 */
public class ProjectileManager {
    private static final int MAX_PROJECTILES = 5;
    private static final float WALK_SPEED = 120f;
    private static final float FLY_SPEED = 150f;
    public static final int WALK_COST = 5;
    public static final int FLY_COST = 8;
    private static final float FRAME_DURATION = 0.06f;
    private static final float HIT_FRAME_DURATION = 0.06f;
    private static final int WALK_FRAMES = 4;
    private static final int FLY_FRAMES = 4;
    private static final int HIT_FRAMES = 3;

    // Sprite dimensions
    private static final int PROJ_W = 16;
    private static final int PROJ_H = 8;
    private static final int HIT_SIZE = 16;

    private final TextureRegion[] walkRight = new TextureRegion[WALK_FRAMES];
    private final TextureRegion[] walkLeft = new TextureRegion[WALK_FRAMES];
    private final TextureRegion[] flyFrames = new TextureRegion[FLY_FRAMES];
    private final TextureRegion[] hitFrames = new TextureRegion[HIT_FRAMES];

    private final ArrayList<Projectile> projectiles = new ArrayList<>();
    private final ArrayList<HitEffect> hitEffects = new ArrayList<>();

    public ProjectileManager(Assets assets) {
        for (int i = 0; i < WALK_FRAMES; i++) {
            walkRight[i] = assets.spritesAtlas.findRegion("weapon_fx_walk", i);
            walkLeft[i] = assets.spritesAtlas.findRegion("weapon_fx_walk", i + WALK_FRAMES);
        }
        for (int i = 0; i < FLY_FRAMES; i++) {
            flyFrames[i] = assets.spritesAtlas.findRegion("weapon_fx_fly", i);
        }
        for (int i = 0; i < HIT_FRAMES; i++) {
            hitFrames[i] = assets.spritesAtlas.findRegion("effect", i);
        }
    }

    public void fireWalk(float blobX, float blobY, boolean facingRight) {
        if (projectiles.size() >= MAX_PROJECTILES) return;
        float px = facingRight ? blobX + Blob.SIZE : blobX - PROJ_W;
        float py = blobY + (Blob.SIZE - PROJ_H) / 2f;
        float vx = facingRight ? WALK_SPEED : -WALK_SPEED;
        projectiles.add(new Projectile(Projectile.Type.WALK, px, py, vx, 0, facingRight));
    }

    public void fireFly(float blobX, float blobY, float dirX, float dirY) {
        if (projectiles.size() >= MAX_PROJECTILES) return;
        // Normalize direction
        float len = (float) Math.sqrt(dirX * dirX + dirY * dirY);
        if (len < 0.01f) { dirX = 1; dirY = 0; len = 1; }
        float nx = dirX / len;
        float ny = dirY / len;
        float px = blobX + (Blob.SIZE - PROJ_W) / 2f + nx * Blob.SIZE / 2f;
        float py = blobY + (Blob.SIZE - PROJ_H) / 2f + ny * Blob.SIZE / 2f;
        projectiles.add(new Projectile(Projectile.Type.FLY, px, py,
                nx * FLY_SPEED, ny * FLY_SPEED, nx >= 0));
    }

    public void update(float delta, Room room) {
        // Update projectiles
        Iterator<Projectile> it = projectiles.iterator();
        while (it.hasNext()) {
            Projectile p = it.next();
            p.timer += delta;
            p.ttl -= delta;

            // Move
            float newX = p.x + p.vx * delta;
            float newY = p.y + p.vy * delta;

            // Check out of room bounds
            if (newX + PROJ_W < 0 || newX > Room.WIDTH || newY + PROJ_H < 0 || newY > Room.HEIGHT) {
                p.alive = false;
            }

            // Check TTL (fly shot)
            if (p.ttl <= 0) {
                p.alive = false;
            }

            // Wall collision
            if (p.alive) {
                if (p.type == Projectile.Type.WALK) {
                    checkWalkCollision(p, newX, newY, room);
                } else {
                    if (!checkFlyCollision(p, newX, newY, room)) {
                        // Reflected — don't advance to newX/newY (stay outside wall)
                        continue;
                    }
                }
            }

            if (p.alive) {
                p.x = newX;
                p.y = newY;
            } else {
                spawnHitEffect(newX + PROJ_W / 2f, newY + PROJ_H / 2f);
                SoundManager.play(SoundManager.SoundType.EXPLOSION);
                it.remove();
            }
        }

        // Update hit effects
        Iterator<HitEffect> hitIt = hitEffects.iterator();
        while (hitIt.hasNext()) {
            HitEffect h = hitIt.next();
            h.timer += delta;
            if (h.timer >= HIT_FRAMES * HIT_FRAME_DURATION) {
                hitIt.remove();
            }
        }
    }

    private void checkWalkCollision(Projectile p, float newX, float newY, Room room) {
        // Check leading edge center point
        float checkX = p.facingRight ? newX + PROJ_W : newX;
        float checkY = newY + PROJ_H / 2f;
        if (room.isSolidAt(checkX, checkY)) {
            p.alive = false;
        }
    }

    /** Returns true if position should advance, false if reflected (keep old position). */
    private boolean checkFlyCollision(Projectile p, float newX, float newY, Room room) {
        float cx = newX + PROJ_W / 2f;
        float cy = newY + PROJ_H / 2f;
        float halfW = PROJ_W / 2f - 1;
        float halfH = PROJ_H / 2f - 1;

        boolean hitX = false, hitY = false;
        if (p.vx != 0) {
            float edgeX = p.vx > 0 ? cx + halfW : cx - halfW;
            if (room.isSolidAt(edgeX, cy)) hitX = true;
        }
        if (p.vy != 0) {
            float edgeY = p.vy > 0 ? cy + halfH : cy - halfH;
            if (room.isSolidAt(cx, edgeY)) hitY = true;
        }
        if (!hitX && !hitY && p.vx != 0 && p.vy != 0) {
            float edgeX = p.vx > 0 ? cx + halfW : cx - halfW;
            float edgeY = p.vy > 0 ? cy + halfH : cy - halfH;
            if (room.isSolidAt(edgeX, edgeY)) {
                hitX = true;
                hitY = true;
            }
        }

        if (hitX || hitY) {
            p.bounces++;
            if (p.bounces > 1) {
                p.alive = false;
                return true; // dead — advance to newX for hit effect position
            }
            // Reflect and stay at old position
            if (hitX) p.vx = -p.vx;
            if (hitY) p.vy = -p.vy;
            return false;
        }
        return true;
    }

    private void spawnHitEffect(float x, float y) {
        hitEffects.add(new HitEffect(x, y));
    }

    public void render(SpriteBatch batch) {
        for (Projectile p : projectiles) {
            int frameIdx = ((int) (p.timer / FRAME_DURATION)) % getFrameCount(p);
            TextureRegion frame = getFrame(p, frameIdx);
            if (frame == null) continue;

            if (p.type == Projectile.Type.FLY) {
                // Rotate fly shot to match travel direction
                float angle = MathUtils.atan2(p.vy, p.vx) * MathUtils.radiansToDegrees;
                batch.draw(frame,
                        p.x, p.y,
                        PROJ_W / 2f, PROJ_H / 2f,
                        PROJ_W, PROJ_H,
                        1f, 1f, angle);
            } else {
                batch.draw(frame, p.x, p.y, PROJ_W, PROJ_H);
            }
        }

        // Hit effects
        for (HitEffect h : hitEffects) {
            int idx = Math.min((int) (h.timer / HIT_FRAME_DURATION), HIT_FRAMES - 1);
            TextureRegion frame = hitFrames[idx];
            if (frame != null) {
                batch.draw(frame, h.x - HIT_SIZE / 2f, h.y - HIT_SIZE / 2f, HIT_SIZE, HIT_SIZE);
            }
        }
    }

    private int getFrameCount(Projectile p) {
        return (p.type == Projectile.Type.WALK) ? WALK_FRAMES : FLY_FRAMES;
    }

    private TextureRegion getFrame(Projectile p, int idx) {
        if (p.type == Projectile.Type.WALK) {
            return p.facingRight ? walkRight[idx] : walkLeft[idx];
        } else {
            return flyFrames[idx];
        }
    }

    public void clear() {
        projectiles.clear();
        hitEffects.clear();
    }

    public int getActiveCount() {
        return projectiles.size();
    }

    private static class HitEffect {
        float x, y, timer;
        HitEffect(float x, float y) { this.x = x; this.y = y; }
    }
}
