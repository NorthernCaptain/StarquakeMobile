package northern.captain.quadronia.gfx.widget;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import northern.captain.gamecore.glx.NContext;
import northern.captain.gamecore.glx.tools.loaders.XMLLayoutLoader;
import northern.captain.quadronia.gfx.GraphicsInitContext;
import northern.captain.quadronia.gfx.IGraphicsInit;
import northern.captain.quadronia.screens.IPagable;
import northern.captain.tools.Log;

public class PagerWidget extends Group implements IGraphicsInit
{
    private Image[] dots;
    private int totalPages;
    private String name;
    private int activePageIdx;
    private IPagable[] pages;

    private static final float touchRadius = 2;
    private float touchSquare;
    private static final float AnimDuration = 0.3f;

    public PagerWidget(String name, int totalPages)
    {
        this.totalPages = totalPages;
        dots = new Image[totalPages];
        pages = new IPagable[totalPages];
        this.name = name;
        activePageIdx = totalPages / 2;
    }

    public PagerWidget setPage(int idx, IPagable page)
    {
        pages[idx] = page;
        return this;
    }

    @Override
    public void initGraphics(XMLLayoutLoader loader, GraphicsInitContext gContext)
    {
        this.clear();
        touchSquare = NContext.current.fScale(touchRadius);
        loader.applyTo(this, name + "pager");
        for(int i=0;i<totalPages;i++)
        {
            dots[i] = loader.newImage(name + "page" + i, gContext.atlas);
            this.addActor(dots[i]);
        }

        setActiveIndicator(activePageIdx);

        setTouchable(Touchable.enabled);
        this.addListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button)
            {
                Log.i("touch", "touchDown " + x + " " + y);
                onTouchDown(x, y);
                return super.touchDown(event, x, y, pointer, button);
            }

            @Override
            public void touchDragged(InputEvent event, float x, float y, int pointer)
            {
                Log.i("touch", "touchDrag " + x + " " + y);
                onTouchDrag(x, y);
                super.touchDragged(event, x, y, pointer);
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button)
            {
                Log.i("touch", "touchUp " + x + " " + y);
                onTouchUp(x, y);
                super.touchUp(event, x, y, pointer, button);
            }
        });
    }

    private float touchPx, lastPx;
    private long touchTime;
    private boolean touchBlocked = false;
    private void onTouchDown(float x, float y)
    {
        if(isScrolling) return;
        touchPx = lastPx = x;
        touchTime = System.currentTimeMillis();
    }



    private void onTouchDrag(float x, float y)
    {
        if(isScrolling) return;
        if(Math.abs(lastPx - x) < touchSquare) return;

        float deltaX = x - touchPx;
        lastPx = x;

        if(touchBlocked)
        {
            //touchPx = x;
            return;
        }

        float maxD = NContext.current.screenWidth * 0.7f;

        float perc = deltaX/maxD;
        if(perc > 1) perc = 1;
        if(perc < -1) perc = -1;

        int nextIdx = perc >= 0 ? activePageIdx - 1 : activePageIdx + 1;
        if(nextIdx >= totalPages || nextIdx < 0) return;

        float pos = perc * NContext.current.screenWidth;
        Log.i("touch", "Drag to: " + pos);
        if(pos >= 0)
        {
            pages[activePageIdx].setPagePosition(pos, 0);
            pages[nextIdx].setPagePosition(pos - NContext.current.screenWidth, 0);
        } else
        {
            pages[activePageIdx].setPagePosition(pos, 0);
            pages[nextIdx].setPagePosition(pos + NContext.current.screenWidth, 0);
        }
    }

    private void onTouchUp(float x, float y)
    {
        if(isScrolling) return;
        float deltaX = x - touchPx;

        float maxD = NContext.current.screenWidth * 0.7f;

        float perc = deltaX/maxD;
        if(perc > 1) perc = 1;
        if(perc < -1) perc = -1;

        int nextIdx = perc >= 0 ? activePageIdx - 1 : activePageIdx + 1;
        if(nextIdx >= totalPages || nextIdx < 0) return;

        float aperc = Math.abs(perc);

        touchBlocked = true;
        if(perc < 0.3 && perc > -0.3)
        {
            Actor curActor = pages[activePageIdx].getPageActor();
            Actor nextActor = pages[nextIdx].getPageActor();
            curActor.addAction(Actions.sequence(Actions.moveTo(0, curActor.getY(), 0.2f, Interpolation.pow3Out),
                    Actions.run(() -> touchBlocked = false)));
            nextActor.addAction(Actions.moveTo(perc < 0 ? NContext.current.screenWidth : -NContext.current.screenWidth, nextActor.getY(), 0.2f, Interpolation.pow3Out));
        } else
        {
            scrollOne(perc >= 0 ? -1 : 1, AnimDuration - aperc * AnimDuration, () -> {this.setCurrent(nextIdx); touchBlocked = false;});
        }
    }

    private void setActiveIndicator(int idx)
    {
        for(int i=0;i<totalPages;i++) {
            if(i == idx)
            {
                dots[idx].setScale(0.7f);
                dots[idx].getColor().a = 1;
                pages[idx].setPageCurrent(true);
                pages[idx].setFromCurrentIdx(0);
                pages[idx].setPagePosition(0, 0);
            } else
            {
                dots[i].setScale(0.5f);
                dots[i].getColor().a = 0.5f;
                pages[i].setPageCurrent(false);
                pages[i].setFromCurrentIdx(i - idx);
                pages[i].setPagePosition((i-idx)* NContext.current.screenWidth, 0);
            }
        }
    }

    public void setCurrent(int idx)
    {
        setActiveIndicator(idx);
        this.activePageIdx = idx;
    }

    public void scrollTo(int idx)
    {
        int delta = idx - activePageIdx;
        scrollOne(delta < 0 ? -1 : 1, Math.abs(delta) > 1 ?
                () -> {
                    scrollOne(delta < 0 ? -1 : 1, null);
                } : null);
    }

    public void scrollOne(int direction, Runnable onDone)
    {
        scrollOne(direction, AnimDuration + 0.1f, onDone);
    }

    private boolean isScrolling = false;

    public void scrollOne(int direction, float duration, Runnable onDone)
    {
        if(duration < 0.002 && onDone != null)
        {
            isScrolling = false;
            onDone.run();
            return;
        }

        isScrolling = true;
        final int nextIdx = activePageIdx + direction;
        Actor curActor = pages[activePageIdx].getPageActor();
        Actor nextActor = pages[nextIdx].getPageActor();

        nextActor.addAction(Actions.sequence(Actions.moveTo(0, nextActor.getY(), duration, Interpolation.pow3Out),
                Actions.run(() -> {
                    setCurrent(nextIdx);

                }), Actions.delay(duration), Actions.run(() -> {
                    isScrolling = false;
                    if(onDone != null) onDone.run();
                })));

        curActor.addAction(Actions.moveTo(NContext.current.screenWidth * -direction, curActor.getY(), duration, Interpolation.pow3Out));
    }
}
