package northern.captain.quadronia.game.perks;

import northern.captain.quadronia.game.Cell;
import northern.captain.quadronia.game.Engine;

/**
 * Created by leo on 05.04.15.
 */
public class BonusSwap extends ElePerk
{
    int lastScore;

    @Override
    public String getSpriteName()
    {
        return "bon_swap";
    }

    @Override
    public int getType()
    {
        return BONUS_SWAP;
    }

    @Override
    public boolean applyBonus(Engine game, Cell cell1)
    {
        game.setElePerk(cell, null);
        return true;
    }

    public void apply(Engine game)
    {
        game.extraSwap();
    }
}
