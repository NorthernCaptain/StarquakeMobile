package northern.captain.tools;

import com.badlogic.gdx.graphics.g2d.Batch;

import java.util.LinkedList;

/**
 * Class provides simple management of a collection of AnimDrawables
 * @author leo
 *
 */
public class AnimContainer
{
	private LinkedList<AnimDrawable>  itemsOne = new LinkedList<AnimDrawable>();
	private LinkedList<AnimDrawable>  itemsTwo = new LinkedList<AnimDrawable>();
	
	private LinkedList<AnimDrawable>  items;
	
	public boolean isActive = false;
	
	public AnimContainer()
	{
		items = itemsOne;
	}

	/**
	 * Add drawable to the container
	 * @param newdraw
	 */
	public void addDrawable(AnimDrawable newdraw)
	{
		items.add(newdraw);
		newdraw.startAnimation();
	}
	
	/**
	 * Remove drawable from the container
	 * @param removedraw
	 */
	public void removeDrawable(AnimDrawable removedraw)
	{
		itemsOne.remove(removedraw);
		itemsTwo.remove(removedraw);
	}

    public void act(float delta)
    {
        for(AnimDrawable animDrawable : items)
        {
            animDrawable.act(delta);
        }
    }

	/**
	 * Draw all animation objects and return true if we have more animation to go
	 * @param canvas
	 * @return
	 */
	public boolean draw(Batch canvas)
	{
		LinkedList<AnimDrawable> others = items == itemsOne ? itemsTwo : itemsOne;
		others.clear();
		AnimDrawable draw;
		isActive = false;
		while( (draw = items.poll()) != null)
		{
			draw.draw(canvas);
			isActive = true;
			if(draw.hasStarted())
			{
				others.add(draw);
			}
		}
		items = others;
		return isActive;
	}

    public boolean draw(Batch canvas, float alpha)
    {
        LinkedList<AnimDrawable> others = items == itemsOne ? itemsTwo : itemsOne;
        others.clear();
        AnimDrawable draw;
        isActive = false;
        while( (draw = items.poll()) != null)
        {
            draw.draw(canvas, alpha);
            isActive = true;
            if(draw.hasStarted())
            {
                others.add(draw);
            }
        }
        items = others;
        return isActive;
    }

    public void clear()
	{
		items.clear();
		itemsOne.clear();
		itemsTwo.clear();
	}
}
