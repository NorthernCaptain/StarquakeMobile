package northern.captain.quadronia;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

import northern.captain.gamecore.glx.ISoundMan;
import northern.captain.gamecore.glx.NContext;
import northern.captain.gamecore.glx.NCore;
import northern.captain.gamecore.glx.res.SharedRes;
import northern.captain.gamecore.glx.tools.Animations;
import northern.captain.gamecore.glx.tools.ClickListenerPrepared;
import northern.captain.gamecore.glx.tools.loaders.IResLoader;
import northern.captain.gamecore.glx.tools.loaders.ResLoader;
import northern.captain.gamecore.glx.tools.loaders.XMLLayoutLoader;
import northern.captain.quadronia.game.events.EModalClosed;
import northern.captain.quadronia.game.events.EModalOpened;
import northern.captain.tools.IOnBackAction;

public abstract class BaseDialog extends ClickListener implements Runnable, IOnBackAction
{
    protected String titleResName="titlelbl";
    protected String xmlName = "dlg";

    protected int clickedButton=0;

	public BaseDialog()
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
        win.addAction(new Animations.TVScaleDown(0.5f).create(0, Actions.run(dismissRun)));
        tbl.addAction(Animations.seq(Actions.delay(0.4f), Actions.alpha(0.0f, 0.2f)));

//        win.addAction(new Animations.DropDownOutOfScreen().create(0, Actions.run(dismissRun)));
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
        NCore.busPost(new EModalClosed(this, clickedButton));
    }

    public void show()
    {
        if(isOpened)
            return;

        create();

        tbl.addAction(Animations.seq(Actions.fadeOut(Animations.MIN_DUR, Animations.ALWAYS_ONE),
            Actions.alpha(1f, 0.2f)));
        win.addAction(new Animations.TVScaleUp(0.5f).create(0.2f));
        NCore.instance().pushOnBackAction(this);

        isOpened = true;
        NCore.busPost(new EModalOpened());
    }

    protected TextureAtlas textureAtlas;


    protected void create()
    {
        IResLoader loader = ResLoader.singleton();
        int layId = loader.loadLayout(xmlName);
        loader.finishLoading();
        TextureAtlas atlas = SharedRes.instance.getCommonAtlas();

        XMLLayoutLoader xmlLoader = loader.getLoaded(layId);
        textureAtlas = atlas;

        win = xmlLoader.newWindow("dialog", atlas);

        createBody(xmlLoader, win);

        boolean needFirst = isNeedFirst();

        {
            final Table tbl = createFirst(xmlLoader, win, needFirst);

            if(tbl != null)
            {
                tbl.setTouchable(Touchable.enabled);
                tbl.addListener(new ClickListenerPrepared()
                {
                    @Override
                    public boolean prepareClicked(InputEvent evt)
                    {
                        return true;
                    }

                    @Override
                    public boolean touchDown(InputEvent event, float x, float y, int pointer, int button)
                    {
                        tbl.setColor(1, 1, 1, 0.65f);
                        NCore.instance().getSoundman().playSound(ISoundMan.SND_JUST_CLICK, true);
                        return super.touchDown(event, x, y, pointer, button);
                    }

                    @Override
                    public void touchUp(InputEvent event, float x, float y, int pointer, int button)
                    {
                        tbl.setColor(1, 1, 1, 1);
                        super.touchUp(event, x, y, pointer, button);
                    }

                    @Override
                    public void clicked(InputEvent event, float x, float y)
                    {
                        firstClicked();
                        super.clicked(event, x, y);
                    }
                });
            }
        }

        {
            final Table tbl = createSecond(xmlLoader, win, needFirst);

            tbl.setTouchable(Touchable.enabled);
            tbl.addListener(new ClickListenerPrepared()
            {
                @Override
                public boolean prepareClicked(InputEvent evt)
                {
                    return true;
                }

                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button)
                {
                    tbl.setColor(1,1,1, 0.65f);
                    NCore.instance().getSoundman().playSound(ISoundMan.SND_JUST_CLICK, true);
                    return super.touchDown(event, x, y, pointer, button);
                }

                @Override
                public void touchUp(InputEvent event, float x, float y, int pointer, int button)
                {
                    tbl.setColor(1,1,1,1);
                    super.touchUp(event, x, y, pointer, button);
                }

                @Override
                public void clicked(InputEvent event, float x, float y)
                {
                    secondClicked();
                    super.clicked(event, x, y);
                }
            });
        }

        win.setOrigin(0, 0);
        win.pack();
        win.center();
        win.setModal(true);
        win.setClip(true);
        win.getColor().a = 0;
        win.setKeepWithinStage(false);
        win.addListener(new InputListener()
        {
            /**
             * Called when a mouse button or a finger touch goes down on the actor. If true is returned, this listener will receive all
             * touchDragged and touchUp events, even those not over this actor, until touchUp is received. Also when true is returned, the
             * event is handled.
             *
             * @param event
             * @param x
             * @param y
             * @param pointer
             * @param button
             * @see InputEvent
             */
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button)
            {
                win.setModal(false);
                if(win.hit(x, y, true) == null)
                {
                    win.setModal(true);
                    return true;
                }
                win.setModal(true);
                return super.touchDown(event, x, y, pointer, button);
            }

            /**
             * Called when a mouse button or a finger touch goes up anywhere, but only if touchDown previously returned true for the mouse
             * button or touch. The touchUp event is always  handled.
             *
             * @param event
             * @param x
             * @param y
             * @param pointer
             * @param button
             * @see InputEvent
             */
            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button)
            {
                win.setModal(false);
                if(win.hit(x, y, true) == null)
                {
                    backClicked();
                    return;
                }
                win.setModal(true);
                super.touchUp(event, x, y, pointer, button);
            }
        });

        loader.unload(layId);

        placeOnTheScreen(win);
    }

    protected void placeOnTheScreen(Table win)
    {
        tbl = new Table();
        tbl.setFillParent(true);
        tbl.add(win).center();

        tbl.setBackground(new TextureRegionDrawable(
                SharedRes.instance.getCommonAtlas().findRegion("semidark")));

        NContext.current.currentStage.addActor(tbl);
        tbl.pack();
    }

    /* (non-Javadoc)
     * @see com.badlogic.gdx.scenes.scene2d.utils.ClickListener#clicked(com.badlogic.gdx.scenes.scene2d.InputEvent, float, float)
     */
    @Override
    public void clicked(InputEvent event, float x, float y)
    {
        super.clicked(event, x, y);
    }

    @Override
    public void run()
    {
        secondClicked();
    }

    @Override
    public void OnBackPressed()
    {
        backClicked();
    }

    protected void secondClicked()
    {
        clickedButton = 2;
        dismiss();
    }

    protected void firstClicked()
    {
        clickedButton = 1;
        dismiss();
    }

    protected void backClicked()
    {
        clickedButton = 0;
        dismiss();
    }

    protected boolean isNeedFirst()
    {
        return true;
    }

    protected abstract Table createSecond(XMLLayoutLoader xmlLoader, Window win, boolean hasAds);

    protected abstract Table createFirst(XMLLayoutLoader xmlLoader, Window win, boolean hasAds);

    protected abstract void createBody(XMLLayoutLoader xmlLoader, Window win);
}
