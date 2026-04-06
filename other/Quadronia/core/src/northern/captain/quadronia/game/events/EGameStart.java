package northern.captain.quadronia.game.events;

import northern.captain.gamecore.BusEvent;
import northern.captain.quadronia.game.core.Game;

/**
 * Created by leo on 04.09.15.
 */
public class EGameStart implements BusEvent
{
    public Game game;
    public boolean restored = false;
    public EGameStart(Game game, boolean restored)
    {
        this.restored = restored;
        this.game = game;
    }
}
