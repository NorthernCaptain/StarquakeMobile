package northern.captain.starquake.services;

/**
 * Platform-agnostic interface for game services (achievements, leaderboards).
 * Implementations: GooglePlayProcessor (Android), GameCenterProcessor (iOS), NoOpProcessor (desktop).
 * Each platform processor maps AchievementDef/LeaderboardDef to platform IDs internally.
 */
public interface GameServicesProcessor {
    void signIn(Runnable onSuccess);
    void signOut();
    boolean isSignedIn();

    void unlockAchievement(AchievementDef achievement);
    void submitScore(LeaderboardDef leaderboard, int score);

    void showAchievements();
    void showLeaderboard(LeaderboardDef leaderboard);
}
