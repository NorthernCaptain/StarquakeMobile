package northern.captain.starquake.world.transitions;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/** Death explosion — particles fly outward from BLOB's position. */
public class ExplosionTransition implements BlobTransition {
    private final ParticleEffect effect;
    private boolean started;

    public ExplosionTransition() {
        effect = new ParticleEffect();
        effect.load(Gdx.files.internal("effects/blob_explode.p"),
                    Gdx.files.internal("effects"));
    }

    @Override
    public void start(float x, float y) {
        effect.setPosition(x, y);
        effect.start();
        started = true;
    }

    @Override
    public void update(float delta) {
        if (started) effect.update(delta);
    }

    @Override
    public void render(SpriteBatch batch) {
        if (started) effect.draw(batch);
    }

    @Override
    public boolean isComplete() {
        return started && effect.isComplete();
    }

    @Override
    public void dispose() {
        effect.dispose();
    }
}
