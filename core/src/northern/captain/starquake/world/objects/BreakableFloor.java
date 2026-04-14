package northern.captain.starquake.world.objects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import northern.captain.starquake.Assets;
import northern.captain.starquake.event.EventBus;
import northern.captain.starquake.event.GameEvent;
import northern.captain.starquake.world.Blob;

import java.util.Random;

/**
 * Breakable floor (tile 39). Solid top 8px, empty below.
 * Breaks when BLOB lands on it from above while walking (not flying).
 * Once broken, stays broken for the rest of the game.
 * Breaking animation: grey chunks shatter downward.
 */
public class BreakableFloor extends GameObject {
    public static final int TILE_ID = 39;

    private static final int SOLID_H = 8;
    private static final int NUM_CHUNKS = 12;
    private static final float ANIM_DURATION = 0.8f;
    private static final float GRAVITY = 200f;

    private final TextureRegion tileRegion;

    // Persistent broken state across room visits
    private static final java.util.HashSet<Long> brokenTiles = new java.util.HashSet<>();

    // Static blob tracking — shared across all BreakableFloor instances
    private static boolean prevOnGround;
    private static Blob trackedBlob;

    private boolean broken;
    private boolean animating;
    private float animTimer;

    // Chunk particles
    private final float[] chunkX = new float[NUM_CHUNKS];
    private final float[] chunkY = new float[NUM_CHUNKS];
    private final float[] chunkVX = new float[NUM_CHUNKS];
    private final float[] chunkVY = new float[NUM_CHUNKS];
    private final float[] chunkSize = new float[NUM_CHUNKS];

    public BreakableFloor(Assets assets, int tileCol, int tileRow) {
        super(assets, tileCol, tileRow);
        this.tileRegion = assets.tileRegions.get(TILE_ID);
    }

    private long persistKey() {
        return room != null ? (long) room.roomIndex * 48 + tileRow * 8 + tileCol : -1;
    }

    @Override
    public void onAddedToRoom(northern.captain.starquake.world.Room room) {
        super.onAddedToRoom(room);
        if (brokenTiles.contains(persistKey())) {
            broken = true;
        }
    }

    @Override
    public boolean isSolidAt(float worldX, float worldY) {
        if (broken) return false;
        if (worldX < x || worldX >= x + TILE_W) return false;
        if (worldY < y || worldY >= y + TILE_H) return false;
        return worldY >= y + TILE_H - SOLID_H;
    }

    /** Call once per frame from any BreakableFloor to update shared blob tracking. */
    public static void updateBlobTracking(Blob blob) {
        trackedBlob = blob;
    }

    /** Call at end of frame to save previous ground state. */
    public static void postUpdateBlobTracking() {
        if (trackedBlob != null) {
            prevOnGround = trackedBlob.onGround;
        }
    }

    @Override
    public void update(float delta) {
        // Check for landing on this tile
        if (!broken && !animating && trackedBlob != null) {
            Blob blob = trackedBlob;
            if (blob.state != Blob.State.FLYING && blob.state != Blob.State.TRANSITION) {
                // Blob just landed this frame (was not on ground, now is)
                if (blob.onGround && !prevOnGround) {
                    float blobBottom = blob.getBottom();
                    float solidTop = y + TILE_H - SOLID_H;
                    // Check blob feet are on or just above the solid surface (within one tile height)
                    if (blobBottom >= solidTop && blobBottom <= solidTop + TILE_H) {
                        // Check horizontal overlap
                        float blobLeft = blob.getX();
                        float blobRight = blobLeft + blob.getWidth();
                        if (blobRight > x && blobLeft < x + TILE_W) {
                            breakFloor();
                        }
                    }
                }
            }
        }

        // Animate chunks
        if (animating) {
            animTimer += delta;
            for (int i = 0; i < NUM_CHUNKS; i++) {
                chunkVY[i] -= GRAVITY * delta;
                chunkX[i] += chunkVX[i] * delta;
                chunkY[i] += chunkVY[i] * delta;
            }
            if (animTimer >= ANIM_DURATION) {
                animating = false;
            }
        }
    }

    private void breakFloor() {
        broken = true;
        animating = true;
        animTimer = 0;
        brokenTiles.add(persistKey());
        EventBus.get().post(GameEvent.FLOOR_BROKEN);

        Random rng = new Random();
        float solidTop = y + TILE_H - SOLID_H;
        for (int i = 0; i < NUM_CHUNKS; i++) {
            chunkX[i] = x + rng.nextFloat() * TILE_W;
            chunkY[i] = solidTop + rng.nextFloat() * SOLID_H;
            chunkVX[i] = (rng.nextFloat() - 0.5f) * 30f;
            chunkVY[i] = -(rng.nextFloat() * 40f + 20f);
            chunkSize[i] = 2 + rng.nextFloat() * 2;
        }
    }

    @Override
    public void render(SpriteBatch batch, float delta) {
        if (broken && !animating) return;

        if (!broken && tileRegion != null && room != null) {
            batch.flush();
            batch.setShader(assets.paletteShader);
            assets.paletteTexture.bind(1);
            Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);
            assets.paletteShader.setUniformi("u_palette", 1);
            assets.paletteShader.setUniformf("u_paletteRow", room.paletteIndex);

            batch.draw(tileRegion, x, y, TILE_W, TILE_H);

            batch.flush();
            batch.setShader(null);
        }

        if (animating) {
            float alpha = 1f - (animTimer / ANIM_DURATION);
            batch.setColor(0.6f, 0.6f, 0.6f, alpha);
            for (int i = 0; i < NUM_CHUNKS; i++) {
                float s = chunkSize[i];
                batch.draw(assets.whitePixel, chunkX[i], chunkY[i], s, s);
            }
            batch.setColor(Color.WHITE);
        }
    }

    public static void resetAll() {
        brokenTiles.clear();
        prevOnGround = false;
        trackedBlob = null;
    }
}
