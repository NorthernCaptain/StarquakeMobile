package northern.captain.quadronia.gfx.panels;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.ui.Label;

import northern.captain.quadronia.gfx.GraphicsInitContext;
import northern.captain.quadronia.gfx.RawButton;
import northern.captain.gamecore.glx.NContext;
import northern.captain.gamecore.glx.tools.loaders.XMLLayoutLoader;

/**
 * Created by leo on 16.05.15.
 */
public class PButton extends RawButton
{
    Sprite back;
    Sprite downBack;
    Sprite downBackGlow;
    Sprite inactiveBack;
    Sprite icon;
    Sprite inactiveIcon;
    Label label;

    Sprite dragIcon;
    String name;

    Sprite currentBack;
    Sprite currentIcon;
    Sprite currentGlow;

    float glowTimeSpent;

    boolean checkMode = false;
    boolean checked = false;

    public PButton(String name)
    {
        this.name = name;
    }

    @Override
    public void initGraphics(XMLLayoutLoader layXml, GraphicsInitContext gContext)
    {
        back = layXml.newSprite(name + "bg", gContext.atlas);
        back.setOriginCenter();

        inactiveBack = layXml.newSprite(name + "bgin", gContext.atlas);
        inactiveBack.setOriginCenter();

        downBack = layXml.newSprite(name + "dn", gContext.atlas);
        downBack.setOriginCenter();

        downBackGlow = layXml.newSprite(name + "glow", gContext.atlas);
        downBackGlow.setOriginCenter();

        icon = layXml.newSprite(name + "icon", gContext.atlas);
        if(icon != null)
        {
            icon.setOriginCenter();
            icon.setRotation(NContext.current.cameraAngle);
            inactiveIcon = layXml.newSprite(name + "iconin", gContext.atlas);
            if(inactiveIcon != null)
            {
                inactiveIcon.setOriginCenter();
            }
        }

        label = layXml.newLabel(name + "lbl");

        this.xBox1 = (int) back.getX();
        this.xBox2 = this.xBox1 + (int) back.getWidth();

        this.yBox1 = (int) back.getY();
        this.yBox2 = (int) (back.getY() + back.getHeight());

        currentBack = back;
        currentIcon = icon;
    }

    @Override
    public void draw(Batch batch, float parentAlpha)
    {
        if (currentGlow != null)
        {
            batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
            currentGlow.draw(batch, parentAlpha);
            batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        }
        currentBack.draw(batch, parentAlpha);
        if(currentIcon != null)
        {
            currentIcon.draw(batch, parentAlpha);
        }
        label.draw(batch, parentAlpha);
    }

    @Override
    public boolean doTouchDown(int fx, int fy)
    {
        if (!pressed)
        {
            if (super.doTouchDown(fx, fy))
            {
                if (press())
                {
                    return true;
                }
                release();
            }
        }
        return false;
    }

    @Override
    public boolean doDrag(int x, int y)
    {
        if (isDragging)
        {
            dragIcon.setCenter(x, y);
        }
        return pressed;
    }

    @Override
    public boolean doTouchUp(int fx, int fy)
    {
        if (pressed && isIn(fx, fy))
        {
            doDrop(fx, fy);
            if(!checkMode || !checked)
            {
                release();
            }
            return true;
        }
        return false;
    }

    boolean press()
    {
        currentIcon = icon;
        currentGlow = downBackGlow;
        currentBack = downBack;

        glowTimeSpent = 0;
        return true;
    }

    void release()
    {
        currentGlow = null;

        if (!active)
        {
            currentIcon = inactiveIcon;
            currentBack = inactiveBack;
            label.setColor(1, 1, 1, 0.5f);
        } else
        {
            currentIcon = icon;
            currentBack = back;
            label.setColor(1, 1, 1, 1);
        }
        pressed = false;
    }

    public boolean doDrop(int fx, int fy)
    {
        if(checkMode)
        {
            checked = !checked;
            return true;
        }
        return false;
    }

    public void act(float deltaTime)
    {
        if (currentGlow != null)
        {
            glowTimeSpent += deltaTime;
            currentGlow.setAlpha(((float) Math.sin(glowTimeSpent * 1.8f - 1.0f) + 1.0f) / 2.4f + 0.1f);
        }
    }

    public boolean isCheckMode()
    {
        return checkMode;
    }

    public PButton setCheckMode(boolean checkMode)
    {
        this.checkMode = checkMode;
        return this;
    }

    public boolean isChecked()
    {
        return checked;
    }

    public void setChecked(boolean checked)
    {
        this.checked = checked;
        if(checked)
        {
            press();
        } else
        {
            release();
        }
    }
}
