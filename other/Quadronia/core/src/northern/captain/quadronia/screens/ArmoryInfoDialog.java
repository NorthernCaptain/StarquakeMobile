package northern.captain.quadronia.screens;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

import northern.captain.quadronia.game.profile.SKUItem;
import northern.captain.gamecore.glx.ISoundMan;
import northern.captain.gamecore.glx.NContext;
import northern.captain.gamecore.glx.NCore;
import northern.captain.gamecore.glx.res.SharedRes;
import northern.captain.gamecore.glx.tools.Animations;
import northern.captain.gamecore.glx.tools.ClickListenerPrepared;
import northern.captain.gamecore.glx.tools.loaders.IResLoader;
import northern.captain.gamecore.glx.tools.loaders.ResLoader;
import northern.captain.gamecore.glx.tools.loaders.XMLLayoutLoader;
import northern.captain.tools.IOnBackAction;

public class ArmoryInfoDialog extends ClickListener implements Runnable, IOnBackAction
{
    private String titleId = null;
    private String text;
    private ClickListenerPrepared first;

    private Window win;
    private Table tbl;
    private Table okBut;

    private SKUItem item;
    private TextureAtlas addOnAtlas;

    public ArmoryInfoDialog(SKUItem item, TextureAtlas addOnAtlas)
    {
        this.item = item;
        this.addOnAtlas = addOnAtlas;

        setTextAndTitle(item.dispName, item.dispDesc);
    }

    public void setTextAndTitle(String titleId, String textId)
    {
        this.titleId = titleId;
        setTextMessage(textId);
    }

    public void setTitle(String titleId)
    {
        this.titleId = titleId;
    }

    public void setTextMessage(String text)
    {
        this.text = text;
    }

    /* (non-Javadoc)
     * @see android.app.Dialog#onBackPressed()
     */
    public void onBackPressed()
    {
    }

    public void dismiss()
    {
        win.clearActions();
        win.addAction(new Animations.DropDownOutOfScreen().create(0, Actions.run(dismissRun)));
    }

    private Runnable dismissRun = new Runnable()
    {

        @Override
        public void run()
        {
            hide();
        }
    };

    public void hide()
    {
        tbl.remove();
        win.remove();
        tbl = null;
        win = null;
        okBut = null;
        NCore.instance().popOnBackAction(this);
        NContext.current.subRefresh();
    }

    public void show()
    {
        create();

        win.addAction(new Animations.DropDownOffScreen().create(0));
        NCore.instance().pushOnBackAction(this);
    }


    protected void create()
    {
        IResLoader loader = ResLoader.singleton();
        int layId = loader.loadLayout("armory_info_dlg");
        loader.finishLoading();
        TextureAtlas atlas = SharedRes.instance.getCommonAtlas();

        XMLLayoutLoader xmlLoader = loader.getLoaded(layId);

        win = xmlLoader.newWindow("dialog", atlas);
        win.setKeepWithinStage(false);
        {
            Image img = xmlLoader.newImage(item.getIconName(), addOnAtlas);

            {
                Stack iframe = new Stack();
                Table itbl = new Table();
                itbl.setFillParent(true);
                Image bgIm = xmlLoader.newImage("back"+item.getIconName(), atlas);
                itbl.add(bgIm).padRight(bgIm.getX()).padBottom(bgIm.getY())
                        .expand().width(bgIm.getWidth()).height(bgIm.getHeight());
                iframe.add(itbl);

                itbl = new Table();
                itbl.setFillParent(true);
                itbl.add(img).padLeft(img.getX()).padTop(img.getY())
                        .expand().width(img.getWidth()).height(img.getHeight());
                iframe.add(itbl);

                win.add(iframe).top().left();
            }


            Table titTbl = new Table();

            Label titlbl = xmlLoader.newLabel("title");
            if(titleId != null)
                titlbl.setText(SharedRes.instance.x(titleId));
            titTbl.add(titlbl).right().expandX().fillX().padRight(titlbl.getX()).top().padTop(titlbl.getY());
            titTbl.row();

            Table costTbl = new Table();

            {
                Label lbl = xmlLoader.newLabel("weap_cost");
                costTbl.add(lbl).expandX().right().top().padRight(lbl.getX()).padTop(lbl.getY());
                lbl.setText(Integer.toString(item.price));

                Image cimg = xmlLoader.newImage("coins", addOnAtlas);

                costTbl.add(cimg).right().top().width(cimg.getWidth()).height(cimg.getHeight()).padRight(cimg.getX()).padTop(cimg.getY());
            }
            titTbl.add(costTbl).expandX().fill();


            win.add(titTbl).expandX().fill();
            win.row();
        }

        Image line = xmlLoader.newImage("sep", atlas);
        xmlLoader.addToCellTiled(win, line);
        win.getCell(line).colspan(2);
        win.row();


        Label lbl;
        lbl = xmlLoader.newLabel("locked");
        lbl.setWrap(true);
        win.add(lbl).left().colspan(2).padLeft(lbl.getX()).padRight(lbl.getX()).padTop(lbl.getY()).expandX().fill();

        lbl.setText(SharedRes.instance.x("have_items").replaceAll("ITEMS", Integer.toString(item.getTotalQty())));

        win.row();

        lbl = xmlLoader.newLabel("info");
        lbl.setWrap(true);
        lbl.setText(text);
        win.add(lbl).center().colspan(2).padLeft(lbl.getX()).padRight(lbl.getX()).expand().fill().padTop(lbl.getY()).padBottom(lbl.getY());
        win.row();

        okBut = xmlLoader.newTextButton("but_ok", atlas);
        okBut.addListener(this);
        win.add(okBut).colspan(2).expandX().center().width(okBut.getWidth()).height(okBut.getHeight()).padBottom(okBut.getY());
        win.pack();
        win.center();
        win.setModal(true);
        win.setClip(true);
        win.getColor().a = 0;

        loader.unload(layId);

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
        NCore.instance().getSoundman().playSound(ISoundMan.SND_JUST_CLICK, true);
        super.clicked(event, x, y);
//		if(event.getTarget() != okBut)
        okBut.addAction(new Animations.MoveOutsideLeft().create(0, Actions.run(this)));
        if(first != null)
        {
            first.prepareClicked(event);
        }
    }

    @Override
    public void run()
    {
        if(first != null)
        {
            InputEvent evt = new InputEvent();
            evt.setTarget(okBut);
            first.clicked(evt, 0, 0);
        }
        dismiss();
    }

    @Override
    public void OnBackPressed()
    {
        InputEvent evt = new InputEvent();
        evt.setTarget(okBut);
        clicked(evt, 0, 0);
    }
}
