package northern.captain.quadronia.gfx;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;

import northern.captain.gamecore.glx.NContext;
import northern.captain.gamecore.glx.tools.loaders.XMLLayoutLoader;

/**
 * Created by leo on 11.03.15.
 */
public class TopBackgroundWidget extends Widget
{
    private Sprite background;
    private float backWidth;
    private float backHeight;
    
    public void initGraphics(XMLLayoutLoader loader, TextureAtlas atlas)
    {
        background = loader.newSprite("back", atlas);        
        backWidth = background.getWidth();
        backHeight = background.getHeight();        
    }

    @Override
    public void draw(Batch batch, float parentAlpha)
    {
        super.draw(batch, parentAlpha);
        batch.draw(background, NContext.current.centerX - backWidth/2, NContext.current.screenHeight - backHeight);
    }

    @Override
    public float getMinWidth () {
        return backWidth;
    }

    @Override
    public float getMinHeight () {
        return backHeight;
    }

    @Override
    public float getPrefWidth () {
        return backWidth;
    }

    @Override
    public float getPrefHeight () {
        return backHeight;
    }

    @Override
    public float getMaxWidth () {
        return backWidth;
    }

    @Override
    public float getMaxHeight () {
        return backHeight;
    }
    
}
