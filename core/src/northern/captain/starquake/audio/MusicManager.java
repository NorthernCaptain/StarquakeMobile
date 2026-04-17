package northern.captain.starquake.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.math.MathUtils;

/**
 * Manages background music playback. Plays tracks sequentially from a
 * shuffled playlist, loops indefinitely. Volume is 30% of SFX volume.
 *
 * Usage: MusicManager.init(); MusicManager.get().setEnabled(true);
 */
public class MusicManager {
    private static MusicManager instance;

    private static final String MUSIC_PATH = "audio/music/";
    private static final String[] TRACKS = {
        "mus_01_v2.mp3", "mus_02_v2.mp3", "mus_03_v2.mp3", "mus_04_v2.mp3",
        "mus_05_v2.mp3", "mus_06_v2.mp3", "mus_07_v2.mp3", "mus_08_v2.mp3"
    };
    private static final float VOLUME_SCALE = 0.25f; // 25% of SFX volume

    private final int[] playlist = new int[TRACKS.length];
    private int currentIndex;
    private Music currentMusic;
    private boolean enabled;
    private boolean paused;
    private float volume = 1f; // master volume (0-1), actual = volume * VOLUME_SCALE

    public static void init() {
        if (instance != null) instance.dispose();
        instance = new MusicManager();
    }

    public static MusicManager get() {
        return instance;
    }

    private MusicManager() {
        shufflePlaylist();
    }

    private void shufflePlaylist() {
        int lastPlayed = (currentIndex > 0) ? playlist[currentIndex - 1] : -1;
        for (int i = 0; i < playlist.length; i++) playlist[i] = i;
        for (int i = playlist.length - 1; i > 0; i--) {
            int j = MathUtils.random(i);
            int tmp = playlist[i]; playlist[i] = playlist[j]; playlist[j] = tmp;
        }
        // If the first track is the same as the last played, swap it with a random other position
        if (playlist.length > 1 && playlist[0] == lastPlayed) {
            int swap = 1 + MathUtils.random(playlist.length - 2);
            playlist[0] = playlist[swap];
            playlist[swap] = lastPlayed;
        }
        currentIndex = 0;
    }

    public void setEnabled(boolean enabled) {
        if (this.enabled == enabled) return;
        this.enabled = enabled;
        if (enabled) {
            shufflePlaylist();
            playNext();
        } else {
            stopCurrent();
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setVolume(float volume) {
        this.volume = Math.max(0, Math.min(1, volume));
        if (currentMusic != null) {
            currentMusic.setVolume(this.volume * VOLUME_SCALE);
        }
    }

    public float getVolume() {
        return volume;
    }

    /** Call each frame to detect track completion and advance playlist. */
    public void update() {
        if (!enabled || currentMusic == null || paused) return;
        if (!currentMusic.isPlaying()) {
            playNext();
        }
    }

    private void playNext() {
        stopCurrent();
        if (!enabled) return;

        String trackName = TRACKS[playlist[currentIndex]];
        try {
            currentMusic = Gdx.audio.newMusic(Gdx.files.internal(MUSIC_PATH + trackName));
            currentMusic.setVolume(volume * VOLUME_SCALE);
            currentMusic.setLooping(false);
            currentMusic.play();
        } catch (Exception e) {
            Gdx.app.error("MusicManager", "Failed to play " + trackName, e);
            currentMusic = null;
        }

        currentIndex++;
        if (currentIndex >= playlist.length) {
            shufflePlaylist(); // reshuffle for next cycle
        }
    }

    private void stopCurrent() {
        if (currentMusic != null) {
            currentMusic.stop();
            currentMusic.dispose();
            currentMusic = null;
        }
    }

    public void pause() {
        paused = true;
        stopCurrent();
    }

    public void resume() {
        if (paused && enabled) {
            paused = false;
            playNext();
        }
    }

    public void dispose() {
        stopCurrent();
        instance = null;
    }
}
