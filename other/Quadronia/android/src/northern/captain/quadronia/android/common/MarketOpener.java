package northern.captain.quadronia.android.common;

import android.content.Intent;
import android.net.Uri;
import northern.captain.gamecore.glx.NCore;

public class MarketOpener extends northern.captain.tools.MarketOpener
{
	public static void initialize()
	{
		singleton = new MarketOpener();
	}
	
	public void showMarket(NCore app)
	{
        AndroidContext.mainHandler.post(new Runnable()
        {
            private String theLink = marketLink;
            @Override
            public void run()
            {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(theLink));
                AndroidContext.activity.startActivity(intent);
            }
        });
	}
}
