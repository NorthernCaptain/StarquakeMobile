package northern.captain.starquake.world.transitions;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import northern.captain.starquake.Assets;
import northern.captain.starquake.world.Blob;

/**
 * Blow-out effect — reverse of suck-in.
 *
 * Phase 1 (0-70%): particles fly from hole to scattered positions
 * Phase 2 (70-100%): particles converge to form BLOB
 */
public class BlowOutTransition implements BlobTransition {
    private static final float DURATION = 1.0f;
    private static final float SCATTER_PHASE = 0.7f;
    private static final float SCATTER_RADIUS = 32f;

    private static Array<TextureAtlas.AtlasRegion> blobFrames;

    private final boolean holeOnRight;
    private boolean facingRight;
    private boolean started;
    private float elapsed;

    private float holeX, holeY;
    private float centerX, centerY;

    private int particleCount;
    private float[] endX, endY;
    private float[] scatterX, scatterY;
    private TextureRegion[] pixelRegions;

    public BlowOutTransition(Assets assets, boolean holeOnRight) {
        this.holeOnRight = holeOnRight;
        if (blobFrames == null) {
            blobFrames = assets.spritesAtlas.findRegions("blob");
        }
    }

    public void setFacingRight(boolean facingRight) {
        this.facingRight = facingRight;
    }

    @Override
    public void start(float cx, float cy) {
        centerX = cx;
        centerY = cy;
        float blobX = cx - Blob.SIZE / 2f;
        float blobY = cy - Blob.SIZE / 2f;

        holeX = holeOnRight ? blobX + Blob.SIZE + 8 : blobX - 8;
        holeY = blobY + 8;

        int frameIdx = facingRight ? 0 : 7;
        TextureAtlas.AtlasRegion frame = blobFrames.get(frameIdx);
        int regW = frame.getRegionWidth();
        int regH = frame.getRegionHeight();

        particleCount = regW * regH;
        endX = new float[particleCount];
        endY = new float[particleCount];
        scatterX = new float[particleCount];
        scatterY = new float[particleCount];
        pixelRegions = new TextureRegion[particleCount];

        int i = 0;
        for (int py = 0; py < regH; py++) {
            for (int px = 0; px < regW; px++) {
                float wx = blobX + px;
                float wy = blobY + (regH - 1 - py);
                endX[i] = wx;
                endY[i] = wy;

                pixelRegions[i] = new TextureRegion(frame.getTexture(),
                        frame.getRegionX() + px, frame.getRegionY() + py, 1, 1);

                // Scatter: radial from center
                float dx = wx - centerX;
                float dy = wy - centerY;
                float len = (float) Math.sqrt(dx * dx + dy * dy);
                if (len < 0.1f) { dx = 1; dy = 0; len = 1; }
                float scatter = SCATTER_RADIUS * (0.5f + (float) Math.random() * 0.5f);
                scatterX[i] = centerX + (dx / len) * scatter;
                scatterY[i] = centerY + (dy / len) * scatter;

                i++;
            }
        }

        elapsed = 0;
        started = true;
    }

    @Override
    public void update(float delta) {
        if (started) elapsed += delta;
    }

    @Override
    public void render(SpriteBatch batch) {
        if (!started) return;

        float t = Math.min(elapsed / DURATION, 1f);

        for (int i = 0; i < particleCount; i++) {
            float px, py;

            if (t < SCATTER_PHASE) {
                float st = t / SCATTER_PHASE;
                float ease = st * (2f - st);
                px = holeX + (scatterX[i] - holeX) * ease;
                py = holeY + (scatterY[i] - holeY) * ease;
            } else {
                float ct = (t - SCATTER_PHASE) / (1f - SCATTER_PHASE);
                float ease = ct * ct;
                px = scatterX[i] + (endX[i] - scatterX[i]) * ease;
                py = scatterY[i] + (endY[i] - scatterY[i]) * ease;
            }

            float alpha = 0.5f + t * 0.5f;
            batch.setColor(1, 1, 1, alpha);
            batch.draw(pixelRegions[i], px, py, 1, 1);
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
