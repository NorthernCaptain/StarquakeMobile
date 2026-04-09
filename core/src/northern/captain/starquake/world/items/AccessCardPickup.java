package northern.captain.starquake.world.items;

import northern.captain.starquake.Assets;
import northern.captain.starquake.input.InputManager;
import northern.captain.starquake.world.Collidable;

/**
 * The Access Card (item 15). Goes into inventory.
 * Opens space locks (tiles 37-38) and enables Cheops Pyramid trading.
 */
public class AccessCardPickup extends ItemPickup implements Inventoriable {

    public AccessCardPickup(Assets assets, int tileCol, int tileRow) {
        super(assets, tileCol, tileRow, ItemType.ACCESS_CARD);
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
