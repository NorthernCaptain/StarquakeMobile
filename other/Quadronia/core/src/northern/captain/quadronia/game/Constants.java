package northern.captain.quadronia.game;

/**
 * Created by leo on 18.08.15.
 */
public class Constants
{
    public static final int MAX_COLORS = 12;
    public static final int MAX_COLORS2 = MAX_COLORS*2;
    public static final int MAX_COLORS3 = MAX_COLORS*3;
    public static final int MAX_COLORS4 = MAX_COLORS*4;

    public static final int LEADERBOARD_ARCADE_EASY;
    public static final int LEADERBOARD_ARCADE_MEDIUM;
    public static final int LEADERBOARD_ARCADE_HARD;

    public static final int LEADERBOARD_EXPRESS_EASY;
    public static final int LEADERBOARD_EXPRESS_MEDIUM;
    public static final int LEADERBOARD_EXPRESS_HARD;

    public static final int LEADERBOARD_QUEST_EASY;
    public static final int LEADERBOARD_QUEST_MEDIUM;
    public static final int LEADERBOARD_QUEST_HARD;

    public static final int EVENT_CIRCUIT_DONE = 1001;
    public static final int EVENT_SCORE_CHANGED = 1002;
    public static final int EVENT_LEVEL_UP = 1003;
    public static final int EVENT_FRAGMENTS_USED = 1004;
    public static final int EVENT_GOLD_CHANGED = 1005;

    public static int getLeaderboardId(int type, int mode)
    {
        return type << 8 | mode;
    }

    static
    {
        LEADERBOARD_ARCADE_EASY = getLeaderboardId(Engine.TYPE_ARCADE, ItemFactory.MODE_TWO);
        LEADERBOARD_ARCADE_MEDIUM = getLeaderboardId(Engine.TYPE_ARCADE, ItemFactory.MODE_TWOTHREE);
        LEADERBOARD_ARCADE_HARD = getLeaderboardId(Engine.TYPE_ARCADE, ItemFactory.MODE_THREE);

        LEADERBOARD_EXPRESS_EASY = getLeaderboardId(Engine.TYPE_EXPRESS, ItemFactory.MODE_TWO);
        LEADERBOARD_EXPRESS_MEDIUM = getLeaderboardId(Engine.TYPE_EXPRESS, ItemFactory.MODE_TWOTHREE);
        LEADERBOARD_EXPRESS_HARD = getLeaderboardId(Engine.TYPE_EXPRESS, ItemFactory.MODE_THREE);

        LEADERBOARD_QUEST_EASY = getLeaderboardId(Engine.TYPE_QUEST, ItemFactory.MODE_TWO);
        LEADERBOARD_QUEST_MEDIUM = getLeaderboardId(Engine.TYPE_QUEST, ItemFactory.MODE_TWOTHREE);
        LEADERBOARD_QUEST_HARD = getLeaderboardId(Engine.TYPE_QUEST, ItemFactory.MODE_THREE);
    }
}
