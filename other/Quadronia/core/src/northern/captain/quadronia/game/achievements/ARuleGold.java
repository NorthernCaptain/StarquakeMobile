package northern.captain.quadronia.game.achievements;

import northern.captain.quadronia.game.Constants;

/**
 * Created by leo on 23.08.15.
 */
public class ARuleGold implements IAchivementRule
{
    private int threshold;
    private int type;
    private int mode = Constants.EVENT_GOLD_CHANGED;

    public ARuleGold(int scoreLimit, int type)
    {
        threshold = scoreLimit;
        this.type = type;
    }

    /**
     * Type of achievement that this rule can produce
     *
     * @return
     */
    @Override
    public int getAchievementType()
    {
        return type;
    }

    /**
     * The key describing game type, mode and achievement type this rule is applied to
     *
     * @return
     */
    @Override
    public int getKey()
    {
        return mode;
    }

    @Override
    public boolean processEvent(int eventType, int... params)
    {
        int val = params[0];
        if(val >= threshold)
        {
            AchieveMan.instance().unlockAchievement(getAchievementType());
        }
        return false;
    }
}
