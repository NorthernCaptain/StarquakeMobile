package northern.captain.gamecore.glx.tools;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

/**
 * Created by leo on 25.11.15.
 */
public class VScrollDrawable implements Drawable
{
    Drawable arrowUpActive;
    Drawable arrowUpInactive;
    Drawable arrowDnActive;
    Drawable arrowDnInactive;

    VScrollKnobDrawable knobDrawable;
    float x, y, w, h;

    public VScrollDrawable(Drawable arrowUpActive,
                           Drawable arrowUpInactive,
                           Drawable arrowDnActive,
                           Drawable arrowDnInactive,
                           VScrollKnobDrawable knobDrawable)
    {
        this.knobDrawable = knobDrawable;
        this.arrowUpActive = arrowUpActive;
        this.arrowDnActive = arrowDnActive;
        this.arrowDnInactive = arrowDnInactive;
        this.arrowUpInactive = arrowUpInactive;
        knobDrawable.setvScrollDrawable(this);
    }

    /**
     * Draws this drawable at the specified bounds. The drawable should be tinted with {@link Batch#getColor()}, possibly by mixing
     * its own color.
     *
     * @param batch
     * @param x
     * @param y
     * @param width
     * @param height
     */
    @Override
    public void draw(Batch batch, float x, float y, float width, float height)
    {
        this.x = x;
        this.y = y;
        w = width;
        h = height;
        knobDrawable.setTotalHeight(height);
        knobDrawable.setStartY(y);
    }

    public void doDraw(Batch batch, boolean upActive, boolean dnActive)
    {
        Drawable drawable = upActive ? arrowUpActive : arrowUpInactive;

        drawable.draw(batch, x, y + h - drawable.getMinHeight(), w, drawable.getMinHeight());

        drawable = dnActive ? arrowDnActive : arrowDnInactive;

        drawable.draw(batch, x, y, w, drawable.getMinHeight());
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
        return arrowUpActive.getMinWidth();
    }

    @Override
    public void setMinWidth(float minWidth)
    {

    }

    @Override
    public float getMinHeight()
    {
        return arrowUpActive.getMinHeight();
    }

    @Override
    public void setMinHeight(float minHeight)
    {

    }
}
