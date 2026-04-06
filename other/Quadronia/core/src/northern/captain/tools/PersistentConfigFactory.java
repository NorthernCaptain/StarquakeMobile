package northern.captain.tools;

public abstract class PersistentConfigFactory
{
	public static PersistentConfigFactory instance = null;
	
	abstract public IPersistentConfig newConfig();
}
