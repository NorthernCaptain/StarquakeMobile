package northern.captain.starquake.world.items;

import northern.captain.starquake.Assets;
import northern.captain.starquake.input.InputManager;
import northern.captain.starquake.world.Collidable;

/**
 * Collectible core parts (items 9-14, 26-34).
 * Go into 4-slot FIFO inventory. Deliverable to core room.
 */
public class CorePartPickup extends ItemPickup implements Inventoriable {

    public CorePartPickup(Assets assets, int tileCol, int tileRow, ItemType itemType) {
        super(assets, tileCol, tileRow, itemType);
    }

    @Override
    public ItemType getItemType() {
        return itemType;
    }

    @Override
    public boolean onAction(Collidable entity, InputManager.Action action) {
        if (action != InputManager.Action.UP) return false;
        if (!canPickUp(entity)) return false;
        collectToInventory();
        return true;
    }
}
