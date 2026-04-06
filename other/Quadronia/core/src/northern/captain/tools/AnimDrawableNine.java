package northern.captain.tools;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.NinePatch;

public class AnimDrawableNine extends AnimDrawable
{
	NinePatch nineDraw;
	
	public AnimDrawableNine(NinePatch target, IAnimationFactory animation)
	{
		super(animation);
		nineDraw = target;
	}

    @Override
    public void draw(Batch canvas, float parentAlpha)
    {
        nineDraw.setColor(getColor());
        nineDraw.draw(canvas, getX(), getY(), getWidth(), getHeight());
    }

    @Override
	public void draw(Batch canvas)
	{
        nineDraw.setColor(getColor());
		nineDraw.draw(canvas, getX(), getY(), getWidth(), getHeight());
	}

    @Override
    public void setColor(Color color)
    {
        super.setColor(color);    //To change body of overridden methods use File | Settings | File Templates.
        nineDraw.setColor(color);
    }

    @Override
    public void setColor(float r, float g, float b, float a)
    {
        super.setColor(r, g, b, a);    //To change body of overridden methods use File | Settings | File Templates.
        nineDraw.setColor(getColor());
    }

}
