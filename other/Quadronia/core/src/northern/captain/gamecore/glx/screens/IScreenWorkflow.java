package northern.captain.gamecore.glx.screens;

import northern.captain.gamecore.glx.INCore;
import northern.captain.tools.IPersistCfg;

public interface IScreenWorkflow extends IPersistCfg
{

	int WF_NEW_GAME = 1;
	int WF_RESUME_GAME = 2;
	int WF_OPTIONS = 3;
	int WF_QUIT = 4;
	int WF_NEXT = 5;
	int WF_BACK = 6;
	int WF_AND_GAME = 7;
	int WF_START_GAME = 8;
	int WF_ANY = 9;
	int WF_WON = 10;
	int WF_LOST = 11;
	int WF_STAY = 12;
	int WF_CHANGE = 13;
	int WF_HELP = 14;
	int WF_CONNECT = 15;
	int WF_BACK_AUTO = 16;
	int WF_CANCEL = 17;
    int WF_PREVIOUS = 18;

	int STATE_INITIAL = 70;
	int STATE_MAIN_MENU = 71;
	int STATE_PLAY_TYPE = 72;
	int STATE_FIELD_GEN = 73;
	int STATE_BATTLE = 74;
	int STATE_WON = 75;
	int STATE_LOST = 77;
	int STATE_OPTIONS = 76;
	int STATE_SCORES = 78;
	int STATE_EXIT = 79;
	int STATE_GLOBAL_SCORES = 80;
	int STATE_HELP = 81;
	int STATE_WEBCON = 82;
	int STATE_ENEMYVIEW = 83;
	int STATE_SAILORPROFILE = 84;
	int STATE_WEBRIVALS = 85;
	int STATE_PLAYER_START = 86;
	int STATE_ARMORY_SHOP = 87;
	int STATE_WALLET = 88;


	INCore getApp();

	/**
	 * Adds a given screen activator for the given state
	 * @param state
	 * @param activator
	 */
	void addScreenActivator(int state, IScreenActivator activator);

	IScreenActivator getScreenActivator(int state);

	/**
	 * Sets the new state as a current and call activate for the new screen.
	 * Also calls deactivate when changing from old state.
	 * @param newCtx
	 */
	void setState(ScreenContext newCtx);

	IScreenActivator getCurrentScreen();

	int getCurrentState();

    ScreenContext getCurrentCtx();

    String getCurrentScreenName();

    /**
	 * Process the given action according to the current state and workflow,
	 * switches to the new state.
	 * @param action
	 */
	boolean doAction(int action);

	/**
	 * Call prepare method on the next screen according to the given action
	 * @param action
	 */
	void prepare(int action);
	/**
	 * Activates current screen according to the current state
	 */
	void activateCurrentScreen();

	void setCurrentState(int state);
	
	void addTrackerOnEnter(IScreenStateTracker tracker);
	
	void removeTrackerOnEnter(IScreenStateTracker tracker);

    void replaceLast(int newOldState);

    void onFullResume();

    void onFullSuspend();
}
