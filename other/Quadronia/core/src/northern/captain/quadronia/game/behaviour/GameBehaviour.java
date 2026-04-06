package northern.captain.quadronia.game.behaviour;

import northern.captain.quadronia.game.Engine;

/**
 * Created by leo on 17.05.15.
 */
public abstract class GameBehaviour
{
    public static final int NO_LEVEL_UP = 9999;
    int timeout;
    Engine game;
    public GameBehaviour(Engine game)
    {
        this.game = game;
    }

    public abstract void doOnTimeOut();

    public int getTimeOut()
    {
        return timeout;
    }

    public abstract void doOnNextMove();
    public abstract void doOnLevelUp(int newLevel);
    public void doOnStart(boolean resumed) {}
    public abstract int nextLevelUpThreshold(int forLevel);
    public boolean levelUpsAllowed() { return true;}
}
