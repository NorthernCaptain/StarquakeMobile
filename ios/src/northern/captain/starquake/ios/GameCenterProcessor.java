package northern.captain.starquake.ios;

import com.badlogic.gdx.Gdx;
import northern.captain.starquake.services.AchievementDef;
import northern.captain.starquake.services.GameServicesProcessor;
import northern.captain.starquake.services.LeaderboardDef;
import org.robovm.apple.foundation.NSArray;
import org.robovm.apple.foundation.NSError;
import org.robovm.apple.foundation.NSString;
import org.robovm.apple.gamekit.GKAchievement;
import org.robovm.apple.gamekit.GKGameCenterViewController;
import org.robovm.apple.gamekit.GKGameCenterViewControllerState;
import org.robovm.apple.gamekit.GKLeaderboard;
import org.robovm.apple.gamekit.GKLocalPlayer;
import org.robovm.apple.uikit.UIViewController;

import java.util.HashMap;
import java.util.Map;

/**
 * Apple Game Center implementation of GameServicesProcessor.
 * Maps AchievementDef/LeaderboardDef enums to Game Center string IDs.
 */
public class GameCenterProcessor implements GameServicesProcessor {
    private final UIViewController viewController;
    private final Map<AchievementDef, String> achievementIds = new HashMap<>();
    private final Map<LeaderboardDef, String> leaderboardIds = new HashMap<>();
    private boolean signedIn;

    public GameCenterProcessor(UIViewController viewController) {
        this.viewController = viewController;
        initIdMappings();
        authenticateLocalPlayer();
    }

    private void initIdMappings() {
        // TODO: Replace with actual App Store Connect Game Center IDs
        for (AchievementDef def : AchievementDef.values()) {
            achievementIds.put(def, "starquake." + def.name().toLowerCase());
        }
        leaderboardIds.put(LeaderboardDef.HIGH_SCORE, "starquake.highscore");
        leaderboardIds.put(LeaderboardDef.EXPLORER, "starquake.explorer");
    }

    private void authenticateLocalPlayer() {
        GKLocalPlayer localPlayer = GKLocalPlayer.getLocal();
        localPlayer.setAuthenticateHandler((gc, error) -> {
            if (gc != null) {
                viewController.presentViewController(gc, true, null);
            } else if (error != null) {
                Gdx.app.error("GameCenter", "Auth error: " + error.getLocalizedDescription());
                signedIn = false;
            } else {
                signedIn = localPlayer.isAuthenticated();
            }
        });
    }

    @Override
    public void signIn(Runnable onSuccess) {
        if (GKLocalPlayer.getLocal().isAuthenticated()) {
            signedIn = true;
            if (onSuccess != null) Gdx.app.postRunnable(onSuccess);
        } else {
            authenticateLocalPlayer();
        }
    }

    @Override
    public void signOut() {
        signedIn = false;
    }

    @Override
    public boolean isSignedIn() {
        return signedIn && GKLocalPlayer.getLocal().isAuthenticated();
    }

    @Override
    public void unlockAchievement(AchievementDef achievement) {
        if (!isSignedIn()) return;
        String id = achievementIds.get(achievement);
        if (id == null) return;
        try {
            GKAchievement gkAchievement = new GKAchievement(id);
            gkAchievement.setPercentComplete(100.0);
            gkAchievement.setShowsCompletionBanner(true);
            GKAchievement.reportAchievements(new NSArray<>(gkAchievement),
                    (NSError error) -> {
                        if (error != null) {
                            Gdx.app.error("GameCenter", "Achievement error: " + error.getLocalizedDescription());
                        }
                    });
        } catch (Exception e) {
            Gdx.app.error("GameCenter", "Failed to unlock achievement: " + achievement, e);
        }
    }

    @Override
    public void submitScore(LeaderboardDef leaderboard, int score) {
        if (!isSignedIn()) return;
        String id = leaderboardIds.get(leaderboard);
        if (id == null) return;
        try {
            GKLeaderboard.submitScore((long) score, 0, GKLocalPlayer.getLocal(),
                    new NSArray<>(new NSString(id)),
                    (NSError error) -> {
                        if (error != null) {
                            Gdx.app.error("GameCenter", "Leaderboard error: " + error.getLocalizedDescription());
                        }
                    });
        } catch (Exception e) {
            Gdx.app.error("GameCenter", "Failed to submit score: " + leaderboard, e);
        }
    }

    @Override
    public void showAchievements() {
        if (!isSignedIn()) return;
        try {
            GKGameCenterViewController gc = new GKGameCenterViewController();
            gc.setViewState(GKGameCenterViewControllerState.Achievements);
            gc.setGameCenterDelegate(gameCenterViewController -> {
                gameCenterViewController.dismissViewController(true, null);
            });
            viewController.presentViewController(gc, true, null);
        } catch (Exception e) {
            Gdx.app.error("GameCenter", "Failed to show achievements", e);
        }
    }

    @Override
    public void showLeaderboard(LeaderboardDef leaderboard) {
        if (!isSignedIn()) return;
        String id = leaderboardIds.get(leaderboard);
        if (id == null) return;
        try {
            GKGameCenterViewController gc = new GKGameCenterViewController();
            gc.setViewState(GKGameCenterViewControllerState.Leaderboards);
            gc.setLeaderboardIdentifier(id);
            gc.setGameCenterDelegate(gameCenterViewController -> {
                gameCenterViewController.dismissViewController(true, null);
            });
            viewController.presentViewController(gc, true, null);
        } catch (Exception e) {
            Gdx.app.error("GameCenter", "Failed to show leaderboard", e);
        }
    }
}
