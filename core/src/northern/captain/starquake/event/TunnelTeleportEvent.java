package northern.captain.starquake.event;

public class TunnelTeleportEvent extends GameEvent {
    public final int roomIndex;
    public final int tileRow;
    public final boolean goingRight;

    public TunnelTeleportEvent(int roomIndex, int tileRow, boolean goingRight) {
        super(Type.TUNNEL_TELEPORT);
        this.roomIndex = roomIndex;
        this.tileRow = tileRow;
        this.goingRight = goingRight;
    }
}
