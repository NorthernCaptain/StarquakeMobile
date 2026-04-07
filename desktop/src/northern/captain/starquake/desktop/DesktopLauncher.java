package northern.captain.starquake.desktop;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import northern.captain.starquake.StarquakeGame;

public class DesktopLauncher {
    public static void main(String[] arg) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Starquake");
        config.setWindowedMode(1280, 960);
        config.setResizable(true);
        new Lwjgl3Application(new StarquakeGame(), config);
    }
}
