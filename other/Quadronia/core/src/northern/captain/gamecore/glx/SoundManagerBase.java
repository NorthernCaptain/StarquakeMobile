package northern.captain.gamecore.glx;

import northern.captain.gamecore.glx.tools.HandlerThread;
import northern.captain.gamecore.glx.tools.Message;
import northern.captain.tools.IPersistentConfig;

import java.io.FileInputStream;
import java.io.FileOutputStream;

public abstract class SoundManagerBase extends HandlerThread implements ISoundMan
{
	protected boolean soundOn = true;
	protected boolean musicOn = true;
	protected boolean vibrationOn = true;
		
	protected long[] vibroPattern = new long[] { 200, 150 };
	protected int lastVolume;
	protected int beforeVolume;
	
	protected int lastPlayedIdx = -1;
	
//	protected final SourceVerifier very = new SourceVerifier3();
	
	public SoundManagerBase()
	{
		super("Soundman");
	}


	/**
	 * Plays the sound by the given index, also could start a vibration
	 * @param idx
	 * @param vibrate
	 */
	protected abstract void playSoundInternal(int idx, boolean vibrate);
	protected abstract void playMusicInternal(int idx);

	protected abstract void stopSoundInternal(int idx);
	protected abstract void stopMusicInternal();

	/**
	 * Starts playing music in the background. Previous music will be stopped
	 *
	 * @param idx
	 */
	@Override
	public void playMusic(int idx)
	{
		Message msg = obtain();
		msg.what = MSG_MUSIC;
		msg.arg1 = idx;
		send(msg);
	}

	@Override
	public void stopMusic()
	{
		Message msg = obtain();
		msg.what = MSG_STOP_MUSIC;
		send(msg);
	}
	/* (non-Javadoc)
         * @see northern.captain.seabattle.ISoundMan#playSound(int, boolean)
         */
	@Override
	public void playSound(int idx, boolean vibrate)
	{
		Message msg = obtain();
		msg.what = MSG_PLAY;
		msg.arg1 = idx;
		msg.arg2 = vibrate ? 1 : 0;
	
		lastPlayedIdx = idx;
		
		send(msg);
	}

	/* (non-Javadoc)
	 * @see northern.captain.seabattle.ISoundMan#stopLastSound()
	 */
	@Override
	public void stopLastSound()
	{
		if(lastPlayedIdx < 0)
			return;
		stopSound(lastPlayedIdx);
	}
	
	/* (non-Javadoc)
	 * @see northern.captain.seabattle.ISoundMan#stopAllSounds()
	 */
	@Override
	public void stopAllSounds()
	{
		for(int i=0;i<SND_MAX;i++)
		{
			Message msg = obtain();
			msg.what = MSG_STOP;
			msg.arg1 = i;
			
			send(msg);					
		}
	}
	/* (non-Javadoc)
	 * @see northern.captain.seabattle.ISoundMan#stopSound(int)
	 */
	@Override
	public void stopSound(int idx)
	{		
		Message msg = obtain();
		msg.what = MSG_STOP;
		msg.arg1 = idx;
		
		send(msg);		
	}
	
	/* (non-Javadoc)
	 * @see android.os.HandlerThread#onLooperPrepared()
	 */
	@Override
	protected void onLooperPrepared()
	{
//		very.doVerification();
	}

	/**
	 * Process sound message in separate thread
	 * @param msg
	 */
	@Override
	protected boolean processMsg(Message msg)
	{
		switch(msg.what)
		{
			case MSG_PLAY:
				playSoundInternal(msg.arg1, msg.arg2 == 1);
				break;
			case MSG_STOP:
				stopSoundInternal(msg.arg1);
				break;
			case MSG_MUSIC:
				playMusicInternal(msg.arg1);
				break;
			case MSG_STOP_MUSIC:
				stopMusicInternal();
				break;
			default:
				if(msg.obj instanceof Runnable)
				{
					Runnable run = (Runnable)msg.obj;
					run.run();
				}
				break;
					
		}
		return true;
	}


	@Override
	public void optionsChanged(IPersistentConfig cfg, String key)
	{
		soundOn = cfg.getBoolean("isSound", true);
		vibrationOn = cfg.getBoolean("isVibration", true);
		musicOn = cfg.getBoolean("isMusic", true);
	}


	/* (non-Javadoc)
	 * @see northern.captain.seabattle.ISoundMan#resumeSounds()
	 */
	@Override
	public void resumeSounds()
	{
	}
	
	/* (non-Javadoc)
	 * @see northern.captain.seabattle.ISoundMan#restoreSystem()
	 */
	@Override
	public void restoreSystem()
	{
	}
	
	@Override
	public void loadData(IPersistentConfig cfg)
	{
		optionsChanged(cfg, null);
		resumeSounds();
		cfg.removeOptionsChangeListener(this);
		cfg.addOptionsChangeListener(this);
	}


	@Override
	public void loadData(FileInputStream fin)
	{
	}


	@Override
	public void saveData(IPersistentConfig cfg)
	{
	}


	@Override
	public void saveData(FileOutputStream fout)
	{
	}
	
	public void initSoundsLoading()
	{	
	}
	
	public void finishSoundsLoading()
	{
		
	}

    @Override
    public void directSoundLoading()
    {

    }
}
