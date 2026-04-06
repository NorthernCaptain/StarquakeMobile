package northern.captain.quadronia.game.events;

import northern.captain.gamecore.BusEvent;
import northern.captain.quadronia.game.IGameTimer;

/**
 * Created by leo on 05.09.15.
 */
public class EGameTimeOver implements BusEvent
{
    public IGameTimer timer;

    public EGameTimeOver(IGameTimer timer)
    {
        this.timer = timer;
    }
}
