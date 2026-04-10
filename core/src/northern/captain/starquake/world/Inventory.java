package northern.captain.starquake.world;

import northern.captain.starquake.world.items.ItemType;

/**
 * 4-slot FIFO inventory. Oldest item is pushed out when a 5th is added.
 * Stores ItemType values. Slot 0 = newest, slot count-1 = oldest.
 */
public class Inventory {
    public static final int MAX_SLOTS = 4;

    private final ItemType[] slots = new ItemType[MAX_SLOTS];
    private int count;

    /**
     * Add an item to the front of the FIFO.
     * Returns the pushed-out ItemType if full, or null if no overflow.
     */
    public ItemType add(ItemType item) {
        ItemType dropped = null;
        if (count >= MAX_SLOTS) {
            dropped = slots[MAX_SLOTS - 1];
            System.arraycopy(slots, 0, slots, 1, MAX_SLOTS - 1);
        } else {
            if (count > 0) {
                System.arraycopy(slots, 0, slots, 1, count);
            }
            count++;
        }
        slots[0] = item;
        return dropped;
    }

    /** Remove the first occurrence of the given item type. */
    public boolean remove(ItemType item) {
        for (int i = 0; i < count; i++) {
            if (slots[i] == item) {
                System.arraycopy(slots, i + 1, slots, i, count - i - 1);
                count--;
                slots[count] = null;
                return true;
            }
        }
        return false;
    }

    public boolean contains(ItemType item) {
        for (int i = 0; i < count; i++) {
            if (slots[i] == item) return true;
        }
        return false;
    }

    public int getCount() {
        return count;
    }

    /** Get item at slot index (0 = newest). Returns null if empty. */
    public ItemType getSlot(int index) {
        return (index >= 0 && index < count) ? slots[index] : null;
    }

    /** Replace item at given slot index. Index must be valid (0..count-1). */
    public void setSlot(int index, ItemType item) {
        if (index >= 0 && index < count) {
            slots[index] = item;
        }
    }

    public void clear() {
        for (int i = 0; i < MAX_SLOTS; i++) slots[i] = null;
        count = 0;
    }
}
