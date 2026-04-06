package northern.captain.quadronia.screens;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.squareup.otto.Subscribe;

import java.util.Objects;

import northern.captain.gamecore.glx.ISoundMan;
import northern.captain.gamecore.glx.NContext;
import northern.captain.gamecore.glx.NCore;
import northern.captain.gamecore.glx.screens.IScreenWorkflow;
import northern.captain.gamecore.glx.screens.ScreenBase;
import northern.captain.gamecore.glx.screens.ScreenContext;
import northern.captain.gamecore.glx.tools.loaders.ResLoader;
import northern.captain.gamecore.glx.tools.loaders.XMLLayoutLoader;
import northern.captain.quadronia.game.Engine;
import northern.captain.quadronia.game.core.Game;
import northern.captain.quadronia.game.events.EOptionsChanged;
import northern.captain.quadronia.gfx.GraphicsInitContext;
import northern.captain.quadronia.gfx.widget.PagerWidget;
import northern.captain.tools.RateTracker;
import northern.captain.tools.analytics.AnalyticsFactory;

public class ScreenMainCarousel extends ScreenBase
{
    private LoadIds load = new LoadIds();
    private TextureAtlas atlas;
    private XMLLayoutLoader layXml;
    private SubScreenMenu menu;

    private SubScreenMockup info, info2;
    private SubScreenSettings settings;
    private SubScreenShop shop;

    private PagerWidget pager = new PagerWidget("bot", 5);

    public ScreenMainCarousel(IScreenWorkflow screenFlow)
    {
        super(screenFlow);
    }

    @Override
    public void show()
    {
        super.show();
        ResLoader.singleton().finishLoading();
    }

    @Override
    public void resize(int width, int height)
    {
        super.resize(width, height);
        stage.clear();

        atlas = ResLoader.singleton().getLoaded(load.tatlasId);
        layXml = ResLoader.singleton().getLoaded(load.layoutId);

        initScreenBack(layXml, atlas);

        Group maingrp = new Group();

        menu = new SubScreenMenu();
        settings = new SubScreenSettings();
        shop = new SubScreenShop();
        info = new SubScreenMockup("info_");
        info2 = new SubScreenMockup("info_");

        Stack frame = new Stack();
        frame.setName("gframe");
        NContext.current.setGameFrame(frame);

        GraphicsInitContext gContext = new GraphicsInitContext(atlas);

        menu.initGraphics(layXml, gContext);
        maingrp.addActor(menu);
        pager.setPage(2, menu);

        info2.initGraphics(layXml, gContext);
        maingrp.addActor(info2);
        info2.setOnClick(()-> RateTracker.instance().showMarket());
        pager.setPage(0, info2);

        settings.initGraphics(layXml, gContext);
//        settings.setOnClick(()-> pager.scrollTo(2));
        maingrp.addActor(settings);
        pager.setPage(1, settings);

        shop.initGraphics(layXml, gContext);
        maingrp.addActor(shop);
        pager.setPage(3, shop);

        info.initGraphics(layXml, gContext);
        info.setOnClick(()-> RateTracker.instance().showMarket());
        maingrp.addActor(info);
        pager.setPage(4, info);

        menu.setOnPlay(() -> {
            AnalyticsFactory.instance().getAnalytics().registerButtonAction("game");
            screenFadeOut(()->doPlay(menu.getGameType()));
        });

        menu.setOnSettings(() -> pager.scrollTo(1))
                .setOnInfo(() -> pager.scrollTo(0))
                .setOnShop(() -> pager.scrollTo(3));

        pager.initGraphics(layXml, gContext);
        maingrp.addActor(pager);
        pager.setCurrent(2);

        maingrp.setX(NContext.current.gameAreaDeltaX);
        stage.addActor(maingrp);

        NContext.current.postDelayed(musicRun, 500);

        NCore.busRegister(this);
    }

    /* (non-Javadoc)
     * @see northern.captain.seabattle.glx.screens.ScreenBase#dispose()
     */
    @Override
    public void dispose()
    {
        super.dispose();
        ResLoader.singleton().unload(load.tatlasId);
        ResLoader.singleton().unload(load.layoutId);
        NCore.busUnregister(this);
    }

    /* (non-Javadoc)
     * @see northern.captain.seabattle.glx.screens.ScreenBase#prepareEnter()
     */
    @Override
    public void prepareEnter()
    {
        super.prepareEnter();
        load.tatlasId = ResLoader.singleton().loadTextureAtlas("virtual-atlas/atlas");
        load.layoutId = ResLoader.singleton().loadLayout("vscreens");
    }

    private Runnable musicRun = this::musicRunCall;

    @Override
    public void deactivate(ScreenContext sctx)
    {
        super.deactivate(sctx);
        stopMusicRun();
    }

    private void stopMusicRun() {
        NContext.current.removePosted(musicRun);
        NCore.instance().getSoundman().stopMusic();
    }

    private void musicRunCall()
    {
        NCore.instance().getSoundman().playMusic(ISoundMan.MUS_MENU);
        NContext.current.postDelayed(musicRun, 56100);
    }

    private void doQuit()
    {
        NCore.instance().doQuit();
    }

    private long backTime = 0;

    @Override
    public boolean onBackAction()
    {
        super.onBackAction();
        long time = System.currentTimeMillis();

        if(time - backTime < 1000)
        {
            screenFadeOut(()->doQuit());
        }

        backTime = time;
        return false;
    }

    private void doPlay(int type)
    {
        Game.defaultGameContext.setGameType(type);
        flow.prepare(IScreenWorkflow.WF_NEW_GAME);
        flow.doAction(IScreenWorkflow.WF_NEW_GAME);
    }

    public void initValues()
    {
        this.menu.initValues();
    }

    @Subscribe
    public void onOptionsChanged(EOptionsChanged event) {
        if ("music".equals(event.optionName)) {
            if (event.optionsMenu.isMusicOn()) musicRunCall();
            else stopMusicRun();
        }
    }
}
