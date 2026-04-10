package northern.captain.starquake.world.transitions;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;

/**
 * Two-phase room transition for teleportation:
 * 1. DISINTEGRATE: chunks explode outward from center along their radial vector,
 *    scaling up and rotating (flying toward the viewer).
 * 2. BLACK_PAUSE: brief black screen while the new room is built.
 * 3. REASSEMBLE: reverse — chunks converge from outside to center, shrinking.
 */
public class TeleportTransition {
    private static final float DISINTEGRATE_TIME = 0.8f;
    private static final float BLACK_PAUSE_TIME = 0.3f;
    private static final float REASSEMBLE_TIME = 0.8f;

    private static final int CHUNK_SIZE = 8;
    private static final int ROOM_W = 256;
    private static final int ROOM_H = 144;
    private static final float CENTER_X = ROOM_W / 2f;
    private static final float CENTER_Y = ROOM_H / 2f;
    private static final int COLS = ROOM_W / CHUNK_SIZE;
    private static final int ROWS = ROOM_H / CHUNK_SIZE;
    private static final int NUM_CHUNKS = COLS * ROWS;

    private static final float MOVE_SPEED = 2.5f;

    // Bulge: center chunks scale/rotate more AND progress faster in time
    private static final float CENTER_END_SCALE = 6f;
    private static final float EDGE_END_SCALE = 2f;
    private static final float CENTER_END_ROT = 0.7f;
    private static final float EDGE_END_ROT = 0.2f;

    // Center chunks reach full explosion at ~35% of animation time,
    // edge chunks take the full duration
    private static final float CENTER_TIME_BOOST = 2.0f;

    public enum Phase { DISINTEGRATE, BLACK_PAUSE, REASSEMBLE, DONE }

    private Phase phase;
    private float phaseTimer;

    private TextureRegion[] sourceChunks;
    private TextureRegion[] targetChunks;
    private boolean hasTarget;

    private final float[] homeX = new float[NUM_CHUNKS];
    private final float[] homeY = new float[NUM_CHUNKS];
    // Radial movement vector
    private final float[] dirX = new float[NUM_CHUNKS];
    private final float[] dirY = new float[NUM_CHUNKS];
    // Per-chunk end state (computed from distance to center + randomization)
    private final float[] endScale = new float[NUM_CHUNKS];
    private final float[] endRot = new float[NUM_CHUNKS];
    // 0=center, 1=edge — controls the bulge mix
    private final float[] normDist = new float[NUM_CHUNKS];

    public void start(TextureRegion source) {
        this.sourceChunks = subdivide(source);
        this.hasTarget = false;
        initChunkData();
        phase = Phase.DISINTEGRATE;
        phaseTimer = 0;
    }

    public void setTarget(TextureRegion target) {
        this.targetChunks = subdivide(target);
        initChunkData(); // fresh random scale/rotation for reassembly
        this.hasTarget = true;
    }

    private void initChunkData() {
        float maxDist = (float) Math.sqrt(CENTER_X * CENTER_X + CENTER_Y * CENTER_Y);

        for (int i = 0; i < NUM_CHUNKS; i++) {
            int col = i % COLS;
            int row = i / COLS;
            homeX[i] = col * CHUNK_SIZE;
            homeY[i] = row * CHUNK_SIZE;

            float cx = homeX[i] + CHUNK_SIZE / 2f;
            float cy = homeY[i] + CHUNK_SIZE / 2f;
            float dx = cx - CENTER_X;
            float dy = cy - CENTER_Y;
            float dist = (float) Math.sqrt(dx * dx + dy * dy);
            if (dist < 1f) dist = 1f;

            // 0 = at center, 1 = at corner
            normDist[i] = Math.min(dist / maxDist, 1f);

            // Movement: outward along radial vector + random perturbation to break formation
            float speedRand = MathUtils.random(0.6f, 1.5f);
            float anglePerturb = MathUtils.random(-0.3f, 0.3f);
            float cos = MathUtils.cos(anglePerturb);
            float sin = MathUtils.sin(anglePerturb);
            float baseDx = (dx / dist) * dist * MOVE_SPEED * speedRand;
            float baseDy = (dy / dist) * dist * MOVE_SPEED * speedRand;
            dirX[i] = baseDx * cos - baseDy * sin;
            dirY[i] = baseDx * sin + baseDy * cos;

            // Bulge: center chunks get big scale + lots of rotation,
            // edge chunks get smaller scale + less rotation
            float centerWeight = 1f - normDist[i]; // 1 at center, 0 at edge
            float baseScale = EDGE_END_SCALE + (CENTER_END_SCALE - EDGE_END_SCALE) * centerWeight;
            float baseRot = EDGE_END_ROT + (CENTER_END_ROT - EDGE_END_ROT) * centerWeight;

            endScale[i] = baseScale * MathUtils.random(0.8f, 1.2f);
            endRot[i] = baseRot * (MathUtils.randomBoolean() ? 1f : -1f) * MathUtils.random(0.7f, 1.3f);
        }
    }

