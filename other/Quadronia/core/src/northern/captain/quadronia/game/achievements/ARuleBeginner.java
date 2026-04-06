package northern.captain.quadronia.game.achievements;

import northern.captain.quadronia.game.Constants;
import northern.captain.quadronia.game.IGameContext;

/**
 * Created by leo on 23.08.15.
 */
public class ARuleBeginner implements IAchivementRule
{
    /**
     * Type of achievement that this rule can produce
     *
     * @return
     */
    @Override
    public int getAchievementType()
    {
        return IGameContext.ACHIEVEMENT_BEGINNER;
    }

    /**
     * The key describing game type, mode and achievement type this rule is applied to
     *
     * @return
     */
    @Override
    public int getKey()
    {
        return Constants.EVENT_CIRCUIT_DONE;
    }

    @Override
    public boolean processEvent(int eventType, int... params)
    {
        int value = params[0];

        if(value > 2)
        {
            AchieveMan.instance().unlockAchievement(getAchievementType());
        }
        return false;
    }
}
