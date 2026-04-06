package northern.captain.quadronia.game.events;

import northern.captain.gamecore.BusEvent;

/**
 * Created by leo on 05.09.15.
 */
public class ENewTimePeriod implements BusEvent
{
    public int periodDurationSec;

    public ENewTimePeriod(int totalSec)
    {
        periodDurationSec = totalSec;
    }
}
