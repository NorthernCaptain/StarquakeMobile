package northern.captain.starquake.world.transitions;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * A visual transition effect for BLOB (death explosion, birth assembly,
 * teleport suck-in/blow-out, etc.).
 *
 * Each transition is self-contained: starts at a position, runs for a duration,
 * renders its own visuals, and reports completion.
 */
public interface BlobTransition {
    /** Start the effect at the given world position. */
    void start(float x, float y);

    /** Advance the effect. */
    void update(float delta);

    /** Draw the effect. Batch is already begun. */
    void render(SpriteBatch batch);

    /** True when the effect has finished playing. */
    boolean isComplete();

    /** Release resources. */
    void dispose();
}
