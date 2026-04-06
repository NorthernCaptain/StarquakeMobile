package northern.captain.quadronia.game.core;

import northern.captain.quadronia.game.Constants;
import northern.captain.quadronia.game.core.behaviour.Behaviour;
import northern.captain.tools.Helpers;

/**
 * Created by leo on 05.09.15.
 */
public class BehaviourColorGenerator implements IColorGenerator
{
    private Behaviour behaviour;
    public BehaviourColorGenerator(Behaviour behaviour)
    {
        this.behaviour = behaviour;
    }

    private boolean canBonusMulti;
    private boolean canBonusX2;
    private boolean canBonusM200;
    private boolean canBonusNO;
    private boolean canBiColor;
    private boolean canBonusCoins;
    private boolean canBonusTime;

    @Override
    public void init()
    {
        canBonusMulti = behaviour.canBonusMulti();
        canBonusM200 = behaviour.canBonusM200();
        canBonusNO = behaviour.canBonusNO();
        canBonusX2 = behaviour.canBonusX2();
        canBiColor = behaviour.canBonusBiColor();
        canBonusCoins = behaviour.canBonusCoins();
        canBonusTime = behaviour.canBonusTime();
    }

    /**
     * Generates color for the quad. Each call return random color
     *
     * @param maxColors
     * @return generated color
     */
    @Override
    public int generate(int maxColors, int x, int y, int width, int height)
    {
        int subval;
        boolean edgeCase = (x==0 || x==width-1) && (y==0 || y==height-1);

        if(canBonusMulti)
        {
            subval = Helpers.RND.nextInt(300);
            if (subval > 289)
            {
                canBonusMulti = false;
                behaviour.bonusMultiOnField();
                return Quad.TYPE_MULTI;
            }
        }

        if(canBonusX2)
        {
            subval = Helpers.RND.nextInt(200);
            if (subval > 196) //197
            {
                Integer idx2;
                int i1 = 1 + behaviour.getDeltaColor();
                for(int i=0;i<2;i++)
                {
                    int idx = (i1 + Helpers.RND.nextInt(behaviour.getMaxColors())) % Constants.MAX_COLORS;
                    idx2 = Quad.x2BonusMap.get(idx);
                    if (idx2 != null)
                    {
                        return idx2;
                    }
                }

                for(int i=0;i<behaviour.getMaxColors();i++)
                {
                    int idx = (i + i1) % Constants.MAX_COLORS;
                    idx2 = Quad.x2BonusMap.get(idx);
                    if (idx2 != null)
                    {
                        return idx2;
                    }
                }
            }
        }

        if(canBonusM200 && !edgeCase)
        {
            subval = Helpers.RND.nextInt(200);
            if (subval > 197) return Quad.TYPE_M200;
        }

        if(canBonusNO && !edgeCase)
        {
            subval = Helpers.RND.nextInt(200);
            if (subval > 196) return Quad.TYPE_NO;
        }

        if(canBonusCoins && !edgeCase)
        {
            subval = Helpers.RND.nextInt(400);
            if (subval > 397)
            {
                canBonusCoins = false;
                behaviour.bonusCoinsOnField();
                return Quad.TYPE_COINS;
            }
        }

        if(canBonusTime && !edgeCase)
        {
            subval = Helpers.RND.nextInt(400);
            if (subval > 398)
            {
                canBonusTime = false;
                behaviour.bonusTimeOnField();
                return Quad.TYPE_TIME;
            }
        }

        if(canBiColor)
        {
            subval = Helpers.RND.nextInt(200);
            if (subval > 190)
            {
                Integer idx2;
                int i1 = 1 + behaviour.getDeltaColor();
                for(int i=0;i<2;i++)
                {
                    int idx = (i1 + Helpers.RND.nextInt(behaviour.getMaxColors()-1)) % Constants.MAX_COLORS;
                    idx2 = Quad.biColorMap.get(idx);
                    if (idx2 != null)
                    {
                        return -idx;
                    }
                }

                for(int i=0;i<behaviour.getMaxColors()-1;i++)
                {
                    int idx = (i + i1) % Constants.MAX_COLORS;
                    idx2 = Quad.biColorMap.get(idx);
                    if (idx2 != null)
                    {
                        return -idx;
                    }
                }
            }

        }

        return 1 + (behaviour.getDeltaColor() + Helpers.RND.nextInt(behaviour.getMaxColors())) % (Constants.MAX_COLORS - 1);
    }
}
