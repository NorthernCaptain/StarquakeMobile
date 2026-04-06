package northern.captain.tools;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class AnimDrawable extends Actor
{
    Action action;
    Sprite drawable;
	boolean started = false;
    IAnimationFactory factory;

    public AnimDrawable(IAnimationFactory animation)
    {
        factory = animation;    	
    }
    
	public AnimDrawable(Sprite target, IAnimationFactory animation)
	{
		drawable = new Sprite(target);
        factory = animation;
        super.setBounds(drawable.getX(), drawable.getY(), drawable.getWidth(), drawable.getHeight());
        super.setOrigin(drawable.getOriginX(), drawable.getOriginY());
        super.setScale(drawable.getScaleX(), drawable.getScaleY());
        super.setRotation(drawable.getRotation());
	}

	public Action getAnimation()
	{
		return action;
	}

	public void setPos(int x, int y)
	{
        super.setPosition(x, y);
        if(drawable!= null)
        {
            drawable.setPosition(x, y);
        }
	}

    /**
     * Sets the x and y.
     */
    @Override
    public void setPosition(float x, float y)
    {
        super.setPosition(x, y);
        if(drawable!= null)
        {
            drawable.setPosition(x, y);
        }
    }

    /**
     * Sets the position using the specified {@link com.badlogic.gdx.scenes.scene2d.utils.Align alignment}. Note this may set the position to non-integer coordinates.
     *
     * @param x
     * @param y
     * @param alignment
     */
    @Override
    public void setPosition(float x, float y, int alignment)
    {
        super.setPosition(x, y, alignment);
        if(drawable != null)
        {
            drawable.setPosition(x, y);
        }
    }

    public AnimDrawable centerOrigin()
    {
        if(drawable != null)
        {
            drawable.setOrigin(drawable.getWidth()/2f, drawable.getHeight()/2f);
        }
        return this;
    }

    public void setAnimationFactory(IAnimationFactory iAnimationFactory)
    {
        this.factory = iAnimationFactory;
        stopAnim();
    }

    public void stopAnim()
    {
        if(action != null)
        {
            action.reset();
            clearActions();
            action = null;
            started = false;
        }
    }


	public void setAnimation(Action anim)
	{
		action = anim;
        started = false;
	}

	public void initialize()
	{
	}
	
	public void startAnimation()
	{
        stopAnim();
        started = true;
        action = factory.create(0);
        addAction(action);
	}

    public void act(float delta)
    {
        if(started && action != null)
        {
            started = !action.act(delta);
        }
    }

	public boolean hasStarted()
	{
		return started;
	}

	public boolean hasEnded()
	{
		return action == null || !started;
	}

	public void draw(Batch canvas)
	{
        drawable.draw(canvas);
	}

    @Override
    public void draw(Batch canvas, float alpha)
    {
        drawable.draw(canvas, alpha);
    }


    public void setWH(int width, int height)
	{
        setSize(width, height);
		if(drawable != null)
		{
            drawable.setSize(width, height);
		}
	}
	
	public Sprite getInnerDrawable()
	{
		return drawable;
	}

    @Override
    public void setX(float x)
    {
        super.setX(x);    //To change body of overridden methods use File | Settings | File Templates.
        if(drawable != null)
            drawable.setX(x);
    }

    @Override
    public void setY(float y)
    {
        super.setY(y);    //To change body of overridden methods use File | Settings | File Templates.
        if(drawable != null)
            drawable.setY(y);
    }

    @Override
    public void setWidth(float width)
    {
        super.setWidth(width);    //To change body of overridden methods use File | Settings | File Templates.
        if(drawable != null)
            drawable.setSize(width, drawable.getHeight());
    }

    @Override
    public void setHeight(float height)
    {
        super.setHeight(height);    //To change body of overridden methods use File | Settings | File Templates.
        if(drawable != null)
            drawable.setSize(drawable.getWidth(), height);
    }

    @Override
    public void setScaleX(float scaleX)
    {
        super.setScaleX(scaleX);    //To change body of overridden methods use File | Settings | File Templates.
        if(drawable != null)
            drawable.setScale(scaleX, drawable.getScaleY());
    }

    @Override
    public void setScaleY(float scaleY)
    {
        super.setScaleY(scaleY);    //To change body of overridden methods use File | Settings | File Templates.
        if(drawable != null)
            drawable.setScale(drawable.getScaleX(), scaleY);
    }

    @Override
    public void setScale(float scale)
    {
        super.setScale(scale);    //To change body of overridden methods use File | Settings | File Templates.
        if(drawable != null)
            drawable.setScale(scale, scale);
    }

    @Override
    public void setScale(float scaleX, float scaleY)
    {
        super.setScale(scaleX, scaleY);    //To change body of overridden methods use File | Settings | File Templates.
        if(drawable != null)
            drawable.setScale(scaleX, scaleY);
    }

    @Override
    public void setRotation(float degrees)
    {
        super.setRotation(degrees);    //To change body of overridden methods use File | Settings | File Templates.
        if(drawable != null)
            drawable.setRotation(degrees);
    }

    @Override
    public void setOriginY(float originY)
    {
        super.setOriginY(originY);    //To change body of overridden methods use File | Settings | File Templates.
        if(drawable != null)
            drawable.setOrigin(drawable.getOriginX(), originY);
    }

    @Override
    public void setOriginX(float originX)
    {
        super.setOriginX(originX);    //To change body of overridden methods use File | Settings | File Templates.
        if(drawable != null)
            drawable.setOrigin(originX, drawable.getOriginY());
    }

    @Override
    public void setColor(Color color)
    {
        super.setColor(color);    //To change body of overridden methods use File | Settings | File Templates.
        if(drawable != null)
            drawable.setColor(color);
    }

    @Override
    public void setColor(float r, float g, float b, float a)
    {
        super.setColor(r, g, b, a);    //To change body of overridden methods use File | Settings | File Templates.
        if(drawable != null)
            drawable.setColor(r, g, b, a);
    }

    @Override
    public void setOrigin(float originX, float originY)
    {
        super.setOrigin(originX, originY);    //To change body of overridden methods use File | Settings | File Templates.
        if(drawable != null)
            drawable.setOrigin(originX, originY);
    }
}
