package northern.captain.quadronia.game.perks;

import northern.captain.quadronia.game.Cell;
import northern.captain.quadronia.game.Engine;

/**
 * Created by leo on 05.04.15.
 */
public class BonusScoreX2 extends ElePerk
{
    int lastScore;

    @Override
    public String getSpriteName()
    {
        return "bon_scorex2";
    }

    @Override
    public int getType()
    {
        return BONUS_SCORE_X2;
    }

    @Override
    public boolean applyBonus(Engine game, Cell cell1)
    {
        lastScore = game.getContext().getLastScoreDelta();
        game.setElePerk(cell, null);
        return true;
    }

    public void applyScore(Engine game)
    {
        game.addScore(lastScore);
    }
}
