package northern.captain.gamecore.glx;

import northern.captain.tools.IOptionsChangeListener;
import northern.captain.tools.IPersistCfg;

public interface ISoundMan extends IOptionsChangeListener, IPersistCfg
{

	public static final int SND_MIN = 1;
	public static final int SND_GAME_ALMOST_LOST = 1;
	public static final int SND_GAME_YLOST = 2;
	public static final int SND_BONUS_X2 = 3;
	public static final int SND_BOMB = 4;
	public static final int SND_BONUS_M200 = 5;
	public static final int SND_WRONG_HIT = 6;
	public static final int SND_QUAD_AREA_DISSAPEAR = 7;
	public static final int SND_QUAD_AREA_APPEAR = 8;
	public static final int SND_TIME_ADD = 9;
	public static final int SND_JUST_CLICK = 10;
	public static final int SND_START_GAME = 11;
	public static final int SND_START_EDIT = 12;
	public static final int SND_TIME_ALMOST_OVER = 13;
	public static final int SND_COINS_FOUND = 14;
	public static final int SND_RECOLOR = 15;
    public static final int SND_PAUSED = 16;
	public static final int SND_MAX = 17;
	public static final int MSG_PLAY = 1;
	public static final int MSG_STOP = 2;
	public static final int MSG_MUSIC = 3;
	public static final int MSG_STOP_MUSIC = 4;

	public static final int MUS_INIT = 0;
	public static final int MUS_MENU = 1;
	public static final int MUS_GAME1 = 2;
	public static final int MUS_GAME2 = 3;
	public static final int MUS_GAME3 = 4;
	public static final int MUS_GAME4 = 5;
	public static final int MUS_GAME5 = 6;
	public static final int MUS_MAX = 7;

	/**
	 * Starts playing given sound and optionally vibrates the phone
	 * @param idx
	 * @param vibrate
	 */
	public abstract void playSound(int idx, boolean vibrate);

	/**
	 * Starts playing music in the background. Previous music will be stopped
	 * @param idx
	 */
	public abstract void playMusic(int idx);

	/**
	 * Stops playing last sound
	 */
	public abstract void stopLastSound();

	public void stopMusic();

	/**
	 * Stops all sounds
	 */
	public abstract void stopAllSounds();

	/**
	 * Stops playing given sound
	 * @param idx
	 */
	public abstract void stopSound(int idx);

	public abstract void resumeSounds();

	public abstract void restoreSystem();
	
	public abstract Object getHandler();

	public void initSoundsLoading();
	
	public void finishSoundsLoading();

    public void directSoundLoading();

    void vibrate();

}
