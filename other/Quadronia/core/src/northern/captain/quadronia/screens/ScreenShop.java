package northern.captain.quadronia.screens;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

import java.util.List;

import northern.captain.quadronia.game.Engine;
import northern.captain.quadronia.game.IGameContext;
import northern.captain.quadronia.game.core.Game;
import northern.captain.quadronia.game.profile.SKUItem;
import northern.captain.quadronia.game.profile.SKUManager;
import northern.captain.quadronia.game.profile.UserBase;
import northern.captain.quadronia.game.profile.UserManager;
import northern.captain.gamecore.glx.NContext;
import northern.captain.gamecore.glx.res.SharedRes;
import northern.captain.gamecore.glx.screens.IScreenWorkflow;
import northern.captain.gamecore.glx.screens.ScreenBase;
import northern.captain.gamecore.glx.tools.Animations;
import northern.captain.gamecore.glx.tools.ClickListenerPrepared;
import northern.captain.gamecore.glx.tools.ScrollTable;
import northern.captain.gamecore.glx.tools.loaders.ResLoader;
import northern.captain.gamecore.glx.tools.loaders.XMLLayoutLoader;
import northern.captain.tools.AnimButtonSet;
import northern.captain.tools.analytics.AnalyticsFactory;

/**
 * Created by leo on 25.05.15.
 */
public class ScreenShop extends ScreenBase
{
    private LoadIds load = new LoadIds();
    private AnimButtonSet anim;
    private AnimButtonSet butAnim;
    private XMLLayoutLoader layXml;

    private Label coinsLbl;
    private ScrollTable scrollTable;
    private ScrollTable.ListAdapter adapter;
    
    private UserBase player;

    private List<SKUItem> items;
    private IGameContext context;
    private int playerCoins;
    private int reservedCoins;

