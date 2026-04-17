package northern.captain.starquake.world.objects;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import northern.captain.starquake.Assets;
import northern.captain.starquake.world.Blob;
import northern.captain.starquake.world.Collidable;
import northern.captain.starquake.world.CoreAssembly;
import northern.captain.starquake.world.Inventory;
import northern.captain.starquake.world.items.ItemPickup;
import northern.captain.starquake.world.items.ItemType;

import java.util.Random;

/**
 * Invisible trigger in the core room (tile col=0, row=4).
 * When BLOB crosses it, starts the core assembly animation.
 * Triggers once per room entrance (flag resets when CoreTrigger is re-created).
 * Drives CoreAssembly.update() and render() so it only runs while in the core room.
 * Owns the CoreAssembly singleton.
 */
public class CoreTrigger extends GameObject {
    public static final int TILE_ID = 122;
    private static final float TRIGGER_X_OFFSET = 12;
    private static final float TRIGGER_WIDTH = 8;
    private static final float ARM_DELAY = 1.0f;

    private static CoreAssembly coreAssembly;
    private boolean triggered;
    private float armTimer;

    public CoreTrigger(Assets assets, int tileCol, int tileRow) {
        super(assets, tileCol, tileRow);
    }

    /** Initialize the core assembly for a new game. Call once at game start. */
    public static void initCoreAssembly(Assets assets, long seed, ItemType[] partPool) {
        coreAssembly = new CoreAssembly(assets);
        coreAssembly.initialize(new Random(seed), partPool);
    }

    /** Create core assembly without initializing grid — for loading saved state. */
    public static void createCoreAssembly(Assets assets) {
        coreAssembly = new CoreAssembly(assets);
    }

    public static CoreAssembly getCoreAssembly() {
        return coreAssembly;
    }

    @Override
    public boolean isSolidAt(float worldX, float worldY) {
        return false;
    }

    @Override
    public void update(float delta) {
        if (armTimer < ARM_DELAY) armTimer += delta;
        coreAssembly.update(delta);
    }

    @Override
    public void render(SpriteBatch batch, float delta) {
        coreAssembly.render(batch);
    }

    @Override
    public void onEnter(Collidable entity) {
        if (triggered || armTimer < ARM_DELAY || coreAssembly.isAnimating()) return;
        if (entity.getType() != Collidable.Type.BLOB) return;
        Blob blob = (Blob) entity;
        if (blob.state != Blob.State.IDLE && blob.state != Blob.State.WALK) return;

        // Check if BLOB overlaps the trigger zone
        float triggerLeft = x + TRIGGER_X_OFFSET;
        float triggerRight = triggerLeft + TRIGGER_WIDTH;
        float blobLeft = blob.getX();
        float blobRight = blobLeft + blob.getWidth();
        if (blobRight <= triggerLeft || blobLeft >= triggerRight) return;

        triggered = true;
        Inventory inventory = ItemPickup.inventory();
        if (inventory == null) return;

        coreAssembly.startAnimation(inventory, blob);
    }
}
