package northern.captain.starquake.world.items;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import northern.captain.starquake.Assets;
import northern.captain.starquake.event.EventBus;
import northern.captain.starquake.event.GameEvent;
import northern.captain.starquake.input.InputManager;
import northern.captain.starquake.world.Collidable;

/**
 * Cheops Pyramid (item 25). Placed randomly in rooms (32 instances).
 * NOT collected on contact. Press UP to open trading screen.
 * Requires Access Card. Consumed after 1 trade.
 */
public class CheopsPyramid extends ItemPickup {
    private static final float FLASH_DURATION = 2f;
    private static final float FLASH_FREQ = 5f; // Hz

    private final TextureRegion cardIcon;
    private float flashTimer;

    public CheopsPyramid(Assets assets, int tileCol, int tileRow) {
        super(assets, tileCol, tileRow, ItemType.PYRAMID);
        this.cardIcon = assets.itemsAtlas.findRegion("item", ItemType.ACCESS_CARD.spriteIndex);
    }

    @Override
    public void update(float delta) {
        if (flashTimer > 0) {
            flashTimer -= delta;
        }
    }

    @Override
    public void render(SpriteBatch batch, float delta) {
        super.render(batch, delta);
        if (collected || cardIcon == null) return;

        // Flash the access card icon above the pyramid when player tried without card
        if (flashTimer > 0) {
            boolean visible = ((int) (flashTimer * FLASH_FREQ * 2)) % 2 == 0;
            if (visible) {
                batch.draw(cardIcon, x + 8, y + 18, 16, 16);
            }
        }
    }

    @Override
    public boolean onAction(Collidable entity, InputManager.Action action) {
        if (action != InputManager.Action.UP) return false;
        if (!canPickUp(entity)) return false;

        if (!inventory().contains(ItemType.ACCESS_CARD)) {
            // No card — show flashing card hint
            flashTimer = FLASH_DURATION;
            return true;
        }

        // TODO: post EnterTradeEvent(this) once trading screen is implemented
        EventBus.get().post(GameEvent.ENTER_TRADE);
        return true;
    }

    /** Called by the trade system after a successful trade. */
    public void consumeAfterTrade() {
        collect();
        itemManager().onItemCollected(this);
    }
}
