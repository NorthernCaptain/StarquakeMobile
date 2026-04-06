package northern.captain.quadronia.gfx;

import com.badlogic.gdx.graphics.g2d.Batch;

/**
 * Created by leo on 19.04.15.
 */
public interface ISimpleDrawable extends IGraphicsInit
{
    void draw(Batch batch, float parentAlpha);
    void drawFBO(Batch fboBatch, float parentAlpha);
}
