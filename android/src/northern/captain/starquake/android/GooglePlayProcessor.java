package northern.captain.starquake.android;

import android.app.Activity;
import com.badlogic.gdx.Gdx;
import northern.captain.starquake.android.R;
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
        GamesSignInClient client = PlayGames.getGamesSignInClient(activity);
        client.isAuthenticated().addOnCompleteListener(task -> {
            signedIn = task.isSuccessful() && task.getResult().isAuthenticated();
            if (!signedIn) {
                client.signIn().addOnCompleteListener(signInTask -> {
                    if (signInTask.isSuccessful()) {
                        client.isAuthenticated().addOnCompleteListener(recheck -> {
                            signedIn = recheck.isSuccessful() && recheck.getResult().isAuthenticated();
                        });
                    }
                });
            }
        });
    }

    private void initIdMappings() {
        achievementIds.put(AchievementDef.FIRST_STEPS, activity.getString(R.string.achievement_first_steps));
        achievementIds.put(AchievementDef.LIFT_OFF, activity.getString(R.string.achievement_lift_off));
        achievementIds.put(AchievementDef.TUNNEL_VISION, activity.getString(R.string.achievement_tunnel_vision));
        achievementIds.put(AchievementDef.BEAM_ME_UP, activity.getString(R.string.achievement_beam_me_up));
        achievementIds.put(AchievementDef.TRADER, activity.getString(R.string.achievement_trader));
        achievementIds.put(AchievementDef.KEY_MASTER, activity.getString(R.string.achievement_key_master));
        achievementIds.put(AchievementDef.CORE_DISCOVERY, activity.getString(R.string.achievement_core_discovery));
        achievementIds.put(AchievementDef.FIRST_DELIVERY, activity.getString(R.string.achievement_first_delivery));
        achievementIds.put(AchievementDef.SHARPSHOOTER, activity.getString(R.string.achievement_sharpshooter));
        achievementIds.put(AchievementDef.EXPLORER, activity.getString(R.string.achievement_explorer));
        achievementIds.put(AchievementDef.FREQUENT_FLYER, activity.getString(R.string.achievement_frequent_flyer));
        achievementIds.put(AchievementDef.HALF_WAY_THERE, activity.getString(R.string.achievement_half_way_there));
        achievementIds.put(AchievementDef.CARTOGRAPHER, activity.getString(R.string.achievement_cartographer));
        achievementIds.put(AchievementDef.PLANET_SAVIOR, activity.getString(R.string.achievement_planet_savior));
        achievementIds.put(AchievementDef.FULL_MAP, activity.getString(R.string.achievement_full_map));
        achievementIds.put(AchievementDef.SPEED_DEMON, activity.getString(R.string.achievement_speed_demon));
        achievementIds.put(AchievementDef.NO_DEATH_RUN, activity.getString(R.string.achievement_no_death_run));
        leaderboardIds.put(LeaderboardDef.HIGH_SCORE, activity.getString(R.string.leaderboard_high_scores));
        leaderboardIds.put(LeaderboardDef.EXPLORER, activity.getString(R.string.leaderboard_exploration_scores));
    }

    @Override
    public void signIn(Runnable onSuccess) {
        GamesSignInClient client = PlayGames.getGamesSignInClient(activity);
        client.signIn().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                client.isAuthenticated().addOnCompleteListener(recheck -> {
                    signedIn = recheck.isSuccessful() && recheck.getResult().isAuthenticated();
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
        String id = achievementIds.get(achievement);
        if (id == null) return;
        try {
            PlayGames.getAchievementsClient(activity).unlock(id);
        } catch (Exception ignored) {}
    }

    @Override
    public void submitScore(LeaderboardDef leaderboard, int score) {
        String id = leaderboardIds.get(leaderboard);
        if (id == null) return;
        try {
            PlayGames.getLeaderboardsClient(activity).submitScore(id, score);
        } catch (Exception ignored) {}
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
