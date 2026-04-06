package northern.captain.gamecore.glx.screens;

public interface IScreenActivator
{
	/**
	 * Called when we want to activate this screen
	 * @param sctx
	 */
	void activate(ScreenContext sctx);
	
	/**
	 * Called just before deactivating the current screen
	 * @param sctx
	 */
	void deactivate(ScreenContext sctx);
	
	/**
	 * Called when back button is pressed
	 * If return false then no back action will be processed
	 */
	boolean onBackAction();
	
	/**
	 * Called when the app goes to suspend mode (hide from screen)
	 */
	void onFullSuspend();
	
	/**
	 * Called when the app goes to full screen mode and shown to the user
	 */
	void onFullResume();
	
	/**
	 * Called before entring the screen. Here we need to ask about resources we need to load for our screen
	 */
	void prepareEnter();

	/**
	 * Called when orientation of the phone has changed
	 */
	void onOrientationChange();
}
