package northern.captain.quadronia;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

import northern.captain.gamecore.glx.ISoundMan;
import northern.captain.gamecore.glx.NContext;
import northern.captain.gamecore.glx.NCore;
import northern.captain.gamecore.glx.res.SharedRes;
import northern.captain.gamecore.glx.setup.ISetupValue;
import northern.captain.gamecore.glx.setup.SetupVisual;
import northern.captain.gamecore.glx.tools.Animations;
import northern.captain.gamecore.glx.tools.ClickListenerPrepared;
import northern.captain.gamecore.glx.tools.loaders.IResLoader;
import northern.captain.gamecore.glx.tools.loaders.ResLoader;
import northern.captain.gamecore.glx.tools.loaders.XMLLayoutLoader;
import northern.captain.gamecore.gplus.GoogleGamesFactory;
import northern.captain.gamecore.gplus.IGoogleGamesProcessor;
import northern.captain.quadronia.game.events.EOptionsMenu;
import northern.captain.tools.IOnBackAction;

public class SetupDialog extends ClickListener implements Runnable, IOnBackAction
{
	public SetupDialog()
	{
	}


    protected Window win;
    protected Table tbl;

    protected static boolean isOpened = false;


    public void dismiss()
    {
        isOpened = false;
        NCore.instance().popOnBackAction(this);
        win.clearActions();
        win.addAction(new Animations.DropDownOutOfScreen().create(0, Actions.run(dismissRun)));
    }

    protected Runnable dismissRun = ()->hide();

    public void hide()
    {
        tbl.remove();
        win.remove();
        tbl = null;
        win = null;
        textureAtlas = null;

        NCore.instance().popOnBackAction(this);
        NContext.current.subRefresh();
        NCore.busPost(new EOptionsMenu(false));
    }

    public void show()
    {
        if(isOpened)
            return;

        create();

        win.addAction(new Animations.PopUpOutOfScreen().create(0));
        NCore.instance().pushOnBackAction(this);

        isOpened = true;
        NCore.busPost(new EOptionsMenu(isOpened));
    }

    protected TextureAtlas textureAtlas;


    protected void create()
    {
        IResLoader loader = ResLoader.singleton();
        int layId = loader.loadLayout("setup_dlg");
        loader.finishLoading();
        TextureAtlas atlas = SharedRes.instance.getCommonAtlas();

        XMLLayoutLoader xmlLoader = loader.getLoaded(layId);
        textureAtlas = atlas;

        win = xmlLoader.newWindow("dialog", atlas);

        float padding = NContext.current.fScale(15);

        final IGameOptionsMenu options = NCore.instance().getGameOptionsMenu();

        {
            Label lbl = xmlLoader.newLabel("vers");
            win.add(lbl).expandX().top().right().colspan(3).padRight(lbl.getX()).padTop(lbl.getY());
            lbl.setText(NCore.instance().versionName);
            win.row();
        }

        {
            SetupVisual vis = xmlLoader.newSetupVisual("sound", textureAtlas);
            win.add(vis).expand().center().width(vis.getWidth()).height(vis.getHeight()).pad(padding);

            vis.assignValue(new ISetupValue()
            {
                @Override
                public boolean getBValue()
                {
                    return options.isSoundOn();
                }

                @Override
                public void setBValue(boolean newValue, SetupVisual visual)
                {
                    if (options.isSoundOn() != newValue)
                        options.flipSoundOption();
                }

                @Override
                public void activateAction(SetupVisual visual)
                {
                    dismiss();
                }
            });
        }

        {
            SetupVisual vis = xmlLoader.newSetupVisual("music", textureAtlas);
            win.add(vis).expand().center().width(vis.getWidth()).height(vis.getHeight()).pad(padding);

            vis.assignValue(new ISetupValue()
            {
                @Override
                public boolean getBValue()
                {
                    return options.isMusicOn();
                }

                @Override
                public void setBValue(boolean newValue, SetupVisual visual)
                {
                    if(options.isMusicOn() != newValue)
                        options.flipMusinOption();
                }

                @Override
                public void activateAction(SetupVisual visual)
                {
                    dismiss();
                }
            });
        }

        {
            SetupVisual vis = xmlLoader.newSetupVisual("vibro", textureAtlas);
            win.add(vis).expand().center().width(vis.getWidth()).height(vis.getHeight()).pad(padding);
            vis.assignValue(new ISetupValue()
            {
                @Override
                public boolean getBValue()
                {
                    return options.isVibrationOn();
                }

                @Override
                public void setBValue(boolean newValue, SetupVisual visual)
                {
                    if(options.isVibrationOn() != newValue)
                        options.flipVibroOption();
                }

                @Override
                public void activateAction(SetupVisual visual)
                {
                    dismiss();
                }
            });
        }

        win.row();

//        {
//            SetupVisual vis = xmlLoader.newSetupVisual("lang", textureAtlas);
//            win.add(vis).expand().center().width(vis.getWidth()).height(vis.getHeight()).pad(padding);
//            vis.assignValue(new ISetupValue()
//            {
//                @Override
//                public boolean getBValue()
//                {
//                    return true;
//                }
//
//                @Override
//                public void setBValue(boolean newValue, SetupVisual visual)
//                {
//                }
//
//                @Override
//                public void activateAction(SetupVisual visual)
//                {
//                    NCore.instance().postRunnableOnMain(new Runnable()
//                    {
//                        @Override
//                        public void run()
//                        {
//                            options.showLangChooser();
//                        }
//                    });
//                    dismiss();
//                }
//            });
//        }
//        {
//            SetupVisual vis = xmlLoader.newSetupVisual("sbar", textureAtlas);
//            win.add(vis).expand().center().width(vis.getWidth()).height(vis.getHeight()).pad(padding);
//            vis.assignValue(new ISetupValue()
//            {
//                @Override
//                public boolean getBValue()
//                {
//                    return options.isStatusBarOn();
//                }
//
//                @Override
//                public void setBValue(boolean newValue, SetupVisual visual)
//                {
//                    if(options.isStatusBarOn() != newValue)
//                        options.flipStatusBarOption();
//                }
//
//                @Override
//                public void activateAction(SetupVisual visual)
//                {
//                    dismiss();
//                }
//            });
//        }
//
//        win.row();
        {
            SetupVisual vis = xmlLoader.newSetupVisual("feed", textureAtlas);
            win.add(vis).expand().center().width(vis.getWidth()).height(vis.getHeight()).pad(padding);
            vis.assignValue(new ISetupValue()
            {
                @Override
                public boolean getBValue()
                {
                    return true;
                }

                @Override
                public void setBValue(boolean newValue, SetupVisual visual)
                {
                }

                @Override
                public void activateAction(SetupVisual visual)
                {
                    NCore.instance().postRunnableOnMain(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            options.openFeedback();
                        }
                    });
                    dismiss();
                }
            });
        }

        {
            SetupVisual vis = xmlLoader.newSetupVisual("tutorial", textureAtlas);
            win.add(vis).expand().center().width(vis.getWidth()).height(vis.getHeight()).pad(padding);
            vis.assignValue(new ISetupValue()
            {
                @Override
                public boolean getBValue()
                {
                    return options.isTutorialOn();
                }

                @Override
                public void setBValue(boolean newValue, SetupVisual visual)
                {
                    if(options.isTutorialOn() != newValue)
                        options.flipTutorial();
                }

                @Override
                public void activateAction(SetupVisual visual)
                {
                    dismiss();
                }
            });
        }


