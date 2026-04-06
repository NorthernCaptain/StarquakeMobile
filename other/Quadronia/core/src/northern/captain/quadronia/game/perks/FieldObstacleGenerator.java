package northern.captain.quadronia.game.perks;

import com.badlogic.gdx.utils.Array;

import northern.captain.quadronia.game.Engine;
import northern.captain.tools.Helpers;

/**
 * Created by leo on 19.05.15.
 */
public class FieldObstacleGenerator
{
    Engine game;
    public FieldObstacleGenerator(Engine game)
    {
        this.game = game;
    }

    public IFieldPerk getNextPerk2()
    {
        IFieldPerk perk = new GravityTower();
        if(perk.putToField(game, game.getField()))
            return perk;
        return null;
    }

    public IFieldPerk getNextPerk()
    {
        int idx;
        int count;

        IFieldPerk nextPerk = null;

        idx = Helpers.RND.nextInt(4) + 1;
        count = 0;

        for(int i=0;i<2;i++)
        {
            switch (idx)
            {
                case 1:
                    nextPerk = new Barrier();
                    break;
                case 2:
                    nextPerk = new Nail();
                    break;
                case 3:
                {
                    Array<IFieldPerk> perks = game.getEveryEleSetPerks();
                    for (IFieldPerk perk : perks)
                    {
                        if (perk.getType() == IFieldPerk.PERK_GRAVITY_TOWER)
                        {
                            count++;
                        }
                    }
                    nextPerk = count > 0 ? new Nail() : new GravityTower();
                }
                break;
                default:
                    nextPerk = new Barrier();
                    break;
            }

            if (nextPerk.putToField(game, game.getField()))
            {
                break;
            }

            nextPerk = null;
        }

        return nextPerk;
    }
}
