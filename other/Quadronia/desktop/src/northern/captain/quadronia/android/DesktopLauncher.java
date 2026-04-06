package northern.captain.quadronia.android;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.glutils.HdpiMode;

import java.util.Locale;

import northern.captain.gamecore.glx.INCore;
import northern.captain.gamecore.glx.NContext;
import northern.captain.gamecore.glx.NCore;
import northern.captain.gamecore.glx.SoundManager2;
import northern.captain.gamecore.glx.tools.TimerManager;
import northern.captain.gamecore.glx.tools.loaders.ResLoader;
import northern.captain.quadronia.TheGame;
import northern.captain.quadronia.android.common.AchieveManDTop;
import northern.captain.quadronia.android.common.GameAchievementsFactoryDTop;
import northern.captain.quadronia.android.common.MarketOpener;
import northern.captain.quadronia.android.common.NCoreQuadronia;
import northern.captain.quadronia.android.common.NativeNFactoryDTop;
import northern.captain.quadronia.android.common.PersistentConfigFactoryDTop;
import northern.captain.quadronia.android.common.ShareManagerDTop;
import northern.captain.tools.analytics.AnalyticsFactory;
import northern.captain.tools.analytics.AnalyticsToLog;
import northern.captain.tools.analytics.IAnalytics;

// Please note that on macOS your application needs to be started with the -XstartOnFirstThread JVM argument
public class DesktopLauncher {
	public static void main (String[] arg) {
		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		config.setForegroundFPS(60);
		config.setTitle("Quadronia");
		config.setWindowedMode(640, 1200);
		config.setResizable(false);
		config.setHdpiMode(HdpiMode.Pixels);

		NCoreQuadronia.init();
		NativeNFactoryDTop.initialize();
		PersistentConfigFactoryDTop.initialize();
		INCore core = NCore.instance();
		core.initialize();

		TimerManager.initialize();
		ResLoader.singleton().initialize();

		NContext.current = new NContextDTop();
		NContext.current.setLang(Locale.getDefault().toString());
		NContext.current.setLangCountry(Locale.getDefault().toString());

		final SoundManager2 soundManager2 = new SoundManager2();
		NCore.instance().setSoundman(soundManager2);
		soundManager2.start();

		MarketOpener.initialize();

		AnalyticsFactory.setSingleton(new AnalyticsFactory()
		{
			private IAnalytics analytic = new AnalyticsToLog();

			@Override
			public IAnalytics getAnalytics()
			{
				return analytic;
			}
		});


		GameAchievementsFactoryDTop.initialize();
		ShareManagerDTop.initialize();
		AchieveManDTop.initialize();


		new Lwjgl3Application(new TheGame(), config);
	}
}
