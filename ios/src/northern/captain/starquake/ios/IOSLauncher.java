package northern.captain.starquake.ios;

import com.badlogic.gdx.backends.iosrobovm.IOSApplication;
import com.badlogic.gdx.backends.iosrobovm.IOSApplicationConfiguration;
import northern.captain.starquake.StarquakeGame;
import org.robovm.apple.foundation.NSAutoreleasePool;
import org.robovm.apple.uikit.UIApplication;

public class IOSLauncher extends IOSApplication.Delegate {
    @Override
    protected IOSApplication createApplication() {
        IOSApplicationConfiguration config = new IOSApplicationConfiguration();
        config.orientationLandscape = true;
        config.orientationPortrait = false;
        return new IOSApplication(new StarquakeGame(), config);
    }

    @Override
    public boolean didFinishLaunching(UIApplication application, org.robovm.apple.uikit.UIApplicationLaunchOptions launchOptions) {
        boolean result = super.didFinishLaunching(application, launchOptions);
        org.robovm.apple.uikit.UIViewController rootVC =
                application.getKeyWindow().getRootViewController();
        IOSGameServicesFactory.initialize(rootVC);
        return result;
    }

    public static void main(String[] argv) {
        NSAutoreleasePool pool = new NSAutoreleasePool();
        UIApplication.main(argv, null, IOSLauncher.class);
        pool.close();
    }
}
