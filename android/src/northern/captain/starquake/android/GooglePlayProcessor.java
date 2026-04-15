package northern.captain.starquake.android;

import android.app.Activity;
import com.badlogic.gdx.Gdx;
import com.google.android.gms.games.AchievementsClient;
import com.google.android.gms.games.GamesSignInClient;
import com.google.android.gms.games.LeaderboardsClient;
import com.google.android.gms.games.PlayGames;
import northern.captain.starquake.services.AchievementDef;
import northern.captain.starquake.services.GameServicesProcessor;
import northern.captain.starquake.services.LeaderboardDef;

import java.util.HashMap;
import java.util.Map;

/**
 * Google Play Games implementation of GameServicesProcessor.
 * Maps AchievementDef/LeaderboardDef enums to Play Games string IDs.
 */
public class GooglePlayProcessor implements GameServicesProcessor {
    private static final int RC_LEADERBOARD = 7899;
    private static final int RC_ACHIEVEMENTS = 7898;

    private final Activity activity;
    private final Map<AchievementDef, String> achievementIds = new HashMap<>();
    private final Map<LeaderboardDef, String> leaderboardIds = new HashMap<>();
    private boolean signedIn;

    public GooglePlayProcessor(Activity activity) {
        this.activity = activity;
        initIdMappings();
        // Check if already authenticated (auto-sign-in from previous session)
        signIn(null);
    }

    private void initIdMappings() {
        // TODO: Replace with actual Google Play Console IDs from strings.xml
        // achievementIds.put(AchievementDef.FIRST_STEPS, activity.getString(R.string.achievement_first_steps));
        // For now, use placeholder strings that won't cause crashes
        for (AchievementDef def : AchievementDef.values()) {
            achievementIds.put(def, "achievement_" + def.name().toLowerCase());
        }
        leaderboardIds.put(LeaderboardDef.HIGH_SCORE, "leaderboard_high_score");
        leaderboardIds.put(LeaderboardDef.EXPLORER, "leaderboard_explorer");
    }

    @Override
    public void signIn(Runnable onSuccess) {
        GamesSignInClient client = PlayGames.getGamesSignInClient(activity);
        client.isAuthenticated().addOnCompleteListener(task -> {
            signedIn = task.isSuccessful() && task.getResult().isAuthenticated();
            if (signedIn && onSuccess != null) {
                Gdx.app.postRunnable(onSuccess);
            }
            if (!signedIn) {
                client.signIn().addOnCompleteListener(signInTask -> {
                    signedIn = signInTask.isSuccessful();
                    if (signedIn && onSuccess != null) {
                        Gdx.app.postRunnable(onSuccess);
                    }
                });
            }
        });
    }

    @Override
    public void signOut() {
        signedIn = false;
    }

    @Override
    public boolean isSignedIn() {
        return signedIn;
    }

    @Override
    public void unlockAchievement(AchievementDef achievement) {
        if (!signedIn) return;
        String id = achievementIds.get(achievement);
        if (id == null) return;
        try {
            AchievementsClient client = PlayGames.getAchievementsClient(activity);
            client.unlock(id);
        } catch (Exception e) {
            Gdx.app.error("GooglePlay", "Failed to unlock achievement: " + achievement, e);
        }
    }

    @Override
    public void submitScore(LeaderboardDef leaderboard, int score) {
        if (!signedIn) return;
        String id = leaderboardIds.get(leaderboard);
        if (id == null) return;
        try {
            LeaderboardsClient client = PlayGames.getLeaderboardsClient(activity);
            client.submitScore(id, score);
        } catch (Exception e) {
            Gdx.app.error("GooglePlay", "Failed to submit score: " + leaderboard, e);
        }
    }

    @Override
    public void showAchievements() {
        if (!signedIn) return;
        try {
            AchievementsClient client = PlayGames.getAchievementsClient(activity);
            client.getAchievementsIntent().addOnSuccessListener(intent -> {
                activity.startActivityForResult(intent, RC_ACHIEVEMENTS);
            });
        } catch (Exception e) {
            Gdx.app.error("GooglePlay", "Failed to show achievements", e);
        }
    }

    @Override
    public void showLeaderboard(LeaderboardDef leaderboard) {
        if (!signedIn) return;
        String id = leaderboardIds.get(leaderboard);
        if (id == null) return;
        try {
            LeaderboardsClient client = PlayGames.getLeaderboardsClient(activity);
            client.getLeaderboardIntent(id).addOnSuccessListener(intent -> {
                activity.startActivityForResult(intent, RC_LEADERBOARD);
            });
        } catch (Exception e) {
            Gdx.app.error("GooglePlay", "Failed to show leaderboard", e);
        }
    }
}
