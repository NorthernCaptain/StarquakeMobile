package northern.captain.starquake.world.items;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import northern.captain.starquake.Assets;
import northern.captain.starquake.input.InputManager;
import northern.captain.starquake.world.Collidable;
import northern.captain.starquake.world.GameState;
import northern.captain.starquake.world.Inventory;
import northern.captain.starquake.world.Blob;
import northern.captain.starquake.world.objects.GameObject;

/**
 * Abstract base for all items placed in rooms.
 * Items are non-solid (BLOB walks through them) and require UP press to collect.
 */
public abstract class ItemPickup extends GameObject {
    public final ItemType itemType;
    protected final TextureRegion icon;
    protected boolean collected;

    // Shared refs — set once from GameScreen via init()
    private static GameState gameState;
    private static Inventory inventory;
    private static ItemManager itemManager;

    public static void init(GameState gs, Inventory inv, ItemManager im) {
        gameState = gs;
        inventory = inv;
        itemManager = im;
    }

    protected static GameState gameState() { return gameState; }
    public static Inventory inventory() { return inventory; }
    protected static ItemManager itemManager() { return itemManager; }

    protected ItemPickup(Assets assets, int tileCol, int tileRow, ItemType itemType) {
        super(assets, tileCol, tileRow);
        this.itemType = itemType;
        this.icon = assets.itemsAtlas.findRegion("item", itemType.spriteIndex);
    }

    @Override
    public boolean isSolidAt(float worldX, float worldY) {
        return false;
    }

    @Override
    public void render(SpriteBatch batch, float delta) {
        if (icon == null || collected) return;
        // 16×16 sprite centered horizontally in 32px tile, sitting on the ground (bottom of tile)
        batch.draw(icon, x + 8, y, 16, 16);
    }

    /** Check if the entity can pick up items (walking/idle BLOB, overlapping 16×16 sprite). */
    protected boolean canPickUp(Collidable entity) {
        if (collected) return false;
        if (entity.getType() != Collidable.Type.BLOB) return false;
        Blob blob = (Blob) entity;
        if (blob.state != Blob.State.IDLE && blob.state != Blob.State.WALK) return false;
        return overlapsSprite(blob);
    }

    /** Check if the entity overlaps the actual 16×16 sprite area (not the full tile). */
    protected boolean overlapsSprite(Collidable entity) {
        float spriteX = x + 8;
        float spriteY = y;
        float ex = entity.getX();
        float ey = entity.getBottom();
        float ew = entity.getWidth();
        float eh = entity.getHeight();
        return ex + ew > spriteX && ex < spriteX + 16
            && ey + eh > spriteY && ey < spriteY + 16;
    }

    /** Remove this item from the room. Guarded against double-collection. */
    protected void collect() {
        if (collected) return;
        collected = true;
        if (room != null) room.removeObject(this);
    }

    /** Shared helper: add this item to inventory, handle FIFO overflow drop. */
    protected void collectToInventory() {
        ItemType dropped = inventory().add(itemType);
        int roomIdx = (room != null) ? room.roomIndex : -1;
        collect();
        itemManager().onItemCollected(this);
        if (dropped != null && roomIdx >= 0) {
            itemManager().dropItem(dropped, roomIdx, tileCol, tileRow);
        }
    }
}
