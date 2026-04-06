package northern.captain.quadronia.screens;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.squareup.otto.Subscribe;

import org.json.JSONObject;

import northern.captain.gamecore.glx.ISoundMan;
import northern.captain.gamecore.glx.NContext;
import northern.captain.gamecore.glx.NCore;
import northern.captain.gamecore.glx.screens.IScreenWorkflow;
import northern.captain.gamecore.glx.screens.ScreenBase;
import northern.captain.gamecore.glx.tools.Vector2i;
import northern.captain.gamecore.glx.tools.loaders.IResLoader;
import northern.captain.gamecore.glx.tools.loaders.ResLoader;
import northern.captain.gamecore.glx.tools.loaders.XMLContentLoader;
import northern.captain.gamecore.glx.tools.loaders.XMLLayoutLoader;
import northern.captain.quadronia.BaseDialog;
import northern.captain.quadronia.LeaveGameDialog;
import northern.captain.quadronia.game.IGameContext;
import northern.captain.quadronia.game.core.Config;
import northern.captain.quadronia.game.core.Game;
import northern.captain.quadronia.game.events.EGameExitRequest;
import northern.captain.quadronia.game.events.EGameResumeNow;
import northern.captain.quadronia.game.events.EModalClosed;
import northern.captain.quadronia.game.events.EModalOpened;
import northern.captain.quadronia.game.events.EOpenShop;
import northern.captain.quadronia.gfx.GraphicsInitContext;
import northern.captain.quadronia.gfx.TopBackgroundWidget;
import northern.captain.quadronia.gfx.widget.GameFieldWidget;

/**
 * Created by leo on 31.08.15.
 */
public class ScreenGame extends ScreenBase
{
    class LoadBoardIds extends LoadIds
    {
        int particle1;
        int particle2;
        int bgAtlasId;
    }
    private LoadBoardIds load = new LoadBoardIds();
    private XMLLayoutLoader layXml;
    private TextureAtlas atlas;
    private TopBackgroundWidget topBackgroundWidget = new TopBackgroundWidget();
    private GameFieldWidget gameFieldWidget = new GameFieldWidget();

    private Game game;
    private Config config;

    public ScreenGame(IScreenWorkflow screenFlow)
    {
        super(screenFlow);
    }

    @Override
    public void show()
    {
        super.show();
        ResLoader.singleton().finishLoading();
        NCore.busRegister(this);
    }

    @Override
    public void resize(int width, int height)
    {
        super.resize(width, height);
        stage.clear();
        Stack frame = new Stack();
        frame.setName("gframe");
        NContext.current.setGameFrame(frame);

        IResLoader resLoader = ResLoader.singleton();
        atlas = resLoader.getLoaded(load.tatlasId);

        layXml = resLoader.getLoaded(load.layoutId);

        initScreenBack(layXml, atlas);

//        Image img = layXml.newImage("back", atlas);
//        img.setColor(1, 1, 1, 0.8f);
//
//        gameAreaTable.add(img).width(img.getWidth()).height(img.getHeight()).expandX();
//        gameAreaTable.row();
//        gameAreaTable.add(new Table()).expand();

        config = new Config(layXml);

        GraphicsInitContext gContext = new GraphicsInitContext(atlas);
        gameFieldWidget.initGraphics(layXml, gContext);
        stage.addActor(gameFieldWidget);
        initGame();
        initMusic();
    }

    /* (non-Javadoc)
         * @see northern.captain.seabattle.glx.screens.ScreenBase#dispose()
         */
    @Override
    public void dispose()
    {
        super.dispose();
        gameFieldWidget.dispose();
        game.dispose();
        ResLoader.singleton().unload(load.tatlasId);
        ResLoader.singleton().unload(load.layoutId);
        atlas = null;
        layXml = null;
        stopMusic();
        NCore.busUnregister(this);
    }

    @Override
    public boolean onBackAction()
    {
        if(!game.isOver)
        {
            onExitAttempt(null);
            return false;
        }
        game.getContext().setDatai(IGameContext.LAST_SCREEN, IScreenWorkflow.STATE_MAIN_MENU);
        game.getContext().saveToDisk();
        return super.onBackAction();
    }

    @Subscribe
    public void onExitAttempt(EGameExitRequest event) {
        NCore.busPost(new EGameResumeNow());
        BaseDialog dialog = new LeaveGameDialog(game);
        dialog.show();
    }

    @Subscribe
    public void onShopOpen(EOpenShop event)
    {
        saveGameProgress();
        game.getContext().saveToDisk();
        NCore.instance().getSoundman().stopAllSounds();
        flow.prepare(IScreenWorkflow.WF_CHANGE);
        NContext.current.post(()->flow.doAction(IScreenWorkflow.WF_CHANGE));
    }

