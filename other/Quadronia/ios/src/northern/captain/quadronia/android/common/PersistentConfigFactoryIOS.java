package northern.captain.quadronia.android.common;

import northern.captain.tools.IPersistentConfig;
import northern.captain.tools.PersistentConfigFactory;

public class PersistentConfigFactoryIOS extends PersistentConfigFactory
{
    public static void initialize()
    {
        instance = new PersistentConfigFactoryIOS();
    }

    @Override
    public IPersistentConfig newConfig()
    {
        return new PersistentConfigIOS();
    }

}

