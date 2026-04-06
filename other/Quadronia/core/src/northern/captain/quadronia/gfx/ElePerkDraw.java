package northern.captain.quadronia.gfx;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;

import northern.captain.quadronia.game.Engine;
import northern.captain.quadronia.game.perks.ElePerk;
import northern.captain.gamecore.glx.tools.loaders.XMLLayoutLoader;

/**
 * Created by leo on 03.04.15.
 */
public abstract class ElePerkDraw implements IAnimDraw
{
    public static final int MODE_STARTING = 1;
    public static final int MODE_LIVE = 2;
    public static final int MODE_DIEING = 3;
    public static final int MODE_DEAD = 4;


    protected Engine engine;
    protected ElePerk perk;
    protected Sprite  sprite;

    protected float cx, cy;

    protected float maxTime = 1.5f;
    protected float time = 0;
    protected float startDelay = 0;
    protected float stepFactor = 1.0f;

    protected int mode = MODE_STARTING;

    protected Interpolation startInterpolation;
    protected Interpolation stopInterpolation;
    protected Interpolation liveInterpolation;

    public ElePerkDraw(ElePerk perk)
    {
        this.perk = perk;
    }

    @Override
    public void initGraphics(XMLLayoutLoader xmlLayoutLoader, TextureAtlas atlas)
    {
        String name = perk.getSpriteName();

        sprite = xmlLayoutLoader.newSprite(name, atlas);

        sprite.setOriginCenter();
        cx = perk.cell.x0;
        cy = perk.cell.y0;

        startInterpolation = Interpolation.elasticOut;
        stopInterpolation = Interpolation.fade;

        time = 0;
        mode = MODE_STARTING;
    }

    public void parentWidget(Actor actor)
    {
        Vector2 vector2 = new Vector2(cx, actor.getHeight() - cy);
        Vector2 vec = actor.localToStageCoordinates(vector2);
        cx = vec.x;
        cy = vec.y;
        sprite.setCenter(cx, cy);
    }

    @Override
    public boolean hasLogicObject(Object obj)
    {
        return perk == obj;
    }

    @Override
    public void draw(Batch batch, float parentAlpha)
    {
        sprite.draw(batch, parentAlpha);
    }

    @Override
    public void act(float delta)
    {
        if(mode == MODE_DEAD) return;

        time += delta*stepFactor;
        if(time > maxTime || time < 0)
        {
            time = time  < 0 ? 0 : maxTime;
            switch (mode)
            {
                case MODE_STARTING:
                    actStarting(delta);
                    mode = MODE_LIVE;
                    onModeLive();
                    return;
                case MODE_DIEING:
                    actDieing(delta);
                    mode = MODE_DEAD;
                    onModeDead();
                    return;
                case MODE_LIVE:
                    stepFactor = -stepFactor;
                    break;
            }
        }

        switch (mode)
        {
            case MODE_STARTING:
                actStarting(delta);
                break;
            case MODE_DIEING:
                actDieing(delta);
            case MODE_LIVE:
                if(liveInterpolation != null)
                {
                    actLiving(delta);
                }
                break;
        }
    }

    protected void onModeLive() {}
    protected void onModeDead() {}

    protected void actLiving(float delta)
    {

    }

    protected void actStarting(float delta)
    {
        float value = time < startDelay ? 0 : startInterpolation.apply(time / maxTime);
        if (mode == MODE_LIVE)
        {
            sprite.setScale(1);
        } else
        {
            sprite.setScale(value);
        }
    }

    protected void actDieing(float delta)
    {
        float value = stopInterpolation.apply(1 - time / maxTime);
        sprite.setAlpha(value);
    }

    public void setMode(int mode)
    {
        time = 0;
        this.mode = mode;
        stepFactor = 1.0f;
    }
}
