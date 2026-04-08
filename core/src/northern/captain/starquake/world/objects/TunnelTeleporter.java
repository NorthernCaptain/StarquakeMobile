package northern.captain.starquake.world.objects;

import northern.captain.starquake.Assets;
import northern.captain.starquake.event.EventBus;
import northern.captain.starquake.event.TunnelTeleportEvent;
import northern.captain.starquake.world.Blob;
import northern.captain.starquake.world.Collidable;

/**
 * Tunnel teleporter entrance (tiles 43/44).
 *
 * Tile 43: entrance going right — 8px solid top + 8px solid right, trigger rightmost 10px.
 * Tile 44: entrance going left — mirrored.
 *
 * Posts TunnelTeleportEvent when BLOB enters the trigger zone facing the correct direction.
 * Knows nothing about the exit — TunnelController handles the rest.
 */
public class TunnelTeleporter extends GameObject {
    public static final int TILE_RIGHT = 43;
    public static final int TILE_LEFT = 44;

    private final boolean goingRight;
    private boolean triggered;

    private TunnelTeleporter(Assets assets, int tileCol, int tileRow, boolean goingRight) {
        super(assets, tileCol, tileRow);
        this.goingRight = goingRight;
    }

    public static TunnelTeleporter right(Assets assets, int tileCol, int tileRow) {
        return new TunnelTeleporter(assets, tileCol, tileRow, true);
    }

    public static TunnelTeleporter left(Assets assets, int tileCol, int tileRow) {
        return new TunnelTeleporter(assets, tileCol, tileRow, false);
    }

    @Override
    public boolean isSolidAt(float worldX, float worldY) {
        if (worldX < x || worldX >= x + TILE_W) return false;
        if (worldY < y || worldY >= y + TILE_H) return false;
        // Top 8px solid
        if (worldY >= y + TILE_H - 8) return true;
        // Side wall 8px
        if (goingRight && worldX >= x + TILE_W - 8) return true;
        if (!goingRight && worldX < x + 8) return true;
        return false;
    }

    @Override
    public void onEnter(Collidable entity) {
        if (entity.getType() != Collidable.Type.BLOB) return;
        Blob blob = (Blob) entity;
        if (blob.state != Blob.State.IDLE && blob.state != Blob.State.WALK) return;

        // Must be facing the trigger direction
        if (goingRight != blob.facingRight) return;

        // Check trigger zone (10px on the deep side)
        boolean inZone;
        if (goingRight) {
            inZone = blob.x + Blob.SIZE > x + TILE_W - 10;
        } else {
            inZone = blob.x < x + 10;
        }

        if (!inZone) {
            triggered = false;
            return;
        }
        if (triggered) return;

        triggered = true;
        EventBus.get().post(new TunnelTeleportEvent(room.roomIndex, tileRow, goingRight));
    }
}
