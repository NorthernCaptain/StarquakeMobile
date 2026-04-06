package northern.captain.quadronia.gfx.widget;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import java.util.ArrayList;
import java.util.List;

import northern.captain.gamecore.glx.tools.loaders.XMLLayoutLoader;
import northern.captain.quadronia.gfx.GraphicsInitContext;
import northern.captain.quadronia.gfx.IGraphicsInit;

public class OptionChoiceWidget extends Group implements IGraphicsInit
{
    private String name;
    private int maxEntries;
    private List<Image> entries;
    private int selectedIdx = -1;

    public interface OnValue
    {
        void onValueChanged(int newValue);
    }

    private OnValue callback;

    public OptionChoiceWidget(String name, int count)
    {
        this.name = name;
        this.maxEntries = count;
        entries = new ArrayList<>(count);
    }

    @Override
    public void initGraphics(XMLLayoutLoader loader, GraphicsInitContext gContext)
    {
        setTransform(false);
        loader.applyTo(this, name + "opt");

        for(int i=0;i<maxEntries;i++)
        {
            final int idx = i;
            Image img = loader.newImage(name+"img"+(i+1), gContext.atlas);
            entries.add(img);
            this.addActor(img);
        }

        this.addListener(new ClickListener()
        {
            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                setCurrent((selectedIdx+1) % maxEntries);
            }
        });

        this.setTouchable(Touchable.enabled);
    }

    public void setCurrent(int idx)
    {
        for(int i=0;i<maxEntries;i++)
        {
            Image img = entries.get(i);
            if(idx == i)
            {
                img.addAction(Actions.scaleTo(1.0f, 1.0f, 0.2f, Interpolation.pow2Out));
                img.addAction(Actions.alpha(1, 0.2f));
            } else
            {
                img.addAction(Actions.scaleTo(0.7f, 0.7f, 0.2f, Interpolation.pow2In));
                img.addAction(Actions.alpha(0.6f, 0.2f));
            }
        }

        selectedIdx = idx;
        if(callback != null)
        {
            callback.onValueChanged(selectedIdx);
        }
    }

    public void setCallback(OnValue callback)
    {
        this.callback = callback;
    }

    public int getSelectedIdx()
    {
        return selectedIdx;
    }
}
