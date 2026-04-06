package northern.captain.quadronia.game.achievements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import northern.captain.quadronia.game.Constants;
import northern.captain.quadronia.game.Engine;
import northern.captain.quadronia.game.IGameContext;
import northern.captain.gamecore.gplus.Achievement;
import northern.captain.gamecore.gplus.GoogleGamesFactory;
import northern.captain.quadronia.game.core.Game;

/**
 * Created by leo on 21.08.15.
 */
public class AchieveMan
{
    protected static AchieveMan singleton;
    public static AchieveMan instance() { return singleton;}

    private Game game;

    private int gameKey;

    private Map<Integer, List<IAchivementRule>> rules = new HashMap<Integer, List<IAchivementRule>>();
    private Set<Integer> unlockedAchievements = new HashSet<Integer>();
    protected Map<Integer, String> achievementTypes = new HashMap<Integer, String>();

    public static int getModeKey(int gameType, int eventType)
    {
        return Constants.getLeaderboardId(gameType, 0) << 16 | eventType;
    }

    public void startGame(Game game)
    {
        this.game = game;

        gameKey = getModeKey(game.getGameMode(), 0);

        IGameContext context = Game.defaultGameContext;
        for(Integer key : achievementTypes.keySet())
        {
            if(context.getDatai(key) == key)
            {
                unlockedAchievements.add(key);
            }
        }
    }

    public void addRule(IAchivementRule rule)
    {
        List<IAchivementRule> ruleList = rules.get(rule.getKey());

        if(ruleList == null)
        {
            ruleList = new ArrayList<IAchivementRule>();
            rules.put(rule.getKey(), ruleList);
        }

        ruleList.add(rule);
    }

    public void registerEvent(int eventType, int... params)
    {
        if (!GoogleGamesFactory.instance().getProcessor().isSignedIn()) return;

        processRules(eventType, eventType, params);
        processRules(gameKey | eventType, eventType, params);
    }

    private void processRules(int theKey, int eventType, int... params)
    {
        List<IAchivementRule> ruleList = rules.get(theKey);
        if(ruleList == null) return;
        for(IAchivementRule rule : ruleList)
        {
            if(unlockedAchievements.contains(rule.getAchievementType())) continue;
            rule.processEvent(eventType, params);
        }
    }

    public boolean unlockAchievement(int achievementType)
    {
        if(unlockedAchievements.contains(achievementType)) return false;

        Achievement achievement = new Achievement(achievementTypes.get(achievementType), achievementType);
        GoogleGamesFactory.instance().getProcessor().unlockAchievement(achievement);
        Game.defaultGameContext.setDatai(achievementType, achievementType);
        unlockedAchievements.add(achievementType);
        return true;
    }
}
