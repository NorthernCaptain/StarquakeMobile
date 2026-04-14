package northern.captain.starquake.event;

import northern.captain.starquake.world.items.ItemType;

public class TradeCompletedEvent extends GameEvent {
    public final ItemType given;
    public final ItemType received;

    public TradeCompletedEvent(ItemType given, ItemType received) {
        super(Type.TRADE_COMPLETED);
        this.given = given;
        this.received = received;
    }
}
