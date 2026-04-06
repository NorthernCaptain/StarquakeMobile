package northern.captain.quadronia.game.events;

import northern.captain.gamecore.BusEvent;
import northern.captain.quadronia.game.core.Quad;
import northern.captain.quadronia.game.core.QuadArea;
import northern.captain.quadronia.game.core.QuadCollector;

/**
 * Created by leo on 05.09.15.
 */
public class EQuadAreaHitFinish implements BusEvent
{
    public QuadArea area;
    public QuadCollector collector;
    public EQuadAreaHitFinish(QuadArea area, QuadCollector collector)
    {
        this.area = area;
        this.collector = collector;
    }
}
