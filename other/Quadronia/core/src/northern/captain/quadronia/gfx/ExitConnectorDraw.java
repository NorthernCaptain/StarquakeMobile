package northern.captain.quadronia.gfx;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;

import northern.captain.quadronia.game.Engine;
import northern.captain.quadronia.game.IGraphicsContext;
import northern.captain.quadronia.game.perks.ElePerk;
import northern.captain.quadronia.game.perks.ExitConnector;
import northern.captain.gamecore.glx.tools.Animations;
import northern.captain.gamecore.glx.tools.loaders.XMLLayoutLoader;
import northern.captain.tools.Helpers;

/**
 * Created by leo on 23.07.15.
 */
public class ExitConnectorDraw extends ElePerkDraw
{
    Sprite glow;
    float glowAlpha;
    float startRotation = 0;
    ExitConnector connector;

    public ExitConnectorDraw(ElePerk perk)
    {
        super(perk);
        connector = (ExitConnector)perk;

        startDelay = connector.getSequenceNumber() * 0.4f;
        maxTime += startDelay;
    }

    @Override
    public void initGraphics(XMLLayoutLoader xmlLayoutLoader, TextureAtlas atlas)
    {
        super.initGraphics(xmlLayoutLoader, atlas);

        glow = xmlLayoutLoader.newSprite("exit1glow", atlas);
        glow.setOriginCenter();
        glowAlpha = 1.0f;

        if(perk.cell.sideUp())
        {
            startRotation = 180;
            glow.setRotation(startRotation);
            sprite.setRotation(startRotation);
        }

        liveInterpolation = Interpolation.pow2In;
        spin60 = new Animations.Spin60();
    }

    @Override
    public void draw(Batch batch, float parentAlpha)
    {
        glow.draw(batch, parentAlpha);
        super.draw(batch, parentAlpha);
    }

    @Override
    public void processRemoval(Engine game)
    {
        engine = game;
        maxTime = 0.8f;
        setMode(MODE_DIEING);
    }

    @Override
    public void parentWidget(Actor actor)
    {
        super.parentWidget(actor);
        glow.setCenter(cx, cy);
    }

    @Override
    protected void onModeDead()
    {
        super.onModeDead();
        if(engine != null)
        {
            IGraphicsContext graphicsContext = engine.getGraphicsContext();
            graphicsContext.removeFromCycle(this);
        }
    }

    @Override
    protected void actStarting(float delta)
    {
        float value = time < startDelay ? 0 : startInterpolation.apply((time - startDelay) / (maxTime - startDelay));
        if (mode == MODE_LIVE)
        {
            sprite.setScale(1);
            glow.setScale(1);
        } else
        {
            sprite.setScale(value);
            glow.setScale(value);
        }
    }

    @Override
    protected void actLiving(float delta)
    {
        float value = 0;
        if(startDelay > 0)
        {
            if(time > startDelay)
            {
                time -= startDelay;
                startDelay = 0;
            }
        } else
        {
            value = liveInterpolation.apply(time / maxTime);
        }
        glow.setAlpha(1.0f - value);
        if(spinAction != null && spinAction.act(delta))
        {
            spinAction.restart();
        }
    }

    public void setLiveAnimation(Action action)
    {
        if(spinAction != null)
        {
            spinAction.reset();
        }
        spinAction = action;
        spinAction.setActor(actor);
    }

    @Override
    protected void onModeLive()
    {
        super.onModeLive();
        sprite.setScale(1);
        glow.setScale(1);
        actor.setPosition(sprite.getX(), sprite.getY());
        maxTime = 1.5f;
        startDelay = Helpers.RND.nextFloat();
        spinAction = spin60.create(12f + startDelay*5f);
        spinAction.setActor(actor);
        time = 0;
    }

    private Animations.Spin60 spin60;
    private Action spinAction;

    private Actor actor = new Actor()
    {
        @Override
        public void setScale(float scaleX, float scaleY)
        {
            super.setScale(scaleX, scaleY);
            sprite.setScale(scaleX);
            glow.setScale(scaleX);
        }

        @Override
        public void rotateBy(float amountInDegrees)
        {
            super.rotateBy(amountInDegrees);
            float angle = getRotation() + startRotation;
            sprite.setRotation(angle);
            glow.setRotation(angle);
        }

        @Override
        public void setPosition(float x, float y, int alignment)
        {
            super.setPosition(x, y, alignment);
            sprite.setCenter(x, y);
            glow.setCenter(x, y);
        }
    };
}
