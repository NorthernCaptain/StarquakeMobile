package northern.captain.quadronia.screens;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.squareup.otto.Subscribe;

import northern.captain.quadronia.game.Engine;
import northern.captain.quadronia.game.IGameContext;
import northern.captain.quadronia.game.ItemFactory;
import northern.captain.quadronia.game.core.Game;
import northern.captain.quadronia.game.events.EUserUpdated;
import northern.captain.quadronia.gfx.GraphicsInitContext;
import northern.captain.quadronia.gfx.MainMenuWidget;
import northern.captain.quadronia.gfx.MainUserInfoWidget;
import northern.captain.gamecore.glx.ISoundMan;
import northern.captain.gamecore.glx.NContext;
import northern.captain.gamecore.glx.NCore;
import northern.captain.gamecore.glx.res.SharedRes;
import northern.captain.gamecore.glx.screens.IScreenWorkflow;
import northern.captain.gamecore.glx.screens.ScreenBase;
import northern.captain.gamecore.glx.screens.ScreenContext;
import northern.captain.gamecore.glx.tools.Animations;
import northern.captain.gamecore.glx.tools.ButtonImage;
import northern.captain.gamecore.glx.tools.ClickListenerPrepared;
import northern.captain.gamecore.glx.tools.Vector2i;
import northern.captain.gamecore.glx.tools.loaders.ResLoader;
import northern.captain.gamecore.glx.tools.loaders.XMLLayoutLoader;
import northern.captain.gamecore.gplus.GoogleGamesFactory;
import northern.captain.tools.AnimButtonSet;
import northern.captain.tools.analytics.AnalyticsFactory;
import northern.captain.tools.sharing.ShareManager;

/**
 * Created by leo on 24.02.15.
 */
public class ScreenMainMenu extends ScreenBase
{
    private LoadIds load = new LoadIds();
    private AnimButtonSet anim;
    private AnimButtonSet animGoogle;

    private MainMenuWidget mainMenuWidget;
    private MainUserInfoWidget mainUserInfoWidget;
    private Label modeLbl;
    private Label scoreLbl;
    private Label fragLbl;
    private Label levelLbl;

