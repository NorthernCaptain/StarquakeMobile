package northern.captain.quadronia.game.core;

import com.badlogic.gdx.utils.Array;

import northern.captain.gamecore.glx.ISoundMan;
import northern.captain.gamecore.glx.NCore;

/**
 * Created by leo on 03.09.15.
 */
public class QuadCollector extends QuadCheck
{
    public static final int NEW_QUAD_SEQ = 1;
    public static final int QUAD_ADDED_SEQ = 2;
    public static final int QUAD_HIT = 3;
    public static final int QUAD_CLEAR_SEQ = 4;


    Array<Quad> selected = new Array<Quad>(5);

    public QuadArea area;

    public QuadCollector(Field field)
    {
        this.field = field;
    }

    public int addQuad(Quad quadToAdd)
    {
        matchedBy.reset();

        if(selected.size == 0)
        {
            selected.add(quadToAdd);
            NCore.instance().getSoundman().playSound(ISoundMan.SND_JUST_CLICK, false);
            return NEW_QUAD_SEQ;
        }

        Quad ourQuad = selected.get(0);

        if(ourQuad.theSame(quadToAdd))
        {
            clearSelected();
            NCore.instance().getSoundman().playSound(ISoundMan.SND_JUST_CLICK, false);
            return QUAD_CLEAR_SEQ;
        }

        if(ourQuad.match2(quadToAdd, curMatch))
        {
            for(Quad quad: selected)
            {
                if(quad.isDiagonally(quadToAdd))
                {
                    if(checkHit(quad, quadToAdd))
                    {
                        selected.add(quadToAdd);
                        grabQuads(quad, quadToAdd);
                        NCore.instance().getSoundman().playSound(ISoundMan.SND_QUAD_AREA_DISSAPEAR, true);
                        return QUAD_HIT;
                    }

                    selected.clear();
                    selected.add(quadToAdd);
                    NCore.instance().getSoundman().playSound(ISoundMan.SND_JUST_CLICK, false);
                    return NEW_QUAD_SEQ;
                }
            }
//            selected.add(quadToAdd);
//            return QUAD_ADDED_SEQ;
        }

        selected.clear();
        selected.add(quadToAdd);
        NCore.instance().getSoundman().playSound(ISoundMan.SND_JUST_CLICK, false);
        return NEW_QUAD_SEQ;
    }

    public int selectAreaCenter(Quad center, int size)
    {
        int hsize = size/2;
        int x = center.x - hsize;
        if(x < 0) x = 0;
        int y = center.y - hsize;
        if(y < 0) y = 0;

        int x2 = x + size - 1;
        if(x2 >= field.width)
        {
            x2 = field.width-1;
            x = x2 - size + 1;
        }

        int y2 = y + size - 1;
        if(y2 >= field.height)
        {
            y2 = field.height - 1;
            y = y2 - size + 1;
        }

        selected.clear();
        selected.add(field.quads[x][y]);
        selected.add(field.quads[x2][y2]);
        grabQuads(field.quads[x][y], field.quads[x2][y2]);
        return QUAD_HIT;
    }

    private void grabQuads(Quad firstCorner, Quad secondCorner)
    {
        int fromX = firstCorner.x < secondCorner.x ? firstCorner.x : secondCorner.x;
        int fromY = firstCorner.y < secondCorner.y ? firstCorner.y : secondCorner.y;
        int toX = firstCorner.x > secondCorner.x ? firstCorner.x : secondCorner.x;
        int toY = firstCorner.y > secondCorner.y ? firstCorner.y : secondCorner.y;
        int len = toX - fromX + 1;
        int hei = toY - fromY + 1;

        area = field.subArray(fromX, fromY, len, hei);
    }

    public void clearSelected()
    {
        selected.clear();
    }

    public boolean needRecolor()
    {
        return matchedBy.quad != null && matchedBy.quad.type < 0;
    }
}
