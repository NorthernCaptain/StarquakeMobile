package northern.captain.starquake.event;

import northern.captain.starquake.world.items.CheopsPyramid;
import northern.captain.starquake.world.items.ItemType;

public class EnterTradeEvent extends GameEvent {
    public final CheopsPyramid pyramid;
    public final ItemType offeredItem;
    public final int slotIndex;

    public EnterTradeEvent(CheopsPyramid pyramid, ItemType offeredItem, int slotIndex) {
        super(Type.ENTER_TRADE);
        this.pyramid = pyramid;
        this.offeredItem = offeredItem;
        this.slotIndex = slotIndex;
    }
}
