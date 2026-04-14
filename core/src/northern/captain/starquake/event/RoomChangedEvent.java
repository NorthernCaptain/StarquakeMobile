package northern.captain.starquake.event;

public class RoomChangedEvent extends GameEvent {
    public final int oldRoom;
    public final int newRoom;

    public RoomChangedEvent(int oldRoom, int newRoom) {
        super(Type.ROOM_CHANGED);
        this.oldRoom = oldRoom;
        this.newRoom = newRoom;
    }
}
