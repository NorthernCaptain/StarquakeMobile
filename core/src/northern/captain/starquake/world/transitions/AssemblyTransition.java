package northern.captain.starquake.world.transitions;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Birth/respawn assembly — particles converge inward from scattered positions
 * to BLOB's center, simulating magnetic reassembly.
 *
 * Custom CPU particle system since libGDX ParticleEffect can't do
 * "move toward center" natively.
 */
public class AssemblyTransition implements BlobTransition {
    private static final int COUNT = 100;
    private static final float DURATION = 1.0f;
    private static final float RADIUS = 100f;

    private static TextureRegion pixel;

    private float centerX, centerY;
    private float elapsed;
    private boolean started;

    // Particle state
    private final float[] startX = new float[COUNT];
    private final float[] startY = new float[COUNT];
    private final float[] size = new float[COUNT];

    @Override
    public void start(float x, float y) {
        centerX = x;
        centerY = y;
        elapsed = 0;
        started = true;

        if (pixel == null) {
            Pixmap px = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            px.setColor(1, 1, 1, 1);
            px.fill();
            pixel = new TextureRegion(new Texture(px));
            px.dispose();
        }

        // Scatter particles in a circle around center
        for (int i = 0; i < COUNT; i++) {
            double angle = Math.random() * Math.PI * 2;
            double dist = RADIUS * (0.3 + Math.random() * 0.7);
            startX[i] = (float) (Math.cos(angle) * dist);
            startY[i] = (float) (Math.sin(angle) * dist);
            size[i] = 1f + (float) (Math.random() * 3f);
        }
    }

    @Override
    public void update(float delta) {
        if (started) elapsed += delta;
    }

    @Override
    public void render(SpriteBatch batch) {
        if (!started) return;

        float t = Math.min(elapsed / DURATION, 1f);
        // Ease-in: accelerate toward center (slow start, fast finish)
        float progress = t * t;

        for (int i = 0; i < COUNT; i++) {
            float px = centerX + startX[i] * (1f - progress);
            float py = centerY + startY[i] * (1f - progress);
            float s = size[i];

            // Fade: dim at start, bright at end
            float alpha = 0.3f + 0.7f * t;
            // Color: cyan → white as they converge
            float r = 0.4f + 0.6f * t;
            float g = 0.6f + 0.4f * t;

            batch.setColor(r, g, 1f, alpha);
            batch.draw(pixel, px - s / 2f, py - s / 2f, s, s);
        }
        batch.setColor(Color.WHITE);
    }

    @Override
    public boolean isComplete() {
        return started && elapsed >= DURATION;
    }

    @Override
    public void dispose() {}
}
