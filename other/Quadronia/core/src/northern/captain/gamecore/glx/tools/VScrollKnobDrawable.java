package northern.captain.gamecore.glx.tools;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

/**
 * Created by leo on 25.11.15.
 */
public class VScrollKnobDrawable implements Drawable
{
    Drawable activeDrawable;
    Drawable inactiveDrawable;
    VScrollDrawable vScrollDrawable;

    float width;
    float height;
    float deltaY;
    float totalHeight;
    float startY;

    public VScrollKnobDrawable(Drawable active, Drawable inactive, int delta)
    {
        activeDrawable = active;
        inactiveDrawable = inactive;
        deltaY = delta;
        width = active.getMinWidth() < inactive.getMinWidth() ? inactive.getMinWidth() : active.getMinWidth();
        height = active.getMinHeight() < inactive.getMinHeight() ? inactive.getMinHeight() : active.getMinHeight();
    }

    /**
     * Draws this drawable at the specified bounds. The drawable should be tinted with {@link Batch#getColor()}, possibly by mixing
     * its own color.
     *
     * @param batch
     * @param x
     * @param y
     * @param width
     * @param drawHeight
     */
    @Override
    public void draw(Batch batch, float x, float y, float width, float drawHeight)
    {
        vScrollDrawable.doDraw(batch, y + drawHeight < startY + totalHeight, y > startY);

        int num = (int)((totalHeight - deltaY) / height);
        float dy = startY + deltaY;
        float fy;
        for(int i=0;i< num;i++)
        {
            fy = dy + i * height;
            if(fy > y && fy < y + drawHeight)
            {
                activeDrawable.draw(batch, x, fy, width, height);
            } else
            {
                inactiveDrawable.draw(batch, x, fy, width, height);
            }
        }
    }

    @Override
    public float getLeftWidth()
    {
        return 0;
    }

    @Override
    public void setLeftWidth(float leftWidth)
    {

    }

    @Override
    public float getRightWidth()
    {
        return 0;
    }

    @Override
    public void setRightWidth(float rightWidth)
    {

    }

    @Override
    public float getTopHeight()
    {
        return 0;
    }

    @Override
    public void setTopHeight(float topHeight)
    {

    }

    @Override
    public float getBottomHeight()
    {
        return 0;
    }

    @Override
    public void setBottomHeight(float bottomHeight)
    {

    }

    @Override
    public float getMinWidth()
    {
        return width;
    }

    @Override
    public void setMinWidth(float minWidth)
    {
        width = minWidth;
    }

    @Override
    public float getMinHeight()
    {
        return height;
    }

    @Override
    public void setMinHeight(float minHeight)
    {
        height = minHeight;
    }

    public void setTotalHeight(float totalHeight)
    {
        this.totalHeight = totalHeight;
    }

    public void setStartY(float startY)
    {
        this.startY = startY;
    }

    public void setvScrollDrawable(VScrollDrawable vScrollDrawable)
    {
        this.vScrollDrawable = vScrollDrawable;
    }
}
