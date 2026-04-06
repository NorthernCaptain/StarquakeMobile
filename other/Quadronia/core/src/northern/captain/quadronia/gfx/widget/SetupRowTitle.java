package northern.captain.quadronia.gfx.widget;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;

import northern.captain.gamecore.glx.tools.loaders.XMLLayoutLoader;
import northern.captain.quadronia.gfx.GraphicsInitContext;
import northern.captain.quadronia.gfx.IGraphicsInit;

public class SetupRowTitle extends Group implements IGraphicsInit
{
    String name;

    Label lbl;

    public SetupRowTitle(String name)
    {
        this.name = name;
    }

    @Override
    public void initGraphics(XMLLayoutLoader loader, GraphicsInitContext gContext)
    {
        setTransform(false);
        loader.applyTo(this, name + "_grp");

        {
            Image img = loader.newImage(name + "_bg", gContext.atlas);
            this.addActor(img);
        }
        {
            Image img = loader.newImage(name + "_bg2", gContext.atlas);
            img.getColor().a = 0.2f;
            this.addActor(img);
        }
        {
            Image img = loader.newImage(name + "_icon", gContext.atlas);
            this.addActor(img);
        }
        {
            lbl = loader.newLabel(name + "_title");
            addActor(lbl);
        }
    }

    public void setText(String text)
    {
        if(lbl != null)
        {
            lbl.setText(text);
        }
    }
}
