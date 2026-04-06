package northern.captain.quadronia.android.common;

import northern.captain.quadronia.game.IGameContext;
import northern.captain.quadronia.game.achievements.ARuleLevel;
import northern.captain.quadronia.game.achievements.ARuleScore;
import northern.captain.quadronia.game.achievements.AchieveMan;
import northern.captain.quadronia.game.core.Game;

public class AchieveManDTop extends AchieveMan
{
    public static void initialize()
    {
        singleton = new AchieveManDTop();
    }

    protected AchieveManDTop()
    {
//        addRule(new ARuleBeginner());
        addRule(new ARuleScore(500, IGameContext.ACHIEVEMENT_SCORE_500));
        addRule(new ARuleScore(3000, IGameContext.ACHIEVEMENT_SCORE_3K));
        addRule(new ARuleScore(7000, IGameContext.ACHIEVEMENT_SCORE_7K));
        addRule(new ARuleScore(15000, IGameContext.ACHIEVEMENT_SCORE_15K));
        addRule(new ARuleScore(50000, IGameContext.ACHIEVEMENT_SCORE_50K));
        addRule(new ARuleScore(100000, IGameContext.ACHIEVEMENT_SCORE_100K));
        addRule(new ARuleScore(250000, IGameContext.ACHIEVEMENT_SCORE_250K));


        addRule(new ARuleScore(500, IGameContext.ACHIEVEMENT_EXPRESS_500, Game.TYPE_EXPRESS));
        addRule(new ARuleScore(3000, IGameContext.ACHIEVEMENT_EXPRESS_3K, Game.TYPE_EXPRESS));
        addRule(new ARuleScore(7000, IGameContext.ACHIEVEMENT_EXPRESS_7K, Game.TYPE_EXPRESS));
        addRule(new ARuleScore(15000, IGameContext.ACHIEVEMENT_EXPRESS_15K, Game.TYPE_EXPRESS));
        addRule(new ARuleScore(35000, IGameContext.ACHIEVEMENT_EXPRESS_35K, Game.TYPE_EXPRESS));
        addRule(new ARuleScore(50000, IGameContext.ACHIEVEMENT_EXPRESS_50K, Game.TYPE_EXPRESS));
        addRule(new ARuleScore(100000, IGameContext.ACHIEVEMENT_EXPRESS_100K, Game.TYPE_EXPRESS));

        addRule(new ARuleLevel(1,  IGameContext.ACHIEVEMENT_EXPRESS_FIRST, Game.TYPE_EXPRESS));
        addRule(new ARuleLevel(1,  IGameContext.ACHIEVEMENT_BEGINNER, Game.TYPE_ARCADE));
    }
}

