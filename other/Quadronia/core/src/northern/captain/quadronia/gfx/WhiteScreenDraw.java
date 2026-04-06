package northern.captain.quadronia.gfx;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;

import northern.captain.quadronia.game.Engine;
import northern.captain.quadronia.game.IGraphicsContext;
import northern.captain.gamecore.glx.tools.loaders.XMLLayoutLoader;

/**
 * Created by leo on 30.07.15.
 */
public class WhiteScreenDraw extends Actor implements IAnimDraw
{
    Sprite sprite;
    Engine game;
    Action action;
    Runnable onAnimationDone;

    public WhiteScreenDraw(Engine game)
    {
        this.game = game;
        setAction(Actions.scaleTo(36, 36, 0.4f, Interpolation.exp5In));
    }

    @Override
    public void initGraphics(XMLLayoutLoader xmlLayoutLoader, TextureAtlas atlas)
    {
        sprite = xmlLayoutLoader.newSprite("bcircle", atlas);
        IGraphicsContext graphicsContext = game.getGraphicsContext();
        sprite.setCenter(graphicsContext.getFieldCenterX(), graphicsContext.getFieldCenterY());
        setPosition(sprite.getX(), sprite.getY());
    }

    @Override
    public void draw(Batch batch, float parentAlpha)
    {
        sprite.draw(batch, parentAlpha*color.a);
    }

    @Override
    public void parentWidget(Actor actor)
    {

    }

    @Override
    public boolean hasLogicObject(Object obj)
    {
        return false;
    }

    public void processRemoval(Engine game)
    {

    }

    @Override
    public void act(float delta)
    {
        if(action != null
            && action.act(delta))
        {
            action = null;
            if(onAnimationDone != null)
            {
                onAnimationDone.run();
            }
        }
    }

    public Action getAction()
    {
        return action;
    }

    public void setAction(Action action)
    {
        this.action = action;
        action.setActor(this);
    }

    @Override
    public void setScale(float scaleX, float scaleY)
    {
        super.setScale(scaleX, scaleY);
        sprite.setScale(scaleX, scaleY);
    }

    public void setOnAnimationDone(Runnable onAnimationDone)
    {
        this.onAnimationDone = onAnimationDone;
    }

    Color color = new Color(1, 1, 1, 1);
    @Override
    public Color getColor()
    {
        return color;
    }
}
