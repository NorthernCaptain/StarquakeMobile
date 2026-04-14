package northern.captain.starquake.world;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import northern.captain.starquake.Assets;
import northern.captain.starquake.event.EventBus;
import northern.captain.starquake.event.GameEvent;
import northern.captain.starquake.event.TunnelTeleportEvent;
import northern.captain.starquake.world.objects.TunnelTeleporter;
import northern.captain.starquake.world.transitions.BlowOutTransition;
import northern.captain.starquake.world.transitions.BlobTransition;
import northern.captain.starquake.world.transitions.PauseTransition;
import northern.captain.starquake.world.transitions.SuckInTransition;

/**
 * Controls the full tunnel teleportation process.
 *
 * On receiving TUNNEL_TELEPORT event:
 * 1. Locks blob
 * 2. Finds exit tile in adjacent room
 * 3. Plays suck-in animation
 * 4. Triggers room transition with slide animation
 * 5. Repositions blob at exit tile
 * 6. Plays blow-out animation
 * 7. Unlocks blob
 *
 * GameScreen just creates this controller — knows nothing about tunnel logic.
 */
public class TunnelController {

    public interface RoomTransitionHandler {
        /** Switch to adjacent room in the given direction. Returns new Room or null. */
        Room transitionHorizontal(int dx);
    }

    private final Assets assets;
    private Blob blob;
    private Room room;
    private RoomTransitionHandler transitionHandler;

    private enum Phase { IDLE, SUCK_IN, ROOM_SWITCH, BLOW_OUT }
    private Phase phase = Phase.IDLE;

    private BlobTransition currentTransition;
    private boolean goingRight;
    private int destTileCol;
    private int destTileRow;
    private float roomSwitchDelay;

    public TunnelController(Assets assets) {
        this.assets = assets;
        EventBus.get().register(GameEvent.Type.TUNNEL_TELEPORT, this::onTunnelEvent);
    }

    public void setBlob(Blob blob) { this.blob = blob; }
    public void setRoom(Room room) { this.room = room; }
    public void setTransitionHandler(RoomTransitionHandler handler) { this.transitionHandler = handler; }

    public boolean isActive() { return phase != Phase.IDLE; }

    private void onTunnelEvent(GameEvent event) {
        if (phase != Phase.IDLE) return;
        TunnelTeleportEvent e = (TunnelTeleportEvent) event;

        goingRight = e.goingRight;
        int dx = goingRight ? 1 : -1;
        int adjacentRoom = Room.adjacentIndex(e.roomIndex, dx, 0);
        if (adjacentRoom < 0) return;

        // Find exit tile in adjacent room — prefer same row, fall back to any row
        int exitTileId = goingRight ? TunnelTeleporter.TILE_LEFT : TunnelTeleporter.TILE_RIGHT;
        int foundCol = -1;
        int foundRow = -1;

        // First: check same row
        for (int col = 0; col < Room.TILE_COLS; col++) {
            if (assets.getTileIdAt(adjacentRoom, col, e.tileRow) == exitTileId) {
                foundCol = col;
                foundRow = e.tileRow;
                break;
            }
        }

        // Fallback: scan all rows
        if (foundCol < 0) {
            for (int row = 0; row < Room.TILE_ROWS; row++) {
                for (int col = 0; col < Room.TILE_COLS; col++) {
                    if (assets.getTileIdAt(adjacentRoom, col, row) == exitTileId) {
                        foundCol = col;
                        foundRow = row;
                        break;
                    }
                }
                if (foundCol >= 0) break;
            }
        }
        if (foundCol < 0) return; // no exit found

        destTileCol = foundCol;
        destTileRow = foundRow;

        // Lock blob and start suck-in
        blob.startTransition();
        phase = Phase.SUCK_IN;

        SuckInTransition suckIn = new SuckInTransition(assets, goingRight);
        suckIn.setFacingRight(blob.facingRight);
        suckIn.start(blob.x + Blob.SIZE / 2f, blob.y + Blob.SIZE / 2f);
        currentTransition = suckIn;
    }

    public void update(float delta) {
        if (phase == Phase.IDLE) return;

        switch (phase) {
            case SUCK_IN:
                currentTransition.update(delta);
                if (currentTransition.isComplete()) {
                    currentTransition.dispose();
                    // Trigger room transition
                    phase = Phase.ROOM_SWITCH;
                    roomSwitchDelay = 0.45f; // wait for slide animation (0.4s) + small buffer
                    if (transitionHandler != null) {
                        int dx = goingRight ? 1 : -1;
                        Room newRoom = transitionHandler.transitionHorizontal(dx);
                        if (newRoom != null) {
                            room = newRoom;
                        }
                    }
                    // Reposition blob centered at bottom of exit tile
                    float exitX = destTileCol * Room.TILE_W;
                    float exitY = (5 - destTileRow) * Room.TILE_H;
                    blob.x = exitX + (Room.TILE_W - Blob.SIZE) / 2f;
                    blob.y = exitY;
                }
                break;

            case ROOM_SWITCH:
                roomSwitchDelay -= delta;
                if (roomSwitchDelay <= 0) {
                    // Start blow-out at destination
                    phase = Phase.BLOW_OUT;
                    BlowOutTransition blowOut = new BlowOutTransition(assets, !goingRight);
                    blowOut.setFacingRight(goingRight); // continue facing travel direction
                    blowOut.start(blob.x + Blob.SIZE / 2f, blob.y + Blob.SIZE / 2f);
                    currentTransition = blowOut;
                }
                break;

            case BLOW_OUT:
                currentTransition.update(delta);
                if (currentTransition.isComplete()) {
                    currentTransition.dispose();
                    currentTransition = null;
                    phase = Phase.IDLE;
                    blob.endTransition(false);
                }
                break;
        }
    }

    public void render(SpriteBatch batch) {
        if (currentTransition != null && (phase == Phase.SUCK_IN || phase == Phase.BLOW_OUT)) {
            currentTransition.render(batch);
        }
    }
}
