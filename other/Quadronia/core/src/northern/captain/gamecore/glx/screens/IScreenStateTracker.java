package northern.captain.gamecore.glx.screens;

/**
 * This interface is called when we switch from one screen to another
 * @author leo
 *
 */
public interface IScreenStateTracker
{
	boolean onScreenHit(ScreenContext curCtx);
	/**
	 * Need to return id of the tracking screen
	 * @return
	 */
	int getTrackingScreenId();
	
	/**
	 * Do we need to leave this tracker or delete it after first hit
	 * @return
	 */
	boolean getRepeatable();
}
