package northern.captain.starquake.world.items;

import northern.captain.starquake.world.GameState;

@FunctionalInterface
public interface BoostEffect {
    void apply(GameState state);
}
