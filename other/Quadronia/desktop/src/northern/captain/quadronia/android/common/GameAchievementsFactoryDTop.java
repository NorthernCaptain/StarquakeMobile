package northern.captain.quadronia.android.common;

import java.io.FileInputStream;
import java.io.FileOutputStream;

import northern.captain.gamecore.gplus.Achievement;
import northern.captain.gamecore.gplus.GoogleGamesFactory;
import northern.captain.gamecore.gplus.IGoogleGamesProcessor;
import northern.captain.tools.IPersistentConfig;

public class GameAchievementsFactoryDTop extends GoogleGamesFactory
{
    public static void initialize()
    {
        singleton = new GameAchievementsFactoryDTop();
    }


    public GameAchievementsFactoryDTop()
    {
        this.processor = new GamesProcessorIOS();
    }
}

class GamesProcessorIOS implements IGoogleGamesProcessor {

    @Override
    public void onStart() {

    }

    @Override
    public void doSignIn(Runnable callOnSuccess) {

    }

    @Override
    public void doSignOut() {

    }

    @Override
    public boolean isSignedIn() {
        return false;
    }

    @Override
    public void onStop() {

    }

    @Override
    public void onActivityResult(int requestCode, int responseCode, Object intent) {

    }

    @Override
    public boolean openLeaderboard() {
        return false;
    }

    @Override
    public boolean openAchievements() {
        return false;
    }

    @Override
    public void submitScore(int id, int score) {

    }

    @Override
    public void addLeaderboard(int id, int resId) {

    }

    @Override
    public void unlockAchievement(Achievement achievement) {

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

    }

    @Override
    public void loadData(FileInputStream fin) {

    }
}