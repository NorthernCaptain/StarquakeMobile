package northern.captain.starquake.world.items;

/**
 * Marker interface for items that enter the 4-slot FIFO inventory.
 * Implemented by CorePartPickup, KeyPickup, AccessCardPickup.
 */
public interface Inventoriable {
    ItemType getItemType();
}
