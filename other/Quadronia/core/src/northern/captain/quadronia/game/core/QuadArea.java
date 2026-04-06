package northern.captain.quadronia.game.core;

import northern.captain.tools.IDisposable;

/**
 * Created by leo on 04.09.15.
 */
public class QuadArea implements IDisposable
{
    public int fromX;
    public int fromY;
    public int len = 0;
    public int hei = 0;

    public Quad[][] quads;

    public QuadArea()
    {

    }

    public void maximize(Quad from, Quad to)
    {
        int len1 = to.x - from.x;
        int hei1 = to.y - from.y;

        if(len*hei < len1*hei1)
        {
            fromX = from.x;
            fromY = from.y;
            len = len1;
            hei = hei1;
        }
    }

    public int square()
    {
        return len * hei;
    }


    public QuadArea(int fromX, int fromY, int len, int hei)
    {
        this.fromX = fromX;
        this.fromY = fromY;
        this.len = len;
        this.hei = hei;
    }

    @Override
    public void dispose()
    {
        if(quads == null) return;
        for(int x = 0; x < quads.length;x++)
        {
            if(quads[x] == null) continue;
            for(int y=0;y< quads[x].length;y++)
            {
                Quad quad = quads[x][y];
                if(quad != null && quad.type != Quad.TYPE_NO)
                {
                    Game.QUAD_POOL.free(quad);
                    quads[x][y] = null;
                }
            }
        }
    }
}
