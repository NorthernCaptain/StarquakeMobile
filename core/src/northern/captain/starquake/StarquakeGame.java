package northern.captain.starquake;

import com.badlogic.gdx.Game;
import northern.captain.starquake.audio.MusicManager;
import northern.captain.starquake.audio.SoundManager;
import northern.captain.starquake.screen.LoadingScreen;

public class StarquakeGame extends Game {
    public Assets assets;

    @Override
    public void create() {
        assets = new Assets();
        setScreen(new LoadingScreen(this));
    }

    @Override
    public void dispose() {
        super.dispose();
        if (MusicManager.get() != null) MusicManager.get().dispose();
        if (SoundManager.get() != null) SoundManager.get().dispose();
        assets.dispose();
    }
}
