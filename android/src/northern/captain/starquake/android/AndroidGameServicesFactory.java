package northern.captain.starquake.android;

import android.app.Activity;
import northern.captain.starquake.services.GameServicesFactory;

public class AndroidGameServicesFactory extends GameServicesFactory {
    public static void initialize(Activity activity) {
        AndroidGameServicesFactory f = new AndroidGameServicesFactory();
        f.setProcessor(new GooglePlayProcessor(activity));
        instance = f;
    }
}
