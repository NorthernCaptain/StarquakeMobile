package northern.captain.gamecore.gplus;

import northern.captain.tools.IPersistentConfig;

import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * Copyright 2013 by Northern Captain Software
 * User: leo
 * Date: 11.05.14
 * Time: 18:41
 * This code is a private property of its owner Northern Captain.
 * Any copying or redistribution of this source code is strictly prohibited.
 */
public class FakeGoogleGamesProcessor implements IGoogleGamesProcessor
{
    @Override
    public void onStart()
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void doSignIn(Runnable callOnSuccess)
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void doSignOut()
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isSignedIn()
    {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void onStop()
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void onActivityResult(int requestCode, int responseCode, Object intent)
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean openLeaderboard()
    {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean openAchievements()
    {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void submitScore(int id, int score)
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void addLeaderboard(int id, int resId)
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Method for saving persistent data into given config object
     *
     * @param cfg
     */
    @Override
    public void saveData(IPersistentConfig cfg)
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Stores persistent data to file stream
     *
     * @param fout
     */
    @Override
    public void saveData(FileOutputStream fout)
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Method for loading persistent data from config object
     *
     * @param cfg
     */
    @Override
    public void loadData(IPersistentConfig cfg)
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Reads persistent data from file stream
     *
     * @param fin
     */
    @Override
    public void loadData(FileInputStream fin)
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void unlockAchievement(Achievement achievementId)
    {

    }

    @Override
    public void cloudSave(String name, byte[] data, Runnable runnable)
    {

    }

    @Override
    public void cloudLoad(String name, Runnable postRun)
    {

    }
}
