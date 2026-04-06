package northern.captain.quadronia.android.common;

import northern.captain.gamecore.glx.NCore;

public class MarketOpener extends northern.captain.tools.MarketOpener
{
    public static void initialize()
    {
        singleton = new MarketOpener();
    }

    public void showMarket(NCore app)
    {
    }
}
