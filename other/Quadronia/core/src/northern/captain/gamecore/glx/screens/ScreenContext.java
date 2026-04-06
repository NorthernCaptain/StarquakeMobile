package northern.captain.gamecore.glx.screens;

import northern.captain.gamecore.glx.INCore;

/**
 * Screen context class, contains information current and previous screen modes
 * @author leo
 *
 */
public class ScreenContext
{
	public INCore app;

	public int currentState = IScreenWorkflow.STATE_INITIAL;// IScreenWorkflow.STATE_MAIN_MENU;
	public int action = IScreenWorkflow.WF_RESUME_GAME;
	
	public IScreenActivator screen = null;
	
	public ScreenContext(INCore ourApp)
	{
		app = ourApp;
	}

	public ScreenContext(INCore ourApp, int newstate, int action)
	{
		app = ourApp;
		currentState = newstate;
		this.action = action;
	}	
	
	public void setScreen(IScreenActivator newScreen)
	{
		screen = newScreen;
	}
}
