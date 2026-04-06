package northern.captain.quadronia.gfx.widget;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;

import northern.captain.gamecore.glx.NContext;
import northern.captain.gamecore.glx.tools.loaders.XMLLayoutLoader;
import northern.captain.gamecore.gplus.GoogleGamesFactory;
import northern.captain.quadronia.gfx.GraphicsInitContext;
import northern.captain.quadronia.gfx.IGraphicsInit;

public class MainMenuButtonsWidget extends Group implements IGraphicsInit
{
    private TripleButton settingsBut;
    private TripleButton infoBut;
    private TripleButton shopBut;
    private TripleButton achivBut;
    private TripleButton playBut;

    public MainMenuButtonsWidget()
    {
        settingsBut = new TripleButton("settings");
        infoBut = new TripleButton("info");
        shopBut = new TripleButton("shop");
        achivBut = new TripleButton("achiv");
        achivBut.setOnClick(() ->
                NContext.current.postDelayed(()->GoogleGamesFactory.instance().getProcessor().openAchievements(), 500));
        playBut = new TripleButton("play");
    }

    @Override
    public void initGraphics(XMLLayoutLoader loader, GraphicsInitContext gContext)
    {
        setTransform(false);
        loader.applyTo(this, "mnu_buttons");

        settingsBut.initGraphics(loader, gContext);
        addActor(settingsBut);

        infoBut.initGraphics(loader, gContext);
        addActor(infoBut);

        shopBut.initGraphics(loader, gContext);
        addActor(shopBut);

        achivBut.initGraphics(loader, gContext);
        addActor(achivBut);

        {
            Image img = loader.newImage("mnu_frm31", gContext.atlas);
            this.addActor(img);
        }
        {
            Image img = loader.newImage("mnu_frm32", gContext.atlas);
            this.addActor(img);
        }
        {
            Image img = loader.newImage("mnu_frm33", gContext.atlas);
            this.addActor(img);
        }
        {
            Image img = loader.newImage("mnu_frm34", gContext.atlas);
            this.addActor(img);
        }

        playBut.initGraphics(loader, gContext);
        addActor(playBut);

    }

    public void setOnPlay(Runnable onPlay)
    {
        playBut.setOnClick(onPlay);
    }

    public void setOnSettings(Runnable onSettings)
    {
        settingsBut.setOnClick(onSettings);
    }

    public void setOnShop(Runnable onShop)
    {
        shopBut.setOnClick(onShop);
    }

    public void setOnInfo(Runnable onInfo)
    {
        infoBut.setOnClick(onInfo);
    }
}
