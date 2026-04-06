package northern.captain.quadronia.android.common;

import northern.captain.gamecore.glx.NContext;
import northern.captain.tools.sharing.ShareManager;

public class ShareManagerDTop extends ShareManager
{

    @Override
    public void doShareMyProgress(int mode)
    {
        NContext.current.postDelayed(() -> doShareInternal(mode), 800);
    }

    protected void doShareInternal(int mode)
    {
    }

    public static void initialize()
    {
        setSingleton(new ShareManagerDTop());
    }

}

