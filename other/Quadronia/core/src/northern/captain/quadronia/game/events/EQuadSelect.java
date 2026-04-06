package northern.captain.quadronia.game.events;

import northern.captain.gamecore.BusEvent;
import northern.captain.quadronia.game.core.Quad;
import northern.captain.quadronia.game.core.QuadCollector;

/**
 * Created by leo on 04.09.15.
 */
public class EQuadSelect implements BusEvent
{
    public Quad quad;
    public int selectionType;
    public QuadCollector collector;

    public EQuadSelect(QuadCollector collector, Quad quad, int type)
    {
        this.collector = collector;
        this.quad = quad;
        selectionType = type;
    }
}
