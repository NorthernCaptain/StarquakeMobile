package northern.captain.quadronia.gfx.widget;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;

import northern.captain.gamecore.glx.tools.ClickListenerPrepared;
import northern.captain.gamecore.glx.tools.loaders.XMLLayoutLoader;
import northern.captain.quadronia.gfx.GraphicsInitContext;
import northern.captain.quadronia.gfx.IGraphicsInit;

public class BoolSwitch extends Group implements IGraphicsInit
{
    public interface OnValueChange
    {
        void onChange(boolean isOn);
    }

    private Image bgImg;
    private Image switchImg;
    private boolean isOn = false;
    private String name;
    private OnValueChange onValueChange;
    private boolean onOffMode = true;

    public BoolSwitch(String name)
    {
        this.name = name;
    }

    @Override
    public void initGraphics(XMLLayoutLoader loader, GraphicsInitContext gContext)
    {
        setTransform(false);
        loader.applyTo(this, name + "switch");

        bgImg = loader.newImage(name + "bg", gContext.atlas);
        addActor(bgImg);

        switchImg = loader.newImage(name + "front", gContext.atlas);
        addActor(switchImg);

        setOn(isOn);

        this.setTouchable(Touchable.enabled);
        this.addListener(new ClickListenerPrepared()
        {
            @Override
            public boolean prepareClicked(InputEvent evt)
            {
                return true;
            }

            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                setOn(!isOn);
            }
        });
    }

    public boolean isOn()
    {
        return isOn;
    }

    public void setOn(boolean on)
    {
        isOn = on;

        float endX = bgImg.getX() + (on ? bgImg.getWidth() - switchImg.getWidth() : 0);
        switchImg.addAction(Actions.moveTo(endX, switchImg.getY(), 0.3f, Interpolation.swingOut));
        if(onOffMode) switchImg.addAction(Actions.alpha(!on ? 0.5f : 1f, 0.3f));
        if(onValueChange != null) onValueChange.onChange(on);
    }

    public OnValueChange getOnValueChange()
    {
        return onValueChange;
    }

    public void setOnValueChange(OnValueChange onValueChange)
    {
        this.onValueChange = onValueChange;
    }

    public void setOnOffMode(boolean onOffMode)
    {
        this.onOffMode = onOffMode;
    }
}