    public ScreenMainMenu(IScreenWorkflow screenFlow)
    {
        super(screenFlow);

        mainMenuWidget = new MainMenuWidget(Game.defaultGameContext);
        mainUserInfoWidget = new MainUserInfoWidget(Game.defaultGameContext, screenFlow);
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

        TextureAtlas commonAtlas = SharedRes.instance.getCommonAtlas();

        TextureAtlas atlas = ResLoader.singleton().getLoaded(load.tatlasId);

        XMLLayoutLoader layXml = ResLoader.singleton().getLoaded(load.layoutId);

        initScreenBack(layXml, commonAtlas);

        Stack frame = new Stack();
        frame.setName("gframe");
        NContext.current.setGameFrame(frame);

        anim = new AnimButtonSet();
        anim.setOnInitialAnimation(new Animations.ScaleZeroFullElastic(1f));
        anim.setOnPressAnimation(new Animations.ScaleUp13());
        anim.setOnReleaseAnimation(new Animations.ScaleDown1());
//        anim.setOnClickedAnimation(new Animations.MoveOutsideRight());
//        anim.setOnOtherClickedAnimation(new Animations.MoveOutsideLeft());

        animGoogle = new AnimButtonSet();
        animGoogle.setOnInitialAnimation(new Animations.ScaleZeroFullElastic(1f));
        animGoogle.setOnPressAnimation(new Animations.ScaleUp13());
        animGoogle.setOnReleaseAnimation(new Animations.ScaleDown1());
        animGoogle.setDeltaDelay(0.3f);

        Table backT = new Table();
        frame.add(backT);
        backT.setFillParent(true);
        gameAreaTable.add(frame).expand().fill();

        {
            Image img = layXml.newImage("ubar", atlas);
            backT.add(img).expandX().fillX().top().padTop(img.getY()).colspan(2).height(img.getHeight());
            backT.row();
            img.setColor(1, 1, 1, 0.7f);

            Table but;
            but = layXml.newImageButton("ibut_new", atlas);
            backT.add(but).expandX().top().right().padRight(but.getX()).padTop(but.getY()).maxHeight(but.getHeight()).maxWidth(but.getWidth());
            but.setTransform(true);
            anim.add(0, but, new ClickListenerPrepared()
            {

                @Override
                public boolean prepareClicked(InputEvent evt)
                {
                    return true;
                }

                /* (non-Javadoc)
                 * @see com.badlogic.gdx.scenes.scene2d.utils.ClickListener#clicked(com.badlogic.gdx.scenes.scene2d.InputEvent, float, float)
                 */
                @Override
                public void clicked(InputEvent event, float x, float y)
                {
                    setLogoAlpha(1);
                    AnalyticsFactory.instance().getAnalytics().registerButtonAction("game");
                    screenFadeOut
                    (()->doPlay(Engine.TYPE_ARCADE));
                }

            });
        }
        {
            Table but;
            but = layXml.newImageButton("ibut_new2", atlas);
            backT.add(but).expandX().top().left().padLeft(but.getX()).padTop(but.getY()).maxHeight(but.getHeight()).maxWidth(but.getWidth());
            but.setTransform(true);
            backT.row();
            anim.add(0, but, new ClickListenerPrepared()
            {

                @Override
                public boolean prepareClicked(InputEvent evt)
                {
                    return true;
                }

                /* (non-Javadoc)
                 * @see com.badlogic.gdx.scenes.scene2d.utils.ClickListener#clicked(com.badlogic.gdx.scenes.scene2d.InputEvent, float, float)
                 */
                @Override
                public void clicked(InputEvent event, float x, float y)
                {
                    setLogoAlpha(1);
                    AnalyticsFactory.instance().getAnalytics().registerButtonAction("game");
                    screenFadeOut
                            (()->doPlay(Engine.TYPE_EXPRESS));
                }

            });
        }
        {
            Table but;
            but = layXml.newImageButton("ibut_new3", atlas);
            backT.add(but).expandX().top().right().padRight(but.getX()).padTop(but.getY()).maxHeight(but.getHeight()).maxWidth(but.getWidth());
            but.setTransform(true);
            anim.add(0, but, new ClickListenerPrepared()
            {

                @Override
                public boolean prepareClicked(InputEvent evt)
                {
                    return true;
                }

                /* (non-Javadoc)
                 * @see com.badlogic.gdx.scenes.scene2d.utils.ClickListener#clicked(com.badlogic.gdx.scenes.scene2d.InputEvent, float, float)
                 */
                @Override
                public void clicked(InputEvent event, float x, float y)
                {
                    NContext.current.postDelayed(()->GoogleGamesFactory.instance().getProcessor().openLeaderboard(), 800);
                }

            });
        }

        {
            Table but;
            but = layXml.newImageButton("ibut_quit", atlas);
            backT.add(but).expandX().top().left().padLeft(but.getX()).padTop(but.getY()).maxHeight(but.getHeight()).maxWidth(but.getWidth());
            but.setTransform(true);
            backT.row();
            anim.add(1, but, new ClickListenerPrepared()
            {

                @Override
                public boolean prepareClicked(InputEvent evt)
                {
                    return true;
                }

                /* (non-Javadoc)
                 * @see com.badlogic.gdx.scenes.scene2d.utils.ClickListener#clicked(com.badlogic.gdx.scenes.scene2d.InputEvent, float, float)
                 */
                @Override
                public void clicked(InputEvent event, float x, float y)
                {
                    NContext.current.postDelayed(()->GoogleGamesFactory.instance().getProcessor().openAchievements(), 800);
                }

            });
            backT.add(new Table()).expand();
        }

        {
            Table frontT = new Table();
            frame.add(frontT);
            frontT.setFillParent(true);

            Image img = layXml.newImage("toplogo", atlas);
            frontT.add(img).expandX().center().padTop(img.getY()).top().width(img.getWidth()).height(img.getHeight());
            frontT.row();

            Label lb = modeLbl = layXml.newLabel("modelbl");
            frontT.add(lb).expandX().right().padRight(lb.getX()).top().padTop(lb.getY());
            frontT.row();

            lb = scoreLbl = layXml.newLabel("scorelbl");
            frontT.add(lb).expandX().right().padRight(lb.getX()).top().padTop(lb.getY());
            frontT.row();

            lb = fragLbl = layXml.newLabel("fraglbl");
            frontT.add(lb).expandX().right().padRight(lb.getX()).top().padTop(lb.getY());
            frontT.row();

            lb = levelLbl = layXml.newLabel("lvllbl");
            frontT.add(lb).expandX().right().padRight(lb.getX()).top().padTop(lb.getY());
            frontT.row();
            frontT.add(new Table()).expand().fill();

        }

        {
            Table frontT = new Table();
            frame.add(frontT);
            frontT.setFillParent(true);

            {
                ButtonImage but = layXml.newImageButton("bshare", atlas);
                frontT.add(but).expand().left().bottom().padBottom(but.getY()).padLeft(but.getX()).height(but.getHeight()).width(but.getWidth());
                animGoogle.add(3, but, new ClickListenerPrepared()
                    {
                        @Override
                        public boolean prepareClicked(InputEvent evt)
                        {
                            return true;
                        }

                        @Override
                        public void clicked(InputEvent event, float x, float y)
                        {
                            NContext.current.postDelayed(()->ShareManager.instance().doShareMyProgress(0), 800);
                        }
                    }
                );

                but.setTransform(true);
            }
            {
                ButtonImage but = layXml.newImageButton("bopt", atlas);
                frontT.add(but).expand().right().bottom().padBottom(but.getY()).padRight(but.getX()).height(but.getHeight()).width(but.getWidth());
                animGoogle.add(4, but, new ClickListenerPrepared()
                    {
                        @Override
                        public boolean prepareClicked(InputEvent evt)
                        {
                            return true;
                        }

                        @Override
                        public void clicked(InputEvent event, float x, float y)
                        {
                            NContext.current.postDelayed(()->NCore.instance().openOptionsMenu(), 800);
                        }
                    }
                );

                but.setTransform(true);
                frontT.row();
            }
        }

        GraphicsInitContext gContext = new GraphicsInitContext(SharedRes.instance.getCommonAtlas());
        mainMenuWidget.initGraphics(layXml, gContext);
//        stage.addActor(mainMenuWidget);
        mainMenuWidget.setEnabled(false);

        mainUserInfoWidget.setDeltaScreenY(this.deltaGameAreaY);
        mainUserInfoWidget.initGraphics(layXml, gContext);
        stage.addActor(mainUserInfoWidget);

        initValues();

        screenFadeIn();
        mainTable.pack();
        anim.startInitialAnimation(1);
        animGoogle.startInitialAnimation(1.3f);
        NContext.current.postDelayed(musicRun, 500);

        NCore.busRegister(this);
    }

