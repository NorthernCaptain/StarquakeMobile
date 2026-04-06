package northern.captain.gamecore.glx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.squareup.otto.Subscribe;

import northern.captain.gamecore.glx.tools.loaders.IResLoader;
import northern.captain.gamecore.glx.tools.loaders.ResLoader;
import northern.captain.quadronia.game.events.EOptionsChanged;
import northern.captain.tools.Log;

public class SoundManager2 extends SoundManagerBase
{

	private Sound[] sounds = new Sound[SND_MAX];
	private Music[] musics = new Music[MUS_MAX];
	private long[] streams = new long[SND_MAX];
	private Music currenMusic;
	private int[] load = new int[SND_MAX];
	
	public SoundManager2() 
	{
		super();
		NCore.busRegister(this);
	}

	@Override
	public void vibrate()
	{
		if(vibrationOn)
			Gdx.input.vibrate(250, true);
	}

	@Override
	protected void playSoundInternal(int idx, boolean vibrate) 
	{
		if(vibrate && vibrationOn)
			Gdx.input.vibrate(250, true);

		if(!soundOn || idx<0 || idx>=SND_MAX || sounds[idx] == null)
			return;
		streams[idx] = sounds[idx].play(0.8f);
	}

	@Override
	protected void stopSoundInternal(int idx) 
	{
        if(sounds[idx] != null)
		    sounds[idx].stop();
	}

	@Override
	protected void playMusicInternal(int idx)
	{
		if(!musicOn || idx<0 || idx>=MUS_MAX || musics[idx] == null)
			return;

		if(currenMusic != null && currenMusic.isPlaying())
		{
			currenMusic.stop();
		}
		currenMusic = musics[idx];
		currenMusic.setVolume(0.1f);
		currenMusic.play();
	}

	@Override
	protected void stopMusicInternal()
	{
		if(currenMusic != null)
		{
			currenMusic.stop();
			currenMusic = null;
		}
	}

	/* (non-Javadoc)
         * @see android.os.HandlerThread#onLooperPrepared()
         */
	@Override
	protected void onLooperPrepared()
	{		
        Thread loadingThread = new Thread(()->{
            directSoundLoading();

//                very.doVerification();
        });

        loadingThread.start();
	}

    @Override
    public void directSoundLoading()
    {
		//wait until files are initialized
		while(Gdx.files == null) {
			try {
				Thread.sleep(100);
			} catch (Exception e) {
			}
		}

        Log.d("ncgames", "Start loading sounds in the sound thread");
        musics[MUS_INIT] = directLoadMusic("mus_init");
        musics[MUS_MENU] = directLoadMusic("mus_menu");
        musics[MUS_GAME1] = directLoadMusic("mus_game1");
        musics[MUS_GAME2] = directLoadMusic("mus_game2");
        musics[MUS_GAME3] = directLoadMusic("mus_game3");
        musics[MUS_GAME4] = directLoadMusic("mus_game4");
        musics[MUS_GAME5] = directLoadMusic("mus_game5");

		sounds[SND_JUST_CLICK] = directLoad("snd_click");
		sounds[SND_WRONG_HIT] = directLoad("snd_wrong_turn");
		sounds[SND_BONUS_M200] = directLoad("m200");
		sounds[SND_QUAD_AREA_APPEAR] = directLoad("mywind");
		sounds[SND_QUAD_AREA_DISSAPEAR] = directLoad("bambambam");
		sounds[SND_TIME_ADD] = directLoad("ticktockwatches");
		sounds[SND_TIME_ALMOST_OVER] = directLoad("ticktock");
		sounds[SND_COINS_FOUND] = directLoad("coin_drop");
		sounds[SND_RECOLOR] = directLoad("cardsshuffle");
		sounds[SND_BONUS_X2] = directLoad("x2");
		sounds[SND_GAME_YLOST] = directLoad("metalhit");
		sounds[SND_GAME_ALMOST_LOST] = directLoad("wauwau");
		sounds[SND_PAUSED] = directLoad("pause");
		sounds[SND_BOMB] = directLoad("explode");
        Log.d("ncgames", "Finished loading sounds in the sound thread");
    }

