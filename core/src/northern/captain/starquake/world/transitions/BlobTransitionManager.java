package northern.captain.starquake.world.transitions;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import northern.captain.starquake.world.Blob;

/**
 * Runs a sequence of {@link BlobTransition} effects on BLOB.
 *
 * While a sequence is active, BLOB is in TRANSITION state (no input, no physics,
 * invisible). When the sequence completes, BLOB returns to IDLE.
 *
 * Sequences are defined as arrays of BlobTransition. Between steps, the manager
 * can optionally reposition BLOB (e.g. for teleport: explode at A, pause, assemble at B).
 *
 * Usage:
 *   manager.start(blob, new BlobTransition[]{ explode, pause, assemble });
 */
public class BlobTransitionManager {
    private Blob blob;
    private BlobTransition[] sequence;
    private int currentStep;
    private boolean active;

    /** Optional callback when the full sequence completes. */
    private Runnable onComplete;

    public boolean isActive() {
        return active;
    }

    /**
     * Start a transition sequence. BLOB enters TRANSITION state immediately.
     */
    public void start(Blob blob, BlobTransition[] sequence) {
        start(blob, sequence, null);
    }

    public void start(Blob blob, BlobTransition[] sequence, Runnable onComplete) {
        // Dispose previous sequence if still active
        if (this.sequence != null) {
            for (BlobTransition t : this.sequence) t.dispose();
        }
        this.blob = blob;
        this.sequence = sequence;
        this.onComplete = onComplete;
        this.currentStep = 0;
        this.active = true;
        blob.startTransition();
        sequence[0].start(blob.x + Blob.SIZE / 2f, blob.y + Blob.SIZE / 2f);
    }

    public void update(float delta) {
        if (!active) return;

        BlobTransition current = sequence[currentStep];
        current.update(delta);

        if (current.isComplete()) {
            currentStep++;
            if (currentStep >= sequence.length) {
                // Sequence complete
                active = false;
                blob.endTransition();
                if (onComplete != null) onComplete.run();
            } else {
                // Start next step at blob's current center
                sequence[currentStep].start(
                        blob.x + Blob.SIZE / 2f,
                        blob.y + Blob.SIZE / 2f);
            }
        }
    }

    public void render(SpriteBatch batch) {
        if (!active) return;
        sequence[currentStep].render(batch);
    }

    /** Reposition BLOB mid-sequence (e.g. for teleport). */
    public void repositionBlob(float x, float y) {
        if (blob != null) {
            blob.x = x;
            blob.y = y;
        }
    }

    public void dispose() {
        if (sequence != null) {
            for (BlobTransition t : sequence) t.dispose();
        }
    }
}
