package northern.captain.quadronia.game.events;

import northern.captain.gamecore.BusEvent;
import northern.captain.quadronia.game.core.QuadArea;

/**
 * Created by leo on 20.09.15.
 */
public class EPerkShowHint extends ESolutionFound implements BusEvent
{
    public EPerkShowHint(QuadArea found)
    {
        super(found);
    }
}