    @Subscribe
    public void onModalOpened(EModalOpened event)
    {
        game.doPause();
        gameFieldWidget.getPanelTimeOnly().pauseTimer();
        inputAllowed = false;
    }

    @Subscribe
    public void onModalClosed(EModalClosed event)
    {
        if(event.dialog instanceof LeaveGameDialog)
        {
            //End game -> back to main menu
            if(event.buttonPressed == 2)
            {
                backToMain();
            }

            //Save & exit
            if(event.buttonPressed == 1)
            {
                NCore.instance().doQuit();
            }

            if(event.buttonPressed == 0)
            {
                game.doResume();
                gameFieldWidget.getPanelTimeOnly().resumeTimer();
                inputAllowed = true;
            }
            return;
        }
    }

    private void backToMain()
    {
        inputAllowed = true;
        game.getContext().setDatai(IGameContext.LAST_SCREEN, IScreenWorkflow.STATE_MAIN_MENU);
        game.getContext().onGameOver(game.getGameMode());
        NCore.instance().getSoundman().stopAllSounds();
        flow.prepare(IScreenWorkflow.WF_BACK_AUTO);
        NContext.current.post(()->flow.doAction(IScreenWorkflow.WF_BACK_AUTO));
    }

    public void saveGameProgress()
    {
        String savedStateString = null;
        if(!game.isOver)
        {
            JSONObject json = game.serializeJSON();
            json.put("tim", gameFieldWidget.getPanelTimeOnly().serializeJSON());
            savedStateString = json.toString();
        }
        game.getContext().setSavedGame(savedStateString);
    }

    @Override
    public void onFullSuspend()
    {
        if(game != null)
        {
            if (!game.isOver)
            {
                game.getContext().setDatai(IGameContext.LAST_SCREEN, IScreenWorkflow.STATE_BATTLE);
            }
            saveGameProgress();
        }

        super.onFullSuspend();
    }

    private void initGame()
    {
        game = new Game(config);
        game.start();
        game.getContext().setDatai(IGameContext.LAST_SCREEN, IScreenWorkflow.STATE_BATTLE);
    }

    /* (non-Javadoc)
     * @see northern.captain.seabattle.glx.screens.ScreenBase#prepareEnter()
     */
    @Override
    public void prepareEnter()
    {
        super.prepareEnter();
        load.tatlasId = ResLoader.singleton().loadTextureAtlas("game-atlas/atlas");
        load.layoutId = ResLoader.singleton().loadLayout("game");
    }

    @Override
    protected void initBackgroundImage(XMLLayoutLoader layXml, TextureAtlas tatlas, XMLContentLoader.Node screenNode)
    {
//        backgroundWidget.setFillParent(true);
//        stage.addActor(backgroundWidget);
    }

    private boolean inputAllowed = true;

    @Override
    public boolean doDrag(int fx, int fy)
    {
        if(!inputAllowed) return false;

        Vector2i vec = screenToWorld(fx, fy);

        return gameFieldWidget.doDrag(vec.x, vec.y);
    }

    @Override
    public boolean doTouchDown(int fx, int fy)
    {
        if(!inputAllowed) return false;

        if(game.isOver)
        {
            backToMain();
            return false;
        }

        Vector2i vec = screenToWorld(fx, fy);

        return gameFieldWidget.doTouchDown(vec.x, vec.y);
    }

    @Override
    public boolean doTouchUp(int fx, int fy)
    {
        if(!inputAllowed) return false;

        Vector2i vec = screenToWorld(fx, fy);

        return gameFieldWidget.doTouchUp(vec.x, vec.y);
    }

    private int[] musics = {
            ISoundMan.MUS_GAME1 << 16 | 104,
            ISoundMan.MUS_GAME2 << 16 | 89,
            ISoundMan.MUS_GAME3 << 16 | 110,
            ISoundMan.MUS_GAME4 << 16 | 95,
            ISoundMan.MUS_GAME5 << 16 | 121,
    };

    private int musicIdx = 0;

    public void initMusic() {
        musicIdx = 0;
        for(int i=0;i<5;i++) {
            int idx = (int)(Math.random()*10.0) % musics.length;
            int val = musics[0];
            musics[0] = musics[idx];
            musics[idx] = val;
        }
        playMusic();
    }

    private Runnable musicRun = this::playMusic;

    public void playMusic() {
        int mus = musics[musicIdx % musics.length];
        NCore.instance().getSoundman().playMusic(mus >> 16);
        NContext.current.postDelayed(musicRun, (mus & 0xffff)*1000);
        musicIdx++;
    }

    private void stopMusic() {
        NContext.current.removePosted(musicRun);
        NCore.instance().getSoundman().stopMusic();
    }
}
