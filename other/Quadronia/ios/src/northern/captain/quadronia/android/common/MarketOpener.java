package northern.captain.quadronia.android.common;

import org.robovm.apple.foundation.NSURL;
import org.robovm.apple.uikit.UIApplication;

import northern.captain.gamecore.glx.NCore;

public class MarketOpener extends northern.captain.tools.MarketOpener
{
    public static void initialize()
    {
        singleton = new MarketOpener();
    }

    public void showMarket(NCore app)
    {
        UIApplication.getSharedApplication().openURL(new NSURL("https://apps.apple.com/us/app/quadronia/id6468920597"));
    }
}
