package northern.captain.quadronia.game.perks;

import northern.captain.quadronia.game.Engine;
import northern.captain.tools.Helpers;

/**
 * Created by leo on 05.04.15.
 */
public class ElePerkGenerator
{
    Engine game;

    public ElePerkGenerator(Engine game)
    {
        this.game = game;
    }

    public ElePerk getNextPerk()
    {
        ElePerk perk = null;
        switch(Helpers.RND.nextInt(4))
        {
            case 0:
                if(game.numberPerksByType(IFieldPerk.PERK_BARRIER) > 0)
                {
                    perk = new BonusAntiBarrier();
                    break;
                }
            case 1:
                perk = new BonusScoreX2();
                break;
            case 2:
                perk = new BonusBackstep();
                break;
            case 3:
                perk = new BonusSwap();
                break;
//            case 4:
//                perk = new ExitConnector();
//                break;
        }
        return  perk;
    }
}
