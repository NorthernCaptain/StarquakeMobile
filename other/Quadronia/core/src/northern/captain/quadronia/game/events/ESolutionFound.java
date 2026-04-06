package northern.captain.quadronia.game.events;

import northern.captain.gamecore.BusEvent;
import northern.captain.quadronia.game.core.QuadArea;

/**
 * Created by leo on 16.09.15.
 */
public class ESolutionFound implements BusEvent
{
    public QuadArea solution;

    public ESolutionFound(QuadArea found)
    {
        solution = found;
    }
}
