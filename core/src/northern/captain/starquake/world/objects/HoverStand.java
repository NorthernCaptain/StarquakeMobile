package northern.captain.starquake.world.objects;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import northern.captain.starquake.Assets;
import northern.captain.starquake.event.EventBus;
import northern.captain.starquake.event.GameEvent;
import northern.captain.starquake.input.InputManager;
import northern.captain.starquake.world.Blob;
import northern.captain.starquake.world.Collidable;
import northern.captain.starquake.world.Room;

/**
 * Hover platform stand (tile 32).
 *
 * Collision behavior:
 * - Pillar sides (left/right edges): solid throughout the tile height.
 * - Center with platform: solid at the platform surface level.
 * - Center without platform: BLOB can descend 8px into the cradle.
 *
 * Mount: press UP while centered on the stand → BLOB enters FLYING state.
 * Dismount: fly into a stand, center up, press LEFT/RIGHT → BLOB walks off.
 *
 * Listens to BLOB_DIED event to reclaim the platform.
 */
public class HoverStand extends GameObject {
    public static final int TILE_ID = 32;

    private static boolean blobHasPlatform = false;

    private final HoverPlatform hoverPlatform;

    private static final float PLATFORM_OFFSET_X = 8;
    private static final float PLATFORM_OFFSET_Y = 16;
    private static final float CRADLE_DEPTH = 8;

    private static final float CENTER_TOLERANCE = 6;
    private static final float DISMOUNT_DEPTH = 5;

    private static final EventBus.Listener DEATH_LISTENER = e -> blobHasPlatform = false;

    public HoverStand(Assets assets, int tileCol, int tileRow) {
        super(assets, tileCol, tileRow);
        hoverPlatform = new HoverPlatform(assets);
    }

    /** Register class-level event listeners. Call once at startup. */
    public static void registerEvents() {
        EventBus.get().register(GameEvent.Type.BLOB_DIED, DEATH_LISTENER);
    }

    public static boolean isBlobCarryingPlatform() {
        return blobHasPlatform;
    }

    public static void setBlobHasPlatform(boolean value) {
        blobHasPlatform = value;
    }

    public static void reset() {
        blobHasPlatform = false;
    }

    @Override
    public boolean isSolidAt(float worldX, float worldY) {
        if (worldX < x || worldX >= x + TILE_W) return false;
        if (worldY < y || worldY >= y + TILE_H) return false;

        float centerLeft = x + PLATFORM_OFFSET_X;
        float centerRight = x + PLATFORM_OFFSET_X + HoverPlatform.WIDTH;
        boolean inCenter = worldX >= centerLeft && worldX < centerRight;

        if (!inCenter) {
            return true;
        }

        if (!blobHasPlatform) {
            float platformTop = y + PLATFORM_OFFSET_Y + HoverPlatform.HEIGHT;
            return worldY < platformTop;
        } else {
            float surface = y + TILE_H - CRADLE_DEPTH;
            return worldY < surface;
        }
    }

    @Override
    public void update(float delta) {
        hoverPlatform.update(delta);
    }

    @Override
    public void render(SpriteBatch batch, float delta) {
        if (!blobHasPlatform) {
            hoverPlatform.render(batch, x + PLATFORM_OFFSET_X, y + PLATFORM_OFFSET_Y);
        }
    }

    private boolean isCentered(Collidable entity) {
        float standCenterX = x + TILE_W / 2f;
        float entityCenterX = entity.getX() + entity.getWidth() / 2f;
        return Math.abs(entityCenterX - standCenterX) <= CENTER_TOLERANCE;
    }

    private boolean isInsideStand(Collidable entity) {
        float bottom = entity.getBottom();
        return bottom <= y + TILE_H - DISMOUNT_DEPTH && bottom >= y;
    }

    @Override
    public boolean onAction(Collidable entity, InputManager.Action action) {
        if (entity.getType() != Collidable.Type.BLOB) return false;
        if (!isCentered(entity)) return false;

        Blob blob = (Blob) entity;

        if (blob.state == Blob.State.FLYING && blobHasPlatform) {
            if (action == InputManager.Action.LEFT || action == InputManager.Action.RIGHT) {
                if (!isInsideStand(entity)) return false;
                blobHasPlatform = false;
                blob.stopFlying();
                EventBus.get().post(GameEvent.BLOB_DISMOUNTED_PLATFORM);
                return true;
            }
            return false;
        }

        if (action != InputManager.Action.UP) return false;
        if (blobHasPlatform) return false;

        blobHasPlatform = true;
        blob.attachment = new HoverPlatform(assets);
        blob.startFlying();
        EventBus.get().post(GameEvent.BLOB_MOUNTED_PLATFORM);
        return true;
    }
}