    public void update(float delta) {
        if (phase == Phase.DONE) return;
        phaseTimer += delta;

        switch (phase) {
            case DISINTEGRATE:
                if (phaseTimer >= DISINTEGRATE_TIME) {
                    phase = Phase.BLACK_PAUSE;
                    phaseTimer = 0;
                }
                break;
            case BLACK_PAUSE:
                if (phaseTimer >= BLACK_PAUSE_TIME && hasTarget) {
                    phase = Phase.REASSEMBLE;
                    phaseTimer = 0;
                }
                break;
            case REASSEMBLE:
                if (phaseTimer >= REASSEMBLE_TIME) {
                    phase = Phase.DONE;
                }
                break;
            default:
                break;
        }
    }

    public void render(SpriteBatch batch) {
        if (phase == Phase.BLACK_PAUSE || phase == Phase.DONE) return;

        boolean disintegrating = (phase == Phase.DISINTEGRATE);
        TextureRegion[] chunks = disintegrating ? sourceChunks : targetChunks;
        if (chunks == null) return;

        float duration = disintegrating ? DISINTEGRATE_TIME : REASSEMBLE_TIME;
        float rawT = Math.min(phaseTimer / duration, 1f);

        float alpha = disintegrating ? (1f - rawT) : 1f;

        for (int i = 0; i < NUM_CHUNKS; i++) {
            if (chunks[i] == null) continue;

            // Per-chunk time: center chunks progress faster (bulge out first).
            // NOT clamped to 1 — chunks keep flying past full explosion off screen.
            float centerWeight = 1f - normDist[i]; // 1 at center, 0 at edge
            float timeScale = 1f + centerWeight * CENTER_TIME_BOOST;
            float chunkT = rawT * timeScale;

            float moveT, scaleT, rotT;
            if (disintegrating) {
                moveT = chunkT;
                scaleT = chunkT;
                rotT = chunkT;
            } else {
                // Reassemble: ALL chunks must finish at exactly rawT=1.
                // Use 1-rawT as base (1→0), same for everyone, but apply
                // different easing: center chunks ease scale fast (pow3),
                // edge chunks ease position fast (pow3).
                float remaining = 1f - rawT; // 1→0

                // Movement: edge chunks cover more distance so use aggressive easing
                // center chunks cover less distance so use gentle easing
                // Both reach 0 at rawT=1.
                float movePow = 1f + normDist[i] * 2f; // edge=3, center=1
                moveT = (float) Math.pow(remaining, movePow);

                // Scale: center starts bigger, needs aggressive shrink
                float scalePow = 1f + centerWeight * 2f; // center=3, edge=1
                scaleT = (float) Math.pow(remaining, scalePow);

                rotT = remaining;
            }

            // Position: home + radial direction × moveT
            float px = homeX[i] + dirX[i] * moveT;
            float py = homeY[i] + dirY[i] * moveT;

            // Scale: 1 at home → endScale at full explosion
            float scale = 1f + (endScale[i] - 1f) * scaleT;
            float drawSize = CHUNK_SIZE * scale;

            // Rotation
            float rot = endRot[i] * rotT;

            // Draw centered (account for scale growth)
            float cx = px + CHUNK_SIZE / 2f;
            float cy = py + CHUNK_SIZE / 2f;
            float halfSize = drawSize / 2f;

            batch.setColor(1, 1, 1, alpha);
            batch.draw(chunks[i],
                    cx - halfSize, cy - halfSize,  // position
                    halfSize, halfSize,             // origin (center of chunk)
                    drawSize, drawSize,             // size
                    1f, 1f,                         // scale (already in drawSize)
                    MathUtils.radiansToDegrees * rot);  // rotation in degrees
        }
        batch.setColor(Color.WHITE);
    }

    public boolean isDone() { return phase == Phase.DONE; }
    public boolean needsTarget() { return phase == Phase.BLACK_PAUSE && !hasTarget; }
    public Phase getPhase() { return phase; }

    private TextureRegion[] subdivide(TextureRegion region) {
        // FBO textures are Y-inverted. Work with raw texture, flip each chunk.
        TextureRegion[] chunks = new TextureRegion[NUM_CHUNKS];
        com.badlogic.gdx.graphics.Texture tex = region.getTexture();

        for (int i = 0; i < NUM_CHUNKS; i++) {
            int col = i % COLS;
            int row = i / COLS;
            int srcX = col * CHUNK_SIZE;
            int srcY = row * CHUNK_SIZE;
            chunks[i] = new TextureRegion(tex, srcX, srcY, CHUNK_SIZE, CHUNK_SIZE);
            chunks[i].flip(false, true);
        }
        return chunks;
    }
}
