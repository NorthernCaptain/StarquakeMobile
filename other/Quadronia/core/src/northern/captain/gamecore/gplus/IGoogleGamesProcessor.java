package northern.captain.gamecore.gplus;

import northern.captain.tools.IPersistCfg;

/**
 * Copyright 2013 by Northern Captain Software
 * User: leo
 * Date: 10.05.14
 * Time: 21:21
 * This code is a private property of its owner Northern Captain.
 * Any copying or redistribution of this source code is strictly prohibited.
 */
public interface IGoogleGamesProcessor extends IPersistCfg
{
    public static final int REQUEST_LEADERBOARD = 7899;
    public static final int REQUEST_ACHIEVEMENTS = 7898;

    public static final int LEADERBOARD_DROID_SCORE = 0;
    public static final int LEADERBOARD_HUMAN_SCORE = 1;
    public static final int LEADERBOARD_STARS = 2;

    void onStart();

    void doSignIn(Runnable callOnSuccess);

    void doSignOut();

    boolean isSignedIn();

    void onStop();

    void onActivityResult(int requestCode, int responseCode, Object intent);

    boolean openLeaderboard();
    boolean openAchievements();

    void submitScore(int id, int score);

    void addLeaderboard(int id, int resId);

    void unlockAchievement(Achievement achievement);

    void cloudSave(String name, byte[] data, Runnable postRun);
    void cloudLoad(String name, Runnable postRunOnSuccess);
}
