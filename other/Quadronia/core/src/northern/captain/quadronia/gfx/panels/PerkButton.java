package northern.captain.quadronia.gfx.panels;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Interpolation;

import northern.captain.gamecore.glx.tools.MyLabel;
import northern.captain.gamecore.glx.tools.loaders.XMLLayoutLoader;
import northern.captain.quadronia.game.BoundingBox;
import northern.captain.quadronia.game.core.Game;
import northern.captain.quadronia.gfx.GraphicsInitContext;
import northern.captain.quadronia.gfx.ICursorEventListener;

/**
 * Created by leo on 19.09.15.
 */
public class PerkButton implements ICursorEventListener
{
    Sprite sprite;
    Sprite icon;
    MyLabel label;
    Game game;
    String name;
    BoundingBox bbox, lbox, ibox;
    boolean selected = false;
    float percent;
    float maxTime = 0.3f;
    float time;
    float maxDeltaY;
    boolean enabled = true;
    int keyIdx;

    private static final int NONE = 0;
    private static final int TO_SELECT = 1;
    private static final int FROM_SELECT = 2;
    int animState = NONE;


    public boolean isSelected()
    {
        return selected;
    }

    public void setSelected(boolean selected)
    {
        if(this.selected == selected) return;
        this.selected = selected;
        animState = selected ? TO_SELECT : FROM_SELECT;
        time = 0;
    }

    public PerkButton(String name, Game game, int key)
    {
        this.game = game;
        this.name = name;
        this.keyIdx = key;
    }

    public void initGraphics(XMLLayoutLoader loader, GraphicsInitContext gContext)
    {
        sprite = loader.newSprite(name, gContext.atlas);
        sprite.setPosition(sprite.getX() - gContext.deltaX, sprite.getY() + gContext.deltaY);
        icon = loader.newSprite(name+"icn", gContext.atlas);
        icon.setPosition(icon.getX() - gContext.deltaX, icon.getY() + gContext.deltaY);
        label = loader.newLabel(name + "lbl");
        label.setPosition(label.getX() - gContext.deltaX, label.getY() + gContext.deltaY);
        bbox = new BoundingBox((int)sprite.getX(), (int)(sprite.getY()),
            (int)(sprite.getX() + sprite.getWidth()), (int)(sprite.getY() + sprite.getHeight()));
        lbox = new BoundingBox((int)label.getX(), (int)(label.getY()),
            (int)(label.getX() + label.getWidth()), (int)(label.getY() + label.getHeight()));
        ibox = new BoundingBox((int)icon.getX(), (int)(icon.getY()),
            (int)(icon.getX() + icon.getWidth()), (int)(icon.getY() + icon.getHeight()));
        maxDeltaY = sprite.getHeight() /8;
        update();
    }

    public void update()
    {
        label.setText(Integer.toString(game.getContext().getBonusByIdx(keyIdx)));
    }

    public void draw(Batch batch, float parentAlpha)
    {
        sprite.draw(batch, parentAlpha);
        icon.draw(batch, parentAlpha);
        label.draw(batch, parentAlpha);
    }

    public void act(float delta)
    {
        if(animState == NONE) return;

        int curAnimState = animState;

        time += delta;

        if(time > maxTime)
        {
            time = maxTime;
            animState = NONE;
        }

        percent = Interpolation.swingIn.apply(time / maxTime);

        if(curAnimState == TO_SELECT)
        {
            sprite.setPosition(bbox.xBox1, bbox.yBox1 - maxDeltaY * percent);
            icon.setPosition(ibox.xBox1, ibox.yBox1 - maxDeltaY * percent);
            label.setPosition(lbox.xBox1, lbox.yBox1 - maxDeltaY * percent);
        } else
        {
            sprite.setPosition(bbox.xBox1, bbox.yBox1 - maxDeltaY * (1 - percent));
            icon.setPosition(ibox.xBox1, ibox.yBox1 - maxDeltaY * (1 - percent));
            label.setPosition(lbox.xBox1, lbox.yBox1 - maxDeltaY * (1 - percent));
        }
    }

    /**
     * Process drag event
     *
     * @param fx
     * @param fy
     * @return true if event was consumed
     */
    @Override
    public boolean doDrag(int fx, int fy)
    {
        return false;
    }

    /**
     * Process touch down event if it's yours
     *
     * @param fx
     * @param fy
     * @return true if event was consumed
     */
    @Override
    public boolean doTouchDown(int fx, int fy)
    {
        if(enabled && bbox.isIn(fx, fy))
        {
            setSelected(!selected);
            onClick();
            return true;
        }
        return false;
    }

    /**
     * Process 'release' touch up event
     *
     * @param fx
     * @param fy
     * @return true if event was consumed
     */
    @Override
    public boolean doTouchUp(int fx, int fy)
    {
        return false;
    }

    public void onClick()
    {

    }
}
