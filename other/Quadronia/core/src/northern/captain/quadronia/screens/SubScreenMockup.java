package northern.captain.quadronia.screens;

import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;

import northern.captain.gamecore.glx.NContext;
import northern.captain.gamecore.glx.NCore;
import northern.captain.gamecore.glx.tools.loaders.XMLLayoutLoader;
import northern.captain.quadronia.gfx.GraphicsInitContext;
import northern.captain.quadronia.gfx.widget.TripleButton;

public class SubScreenMockup extends SubScreenBase
{
    private String name;
    private TripleButton mockup;
    private TripleButton moreGames;

    public SubScreenMockup(String name)
    {
        this.name = name;
        mockup = new TripleButton(name + "mockup");
        moreGames = new TripleButton(name + "moreg");
    }

    @Override
    public void initGraphics(XMLLayoutLoader loader, GraphicsInitContext gContext)
    {
        setTransform(false);
        loader.applyTo(this, name + "subscreen");
        float origHeight = getHeight();
        float height = NContext.current.screenHeight - getY();
        setHeight(height);

        float factorY = height / origHeight;

        {
            Image img = loader.newImage(name + "logo", gContext.atlas);
            img.setWidth(img.getWidth()*factorY);
            img.setHeight(img.getHeight()*factorY);
            setActorY(img, factorY, height);
            img.setX(img.getX()/factorY);
            this.addActor(img);
        }


//        mockup.initGraphics(loader, gContext);
//        mockup.setWidth(mockup.getWidth()*factorY/2);
//        mockup.setHeight(mockup.getHeight()*factorY/2);
//        setActorY(mockup, factorY, height);
//        this.addActor(mockup);

        moreGames.initGraphics(loader, gContext);
        setActorY(moreGames, factorY, height);

        this.addActor(moreGames);

        Label cpyLbl = loader.newLabel(name + "copyright");
        setActorY(cpyLbl, factorY, height);
        addActor(cpyLbl);

        Label musLbl = loader.newLabel(name + "musicby");
        setActorY(musLbl, factorY, height);
        addActor(musLbl);

        Label verLbl = loader.newLabel(name + "appversion");
        verLbl.setText("version " + NCore.instance().versionName);
        setActorY(verLbl, factorY, height);
        addActor(verLbl);

        {
            Image img = loader.newImage(name + "quadronia", gContext.atlas);
            setActorY(img, factorY, height);
            this.addActor(img);
        }

    }

    public void setOnClick(Runnable runnable)
    {
        mockup.setOnClick(runnable);
        moreGames.setOnClick(runnable);
    }
}
