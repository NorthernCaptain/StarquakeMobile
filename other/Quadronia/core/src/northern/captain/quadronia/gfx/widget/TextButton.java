package northern.captain.quadronia.gfx.widget;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

import northern.captain.gamecore.glx.tools.loaders.XMLLayoutLoader;
import northern.captain.quadronia.gfx.GraphicsInitContext;
import northern.captain.quadronia.gfx.IGraphicsInit;

public class TextButton extends Group implements IGraphicsInit
{
    private String name;
    private Image bgImg;
    private Label text;

    public TextButton(String name)
    {
        this.name = name;
    }

    @Override
    public void initGraphics(XMLLayoutLoader loader, GraphicsInitContext gContext)
    {
        setTransform(false);
        loader.applyTo(this, name + "but");

        bgImg = loader.newImage(name + "bg", gContext.atlas);
        addActor(bgImg);

        text = loader.newLabel(name + "text");
        addActor(text);
    }
}
