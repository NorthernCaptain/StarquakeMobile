package northern.captain.starquake.world;

/**
 * A single active projectile (walk laser bolt or fly energy ball).
 */
public class Projectile {
    public enum Type { WALK, FLY }

    public final Type type;
    public float x, y;       // draw position (top-left corner)
    public float vx, vy;     // velocity (px/s)
    public boolean alive = true;
    public int bounces;       // fly shot: incremented on wall reflect
    public float ttl;         // time to live (fly shot)
    public float timer;       // total elapsed (for animation)
    public boolean facingRight; // walk shot direction

    public Projectile(Type type, float x, float y, float vx, float vy, boolean facingRight) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.facingRight = facingRight;
        this.ttl = (type == Type.FLY) ? 2f : 999f;
    }
}
