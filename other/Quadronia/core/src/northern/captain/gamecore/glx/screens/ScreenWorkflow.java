package northern.captain.gamecore.glx.screens;

import northern.captain.gamecore.glx.INCore;
import northern.captain.tools.IPersistentConfig;
import northern.captain.tools.LimitedList;
import northern.captain.tools.Log;
import northern.captain.tools.analytics.AnalyticsFactory;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Realize screen workflow, i.e. where to go after one screen is finished 
 * with a given state.
 * @author leo
 *
 */
public class ScreenWorkflow implements IScreenWorkflow
{
	protected Map<Integer, Integer>  stateMap = new HashMap<Integer, Integer>();
	protected Map<Integer, IScreenActivator> stateScreens = new HashMap<Integer, IScreenActivator>();
	
	protected List<IScreenStateTracker> trackersOnEnter = new ArrayList<IScreenStateTracker>();

    protected static final int MAX_BACK_HISTORY_SIZE = 5;
    protected LimitedList<Integer> stateStack = new LimitedList<Integer>(MAX_BACK_HISTORY_SIZE);

	protected ScreenContext currentCtx;
	protected INCore app;

    protected Map<Integer, String> screenNames = new HashMap<Integer, String>();

	/* (non-Javadoc)
	 * @see northern.captain.seabattle.screens.IScreenWorkflow#getApp()
	 */
	@Override
	public INCore getApp()
	{
		return app;
	}

    protected void initScreenNames()
    {
    }

	public ScreenWorkflow(INCore app)
	{
		this.app = app;
		
		app.addPersistListener(this);

        currentCtx = new ScreenContext(app);

        initScreenNames();

	}

    protected String getScreenName(int id)
    {
        String name = screenNames.get(id);
        if(name == null)
            name = "unknown";
        return name;
    }

    @Override
    public String getCurrentScreenName()
    {
        if(currentCtx == null)
            return "unknown";

        return getScreenName(currentCtx.currentState);
    }

	protected int stateAct(int state, int action)
	{
		return (state << 8) | action;
	}
	
	/* (non-Javadoc)
	 * @see northern.captain.seabattle.screens.IScreenWorkflow#addScreenActivator(int, northern.captain.seabattle.screens.IScreenActivator)
	 */
	@Override
	public void addScreenActivator(int state, IScreenActivator activator)
	{
		stateScreens.put(state, activator);
	}
	
	/* (non-Javadoc)
	 * @see northern.captain.seabattle.screens.IScreenWorkflow#getScreenActivator(int)
	 */
	@Override
	public IScreenActivator getScreenActivator(int state)
	{
		return stateScreens.get(state);
	}

    @Override
    public void replaceLast(int newOldState)
    {
        stateStack.popBack(0);
        stateStack.add(newOldState);
    }

	/* (non-Javadoc)
	 * @see northern.captain.seabattle.screens.IScreenWorkflow#setState(northern.captain.seabattle.screens.ScreenContext)
	 */
	@Override
	public void setState(ScreenContext newCtx)
	{
        int action=newCtx.action;

        //Saving old state to stack
        if(
                   action != WF_BACK
                && action != WF_BACK_AUTO
                && action != WF_PREVIOUS
                && action != WF_STAY
          )
        {
		    stateStack.add(currentCtx.currentState);
        }
		
		if(currentCtx.screen != null)
		{
			currentCtx.screen.deactivate(newCtx);
			Log.i("ncgame", "#### Screen deactivated, doing GC");
			System.gc();
		}
		
		currentCtx = newCtx;
		currentCtx.screen = stateScreens.get(currentCtx.currentState);
		
		if(currentCtx.screen != null)
		{
            Log.i("ncgame", "#### Activating next screen");
            AnalyticsFactory.instance().getAnalytics().registerScreenEnter(getScreenName(currentCtx.currentState));
            app.setLang();
            currentCtx.screen.activate(currentCtx);

            //call all trackers for this screen
            for(int i=0;i < trackersOnEnter.size();i++)
            {
                IScreenStateTracker tracker = trackersOnEnter.get(i);
                if(tracker.getTrackingScreenId() == currentCtx.currentState)
                {
                    tracker.onScreenHit(currentCtx);
                    if(!tracker.getRepeatable())
                    {
                        trackersOnEnter.remove(i);
                        i--;
                    }
                }
            }
//			Log.i("ncgame", "#### Activating next screen");
//			app.setLang();
//			currentCtx.screen.activate(currentCtx);
		}
	}

