package northern.captain.quadronia.android.common;

import android.os.Build;
import android.util.DisplayMetrics;
import northern.captain.gamecore.glx.NContext;

public class NContextAnd extends NContext
{

	public NContextAnd()
	{
        DisplayMetrics metrics = new DisplayMetrics();
        AndroidContext.activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);

        preInitialize(metrics.widthPixels, metrics.heightPixels);
    }

	/* (non-Javadoc)
	 * @see northern.captain.seabattle.glx.NContext#getDeviceModel()
	 */
	@Override
	public String getDeviceModel()
	{
		return Build.MODEL;
	}

	/* (non-Javadoc)
	 * @see northern.captain.seabattle.glx.NContext#getPackageCodePath()
	 */
	@Override
	public String getPackageCodePath()
	{
		 return AndroidContext.app.getPackageCodePath();
	}

	
}
