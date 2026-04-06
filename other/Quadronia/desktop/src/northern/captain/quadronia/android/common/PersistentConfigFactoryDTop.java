package northern.captain.quadronia.android.common;

import northern.captain.tools.IPersistentConfig;
import northern.captain.tools.PersistentConfigFactory;

public class PersistentConfigFactoryDTop extends PersistentConfigFactory
{
    public static void initialize()
    {
        instance = new PersistentConfigFactoryDTop();
    }

    @Override
    public IPersistentConfig newConfig()
    {
        return new PersistentConfigDTop();
    }

}

