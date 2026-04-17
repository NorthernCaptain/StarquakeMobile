package northern.captain.starquake;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

/**
 * Persistent app-level settings. Survives across games (never cleared by SaveManager).
 * Initialize once after libGDX is ready: AppSettings.init().
 */
public class AppSettings {
    private static AppSettings instance;

    private final Preferences prefs;

    private boolean musicEnabled;
    private boolean soundEnabled;
    private boolean dpadRight;

    public static void init() {
        instance = new AppSettings();
    }

    public static AppSettings get() {
        return instance;
    }

    private AppSettings() {
        prefs = Gdx.app.getPreferences("app_settings");
        musicEnabled = prefs.getBoolean("music", true);
        soundEnabled = prefs.getBoolean("sound", true);
        dpadRight = prefs.getBoolean("dpad_right", true);
    }

    public boolean isMusicEnabled() { return musicEnabled; }
    public boolean isSoundEnabled() { return soundEnabled; }
    public boolean isDpadRight() { return dpadRight; }

    public void setMusicEnabled(boolean enabled) {
        musicEnabled = enabled;
        prefs.putBoolean("music", enabled);
        prefs.flush();
    }

    public void setSoundEnabled(boolean enabled) {
        soundEnabled = enabled;
        prefs.putBoolean("sound", enabled);
        prefs.flush();
    }

    public void setDpadRight(boolean right) {
        dpadRight = right;
        prefs.putBoolean("dpad_right", right);
        prefs.flush();
    }
}
