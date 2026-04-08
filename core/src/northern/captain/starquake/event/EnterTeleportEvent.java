package northern.captain.starquake.event;

public class EnterTeleportEvent extends GameEvent {
    public final int roomIndex;

    public EnterTeleportEvent(int roomIndex) {
        super(Type.ENTER_TELEPORT);
        this.roomIndex = roomIndex;
    }
}
