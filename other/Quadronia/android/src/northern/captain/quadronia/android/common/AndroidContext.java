package northern.captain.quadronia.android.common;

import android.app.Activity;
import android.app.Application;
import android.os.Handler;

import northern.captain.quadronia.screens.ScreenMainCarousel;
import northern.captain.quadronia.screens.ScreenMainMenu;
import northern.captain.gamecore.glx.NCore;
import northern.captain.gamecore.glx.screens.IScreenActivator;

public class AndroidContext
{
	public static final String CLOUD_SAVE_NAME = "nc.quadronia.cloud.save";
	public static Application app;
	public static Activity activity;
	public static Handler mainHandler;

	public static void onCloudLoaded()
	{
		IScreenActivator screen = NCore.instance().getScreenFlow().getCurrentScreen();
		if(screen instanceof ScreenMainCarousel)
		{
			ScreenMainCarousel mainMenu = (ScreenMainCarousel)screen;
			mainMenu.initValues();
		}
	}
}
