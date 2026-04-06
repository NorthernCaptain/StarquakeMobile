package northern.captain.quadronia.game.events;

import northern.captain.gamecore.BusEvent;
import northern.captain.quadronia.game.core.Game;

/**
 * Created by leo on 14.09.15.
 */
public class EGameOver implements BusEvent
{
    public  Game game;
    public EGameOver(Game game)
    {
        this.game = game;
    }
}
