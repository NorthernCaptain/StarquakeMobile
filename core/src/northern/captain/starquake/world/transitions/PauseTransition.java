package northern.captain.starquake.world.transitions;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/** A silent pause between other transitions (e.g. the 2s wait after death). */
public class PauseTransition implements BlobTransition {
    private final float duration;
    private float elapsed;

    public PauseTransition(float duration) {
        this.duration = duration;
    }

    @Override public void start(float x, float y) { elapsed = 0; }
    @Override public void update(float delta) { elapsed += delta; }
    @Override public void render(SpriteBatch batch) {}
    @Override public boolean isComplete() { return elapsed >= duration; }
    @Override public void dispose() {}
}
