package northern.captain.quadronia.screens;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;

import northern.captain.gamecore.glx.NContext;
import northern.captain.quadronia.gfx.IGraphicsInit;

public abstract class SubScreenBase extends Group implements IGraphicsInit, IPagable
{
    @Override
    public void setPageActive(boolean isActive)
    {
        setVisible(isActive);
    }

    @Override
    public void setPageCurrent(boolean isCurrent)
    {

    }

    @Override
    public void setFromCurrentIdx(int deltaIdx)
    {
        setPageActive(deltaIdx >= -1 && deltaIdx <= 1);
    }

    @Override
    public void setPagePosition(float x, float y)
    {
        setX(x);
    }

    @Override
    public Actor getPageActor()
    {
        return this;
    }

    protected void setActorY(Actor actor, float factor, float height)
    {
        float y = actor.getY() * factor;
        actor.setY(y);
    }
}
