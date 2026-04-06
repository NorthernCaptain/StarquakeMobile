package northern.captain.quadronia.game.core;

import northern.captain.tools.IDisposable;
import northern.captain.tools.Log;

/**
 * Created by leo on 15.09.15.
 */
public class QuadSolver extends QuadCheck implements IDisposable
{
    private int runCount = 0;

    public QuadSolver(Field field)
    {
        this.field = new Field(field.width, field.height);
        this.field.initEmptyField();
    }

    public void init(Field field)
    {
        for(int x = 0; x < field.width; x++)
        {
            for(int y = 0; y < field.height; y++)
            {
                this.field.quads[x][y].x = x;
                this.field.quads[x][y].y = y;
                this.field.quads[x][y].copyFrom(field.quads[x][y]);
            }
        }
    }

    public QuadArea solve(int sequence) throws Throwable
    {
        QuadArea area = new QuadArea();

        Log.i("ncgames", "Solver: starting to find the solution, seq=" + sequence);

        try
        {

            for (int x = 0; x < field.width - 1; x++)
            {
                for (int y = 0; y < field.height - 1; y++)
                {
                    Quad quad = field.quads[x][y];
                    int fromX = x + 1;

                    while (true)
                    {
                        Quad hquad = findQuadX(quad, fromX, y);
                        if (hquad == null) break;

                        if (hquad.y != y || hquad.x < fromX)
                        {
                            Log.e("ncgames", "Solver: wrong quad returned x! X=" + hquad.x + ", Y=" +  hquad.y + ", startX=" + fromX + ", y=" + y);
                            throw new RuntimeException("Solver: wrong quad for X");
                        }

                        int fromY = y + 1;

                        while (true)
                        {
                            Quad vquad = findQuadY(quad, x, fromY);
                            if (vquad == null) break;

                            if (vquad.y < fromY || vquad.x != x)
                            {
                                Log.e("ncgames", "Solver: wrong quad returned y! X=" + vquad.x + ", Y=" +  vquad.y + ", startX=" + x + ", y=" + fromY);
                                throw new RuntimeException("Solver: wrong quad for Y");
                            }

                            Quad forth = field.quads[hquad.x][vquad.y];
                            if (checkHit(quad, forth))
                            {
                                area.maximize(quad, forth);
                            }
                            fromY = vquad.y + 1;
                        }

                        fromX = hquad.x + 1;
                    }
                }
            }

        } catch (Throwable ex)
        {
            Log.e("ncgames", "Solver Exception: " + ex.getMessage(), ex);
            throw ex;
        }

        runCount++;

        Log.i("ncgames", "Solver: found solution " + area.fromX + ", " + area.fromY + " " + area.len + "X" + area.hei);
        return area.square() > 0 ? area : null;
    }

    private Quad findQuadX(Quad orig, int fromX, int fromY)
    {
        for(int x = fromX;x<field.width;x++)
        {
            if(orig.match(field.quads[x][fromY]))
            {
                return field.quads[x][fromY];
            }
        }
        return null;
    }

    private Quad findQuadY(Quad orig, int fromX, int fromY)
    {
        for(int y = fromY;y<field.height;y++)
        {
            if(orig.match(field.quads[fromX][y]))
            {
                return field.quads[fromX][y];
            }
        }
        return null;
    }

    @Override
    public void dispose()
    {
        field.dispose();
    }
}
