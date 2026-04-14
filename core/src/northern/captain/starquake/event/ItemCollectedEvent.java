package northern.captain.starquake.event;

import northern.captain.starquake.world.items.ItemType;

public class ItemCollectedEvent extends GameEvent {
    public final ItemType itemType;
    public final int roomIndex;

    public ItemCollectedEvent(ItemType itemType, int roomIndex) {
        super(Type.ITEM_COLLECTED);
        this.itemType = itemType;
        this.roomIndex = roomIndex;
    }
}