    @Override
	public void initSoundsLoading()
	{	
		IResLoader res = ResLoader.singleton();
		load[SND_GAME_ALMOST_LOST] = res.loadSound("snd_victory");
		load[SND_GAME_YLOST] = res.loadSound("snd_looser"); 
		load[SND_BONUS_X2] = res.loadSound("snd_hit_missed3");
		load[SND_BOMB] = res.loadSound("snd_hit_ship2");
		load[SND_BONUS_M200] = res.loadSound("m200");
		load[SND_WRONG_HIT] = res.loadSound("snd_wrong_turn");
		load[SND_QUAD_AREA_DISSAPEAR] = res.loadSound("e_snd_hit_missed3");
		load[SND_QUAD_AREA_APPEAR] = res.loadSound("e_snd_hit_ship3");
		load[SND_TIME_ADD] = res.loadSound("e_snd_hit_ship_dead2");
		load[SND_JUST_CLICK] = res.loadSound("snd_click");		
		load[SND_START_GAME] = res.loadSound("snd_call2arms");		
		load[SND_START_EDIT] = res.loadSound("snd_edit_field");		
		load[SND_TIME_ALMOST_OVER] = res.loadSound("bell");
        load[SND_PAUSED] = res.loadSound("snd_first_call");
//        load[SND_HISS] = res.loadSound("hiss");
//		load[SND_SERVO] = res.loadSound("servo");
		
	}

	@Override
	public void finishSoundsLoading()
	{
		IResLoader res = ResLoader.singleton();
		sounds[SND_GAME_ALMOST_LOST] = res.getLoaded(load[SND_GAME_ALMOST_LOST]);
		sounds[SND_GAME_YLOST] = res.getLoaded(load[SND_GAME_YLOST]); 
		sounds[SND_BONUS_X2] = res.getLoaded(load[SND_BONUS_X2]);
		sounds[SND_BOMB] = res.getLoaded(load[SND_BOMB]);
		sounds[SND_BONUS_M200] = res.getLoaded(load[SND_BONUS_M200]);
		sounds[SND_WRONG_HIT] = res.getLoaded(load[SND_WRONG_HIT]);
		sounds[SND_QUAD_AREA_DISSAPEAR] = res.getLoaded(load[SND_QUAD_AREA_DISSAPEAR]);
		sounds[SND_QUAD_AREA_APPEAR] = res.getLoaded(load[SND_QUAD_AREA_APPEAR]);
		sounds[SND_TIME_ADD] = res.getLoaded(load[SND_TIME_ADD]);
		sounds[SND_JUST_CLICK] = res.getLoaded(load[SND_JUST_CLICK]);
		sounds[SND_START_GAME] = res.getLoaded(load[SND_START_GAME]);
		sounds[SND_START_EDIT] = res.getLoaded(load[SND_START_EDIT]);
		sounds[SND_TIME_ALMOST_OVER] = res.getLoaded(load[SND_TIME_ALMOST_OVER]);
		sounds[SND_PAUSED] = res.getLoaded(load[SND_PAUSED]);
//		sounds[SND_HISS] = res.getLoaded(load[SND_HISS]);
//		sounds[SND_SERVO] = res.getLoaded(load[SND_SERVO]);
	}

    
	@Override
	public Object getHandler()
	{
		return null;
	}


    private Sound directLoad(String name)
    {
        Sound sound = Gdx.audio.newSound(
                Gdx.files.internal(NContext.current.getSoundDir(name)));
        Log.d("ncgames", "Done loading sound: " + name);
        return sound;
    }

	private Music directLoadMusic(String name)
	{
		Music sound = Gdx.audio.newMusic(
			Gdx.files.internal(NContext.current.getSoundDir(name)));
		Log.d("ncgames", "Done loading sound: " + name);
		return sound;
	}

	@Subscribe
	public void onOptionsChanged(EOptionsChanged event)
	{
		soundOn = event.optionsMenu.isSoundOn();
		vibrationOn = event.optionsMenu.isVibrationOn();
		musicOn = event.optionsMenu.isMusicOn();
		if(!musicOn) stopMusic();
	}

}
