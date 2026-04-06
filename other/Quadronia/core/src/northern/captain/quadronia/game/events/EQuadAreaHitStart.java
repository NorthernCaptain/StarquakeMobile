package northern.captain.quadronia.game.events;

import northern.captain.gamecore.BusEvent;
import northern.captain.quadronia.game.core.QuadArea;
import northern.captain.quadronia.game.core.QuadCollector;

/**
 * Created by leo on 04.09.15.
 */
public class EQuadAreaHitStart implements BusEvent
{
    public QuadCollector collector;
    public QuadArea area;
    public int touchMode;

    public EQuadAreaHitStart(QuadCollector collector, int touch)
    {
        this.collector = collector;
        area = collector.area;
        touchMode = touch;
    }
}
