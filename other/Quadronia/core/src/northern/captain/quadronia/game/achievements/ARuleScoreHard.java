package northern.captain.quadronia.game.achievements;

import northern.captain.quadronia.game.Constants;
import northern.captain.quadronia.game.Engine;
import northern.captain.quadronia.game.ItemFactory;

/**
 * Created by leo on 23.08.15.
 */
public class ARuleScoreHard implements IAchivementRule
{
    private int threshold;
    private int type;
    private int mode = Constants.EVENT_SCORE_CHANGED;

    public ARuleScoreHard(int scoreLimit, int type)
    {
        threshold = scoreLimit;
        this.type = type;
    }

    public ARuleScoreHard(int scoreLimit, int type, int gameMode)
    {
        threshold = scoreLimit;
        this.type = type;
        mode = AchieveMan.getModeKey(gameMode, Constants.EVENT_SCORE_CHANGED);
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
        if(val >= threshold && Engine.mode == ItemFactory.MODE_THREE)
        {
            AchieveMan.instance().unlockAchievement(getAchievementType());
        }
        return false;
    }
}
