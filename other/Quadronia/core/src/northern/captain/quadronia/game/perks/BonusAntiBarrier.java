package northern.captain.quadronia.game.perks;

import com.badlogic.gdx.utils.Array;

import northern.captain.quadronia.game.Cell;
import northern.captain.quadronia.game.Engine;
import northern.captain.tools.Helpers;

/**
 * Created by leo on 05.04.15.
 */
public class BonusAntiBarrier extends ElePerk
{
    public Barrier chosenBarrier;

    public BonusAntiBarrier()
    {
    }

    public BonusAntiBarrier(int side1, int side2)
    {
        super(side1, side2);
    }

    public BonusAntiBarrier(int side1)
    {
        super(side1);
    }


    @Override
    public int getType()
    {
        return BONUS_ANTI_BARRIER;
    }

    @Override
    public String getSpriteName()
    {
        return "bon_barrier";
    }

    @Override
    public boolean applyBonus(Engine game, Cell cell1)
    {
        game.setElePerk(cell, null);

        Array<IFieldPerk> perks = game.getAllPerks();

        int numBarriers = 0;
        for(IFieldPerk perk : perks)
        {
            if(perk.getType() == PERK_BARRIER)
            {
                numBarriers++;
            }
        }

        if(numBarriers == 0) return true;

        int barrierIdx = Helpers.RND.nextInt(numBarriers);

        for(IFieldPerk perk : perks)
        {
            if(perk.getType() == PERK_BARRIER)
            {
                if(barrierIdx == 0)
                {
                    chosenBarrier = (Barrier)perk;
                    chosenBarrier.removeFromField(game);
                    return true;
                }
                barrierIdx--;
            }
        }

        return true;
    }
}
