package northern.captain.quadronia.android.common;

import northern.captain.tools.IPersistentConfig;
import northern.captain.tools.PersistentConfigFactory;

public class PersistentConfigFactoryAnd extends PersistentConfigFactory
{
	public static void initialize()
	{
		instance = new PersistentConfigFactoryAnd();
	}
	
	@Override
	public IPersistentConfig newConfig()
	{
		return new PersistentConfig(AndroidContext.activity);
	}

}