    public ScreenShop(IScreenWorkflow workflow)
    {
        super(workflow);
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

        player = UserManager.instance.getCurrentUser();
        context = Game.defaultGameContext;

        TextureAtlas tatlas = SharedRes.instance.getCommonAtlas();

        layXml = ResLoader.singleton().getLoaded(load.layoutId);

        initScreenBack(layXml, tatlas);

        Stack frame = new Stack();
        frame.setName("gframe");
        NContext.current.setGameFrame(frame);

        anim = new AnimButtonSet();
        anim.setOnInitialAnimation(new Animations.MoveInFromOutsideLeft());
        anim.setOnPressAnimation(new Animations.ScaleUp13());
        anim.setOnReleaseAnimation(new Animations.ScaleDown1());
        anim.setOnClickedAnimation(new Animations.MoveOutsideRight());
        anim.setOnOtherClickedAnimation(new Animations.MoveOutsideLeft());

        butAnim = new AnimButtonSet();
        butAnim.setInitialAnimDelay(0);
        butAnim.setOnPressAnimation(new Animations.ScaleUp13());
        butAnim.setOnReleaseAnimation(new Animations.ScaleDown1());


        Table backT = new Table();
        frame.add(backT);
        backT.setFillParent(true);
        gameAreaTable.add(frame).expand().fill();
        
        {
            Table topT = new Table();
            
            Label lbl = layXml.newLabel("unamelbl");
            lbl.setText(player.getName());
            topT.add(lbl).top().left().padLeft(lbl.getX()).padTop(lbl.getY()).width(lbl.getWidth()).expandX();

            Image img = layXml.newImage("coin", tatlas);
            topT.add(img).top().left().padLeft(img.getX()).padTop(img.getY()).width(img.getWidth());

            lbl = coinsLbl = layXml.newLabel("coinlbl");
            topT.add(lbl).top().left().padLeft(lbl.getX()).padTop(lbl.getY()).width(lbl.getWidth());

            backT.add(topT).expandX().center().top();
        }

        backT.row();


        //==============================
        //List and adapter init
        //==============================
        items = SKUManager.instance.getItems();
        adapter = new Adapter();

        scrollTable = layXml.newScrollTable("skulist", tatlas);

        backT.add(scrollTable).expand().fill().width(scrollTable.getWidth()).padLeft(scrollTable.getX()).padRight(scrollTable.getX())
                .padTop(scrollTable.getY()).padBottom(scrollTable.getY());

        scrollTable.setAdapter(adapter);



        backT.row();

        {
            Table bottomT = new Table();
            Table but;
            but = layXml.addToCell(bottomT, layXml.newTextButton("but_cancel", tatlas));
            but.setTransform(true);

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
                    setLogoAlpha(1);
                    AnalyticsFactory.instance().getAnalytics().registerButtonAction("shop_cancel");
                    NContext.current.post
                            (new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    doCancel();
                                }
                            });
                }

            });

            but = layXml.addToCell(bottomT, layXml.newTextButton("but_ok", tatlas));
            but.setTransform(true);

            anim.add(2, but, new ClickListenerPrepared()
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
                    AnalyticsFactory.instance().getAnalytics().registerButtonAction("shop_ok");
                    NContext.current.post
                            (new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    doOK();
                                }
                            });
                }

            });

            backT.add(bottomT).expandX();
            backT.row();
            Table spacer = new Table();
            spacer.setHeight(but.getHeight()/2);
            backT.add(spacer).expandX().height(spacer.getHeight());
        }

        setup();
        screenFadeIn();
        mainTable.pack();
        anim.startInitialAnimation(1);
    }

    private void setup()
    {
        playerCoins = player.getCoins();
        reservedCoins = 0;
        updateCoinsNfo();
        scrollTable.setNeedFill();
        scrollTable.scrollToStart();
    }

    private void updateCoinsNfo()
    {
        coinsLbl.setText(Integer.toString(playerCoins - reservedCoins));
    }


    class Adapter implements ScrollTable.ListAdapter
    {
        Adapter()
        {

        }
        /**
         * Adds new row content to the table via creating sub-table and returning it
         *
         * @param table
         * @param idx
         */
        @Override
        public Table addRow(Table table, int idx)
        {
            Table tbl = new Table();

            Label lbl;

            final SKUItem item = items.get(idx);
            item.init(context);

            float alpha = 1;

            TextureAtlas atlas = SharedRes.instance.getCommonAtlas();

            {
                Image img = layXml.newImage(item.getIconName(), atlas);
                if(img == null)
                    return null;

                if(item.isLocked())
                {
                    alpha=0.5f;
                }

                Image imgHelp = layXml.newImage("help", atlas);

                imgHelp.addListener(new ClickListenerPrepared()
                {
                    @Override
                    public boolean prepareClicked(InputEvent evt)
                    {
                        return true;
                    }

                    @Override
                    public void clicked(InputEvent event, float x, float y)
                    {
                        showHelp(item);
                    }
                });

                img.addListener(new ClickListenerPrepared()
                {
                    @Override
                    public boolean prepareClicked(InputEvent evt)
                    {
                        return true;
                    }

                    @Override
                    public void clicked(InputEvent event, float x, float y)
                    {
                        showHelp(item);
                    }
                });

                Stack iframe = new Stack();
                Table itbl = new Table();
                itbl.setFillParent(true);
                Image bgIm = layXml.newImage("back"+item.getIconName(), atlas);
                itbl.add(bgIm).padRight(bgIm.getX()).padBottom(bgIm.getY())
                        .expand().width(bgIm.getWidth()).height(bgIm.getHeight());
                iframe.add(itbl);

                itbl = new Table();
                itbl.setFillParent(true);
                itbl.add(img).padLeft(img.getX()).padTop(img.getY()).padRight(imgHelp.getX()).padBottom(imgHelp.getY())
                        .expand().width(img.getWidth()).height(img.getHeight());
                iframe.add(itbl);
                
                itbl = new Table();
                itbl.setFillParent(true);

//                img = layXml.newImage("numplate", atlas);
//                itbl.add(img).expand().top().left().width(img.getWidth()).height(img.getHeight()).padLeft(img.getX()).padTop(img.getY());

                img = imgHelp;
                itbl.add(img).expand().bottom().right().width(img.getWidth()).height(img.getHeight());
                iframe.add(itbl);

                itbl = new Table();
                itbl.setFillParent(true);

                lbl = layXml.newLabel("numweap");
                itbl.add(lbl).width(lbl.getWidth()).padTop(lbl.getY()).padLeft(lbl.getX()).left().top().expand();
                lbl.setText(alpha> 0.9f ? Integer.toString(item.getTotalQty()) : "0");
                iframe.add(itbl);

                iframe.getColor().a = alpha;

                tbl.add(iframe);
            }

            final Label numWeap = lbl;

            {
                Table tblT = new Table();

                lbl = layXml.newLabel("weap_name");
                lbl.setText(item.dispName);

                tblT.add(lbl).padLeft(lbl.getX()).padTop(lbl.getY()).expandX().left().top();
                tblT.row();

                Table costT = new Table();

                tblT.add(costT).expandX().right().top();

                lbl = layXml.newLabel("weap_cost");
                lbl.setText(Integer.toString(item.price));

                costT.add(lbl).expandX().padRight(lbl.getX()).padTop(lbl.getY()).top().right();

                Image img = layXml.newImage("coins", atlas);

                costT.add(img).padRight(img.getX()).padTop(img.getY()).top().right().width(img.getWidth()).height(img.getHeight());

                tblT.getColor().a = alpha;

                tbl.add(tblT).expand().fill().left().top();
            }


            if(alpha > 0.9f)
            {
                Image img = layXml.newImage("sellbg", atlas);

                Stack iframe = new Stack();
                Table itbl = new Table();
                itbl.setFillParent(true);
                itbl.add(img).padLeft(img.getX()).padTop(img.getY()).expand().width(img.getWidth()).height(img.getHeight());
                iframe.add(itbl);

                itbl = new Table();
                itbl.setFillParent(true);
                Table but = layXml.newImageButton("plus_coin_but", atlas);
                itbl.add(but).expand().top().left().width(but.getWidth()).height(but.getHeight());
                butAnim.add(idx, but, new ClickListenerPrepared()
                {
                    @Override
                    public boolean prepareClicked(InputEvent evt)
                    {
                        return true;
                    }

                    @Override
                    public void clicked(InputEvent event, float x, float y)
                    {
                        doPurchase(item, 1);
                        numWeap.setText(Integer.toString(item.getTotalQty()));
                    }
                });

                but = layXml.newImageButton("minus_coin_but", atlas);
                itbl.add(but).expand().bottom().right().width(but.getWidth()).height(but.getHeight());
                butAnim.add(idx, but, new ClickListenerPrepared()
                {
                    @Override
                    public boolean prepareClicked(InputEvent evt)
                    {
                        return true;
                    }

                    @Override
                    public void clicked(InputEvent event, float x, float y)
                    {
                        doPurchase(item, -1);
                        numWeap.setText(Integer.toString(item.getTotalQty()));
                    }
                });

                iframe.add(itbl);

                iframe.getColor().a = alpha;

                tbl.add(iframe).padTop(img.getY()).padBottom(img.getY()).padLeft(img.getX()).padRight(img.getX());
            } else
            {
                Stack iframe = new Stack();
                Table itbl = new Table();
                itbl.setFillParent(true);
                Table but = layXml.newImageButton("locked_but", atlas);
                itbl.add(but).expand().center().width(but.getWidth()).height(but.getHeight());
                butAnim.add(idx, but, new ClickListenerPrepared()
                {
                    @Override
                    public boolean prepareClicked(InputEvent evt)
                    {
                        return true;
                    }

                    @Override
                    public void clicked(InputEvent event, float x, float y)
                    {
                        showHelp(item);
                    }
                });

                iframe.add(itbl);

                tbl.add(iframe).padTop(but.getY()).padBottom(but.getY()).padLeft(but.getX()).padRight(but.getX());
            }

            if(idx % 2 != 0)
            {
                final TextureAtlas commonAtlas = SharedRes.instance.getCommonAtlas();

                Drawable tablebg = layXml.getDrawable("tablebg", commonAtlas);
                tbl.setBackground(tablebg);
            }
            return tbl;
        }

        /**
         * Number of elements in total
         *
         * @return
         */
        @Override
        public int getSize()
        {
            return items.size();
        }

        @Override
        public void setParentTable(ScrollTable tbl)
        {
            butAnim.clear();
        }

        /**
         * Called by the table when the item is clicked. Passes item idx in the table
         *
         * @param idx
         */
        @Override
        public void itemClicked(int idx)
        {

        }

        /**
         * Called by the table when the item is long pressed and held.
         *
         * @param idx
         */
        @Override
        public void itemLongClicked(int idx)
        {

        }
    }

    private void showHelp(SKUItem item)
    {
        ArmoryInfoDialog dlg = new ArmoryInfoDialog(item, SharedRes.instance.getCommonAtlas());
        dlg.show();
    }

    private void doPurchase(SKUItem item, int qty)
    {
        int deltaCoins = item.price * qty;
        if(playerCoins -  reservedCoins - deltaCoins >= 0)
        {
            reservedCoins += deltaCoins;
            item.reservedQty += qty;
            updateCoinsNfo();
        }
    }

    private void doCancel()
    {
        flow.prepare(IScreenWorkflow.WF_BACK);
        screenFadeOut(new Runnable()
        {
            @Override
            public void run()
            {
                flow.doAction(IScreenWorkflow.WF_BACK);
            }
        });
    }

    private void doOK()
    {
        boolean changed = false;
        for(SKUItem item : items)
        {
            if(item.reservedQty != 0)
            {
                changed = true;
                item.processReserve();
            }
        }

        if(changed)
        {
            context.saveToDisk();
        }

        doCancel();
    }

    /* (non-Javadoc)
         * @see northern.captain.seabattle.glx.screens.ScreenBase#dispose()
         */
    @Override
    public void dispose()
    {
        super.dispose();
        ResLoader.singleton().unload(load.layoutId);
        anim.clearAll();
        anim = null;
    }

    /* (non-Javadoc)
     * @see northern.captain.seabattle.glx.screens.ScreenBase#prepareEnter()
     */
    @Override
    public void prepareEnter()
    {
        super.prepareEnter();
        load.layoutId = ResLoader.singleton().loadLayout("shop");
    }

}
