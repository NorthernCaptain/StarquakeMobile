package northern.captain.quadronia.android.common;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.iosrobovm.IOSApplication;

import org.robovm.apple.uikit.UIViewController;

import de.golfgl.gdxgamesvcs.GameCenterClient;
import de.golfgl.gdxgamesvcs.IGameServiceClient;
import northern.captain.gamecore.gplus.GoogleGamesFactory;
import northern.captain.quadronia.TheGame;
import northern.captain.tools.sharing.ShareManager;

public class IOSGame extends TheGame {
    public IGameServiceClient gsClient;

    @Override
    public void create() {
        Gdx.app.setLogLevel(Application.LOG_DEBUG);
        UIViewController mainController = ((IOSApplication) Gdx.app).getUIViewController();
        gsClient = new GameCenterClient(mainController);
        GamesProcessorIOS gameProc = (GamesProcessorIOS)GoogleGamesFactory.instance().getProcessor();
        gameProc.setGSClient(gsClient);

        ShareManagerIOS shareMan = (ShareManagerIOS) ShareManager.instance();
        shareMan.setMainController(mainController);
        super.create();
    }

    @Override
    public void pause() {
        super.pause();
        GoogleGamesFactory.instance().getProcessor().onStop();
    }

    @Override
    public void resume() {
        super.resume();

        GoogleGamesFactory.instance().getProcessor().onStart();
    }
}

