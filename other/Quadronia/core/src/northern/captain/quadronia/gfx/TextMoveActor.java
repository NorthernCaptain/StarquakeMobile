package northern.captain.quadronia.gfx;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.badlogic.gdx.utils.Align;

import northern.captain.gamecore.glx.NContext;
import northern.captain.gamecore.glx.res.SharedRes;

/**
 * Created by leo on 02.04.15.
 */
public class TextMoveActor extends Widget
{
    String text;

    float tx, ty;
    float startX, startY;
    float totalTime;
    float maxTime;
    float stepX = 0, stepY = 100;
    float percent;

    BitmapFont font;
    BitmapFontCache cache;

    public TextMoveActor(String fontName, float stepX, float stepY, float maxTime)
    {
        this.font = SharedRes.instance.font(fontName);
        this.stepX = stepX;
        this.stepY = stepY;
        this.maxTime = maxTime;
        cache = new BitmapFontCache(font, true);
    }

    public void init(float startX, float startY, String text)
    {
        init(startX, startY, text, 0.0f, 0.7f, 0.93f);
    }

    public void init(float startX, float startY, String text, float r, float g, float b)
    {
        startX = startX  - NContext.current.centerX/2;

        this.startX = startX;
        this.startY = startY;
        this.text = text;
        percent = 0;
        totalTime = 0;
        tx = startX;
        ty = startY;

        cache.setColor(r, g, b, 1);
        cache.clear();
        cache.setPosition(tx, ty);
        cache.addText(text, tx, ty, NContext.current.centerX, Align.center, false);
    }

    public void show()
    {
        if(this.hasParent())
            return;

        if(NContext.current.currentStage != null)
        {
            NContext.current.currentStage.addActor(this);
            NContext.current.addRefresh();
        }
    }

    public void hide()
    {
        if(this.hasParent())
        {
            this.remove();
            NContext.current.subRefresh();
            setParent(null);
        }
    }

    @Override
    public void act(float delta)
    {
        super.act(delta);

        if(totalTime >= maxTime)
        {
            hide();
            return;
        }

        totalTime += delta;
        totalTime = totalTime > maxTime ? maxTime : totalTime;

        percent = totalTime / maxTime;

        tx = startX + stepX * percent;
        ty = startY + stepY * percent;

        cache.setPosition(tx, ty);
        cache.setAlphas(1 - percent);
    }

    @Override
    public void draw(Batch batch, float parentAlpha)
    {
        super.draw(batch, parentAlpha);
        cache.draw(batch);
    }

    public float getPrefWidth () {
        return 0;
    }

    public float getPrefHeight () {
        return 0;
    }

    public float getMaxWidth () {
        return 0;
    }

    public float getMaxHeight () {
        return 0;
    }

    @Override
    public boolean fire(Event event)
    {
        return false;
    }
}
