package northern.captain.quadronia.android;

import northern.captain.gamecore.glx.NContext;

public class NContextDTop extends NContext {

    public NContextDTop()
    {
        preInitialize(1080, 1920);
    }

    /* (non-Javadoc)
     * @see northern.captain.seabattle.glx.NContext#getDeviceModel()
     */
    @Override
    public String getDeviceModel()
    {
        return "Unk-DTOP";
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
