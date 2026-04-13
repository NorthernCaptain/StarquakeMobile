package northern.captain.starquake.world.items;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import northern.captain.starquake.Assets;
import northern.captain.starquake.audio.SoundManager;
import northern.captain.starquake.event.EnterTradeEvent;
import northern.captain.starquake.event.EventBus;
import northern.captain.starquake.input.InputManager;
import northern.captain.starquake.world.Collidable;
import northern.captain.starquake.world.Inventory;
import com.badlogic.gdx.math.MathUtils;

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
            flashTimer = FLASH_DURATION;
            SoundManager.play(SoundManager.SoundType.ACCESS_DENIED);
            return true;
        }

        // Pick random eligible inventory item (non-card, non-key, non-pyramid)
        int[] eligibleSlots = new int[Inventory.MAX_SLOTS];
        int count = 0;
        for (int i = 0; i < Inventory.MAX_SLOTS; i++) {
            ItemType it = inventory().getSlot(i);
            if (it != null && it != ItemType.ACCESS_CARD && it != ItemType.KEY && it != ItemType.PYRAMID) {
                eligibleSlots[count++] = i;
            }
        }
        if (count == 0) return true; // nothing to trade

        int slot = eligibleSlots[MathUtils.random(count - 1)];
        EventBus.get().post(new EnterTradeEvent(this, inventory().getSlot(slot), slot));
        return true;
    }

    /** Called by the trade system after a successful trade. */
    public void consumeAfterTrade() {
        collect();
        itemManager().onItemCollected(this);
    }
}
