package northern.captain.tools;

/**
 * Listener interface for catching options changes
 * @author Leo
 *
 */
public interface IOptionsChangeListener
{
	/**
	 * Called when options has been changed in the config
	 * @param cfg
	 */
	void optionsChanged(IPersistentConfig cfg, String key);
}