    public void initValues()
    {
        IGameContext ctx = Game.defaultGameContext;
        int mode =  ctx.getLastMode();

        switch (mode)
        {
            case Game.TYPE_ARCADE:
                modeLbl.setText("Classic");
                break;
            case Game.TYPE_EXPRESS:
                modeLbl.setText("Express 5");
                break;
        }
        scoreLbl.setText(Integer.toString(ctx.getMaxScore(mode)));
        fragLbl.setText(Integer.toString(ctx.getMaxFragments(mode)));
        levelLbl.setText(Integer.toString(ctx.getMaxLevel(mode)));

        mainUserInfoWidget.initValues();
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
        anim.clearAll();
        anim = null;
        NCore.busUnregister(this);
    }



    /* (non-Javadoc)
     * @see northern.captain.seabattle.glx.screens.ScreenBase#prepareEnter()
     */
    @Override
    public void prepareEnter()
    {
        super.prepareEnter();
        load.tatlasId = ResLoader.singleton().loadTextureAtlas("mainmenu-atlas/atlas");
        load.layoutId = ResLoader.singleton().loadLayout("mainmenu");
    }

    private void doQuit()
    {
        NCore.instance().doQuit();
    }

    private void doPlay(int type)
    {
        Game.defaultGameContext.setGameType(type);
        flow.prepare(IScreenWorkflow.WF_NEW_GAME);
        flow.doAction(IScreenWorkflow.WF_NEW_GAME);
    }

    @Override
    public boolean doTouchDown(int fx, int fy)
    {
        Vector2i vec = screenToWorld(fx, fy);
        if(mainMenuWidget.doTouchDown(vec.x, vec.y)) return true;
        if(mainUserInfoWidget.doTouchDown(vec.x, vec.y)) return true;

        return false;
    }

    @Override
    public boolean doTouchUp(int fx, int fy)
    {
        Vector2i vec = screenToWorld(fx, fy);
        if(mainMenuWidget.doTouchUp(vec.x, vec.y))
        {
            initValues();
            return true;
        }
        if(mainUserInfoWidget.doTouchUp(vec.x, vec.y))
        {
            return true;
        }
        return false;
    }

    private Runnable musicRun = new Runnable()
    {
        @Override
        public void run()
        {
            NCore.instance().getSoundman().playMusic(ISoundMan.MUS_MENU);
            NContext.current.postDelayed(musicRun, 55000);
        }
    };

    @Override
    public void deactivate(ScreenContext sctx)
    {
        super.deactivate(sctx);
        NContext.current.removePosted(musicRun);
        NCore.instance().getSoundman().stopMusic();
    }

    long backTime = 0;

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

    @Subscribe
    public void onUserUpdated(EUserUpdated event)
    {
        initValues();
    }
}
