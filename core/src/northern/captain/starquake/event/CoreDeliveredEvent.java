package northern.captain.starquake.event;

public class CoreDeliveredEvent extends GameEvent {
    public final int totalDelivered;

    public CoreDeliveredEvent(int totalDelivered) {
        super(Type.CORE_DELIVERED);
        this.totalDelivered = totalDelivered;
    }
}
