package northern.captain.quadronia.gfx;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Actor;

import northern.captain.quadronia.game.Engine;
import northern.captain.quadronia.game.IGraphicObject;
import northern.captain.gamecore.glx.tools.loaders.XMLLayoutLoader;

/**
 * Created by leo on 03.04.15.
 */
public interface IDraw extends IGraphicObject
{
    void initGraphics(XMLLayoutLoader xmlLayoutLoader, TextureAtlas atlas);
    void draw(Batch batch, float parentAlpha);
    void parentWidget(Actor actor);
}
