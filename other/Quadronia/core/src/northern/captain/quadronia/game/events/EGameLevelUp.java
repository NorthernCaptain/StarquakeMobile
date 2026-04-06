package northern.captain.quadronia.game.events;

import northern.captain.gamecore.BusEvent;

/**
 * Created by leo on 05.09.15.
 */
public class EGameLevelUp implements BusEvent
{
    public int level;
    public int bonus;

    public EGameLevelUp(int bonus, int level)
    {
        this.level = level;
        this.bonus = bonus;
    }
}
