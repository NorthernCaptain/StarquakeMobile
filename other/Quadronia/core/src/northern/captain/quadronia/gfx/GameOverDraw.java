package northern.captain.quadronia.gfx;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;

import northern.captain.gamecore.glx.NContext;
import northern.captain.quadronia.game.Engine;
import northern.captain.gamecore.glx.tools.loaders.XMLLayoutLoader;

/**
 * Created by leo on 26.04.15.
 */
public class GameOverDraw implements IAnimDraw
{
    Sprite sprite;
    Interpolation interpolation;
    private float maxTime = 0.5f;
    private float time = 0;

    private boolean startDone = false;

    @Override
    public void act(float delta)
    {
        if(startDone) return;

        time += delta;
        if(time > maxTime)
        {
            time = maxTime;
            startDone = true;
        }

        float value = interpolation.apply(1 - time/maxTime);
        if(!startDone)
        {
            sprite.setScale(1.5f + value * 6);
        } else
        {
            sprite.setScale(1.5f);
        }
    }

    @Override
    public void initGraphics(XMLLayoutLoader layXml, TextureAtlas atlas)
    {
        sprite = layXml.newSprite("over", atlas);
        sprite.setOriginCenter();
        sprite.setScale(6);
        interpolation = Interpolation.pow3Out;
    }

    @Override
    public void draw(Batch batch, float parentAlpha)
    {
        sprite.draw(batch);
    }

    @Override
    public void parentWidget(Actor actor)
    {
//        Vector2 vector2 = new Vector2(sprite.getX(), Math.round(actor.getHeight() - sprite.getY()));
//        Vector2 vec = actor.localToStageCoordinates(vector2);
        sprite.setCenter(NContext.current.centerX, NContext.current.centerY);
    }

    @Override
    public boolean hasLogicObject(Object obj)
    {
        return false;
    }

    @Override
    public void processRemoval(Engine game)
    {

    }

    public void start()
    {
        startDone = false;
        time = 0;
    }
}
