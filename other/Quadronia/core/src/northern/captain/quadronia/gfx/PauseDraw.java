package northern.captain.quadronia.gfx;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;

import northern.captain.gamecore.glx.ISoundMan;
import northern.captain.gamecore.glx.NContext;
import northern.captain.gamecore.glx.NCore;
import northern.captain.gamecore.glx.tools.ClickListenerPrepared;
import northern.captain.quadronia.game.Engine;
import northern.captain.gamecore.glx.tools.loaders.XMLLayoutLoader;
import northern.captain.quadronia.game.events.EGameExitRequest;

/**
 * Created by leo on 26.04.15.
 */
public class PauseDraw implements IAnimDraw, ICursorEventListener
{
    Sprite sprite;
    Interpolation interpolation;
    private float maxTime = 0.5f;
    private float time = 0;

    Sprite exitSprite;
    RawButton exitButton;

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
            sprite.setScale(1 + value * 6);
            exitSprite.setAlpha(Math.max((1 - value)*0.7f, 0));
        }

    }

    @Override
    public void initGraphics(XMLLayoutLoader layXml, TextureAtlas atlas)
    {
        sprite = layXml.newSprite("pause", atlas);
        sprite.setOriginCenter();
        sprite.setScale(6);
        interpolation = Interpolation.pow3Out;
        exitSprite = layXml.newSprite("exitbut", atlas);
        exitSprite.setOriginCenter();
    }

    @Override
    public void draw(Batch batch, float parentAlpha)
    {
        sprite.draw(batch);
        exitSprite.draw(batch);
    }

    @Override
    public void parentWidget(Actor actor)
    {
//        Vector2 vector2 = new Vector2(sprite.getX(), Math.round(actor.getHeight() - sprite.getY()));
//        Vector2 vec = actor.localToStageCoordinates(vector2);
        sprite.setOriginCenter();
        sprite.setScale(6);
        sprite.setCenter(NContext.current.centerX, NContext.current.centerY);
        exitSprite.setCenter(NContext.current.centerX, 180);
        exitSprite.setAlpha(0);
        exitButton = new RawButton((int) exitSprite.getX(), (int) exitSprite.getY(),
                (int) (exitSprite.getX() + exitSprite.getWidth()),
                (int) (exitSprite.getY() + exitSprite.getHeight())) {
            @Override
            public boolean doTouchUp(int fx, int fy) {
                if (super.doTouchUp(fx, fy)) {
                    NCore.busPost(new EGameExitRequest());
                    return true;
                }
                return false;
            }
        };
        time = 0;
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

    @Override
    public boolean doTouchDown(int fx, int fy) {
        return exitButton != null && exitButton.doTouchDown(fx, fy);
    }

    @Override
    public boolean doDrag(int fx, int fy) {
        return false;
    }

    @Override
    public boolean doTouchUp(int fx, int fy) {
        return exitButton != null && exitButton.doTouchUp(fx, fy);
    }
}
