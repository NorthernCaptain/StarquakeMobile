package northern.captain.quadronia.android.common;

import com.badlogic.gdx.Gdx;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

import de.golfgl.gdxgamesvcs.GameServiceException;
import de.golfgl.gdxgamesvcs.IGameServiceClient;
import de.golfgl.gdxgamesvcs.IGameServiceListener;
import northern.captain.gamecore.glx.NContext;
import northern.captain.gamecore.gplus.Achievement;
import northern.captain.gamecore.gplus.GoogleGamesFactory;
import northern.captain.gamecore.gplus.IGoogleGamesProcessor;
import northern.captain.quadronia.game.Constants;
import northern.captain.quadronia.game.profile.UserManager;
import northern.captain.tools.IPersistentConfig;
import northern.captain.tools.Log;

public class GameAchievementsFactoryIOS extends GoogleGamesFactory
{
    public static void initialize()
    {
        singleton = new GameAchievementsFactoryIOS();
    }


    public GameAchievementsFactoryIOS()
    {
        this.processor = new GamesProcessorIOS();
    }
}

class GamesProcessorIOS implements IGoogleGamesProcessor {
    protected Map<Integer, String> leaderboardNames = new HashMap<>();
    private IGameServiceClient gsClient;

    {
        leaderboardNames.put(Constants.LEADERBOARD_ARCADE_EASY, "grp.quadronia.classic");
        leaderboardNames.put(Constants.LEADERBOARD_ARCADE_MEDIUM, "grp.quadronia.classic");
        leaderboardNames.put(Constants.LEADERBOARD_EXPRESS_EASY, "grp.quadronia.sprint");
        leaderboardNames.put(Constants.LEADERBOARD_EXPRESS_MEDIUM, "grp.quadronia.sprint");
    }

    public void setGSClient(IGameServiceClient gsClient) {
        this.gsClient = gsClient;
        gsClient.setListener(new IGameServiceListener() {
            @Override
            public void gsOnSessionActive() {
                String name = gsClient.getPlayerDisplayName();
                if(name != null && !name.isEmpty()) {
                    NContext.current.post(() -> {
                        UserManager.instance.setUserInfo(name, "GC" + gsClient.getGameServiceId());
                    });
                }
            }

            @Override
            public void gsOnSessionInactive() {

            }

            @Override
            public void gsShowErrorToUser(GsErrorType et, String msg, Throwable t) {
                Log.e("ncgame", "GameCenter: " + msg);
            }
        });
    }

    @Override
    public void onStart() {
        gsClient.resumeSession();
    }

    @Override
    public void doSignIn(Runnable callOnSuccess) {
        gsClient.logIn();
    }

    @Override
    public void doSignOut() {

    }

    @Override
    public boolean isSignedIn() {
        return gsClient.isSessionActive();
    }

    @Override
    public void onStop() {

    }

    @Override
    public void onActivityResult(int requestCode, int responseCode, Object intent) {

    }

    @Override
    public boolean openLeaderboard() {
        try {
            //TODO: use the current game mode to choose the correct leaderboard
            gsClient.showLeaderboards(null);
            return true;
        } catch (GameServiceException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean openAchievements() {
        try {
            gsClient.showAchievements();
            return true;
        } catch (GameServiceException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void submitScore(int id, int score) {
        gsClient.submitToLeaderboard(leaderboardNames.get(id), score, "");
    }

    @Override
    public void addLeaderboard(int id, int resId) {

    }

    @Override
    public void unlockAchievement(Achievement achievement) {
        gsClient.unlockAchievement(achievement.id);
    }

    @Override
    public void cloudSave(String name, byte[] data, Runnable postRun) {

    }

    @Override
    public void cloudLoad(String name, Runnable postRunOnSuccess) {

    }

    @Override
    public void saveData(IPersistentConfig cfg) {

    }

    @Override
    public void saveData(FileOutputStream fout) {

    }

    @Override
    public void loadData(IPersistentConfig cfg) {
        gsClient.resumeSession();
    }

    @Override
    public void loadData(FileInputStream fin) {
    }
}