	/* (non-Javadoc)
	 * @see northern.captain.seabattle.screens.IScreenWorkflow#getCurrentScreen()
	 */
	@Override
	public IScreenActivator getCurrentScreen()
	{
		return currentCtx.screen;
	}
	
	/* (non-Javadoc)
	 * @see northern.captain.seabattle.screens.IScreenWorkflow#getCurrentState()
	 */
	@Override
	public int getCurrentState()
	{
        if(currentCtx == null)
            return 0;
		return currentCtx.currentState;
	}
	
	protected Integer getNewState(int action, boolean justCheck)
	{
        if(action == WF_PREVIOUS)
        {
            return justCheck ? stateStack.peekBack(currentCtx.currentState) : stateStack.popBack(currentCtx.currentState);
        }

		int mix = stateAct(currentCtx.currentState, action);
		Integer newState = stateMap.get(mix);
		if(newState == null)
		{
			mix = stateAct(currentCtx.currentState, WF_ANY);
			newState = stateMap.get(mix);
		}
		return newState;
	}
	/* (non-Javadoc)
	 * @see northern.captain.seabattle.screens.IScreenWorkflow#doAction(int)
	 */
	@Override
	public boolean doAction(int action)
	{
		Integer newState = getNewState(action, false);
		
		if(newState == null)
			return false; //do not change the state - wrong action
		
		if(action == WF_BACK && currentCtx.screen !=null && !currentCtx.screen.onBackAction())
			return false;
		
		ScreenContext ctx = new ScreenContext(app, newState.intValue(), action);
		setState(ctx);
		return true;
	}
	
	
	/* (non-Javadoc)
	 * @see northern.captain.seabattle.screens.IScreenWorkflow#prepare(int)
	 */
	@Override
	public void prepare(int action)
	{
		Integer newState = getNewState(action, true);

		if(newState == null)
			return; //nothing to do - wrong action
		
		IScreenActivator screen = stateScreens.get(newState);
		if(screen == null)
			return;
		
		screen.prepareEnter();
	}

	/* (non-Javadoc)
	 * @see northern.captain.seabattle.screens.IScreenWorkflow#activateCurrentScreen()
	 */
	@Override
	public void activateCurrentScreen()
	{
		currentCtx.screen = stateScreens.get(currentCtx.currentState);
		
		if(currentCtx.screen != null)
		{
			currentCtx.screen.activate(currentCtx);
		}		
	}

	/* (non-Javadoc)
	 * @see northern.captain.seabattle.screens.IScreenWorkflow#setCurrentState(int)
	 */
	@Override
	public void setCurrentState(int state)
	{
		currentCtx.currentState = state;
	}
	
	@Override
	public void addTrackerOnEnter(IScreenStateTracker tracker)
	{
		trackersOnEnter.add(tracker);
	}
	
	@Override
	public void removeTrackerOnEnter(IScreenStateTracker tracker)
	{
		trackersOnEnter.remove(tracker);
	}
	
	public void loadData(IPersistentConfig cfg)
	{
		currentCtx.currentState = cfg.getInt("currentState", STATE_MAIN_MENU);
		stateStack.add(cfg.getInt("oldState", STATE_WON));
		if(currentCtx.currentState != STATE_BATTLE &&
			currentCtx.currentState != STATE_PLAY_TYPE)
		{
			currentCtx.currentState = STATE_MAIN_MENU;
			Log.d("ncgame", "Restoring workflow to main menu");
		}
		else
			Log.d("ncgame", "Restoring workflow to current screen");
			
	}

	public void loadData(FileInputStream fin)
	{
	}

	public void saveData(IPersistentConfig cfg)
	{
		cfg.setInt("currentState", currentCtx.currentState);
		cfg.setInt("oldState", stateStack.popBack(currentCtx.currentState));
	}

	public void saveData(FileOutputStream fout)
	{
	}

    @Override
    public ScreenContext getCurrentCtx()
    {
        return currentCtx;
    }

    @Override
    public void onFullSuspend()
    {
        if(currentCtx.screen != null)
        {
            currentCtx.screen.onFullSuspend();
        }
    }

    @Override
    public void onFullResume()
    {
        if(currentCtx.screen != null)
        {
            currentCtx.screen.onFullResume();
        }
    }

}
