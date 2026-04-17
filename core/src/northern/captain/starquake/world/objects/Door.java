package northern.captain.starquake.world.objects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import northern.captain.starquake.Assets;
import northern.captain.starquake.audio.SoundManager;
import northern.captain.starquake.event.EventBus;
import northern.captain.starquake.event.GameEvent;
import northern.captain.starquake.world.SaveManager;
import northern.captain.starquake.input.InputManager;
import northern.captain.starquake.world.Blob;
import northern.captain.starquake.world.Collidable;
import northern.captain.starquake.world.Inventory;
import northern.captain.starquake.world.items.ItemPickup;
import northern.captain.starquake.world.items.ItemType;

/**
 * Locked door (tile 45). Draws itself (not in terrain FBO).
 * Key is reusable — not consumed. Once opened, stays open permanently.
 * No key → blinks key icon on far side. Has key → door blinks and disappears.
 */
public class Door extends CollisionTile {
    public static final int TILE_ID = 45;
    public static java.util.Set<Integer> openedRooms; // loaded from save, checked on room enter

    private static final float HINT_DURATION = 2f;
    private static final float HINT_FREQ = 5f;
    private static final float UNLOCK_DURATION = 1f;
    private static final float UNLOCK_FREQ = 8f;

    private final TextureRegion tileRegion;
    private final TextureRegion keyIcon;

    private boolean opened;
    private float hintTimer;      // key icon blink (no key)
    private boolean hintOnRight;  // which side to show key icon
    private float unlockTimer;    // door blink before disappearing

    public Door(Assets assets, int tileCol, int tileRow) {
        super(assets, tileCol, tileRow, 8, 16, 0, 0);
        this.tileRegion = assets.tileRegions.get(TILE_ID);
        this.keyIcon = assets.itemsAtlas.findRegion("item", ItemType.KEY.spriteIndex);
    }

    @Override
    public void onAddedToRoom(northern.captain.starquake.world.Room room) {
        super.onAddedToRoom(room);
        if (openedRooms != null && openedRooms.contains(room.roomIndex)) {
            opened = true;
        }
    }

    @Override
    public boolean isSolidAt(float worldX, float worldY) {
        if (opened || unlockTimer > 0) return false;
        return super.isSolidAt(worldX, worldY);
    }

    @Override
    public void update(float delta) {
        if (hintTimer > 0) hintTimer -= delta;
        if (unlockTimer > 0) {
            unlockTimer -= delta;
            if (unlockTimer <= 0) opened = true;
        }
    }

    @Override
    public void render(SpriteBatch batch, float delta) {
        if (opened) return;

        // Draw the door tile using palette shader
        if (tileRegion != null) {
            boolean doorVisible = true;
            if (unlockTimer > 0) {
                doorVisible = ((int) (unlockTimer * UNLOCK_FREQ * 2)) % 2 == 0;
            }
            if (doorVisible) {
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
        }

        // Blink key icon when player tried without key
        if (hintTimer > 0 && keyIcon != null) {
            boolean visible = ((int) (hintTimer * HINT_FREQ * 2)) % 2 == 0;
            if (visible) {
                // Solid column is x+8 to x+16. Place key 2px from the solid edge on the far side.
                float iconX = hintOnRight ? x + 16 + 2 : x + 8 - 2 - 16;
                float iconY = y + (TILE_H - 16) / 2f;
                batch.setColor(Color.WHITE);
                batch.draw(keyIcon, iconX, iconY, 16, 16);
            }
        }
    }

    @Override
    public boolean onAction(Collidable entity, InputManager.Action action) {
        if (opened || unlockTimer > 0) return false;
        if (action != InputManager.Action.UP) return false;
        if (entity.getType() != Collidable.Type.BLOB) return false;

        Inventory inventory = ItemPickup.inventory();
        if (inventory == null) return false;

        if (!inventory.contains(ItemType.KEY)) {
            // No key — blink key icon on the far side from BLOB
            Blob blob = (Blob) entity;
            float blobCenterX = blob.getX() + blob.getWidth() / 2f;
            float doorCenterX = x + TILE_W / 2f;
            hintOnRight = blobCenterX < doorCenterX;
            hintTimer = HINT_DURATION;
            SoundManager.play(SoundManager.SoundType.ACCESS_DENIED);
            return true;
        }

        // Has key — start unlock animation (door blinks, collision off immediately)
        unlockTimer = UNLOCK_DURATION;
        SoundManager.play(SoundManager.SoundType.ACCESS_OK);
        EventBus.get().post(GameEvent.DOOR_OPENED);
        if (SaveManager.get() != null && room != null) {
            SaveManager.get().saveDoorOpened(room.roomIndex);
        }
        return true;
    }
}
