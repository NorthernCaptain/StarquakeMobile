package northern.captain.quadronia.android.common;

import northern.captain.quadronia.game.IGameContext;
import northern.captain.quadronia.game.achievements.ARuleLevel;
import northern.captain.quadronia.game.achievements.ARuleScore;
import northern.captain.quadronia.game.achievements.AchieveMan;
import northern.captain.quadronia.game.core.Game;

public class AchieveManIOS extends AchieveMan
{
    public static void initialize()
    {
        singleton = new AchieveManIOS();
    }

    protected AchieveManIOS()
    {
        achievementTypes.put(IGameContext.ACHIEVEMENT_BEGINNER, "grp.classic.begginer");
        achievementTypes.put(IGameContext.ACHIEVEMENT_EXPRESS_FIRST, "grp.sprint.first");

        achievementTypes.put(IGameContext.ACHIEVEMENT_SCORE_500, "grp.classic.s500");
        achievementTypes.put(IGameContext.ACHIEVEMENT_SCORE_3K, "grp.classic.s3000");
        achievementTypes.put(IGameContext.ACHIEVEMENT_SCORE_7K, "grp.classic.s7000");
        achievementTypes.put(IGameContext.ACHIEVEMENT_SCORE_15K, "grp.classic.s15k");
        achievementTypes.put(IGameContext.ACHIEVEMENT_SCORE_50K, "grp.classic.s50k");
        achievementTypes.put(IGameContext.ACHIEVEMENT_SCORE_100K, "grp.classic.s100k");
        achievementTypes.put(IGameContext.ACHIEVEMENT_SCORE_250K, "grp.classic.s250k");
        achievementTypes.put(IGameContext.ACHIEVEMENT_SCORE_500K, "grp.classic.s500k");

        achievementTypes.put(IGameContext.ACHIEVEMENT_EXPRESS_500, "grp.sprint.s500");
        achievementTypes.put(IGameContext.ACHIEVEMENT_EXPRESS_3K, "grp.sprint.s3000");
        achievementTypes.put(IGameContext.ACHIEVEMENT_EXPRESS_7K, "grp.sprint.s7000");
        achievementTypes.put(IGameContext.ACHIEVEMENT_EXPRESS_15K, "grp.sprint.s15k");
        achievementTypes.put(IGameContext.ACHIEVEMENT_EXPRESS_35K, "grp.sprint.s35k");
        achievementTypes.put(IGameContext.ACHIEVEMENT_EXPRESS_50K, "grp.sprint.s50k");
        achievementTypes.put(IGameContext.ACHIEVEMENT_EXPRESS_100K, "grp.sprint.s100k");
        achievementTypes.put(IGameContext.ACHIEVEMENT_EXPRESS_200K, "grp.sprint.s200k");

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

