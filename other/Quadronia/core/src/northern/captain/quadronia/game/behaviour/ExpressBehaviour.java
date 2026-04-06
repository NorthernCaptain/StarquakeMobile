package northern.captain.quadronia.game.behaviour;

import northern.captain.quadronia.game.Engine;

/**
 * Created by leo on 17.05.15.
 */
public class ExpressBehaviour extends GameBehaviour
{
    public ExpressBehaviour(Engine game)
    {
        super(game);
        timeout = 5*60;
    }

    @Override
    public void doOnTimeOut()
    {
        game.doGameOver();
    }

    @Override
    public void doOnLevelUp(int newLevel)
    {

    }

    @Override
    public void doOnNextMove()
    {
    }

    @Override
    public int nextLevelUpThreshold(int forLevel)
    {
        return 70;
    }
}
