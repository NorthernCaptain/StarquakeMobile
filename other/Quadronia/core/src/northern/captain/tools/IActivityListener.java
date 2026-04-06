package northern.captain.tools;


/**
 * Interface allows registration for listening onActivityResult events
 * @author Leo
 *
 * @return true if event was swallowed and we do not want farther processing
 */
public interface IActivityListener
{
	boolean onActivityResult(int request, int result, Object intent);
}
