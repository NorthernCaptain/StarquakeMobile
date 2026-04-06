package northern.captain.quadronia.android;

import org.robovm.apple.foundation.NSAutoreleasePool;
import org.robovm.apple.uikit.UIApplication;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.iosrobovm.IOSApplication;
import com.badlogic.gdx.backends.iosrobovm.IOSApplicationConfiguration;
import com.badlogic.gdx.graphics.glutils.HdpiMode;

import java.util.Locale;

import de.golfgl.gdxgamesvcs.GameCenterClient;
import de.golfgl.gdxgamesvcs.IGameServiceClient;
import northern.captain.gamecore.glx.INCore;
import northern.captain.gamecore.glx.NContext;
import northern.captain.gamecore.glx.NCore;
import northern.captain.gamecore.glx.SoundManager2;
import northern.captain.gamecore.glx.tools.TimerManager;
import northern.captain.gamecore.glx.tools.loaders.ResLoader;
import northern.captain.quadronia.TheGame;
import northern.captain.quadronia.android.common.AchieveManIOS;
import northern.captain.quadronia.android.common.GameAchievementsFactoryIOS;
import northern.captain.quadronia.android.common.IOSGame;
import northern.captain.quadronia.android.common.MarketOpener;
import northern.captain.quadronia.android.common.NCoreQuadronia;
import northern.captain.quadronia.android.common.NativeNFactoryIOS;
import northern.captain.quadronia.android.common.PersistentConfigFactoryIOS;
import northern.captain.quadronia.android.common.ShareManagerIOS;
import northern.captain.quadronia.game.profile.UserBase;
import northern.captain.quadronia.game.profile.UserManager;
import northern.captain.tools.analytics.AnalyticsFactory;
import northern.captain.tools.analytics.AnalyticsToLog;
import northern.captain.tools.analytics.IAnalytics;

public class IOSLauncher extends IOSApplication.Delegate {
    @Override
    protected IOSApplication createApplication() {
        UserManager.instance = new UserManager() {
            @Override
            public void setUserInfo(String name, String googleId) {
                //always set the name from the game center
                if(name != null && !name.isEmpty() && !UserBase.UNKNOWN_NAME.equals(name)) {
                    currentUser.setName(name);
                }
                super.setUserInfo(name, googleId);
            }
        };


        NCoreQuadronia.init();
        NativeNFactoryIOS.initialize();
        PersistentConfigFactoryIOS.initialize();
        INCore core = NCore.instance();
        core.initialize();

        TimerManager.initialize();
        ResLoader.singleton().initialize();

        NContext.current = new NContextIOS();
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


        GameAchievementsFactoryIOS.initialize();
        ShareManagerIOS.initialize();
        AchieveManIOS.initialize();

        IOSApplicationConfiguration config = new IOSApplicationConfiguration();
        config.preventScreenDimming = true;
        config.hdpiMode = HdpiMode.Pixels;
        config.statusBarVisible = true;
        config.orientationLandscape = false;
        return new IOSApplication(new IOSGame(), config);
    }

    public static void main(String[] argv) {
        NSAutoreleasePool pool = new NSAutoreleasePool();
        UIApplication.main(argv, null, IOSLauncher.class);
        pool.close();
    }
}
