package northern.captain.starquake.world;

/**
 * Interface for entities that can collide with game objects (BLOB, enemies, projectiles).
 *
 * Game objects receive a Collidable in their collision callbacks.
 * They can check the type to determine specific behavior (e.g. only BLOB
 * can pick up the hover platform).
 */
public interface Collidable {

    enum Type { BLOB, ENEMY }

    Type getType();

    float getX();
    float getY();
    float getWidth();
    /** Effective collision height — may change with state (e.g. taller when flying). */
    float getHeight();

    /** Bottom of the full collision box (accounts for attachments like hover platform). */
    float getBottom();
}
