package northern.captain.starquake.world.objects;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Array;
import northern.captain.starquake.Assets;
import northern.captain.starquake.world.Renderable;

/**
 * Animated hover platform sprite (3-frame cycle from laser_00..02).
 *
 * Used in two contexts:
 * - Sitting inside a HoverStand (drawn at stand's center)
 * - Attached under a flying BLOB (implements Renderable)
 */
public class HoverPlatform implements Renderable {
    public static final int WIDTH = 16;
    public static final int HEIGHT = 8;
    private static final float FRAME_DURATION = 0.12f;

    private static Array<TextureAtlas.AtlasRegion> sharedFrames;

    private float animTimer;

    public HoverPlatform(Assets assets) {
        if (sharedFrames == null) {
            sharedFrames = assets.spritesAtlas.findRegions("laser");
        }
    }

    @Override
    public void update(float delta) {
        animTimer += delta;
    }

    @Override
    public void render(SpriteBatch batch, float x, float y) {
        int frameIdx = (int) (animTimer / FRAME_DURATION) % sharedFrames.size;
        batch.draw(sharedFrames.get(frameIdx), x, y, WIDTH, HEIGHT);
    }
}
