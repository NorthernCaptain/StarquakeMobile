package northern.captain.starquake.world.items;

import northern.captain.starquake.Assets;
import northern.captain.starquake.audio.SoundManager;
import northern.captain.starquake.audio.SoundManager.SoundType;
import northern.captain.starquake.world.Collidable;
import northern.captain.starquake.world.GameState;

/**
 * Boost items (17-24): consumed instantly on contact.
 * Auto-collected when BLOB walks or flies over them.
 * Respawn after a delay via ItemManager.
 */
public class BoostPickup extends ItemPickup {
    private final BoostEffect effect;

    public BoostPickup(Assets assets, int tileCol, int tileRow, ItemType itemType) {
        super(assets, tileCol, tileRow, itemType);
        this.effect = effectFor(itemType);
    }

    @Override
    public void onEnter(Collidable entity) {
        if (collected) return;
        if (entity.getType() != Collidable.Type.BLOB) return;
        if (!overlapsSprite(entity)) return;
        effect.apply(gameState());
        collect();
        itemManager().onItemCollected(this);
        SoundManager.play(pickupSound(itemType));
    }

    public static BoostEffect effectFor(ItemType type) {
        switch (type) {
            case HEALTH_SMALL:    return s -> s.heal(30);
            case HEALTH_FULL:     return s -> s.heal(GameState.MAX_HEALTH);
            case UNIVERSAL_BOOST: return GameState::universalBoost;
            case PLATFORM_SMALL:  return s -> s.addPlatforms(5);
            case LASER_SMALL:     return s -> s.rechargeLaser(25);
            case LASER_FULL:      return s -> s.rechargeLaser(GameState.MAX_LASER);
            case PLATFORM_FULL:   return s -> s.addPlatforms(GameState.MAX_PLATFORMS);
            case EXTRA_LIFE:      return GameState::addLife;
            default:              return s -> {};
        }
    }

    private static SoundType pickupSound(ItemType type) {
        switch (type) {
            case HEALTH_SMALL:
            case HEALTH_FULL:     return SoundType.PICKUP_ENERGY;
            case PLATFORM_SMALL:
            case PLATFORM_FULL:   return SoundType.PICKUP_PLATFORM;
            case LASER_SMALL:
            case LASER_FULL:      return SoundType.PICKUP_AMMO;
            case UNIVERSAL_BOOST:
            case EXTRA_LIFE:      return SoundType.PICKUP_MULTI;
            default:              return SoundType.PICKUP_ENERGY;
        }
    }
}
