package northern.captain.quadronia.game.behaviour;

import northern.captain.quadronia.game.Engine;
import northern.captain.quadronia.game.IGameTimer;

/**
 * Created by leo on 17.05.15.
 */
public class ArcadeBehaviour extends GameBehaviour
{
    private int maxTimeout = 25;

    public ArcadeBehaviour(Engine game)
    {
        super(game);

        timeout = maxTimeout;
    }

    @Override
    public void doOnTimeOut()
    {
        IGameTimer gameTimer = game.getGameTimer();
        gameTimer.resetTimer(getTimeOut());
        game.doFieldPerk();
    }

    @Override
    public void doOnNextMove()
    {
        IGameTimer gameTimer = game.getGameTimer();
        gameTimer.resetTimer(getTimeOut());
    }

    @Override
    public void doOnLevelUp(int newLevel)
    {
        int delta = newLevel;
        timeout = Math.max(5, maxTimeout - delta);
    }

    @Override
    public int nextLevelUpThreshold(int forLevel)
    {
        return 90;
    }
}
