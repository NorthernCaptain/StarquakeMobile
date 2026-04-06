package northern.captain.quadronia.screens;

import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import java.util.List;

import northern.captain.gamecore.glx.NContext;
import northern.captain.gamecore.glx.tools.Animations;
import northern.captain.gamecore.glx.tools.loaders.XMLLayoutLoader;
import northern.captain.quadronia.game.core.Game;
import northern.captain.quadronia.game.profile.SKUItem;
import northern.captain.quadronia.game.profile.SKUManager;
import northern.captain.quadronia.gfx.GraphicsInitContext;
import northern.captain.quadronia.gfx.widget.SetupRowTitle;
import northern.captain.quadronia.gfx.widget.SpinnerWidget;
import northern.captain.tools.IAnimationFactory;

public class SubScreenShop extends SubScreenBase
{
    private Label coinsLbl;
    private int reservedCoins = 0;
    private IAnimationFactory coinsRotatorFactory;
    private List<SKUItem> items;
    private String name;

    public SubScreenShop()
    {
        this("shop_subscreen");
    }

    public SubScreenShop(String name)
    {
        this.name = name;
    }

    @Override
    public void initGraphics(XMLLayoutLoader loader, GraphicsInitContext gContext)
    {
        setTransform(false);
        loader.applyTo(this, name);
        float origHeight = getHeight();
        float height = NContext.current.screenHeight - getY();
        setHeight(height);

        float factorY = height / origHeight;

        {
            Image img = loader.newImage("shop_icon", gContext.atlas);
            setActorY(img, factorY, height);
            this.addActor(img);
        }
        for(int i=1;i<=4;i++)
        {
            Image img = loader.newImage("shop_topq"+i, gContext.atlas);
            setActorY(img, factorY, height);
            this.addActor(img);
        }
        {
            Image img = loader.newImage("shop_bank_img", gContext.atlas);
            setActorY(img, factorY, height);
            this.addActor(img);
        }

        {
            Label lbl = loader.newLabel("shop_toptitle");
            setActorY(lbl, factorY, height);
            addActor(lbl);
        }

        {
            coinsLbl = loader.newLabel("shop_mon_val");
            setActorY(coinsLbl, factorY, height);
            addActor(coinsLbl);
        }

        items = SKUManager.instance.getItems();
        for(SKUItem item: items)
        {
            item.init(Game.defaultGameContext);
        }

        //shop item rows
        for(int i=0;i<items.size();i++)
        {
            SKUItem item = items.get(i);

            final int idx = i+1;
            SetupRowTitle rowTitle = new SetupRowTitle("shop_set"+idx);
            rowTitle.initGraphics(loader, gContext);
            setActorY(rowTitle, factorY, height);
            this.addActor(rowTitle);
            rowTitle.setText(item.dispName + ",  " + item.price + "¢");

            SpinnerWidget spinnerWidget = new SpinnerWidget("shop_set"+idx+"_sw");
            spinnerWidget.initGraphics(loader, gContext);
            setActorY(spinnerWidget, factorY, height);
            this.addActor(spinnerWidget);
            spinnerWidget.setCallback(new SpinnerWidget.ISpinnable()
            {
                @Override
                public void onPlus()
                {
                    doPurchase(idx-1, 1, spinnerWidget);
                }

                @Override
                public void onMinus()
                {
                    doPurchase(idx-1, -1, spinnerWidget);
                }
            });
            spinnerWidget.setValue(item.getQty());
        }

        reservedCoins = 0;
        coinsRotatorFactory = new Animations.DigitsRotator(1);
        updateCoinsNfo();
    }

    private void updateCoinsNfo()
    {
        coinsLbl.addAction(coinsRotatorFactory.create(Game.defaultGameContext.getCurrentPlayer().getCoins() - reservedCoins));
    }

    private void doPurchase(int idx, int deltaValue, SpinnerWidget toUpdate)
    {
        SKUItem item = items.get(idx);
        item.reservedQty = deltaValue;
        int qty = item.getTotalQty();
        if(qty < 0 || qty > 9) return;
        item.processReserve();
        Game.defaultGameContext.saveToDisk();
        toUpdate.setValue(item.getQty());
        updateCoinsNfo();
    }
}
