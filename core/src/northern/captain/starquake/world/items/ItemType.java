package northern.captain.starquake.world.items;

/**
 * All item types in the game, indexed by Atari ST sprite index (0-34).
 */
public enum ItemType {
    // Core display pieces (0-8) — not placeable, only for core room rendering
    CORE_TL(0), CORE_TC(1), CORE_TR(2),
    CORE_ML(3), CORE_MC(4), CORE_MR(5),
    CORE_BL(6), CORE_BC(7), CORE_BR(8),

    // Collectible core parts — set A (9-14)
    PART_A0(9), PART_A1(10), PART_A2(11),
    PART_A3(12), PART_A4(13), PART_A5(14),

    // Special inventory items
    ACCESS_CARD(15),
    KEY(16),

    // Boost pickups — instant consumption (17-24)
    HEALTH_SMALL(17), HEALTH_FULL(18),
    UNIVERSAL_BOOST(19),
    PLATFORM_SMALL(20), LASER_SMALL(21),
    LASER_FULL(22), PLATFORM_FULL(23),
    EXTRA_LIFE(24),

    // Cheops pyramid — trade point (25)
    PYRAMID(25),

    // Collectible core parts — set B (26-34)
    PART_B0(26), PART_B1(27), PART_B2(28), PART_B3(29),
    PART_B4(30), PART_B5(31), PART_B6(32), PART_B7(33), PART_B8(34);

    public final int spriteIndex;

    ItemType(int spriteIndex) {
        this.spriteIndex = spriteIndex;
    }

    private static final ItemType[] BY_INDEX = new ItemType[35];
    static {
        for (ItemType t : values()) BY_INDEX[t.spriteIndex] = t;
    }

    public static ItemType fromIndex(int idx) {
        return (idx >= 0 && idx < BY_INDEX.length) ? BY_INDEX[idx] : null;
    }

    public boolean isBoost() {
        return spriteIndex >= 17 && spriteIndex <= 24;
    }

    public boolean isCorePart() {
        return (spriteIndex >= 9 && spriteIndex <= 14)
            || (spriteIndex >= 26 && spriteIndex <= 34);
    }

    public boolean isCoreDisplay() {
        return spriteIndex >= 0 && spriteIndex <= 8;
    }

    public boolean isPyramid() {
        return this == PYRAMID;
    }

    public boolean isInventoriable() {
        return isCorePart() || this == ACCESS_CARD || this == KEY;
    }
}
