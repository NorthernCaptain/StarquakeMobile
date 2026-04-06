package northern.captain.quadronia.game.events;

import northern.captain.gamecore.BusEvent;

public class ETimeDeltaChange implements BusEvent
{
    public int deltaTimeSec;

    public ETimeDeltaChange(int deltaTimeSec)
    {
        this.deltaTimeSec = deltaTimeSec;
    }
}