//        {
//            SetupVisual vis = xmlLoader.newSetupVisual("share", textureAtlas);
//            win.add(vis).expand().center().width(vis.getWidth()).height(vis.getHeight()).pad(padding);
//            vis.assignValue(new ISetupValue()
//            {
//                @Override
//                public boolean getBValue()
//                {
//                    return true;
//                }
//
//                @Override
//                public void setBValue(boolean newValue, SetupVisual visual)
//                {
//                }
//
//                @Override
//                public void activateAction(SetupVisual visual)
//                {
//                    ShareManager.instance().doShareMyProgress();
//                    dismiss();
//                }
//            });
//        }
        {
            SetupVisual vis = xmlLoader.newSetupVisual("gplus", textureAtlas);
            win.add(vis).expand().center().width(vis.getWidth()).height(vis.getHeight()).pad(padding);
            vis.assignValue(new ISetupValue()
            {
                @Override
                public boolean getBValue()
                {
                    return GoogleGamesFactory.instance().getProcessor().isSignedIn();
                }

                @Override
                public void setBValue(boolean newValue, SetupVisual visual)
                {
                }

                @Override
                public void activateAction(SetupVisual visual)
                {
                    NContext.current.postDelayed(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            NCore.instance().postRunnableOnMain(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    IGoogleGamesProcessor processor = GoogleGamesFactory.instance().getProcessor();

                                    if (!processor.isSignedIn())
                                    {
                                        processor.doSignIn(null);
                                    } else
                                    {
                                        processor.doSignOut();
                                    }
                                }
                            });

                        }
                    }, 500);

                    dismiss();
                }
            });
        }


//        win.setsetTitleAlignment(Align.left|Align.center);
        win.pack();
        win.center();
        win.setModal(true);
        win.setClip(true);
        win.getColor().a = 0;
        win.setKeepWithinStage(false);

        loader.unload(layId);

        placeOnTheScreen(win);
    }

    protected void placeOnTheScreen(Table win)
    {
        tbl = new Table();
        tbl.setFillParent(true);
        tbl.add(win).center().bottom().padTop(NContext.current.screenHeight - win.getHeight());

        tbl.setBackground(new TextureRegionDrawable(
                SharedRes.instance.getCommonAtlas().findRegion("semidark")));

        NContext.current.currentStage.addActor(tbl);
        tbl.pack();
    }

    protected ClickListenerPrepared clickedObj;
    /* (non-Javadoc)
     * @see com.badlogic.gdx.scenes.scene2d.utils.ClickListener#clicked(com.badlogic.gdx.scenes.scene2d.InputEvent, float, float)
     */
    @Override
    public void clicked(InputEvent event, float x, float y)
    {
        NCore.instance().getSoundman().playSound(ISoundMan.SND_JUST_CLICK, true);
        super.clicked(event, x, y);
    }

    @Override
    public void run()
    {
        if(clickedObj != null)
        {
            InputEvent evt = new InputEvent();
            clickedObj.clicked(evt, 0, 0);
        }
        dismiss();
    }

    @Override
    public void OnBackPressed()
    {
        dismiss();
    }
}
