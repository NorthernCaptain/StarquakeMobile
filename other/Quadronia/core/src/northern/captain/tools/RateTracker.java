package northern.captain.tools;

import northern.captain.gamecore.glx.NCore;

import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * Counts call to askRating and asks user to rate the app
 */
public class RateTracker implements IPersistCfg
{
    protected static RateTracker singleton = new RateTracker();

    public static RateTracker instance()
    {
        return singleton;
    }

    protected RateTracker()
    {

    }

    private int numAskTries = 2;
    protected static int askEveryTimes = 5;
    private long rateTime = 0;
    private boolean rated = false;
    private static final long rateTimeThreshold = 4000;

    private void setRated()
    {
        rated = true;
        rateTime = System.currentTimeMillis();
    }


    public void askRating()
    {
        numAskTries++;

        if(rated || numAskTries % askEveryTimes != 0)
            return;

        showRating();

    }

    public void showRating()
    {
//        final TwoButDialog dialog = new TwoButDialog();
//
//        dialog.setFirstButton("rateButLbl", new ClickListenerPrepared()
//        {
//            @Override
//            public void clicked(InputEvent event, float x, float y)
//            {
//                showMarket();
//                setRated();
//            }
//
//            @Override
//            public boolean prepareClicked(InputEvent evt)
//            {
//                return true;
//            }
//        });
//
//        dialog.setSecondButton("cancel", new ClickListenerPrepared()
//        {
//            @Override
//            public void clicked(InputEvent event, float x, float y)
//            {
//            }
//
//            @Override
//            public boolean prepareClicked(InputEvent evt)
//            {
//                return true;
//            }
//        });
//
//        dialog.setTextAndTitle("rateTitle", "rateLbl");
//        dialog.show();
    }

    public void ratedBack()
    {
        long now = System.currentTimeMillis();

        if( numAskTries == 0 || numAskTries % askEveryTimes != 0 ||
                now - rateTime <= rateTimeThreshold)
            return;

        rated = true;
    }

    /**
     * Method for saving persistent data into given config object
     *
     * @param cfg
     */
    @Override
    public void saveData(IPersistentConfig cfg)
    {
        cfg.setInt("natr", numAskTries);
        cfg.setBoolean("rat", rated);
    }

    /**
     * Stores persistent data to file stream
     *
     * @param fout
     */
    @Override
    public void saveData(FileOutputStream fout)
    {

    }

    /**
     * Method for loading persistent data from config object
     *
     * @param cfg
     */
    @Override
    public void loadData(IPersistentConfig cfg)
    {
        numAskTries = cfg.getInt("natr", numAskTries);
        rated = cfg.getBoolean("rat", false);
    }

    /**
     * Reads persistent data from file stream
     *
     * @param fin
     */
    @Override
    public void loadData(FileInputStream fin)
    {

    }

    public void showMarket()
    {
        MarketOpener market = MarketOpener.instance();
        market.showMarket(NCore.instance());
    }
}
