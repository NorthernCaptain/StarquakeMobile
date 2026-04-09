package northern.captain.starquake.world.items;

import northern.captain.starquake.Assets;
import northern.captain.starquake.input.InputManager;
import northern.captain.starquake.world.Collidable;

/**
 * The Key (item 16). Goes into inventory. Opens doors (tile 45). Reusable.
 */
public class KeyPickup extends ItemPickup implements Inventoriable {

    public KeyPickup(Assets assets, int tileCol, int tileRow) {
        super(assets, tileCol, tileRow, ItemType.KEY);
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
