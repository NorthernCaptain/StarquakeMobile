package northern.captain.quadronia.android.achievements;

import android.content.res.Resources;

import northern.captain.quadronia.android.R;
import northern.captain.quadronia.android.common.AndroidContext;
import northern.captain.quadronia.game.IGameContext;
import northern.captain.quadronia.game.achievements.ARuleLevel;
import northern.captain.quadronia.game.achievements.ARuleScore;
import northern.captain.quadronia.game.achievements.AchieveMan;
import northern.captain.quadronia.game.core.Game;

/**
 * Created by leo on 23.08.15.
 */
public class AchieveManAnd extends AchieveMan
{
    public static void initialize()
    {
        singleton = new AchieveManAnd();
    }

    protected AchieveManAnd()
    {
        Resources res = AndroidContext.activity.getResources();

        achievementTypes.put(IGameContext.ACHIEVEMENT_BEGINNER, res.getString(R.string.achievement_its_a_start));
        achievementTypes.put(IGameContext.ACHIEVEMENT_EXPRESS_FIRST, res.getString(R.string.achievement_first_sprint));

        achievementTypes.put(IGameContext.ACHIEVEMENT_SCORE_500, res.getString(R.string.achievement_classic_500));
        achievementTypes.put(IGameContext.ACHIEVEMENT_SCORE_3K, res.getString(R.string.achievement_classic_3000));
        achievementTypes.put(IGameContext.ACHIEVEMENT_SCORE_7K, res.getString(R.string.achievement_classic_7000));
        achievementTypes.put(IGameContext.ACHIEVEMENT_SCORE_15K, res.getString(R.string.achievement_classic_15k));
        achievementTypes.put(IGameContext.ACHIEVEMENT_SCORE_50K, res.getString(R.string.achievement_classic_50k));
        achievementTypes.put(IGameContext.ACHIEVEMENT_SCORE_100K, res.getString(R.string.achievement_classic_100k));
        achievementTypes.put(IGameContext.ACHIEVEMENT_SCORE_250K, res.getString(R.string.achievement_insane_200k));
        achievementTypes.put(IGameContext.ACHIEVEMENT_SCORE_500K, res.getString(R.string.achievement_unbelievable_500k));

        achievementTypes.put(IGameContext.ACHIEVEMENT_EXPRESS_500, res.getString(R.string.achievement_sprint_500));
        achievementTypes.put(IGameContext.ACHIEVEMENT_EXPRESS_3K, res.getString(R.string.achievement_sprint_3000));
        achievementTypes.put(IGameContext.ACHIEVEMENT_EXPRESS_7K, res.getString(R.string.achievement_sprint_7000));
        achievementTypes.put(IGameContext.ACHIEVEMENT_EXPRESS_15K, res.getString(R.string.achievement_sprint_15k));
        achievementTypes.put(IGameContext.ACHIEVEMENT_EXPRESS_35K, res.getString(R.string.achievement_sprint_35k));
        achievementTypes.put(IGameContext.ACHIEVEMENT_EXPRESS_50K, res.getString(R.string.achievement_sprint_50k));
        achievementTypes.put(IGameContext.ACHIEVEMENT_EXPRESS_100K, res.getString(R.string.achievement_furious_80k));
        achievementTypes.put(IGameContext.ACHIEVEMENT_EXPRESS_200K, res.getString(R.string.achievement_impossible_200k));


//        addRule(new ARuleBeginner());
        addRule(new ARuleScore(500, IGameContext.ACHIEVEMENT_SCORE_500));
        addRule(new ARuleScore(3000, IGameContext.ACHIEVEMENT_SCORE_3K));
        addRule(new ARuleScore(7000, IGameContext.ACHIEVEMENT_SCORE_7K));
        addRule(new ARuleScore(15000, IGameContext.ACHIEVEMENT_SCORE_15K));
        addRule(new ARuleScore(50000, IGameContext.ACHIEVEMENT_SCORE_50K));
        addRule(new ARuleScore(100000, IGameContext.ACHIEVEMENT_SCORE_100K));
        addRule(new ARuleScore(250000, IGameContext.ACHIEVEMENT_SCORE_250K));
        addRule(new ARuleScore(500000, IGameContext.ACHIEVEMENT_SCORE_500K));


        addRule(new ARuleScore(500, IGameContext.ACHIEVEMENT_EXPRESS_500, Game.TYPE_EXPRESS));
        addRule(new ARuleScore(3000, IGameContext.ACHIEVEMENT_EXPRESS_3K, Game.TYPE_EXPRESS));
        addRule(new ARuleScore(7000, IGameContext.ACHIEVEMENT_EXPRESS_7K, Game.TYPE_EXPRESS));
        addRule(new ARuleScore(15000, IGameContext.ACHIEVEMENT_EXPRESS_15K, Game.TYPE_EXPRESS));
        addRule(new ARuleScore(35000, IGameContext.ACHIEVEMENT_EXPRESS_35K, Game.TYPE_EXPRESS));
        addRule(new ARuleScore(50000, IGameContext.ACHIEVEMENT_EXPRESS_50K, Game.TYPE_EXPRESS));
        addRule(new ARuleScore(100000, IGameContext.ACHIEVEMENT_EXPRESS_100K, Game.TYPE_EXPRESS));
        addRule(new ARuleScore(200000, IGameContext.ACHIEVEMENT_EXPRESS_200K, Game.TYPE_EXPRESS));

        addRule(new ARuleLevel(1,  IGameContext.ACHIEVEMENT_EXPRESS_FIRST, Game.TYPE_EXPRESS));
        addRule(new ARuleLevel(1,  IGameContext.ACHIEVEMENT_BEGINNER, Game.TYPE_ARCADE));
    }
}
