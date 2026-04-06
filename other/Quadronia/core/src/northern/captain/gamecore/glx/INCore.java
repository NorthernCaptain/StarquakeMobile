package northern.captain.gamecore.glx;

import northern.captain.gamecore.glx.screens.IScreenActivator;
import northern.captain.gamecore.glx.screens.IScreenWorkflow;
import northern.captain.quadronia.IGameOptionsMenu;
import northern.captain.tools.IActivityListener;
import northern.captain.tools.IOnBackAction;
import northern.captain.tools.IPersistCfg;
import northern.captain.tools.IPersistentConfig;

import java.io.File;
import java.util.UUID;

public interface INCore extends IScreenActivator
{
	void initialize();
	
	IPersistentConfig getCfg();

	void addPersistListener(IPersistCfg persistObj);

	void removePersistListener(IPersistCfg persistObj);

	String getOurPackageName();

	File logFileDir();

	/**
	 * Quit the app
	 */
	void doQuit();

	/**
	 * Getter for the sound manager of the game
	 * @return
	 */
	ISoundMan getSoundman();
    void setSoundman(ISoundMan soundman);

    String getUUIDString();
    UUID getUUID();

	boolean isNewVersion();

	void resetNewVersion();

	boolean getStopped();

	void setStopped(boolean stopped);

	void setLang();
	
	/**
	 * Adds listener for receiving onActivityResult events
	 * @param listener
	 */
	void addActivityResultListener(IActivityListener listener);

    void onActivityResult(int requestCode, int resultCode, Object data);

    void removeActivityResultListener(Class<? extends IActivityListener> clazz);

	void saveImmediateData();

	/**
	 * Opens software keyboard on the screen for input
	 * @param from
	 */
	void openSoftKeyboard(Object from);

	void closeSoftKeyboard(Object from);

	IScreenWorkflow getScreenFlow();

	/**
	 * Save all data to persistent storage
	 */
	void saveData();
	/**
	 * Loads data from the storage
	 */
	void loadData();
	
	void initGame();
	
	void openOptionsMenu();

	void pushOnBackAction(IOnBackAction action);
    void popOnBackAction(IOnBackAction action);

    void postRunnableOnMain(Runnable runnable);

    void loadOnIntro();

    void onDestroy();

    void onBackPressed();

	INameDialog newNameDialog();

	void setGameOptionsMenu(IGameOptionsMenu menu);
	IGameOptionsMenu getGameOptionsMenu();

	void toastLong(String text);
	void toastShort(String text);
}
