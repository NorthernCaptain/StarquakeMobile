package northern.captain.quadronia.gfx;

import com.badlogic.gdx.graphics.g2d.Batch;

import northern.captain.quadronia.game.BoundingBox;
import northern.captain.gamecore.glx.tools.loaders.XMLLayoutLoader;

/**
 * Created by leo on 09.05.15.
 */
public class RawButton extends BoundingBox implements ISimpleDrawable, ICursorEventListener
{
    protected boolean isDragging = false;
    protected boolean isDraggable = false;

    protected boolean pressed = false;
    protected boolean active = true;

    public RawButton()
    {
    }

    public RawButton(int x1, int y1, int x2, int y2)
    {
        super(x1, y1, x2, y2);
    }

    @Override
    public boolean doTouchDown(int fx, int fy)
    {
        if(!isIn(fx, fy)) return false;

        if(isDraggable)
        {
            isDragging = true;
        }

        pressed = true;

        return true;
    }

    @Override
    public boolean doDrag(int x, int y)
    {
        return isDragging;
    }

    @Override
    public boolean doTouchUp(int fx, int fy)
    {
        pressed = false;

        if(!isDraggable) return isIn(fx, fy);

        boolean drag = isDragging;
        isDragging = false;

        return drag;
    }

    @Override
    public void draw(Batch batch, float parentAlpha)
    {

    }

    @Override
    public void drawFBO(Batch fboBatch, float parentAlpha)
    {

    }

    @Override
    public void initGraphics(XMLLayoutLoader xmlLayoutLoader, GraphicsInitContext gContext)
    {

    }
}
