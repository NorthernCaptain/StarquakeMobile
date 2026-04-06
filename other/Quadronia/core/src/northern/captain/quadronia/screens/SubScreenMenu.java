package northern.captain.quadronia.screens;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;

import northern.captain.gamecore.glx.NContext;
import northern.captain.gamecore.glx.tools.loaders.XMLLayoutLoader;
import northern.captain.quadronia.game.core.Game;
import northern.captain.quadronia.gfx.GraphicsInitContext;
import northern.captain.quadronia.gfx.widget.BoolSwitch;
import northern.captain.quadronia.gfx.widget.MainMenuButtonsWidget;
import northern.captain.quadronia.gfx.widget.MainMenuUserWidget;

public class SubScreenMenu extends SubScreenBase
{
    private MainMenuButtonsWidget buttonsWidget;
    private MainMenuUserWidget userWidget;
    private BoolSwitch isExpressGameSwitch;
    private Label classicLbl;
    private Label sprintLbl;

    public SubScreenMenu()
    {
        buttonsWidget = new MainMenuButtonsWidget();
        isExpressGameSwitch = new BoolSwitch("gametype");
        isExpressGameSwitch.setOnOffMode(false);
        userWidget = new MainMenuUserWidget();
    }

    @Override
    public void initGraphics(XMLLayoutLoader loader, GraphicsInitContext gContext)
    {
        setTransform(false);
        loader.applyTo(this, "main_subscreen");
        float origHeight = getHeight();
        float height = NContext.current.screenHeight - getY();
        setHeight(height);

        float factorY = height / origHeight;

        buttonsWidget.initGraphics(loader, gContext);
        setActorY(buttonsWidget, factorY, height);
        addActor(buttonsWidget);

        classicLbl = loader.newLabel("classictext");
        setActorY(classicLbl, factorY, height);
        addActor(classicLbl);
        sprintLbl = loader.newLabel("sprinttext");
        setActorY(sprintLbl, factorY, height);
        addActor(sprintLbl);

        {
            Image img = loader.newImage("mnu_quadronia", gContext.atlas);
            setActorY(img, factorY, height);
            this.addActor(img);
        }

        userWidget.initGraphics(loader, gContext);
        setActorY(userWidget, factorY, height);
        addActor(userWidget);

        isExpressGameSwitch.setOnValueChange((boolean val) -> {
            sprintLbl.getColor().a = val ? 1 : 0.5f;
            classicLbl.getColor().a = val ? 0.5f : 1;
            userWidget.setGameMode(val ? Game.TYPE_EXPRESS : Game.TYPE_ARCADE);
        });

        isExpressGameSwitch.initGraphics(loader, gContext);
        setActorY(isExpressGameSwitch, factorY, height);
        addActor(isExpressGameSwitch);
    }

    public SubScreenMenu setOnPlay(Runnable onPlayCallback) {
        buttonsWidget.setOnPlay(onPlayCallback);
        return this;
    }

    public SubScreenMenu setOnSettings(Runnable onSettings) {
        buttonsWidget.setOnSettings(onSettings);
        return this;
    }

    public SubScreenMenu setOnInfo(Runnable onInfo)
    {
        buttonsWidget.setOnInfo(onInfo);
        return this;
    }

    public SubScreenMenu setOnShop(Runnable onShop)
    {
        buttonsWidget.setOnShop(onShop);
        userWidget.setOnShop(onShop);
        return this;
    }

    public int getGameType()
    {
        return isExpressGameSwitch.isOn() ? Game.TYPE_EXPRESS : Game.TYPE_ARCADE;
    }

    public void initValues()
    {
        userWidget.initValues();
    }
}
