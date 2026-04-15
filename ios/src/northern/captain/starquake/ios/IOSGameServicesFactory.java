package northern.captain.starquake.ios;

import northern.captain.starquake.services.GameServicesFactory;
import org.robovm.apple.uikit.UIViewController;

public class IOSGameServicesFactory extends GameServicesFactory {
    public static void initialize(UIViewController viewController) {
        IOSGameServicesFactory f = new IOSGameServicesFactory();
        f.setProcessor(new GameCenterProcessor(viewController));
        instance = f;
    }
}
