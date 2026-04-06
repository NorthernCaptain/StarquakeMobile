package northern.captain.quadronia.game.core.behaviour;

import northern.captain.quadronia.game.Engine;
import northern.captain.quadronia.game.core.Game;
import northern.captain.tools.Helpers;
import northern.captain.tools.IJSONSerializer;

/**
 * Created by leo on 17.05.15.
 */
public abstract class Behaviour implements IJSONSerializer
{
    public static final int NO_LEVEL_UP = 9999;
    int timeout;
    Game game;
    int bonusTimeSet = 0;
    int bonusCoinsSet = 0;
    int bonusMultiSet = 0;

    public Behaviour(Game game)
    {
        this.game = game;
    }

    public abstract void doOnTimeOut();

    public int getTimeOut()
    {
        return timeout;
    }

    public abstract void doOnNextMove();
    public void doOnLevelUp(int newLevel)
    {
        bonusTimeSet = bonusCoinsSet = bonusMultiSet = 0;
    }
    public void doOnStart(boolean resumed) {}
    public abstract int nextLevelUpThreshold(int forLevel);
    public boolean levelUpsAllowed() { return true;}
    public boolean needTimerRestartOnMove() { return true;}

    public abstract int getMaxColors();
    public abstract int getDeltaColor();

    public boolean canBonusMulti()
    {
        return bonusMultiSet < 2 && game.level > 1 && game.level % 2 == 0;
    }

    public boolean canBonusX2()
    {
        return game.level > 4 || game.level == 3;
    }

    public boolean canBonusM200()
    {
        return game.level > 5 || game.level == 4;
    }

    public boolean canBonusNO()
    {
        return game.level > 2;
    }

    public boolean canBonusBiColor()
    {
        return game.level > -1;
    }

    public boolean canBonusCoins() {
        return game.level > 2 && bonusCoinsSet == 0 && Helpers.RND.nextBoolean();
    }

    public boolean canBonusTime() {
        return game.level > 1 && game.level % 3 > 0 && bonusTimeSet == 0;
    }

    public void doOnTimeBonus()
    {
        bonusTimeSet++;
    }

    public void bonusTimeOnField()
    {
        bonusTimeSet++;
    }

    public void bonusMultiOnField()
    {
        bonusMultiSet++;
    }

    public void bonusCoinsOnField()
    {
        bonusCoinsSet++;
    }

}
