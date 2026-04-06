package northern.captain.quadronia.gfx;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;

/**
 * Created by leo on 4/21/15.
 */
public class GraphicsInitContext
{
    public TextureAtlas atlas;
    public TextureAtlas bgAtlas;
    public int deltaY;
    public int deltaX;

    public GraphicsInitContext(TextureAtlas atlas)
    {
        this.atlas = atlas;
    }

    public GraphicsInitContext()
    {
    }

    public GraphicsInitContext setAtlas(TextureAtlas atlas)
    {
        this.atlas = atlas;
        return this;
    }

    public GraphicsInitContext setBgAtlas(TextureAtlas bgAtlas)
    {
        this.bgAtlas = bgAtlas;
        return this;
    }
}
