package northern.captain.starquake.services;

/**
 * Singleton factory for platform game services.
 * Default: NoOpProcessor (desktop). Android/iOS subclasses set their own processor.
 */
public class GameServicesFactory {
    protected static GameServicesFactory instance;
    private GameServicesProcessor processor = new NoOpProcessor();

    public static void initialize() {
        if (instance == null) instance = new GameServicesFactory();
    }

    public static GameServicesFactory get() {
        if (instance == null) initialize();
        return instance;
    }

    public GameServicesProcessor getProcessor() {
        return processor;
    }

    protected void setProcessor(GameServicesProcessor p) {
        this.processor = p;
    }

    /** No-op implementation for desktop or when game services unavailable. */
    private static class NoOpProcessor implements GameServicesProcessor {
        @Override public void signIn(Runnable onSuccess) {}
        @Override public void signOut() {}
        @Override public boolean isSignedIn() { return false; }
        @Override public void unlockAchievement(AchievementDef achievement) {}
        @Override public void submitScore(LeaderboardDef leaderboard, int score) {}
        @Override public void showAchievements() {}
        @Override public void showLeaderboard(LeaderboardDef leaderboard) {}
        @Override public void showAllLeaderboards() {}
    }
}
