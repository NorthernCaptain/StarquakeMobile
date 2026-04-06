package northern.captain.quadronia.android;
import com.badlogic.gdx.Gdx;

import northern.captain.gamecore.glx.NContext;

public class NContextIOS extends NContext {

    public NContextIOS()
    {
        preInitialize(1080, 1920);
    }

    /* (non-Javadoc)
     * @see northern.captain.seabattle.glx.NContext#getDeviceModel()
     */
    @Override
    public String getDeviceModel()
    {
        return "Unk-IOS";
    }

    /* (non-Javadoc)
     * @see northern.captain.seabattle.glx.NContext#getPackageCodePath()
     */
    @Override
    public String getPackageCodePath()
    {
        return ".";
    }

}
