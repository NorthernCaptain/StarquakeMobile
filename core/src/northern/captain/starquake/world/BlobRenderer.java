package northern.captain.starquake.world;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import northern.captain.starquake.Assets;

/**
 * Renders the BLOB player character with directional walk and turn animations.
 *
 * 11 sprite frames form a rotation sequence:
 *   0–3:  walking right
 *   4–7:  turning (right → left)
 *   8–10: walking left
 *
 * Walking cycles ping-pong within the directional range.
 * Turning plays sequentially through frames 3→8 (right→left) or 8→3 (left→right).
 * On turn complete, notifies Blob to flip facing and resume movement.
 */
public class BlobRenderer {
    private static final float FRAME_DURATION = 0.08f;

    // Frame ranges: 0-3 walk right, 4-6 rotation, 7-10 walk left
    private static final int WALK_RIGHT_START = 0;
    private static final int WALK_RIGHT_END   = 3;
    private static final int WALK_LEFT_START  = 7;
    private static final int WALK_LEFT_END    = 10;
    private static final int TURN_R2L_START   = 4;  // right → left
    private static final int TURN_R2L_END     = 6;
    private static final int TURN_L2R_START   = 6;  // left → right
    private static final int TURN_L2R_END     = 4;

    private final Array<TextureAtlas.AtlasRegion> frames;
    private float animTimer;
    private int currentFrame;
    private boolean wasTurning;

    public BlobRenderer(Assets assets) {
        frames = assets.spritesAtlas.findRegions("blob");
        currentFrame = WALK_RIGHT_START;
    }

    public void render(SpriteBatch batch, Blob blob, float delta) {
        animTimer += delta;

        switch (blob.state) {
            case IDLE:
                // Show idle frame for current facing
                currentFrame = blob.facingRight ? WALK_RIGHT_START : WALK_LEFT_START;
                animTimer = 0;
                wasTurning = false;
                break;

            case WALK:
                wasTurning = false;
                if (blob.facingRight) {
                    currentFrame = walkPingPong(WALK_RIGHT_START, WALK_RIGHT_END);
                } else {
                    currentFrame = walkPingPong(WALK_LEFT_START, WALK_LEFT_END);
                }
                break;

            case TURNING:
                if (!wasTurning) {
                    animTimer = 0;
                    wasTurning = true;
                }
                int step = (int) (animTimer / FRAME_DURATION);
                int turnFrames = TURN_R2L_END - TURN_R2L_START + 1; // 3 frames
                if (step >= turnFrames) {
                    blob.onTurnComplete();
                    wasTurning = false;
                } else if (blob.facingRight) {
                    // Turning right → left: frames 4, 5, 6
                    currentFrame = TURN_R2L_START + step;
                } else {
                    // Turning left → right: frames 6, 5, 4
                    currentFrame = TURN_L2R_START - step;
                }
                break;

            case FLYING:
                // Idle frame only — no walk animation while flying
                currentFrame = blob.facingRight ? WALK_RIGHT_START : WALK_LEFT_START;
                wasTurning = false;
                break;

            case LIFTING:
                // Idle frame — visible while being lifted
                currentFrame = blob.facingRight ? WALK_RIGHT_START : WALK_LEFT_START;
                wasTurning = false;
                break;

            case TRANSITION:
                // Invisible — transition effect handles rendering
                return;
        }

        // Clamp to valid range
        currentFrame = Math.max(0, Math.min(currentFrame, frames.size - 1));

        TextureRegion frame = frames.get(currentFrame);
        float alpha = blob.getAlpha();
        if (alpha < 1f) {
            batch.setColor(1, 1, 1, alpha);
        }
        batch.draw(frame, blob.x, blob.y, Blob.SIZE, Blob.SIZE);
        if (alpha < 1f) {
            batch.setColor(1, 1, 1, 1);
        }
    }

    /** Ping-pong within [start, end]: 0,1,2,3,2,1,0,1,2,3... */
    private int walkPingPong(int start, int end) {
        int range = end - start;
        int cycleLen = range * 2;
        int step = (int) (animTimer / FRAME_DURATION) % cycleLen;
        if (step <= range) {
            return start + step;
        } else {
            return end - (step - range);
        }
    }
}
