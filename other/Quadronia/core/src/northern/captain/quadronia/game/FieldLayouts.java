package northern.captain.quadronia.game;

import java.util.ArrayList;
import java.util.List;

import static northern.captain.quadronia.game.Face.SOUTH_E_;
import static northern.captain.quadronia.game.Face.SOUTH_AL;
import static northern.captain.quadronia.game.Face.SOUTH_W_;
import static northern.captain.quadronia.game.Face.SOUTH_WE;
import static northern.captain.quadronia.game.Face.NORTH_AL;
import static northern.captain.quadronia.game.Face.NORTH_W_;
import static northern.captain.quadronia.game.Face.NORTH_WE;
import static northern.captain.quadronia.game.Face.NORTH_E_;
import static northern.captain.quadronia.game.Face.NONENONE;

/**
 * Created by leo on 17.04.15.
 */
public class FieldLayouts
{
    private static final int FIELD_HEI=8;
    private static final int FIELD_LEN=11;

    private static final int[][] CIRCLE = new int[][]
        {
            {SOUTH_E_, NORTH_WE, SOUTH_W_},
            {NORTH_E_, SOUTH_WE, NORTH_W_}
        };

    public static class Layout
    {
        public int[][] centers;
        public int[][] cells;

        public Layout(int[][] centers)
        {
            this.centers = centers;
            cells = new int[FIELD_HEI][];
            for(int i=0;i<cells.length;i++)
                cells[i] = new int[FIELD_LEN];

            for(int x = 0;x<centers.length;x++)
            {
                for(int y = 0;y<centers[x].length;y++)
                {
                    if(centers[x][y] == 3) //full circle
                    {
                        int delta = (x % 2 == 0) ? 0 : -1;
                        putCircle(x, y*2 + delta);
                    }
                }
            }
        }

        private void putCircle(int x, int y)
        {
            for(int cx = 0; cx < CIRCLE.length;cx++)
            {
                for(int cy = 0; cy<CIRCLE[cx].length; cy++)
                {
                    int yy = (y + cy + FIELD_LEN) % FIELD_LEN;
                    cells[x+cx][yy] |= CIRCLE[cx][cy];
                }
            }
        }
    }

    private static List<Layout> LAYOUTS = new ArrayList<Layout>();

    public static List<Layout> getLAYOUTS()
    {
        return LAYOUTS;
    }

    private static final int[][] CENTERS_FULL = new int[][]
        {
            {3, 3, 3, 3, 0},
            {3, 3, 3, 3, 3},
            {3, 3, 3, 3, 0},
            {3, 3, 3, 3, 3},
            {3, 3, 3, 3, 0},
            {3, 3, 3, 3, 3},
            {3, 3, 3, 3, 0},
        };

    private static final int[][] CENTERS_RHOMBUS = new int[][]
        {
            {2, 3, 3, 2, 0},
            {2, 3, 3, 3, 2},
            {3, 3, 3, 3, 0},
            {3, 3, 3, 3, 3},
            {3, 3, 3, 3, 0},
            {2, 3, 3, 3, 2},
            {2, 3, 3, 2, 0},
        };

    private static final int[][] CENTERS_V2 = new int[][]
        {
            {3, 3, 2, 2, 0},
            {3, 3, 3, 2, 2},
            {2, 3, 3, 2, 0},
            {2, 2, 3, 3, 3},
            {2, 3, 3, 2, 0},
            {3, 3, 3, 2, 2},
            {3, 3, 2, 2, 0},
        };

    private static final int[][] CENTERS_V3 = new int[][]
        {
            {2, 3, 3, 2, 0},
            {2, 3, 3, 3, 2},
            {3, 2, 2, 3, 0},
            {3, 2, 0, 2, 3},
            {3, 2, 2, 3, 0},
            {2, 3, 3, 3, 2},
            {2, 3, 3, 2, 0},
        };

    private static final int[][] CENTERS_V4 = new int[][]
        {
            {3, 3, 3, 3, 0},
            {2, 3, 3, 3, 2},
            {2, 3, 3, 2, 0},
            {2, 2, 3, 2, 2},
            {2, 3, 3, 2, 0},
            {2, 3, 3, 3, 2},
            {3, 3, 3, 3, 0},
        };

    private static final int[][] CENTERS_V5 = new int[][]
        {
            {3, 2, 2, 2, 0},
            {2, 3, 3, 2, 2},
            {3, 3, 3, 2, 0},
            {2, 3, 3, 3, 2},
            {2, 3, 3, 3, 0},
            {2, 2, 3, 3, 2},
            {2, 2, 2, 3, 0},
        };

    private static final int[][] CENTERS_V6 = new int[][]
        {
            {3, 3, 2, 2, 0},
            {2, 3, 3, 2, 2},
            {2, 3, 3, 2, 0},
            {2, 2, 2, 3, 2},
            {2, 3, 3, 2, 0},
            {2, 3, 3, 2, 2},
            {3, 3, 2, 2, 0},
        };

    private static final int[][] CENTERS_V7 = new int[][]
        {
            {2, 2, 3, 3, 0},
            {2, 3, 3, 2, 2},
            {3, 3, 2, 2, 0},
            {3, 2, 2, 2, 2},
            {3, 3, 2, 2, 0},
            {2, 3, 3, 2, 2},
            {2, 2, 3, 3, 0},
        };

    private static final int[][] CENTERS_V8 = new int[][]
        {
            {3, 2, 2, 2, 0},
            {2, 3, 2, 2, 3},
            {2, 3, 2, 3, 0},
            {2, 2, 3, 3, 2},
            {2, 3, 2, 3, 0},
            {2, 3, 2, 2, 3},
            {3, 2, 2, 2, 0},
        };

    private static final int[][] CENTERS_V9 = new int[][]
        {
            {2, 3, 2, 2, 0},
            {3, 2, 3, 2, 2},
            {3, 2, 3, 2, 0},
            {3, 3, 3, 3, 3},
            {3, 2, 3, 2, 0},
            {3, 2, 3, 2, 2},
            {2, 3, 2, 2, 0},
        };

    private static final int[][] CENTERS_V10 = new int[][]
        {
            {3, 3, 3, 3, 0},
            {2, 3, 3, 3, 3},
            {2, 2, 2, 3, 0},
            {2, 2, 2, 2, 3},
            {2, 2, 2, 3, 0},
            {2, 3, 3, 3, 3},
            {3, 2, 3, 3, 0},
        };

    private static final int[][] CENTERS_V11 = new int[][]
        {
            {3, 3, 3, 3, 0},
            {2, 2, 2, 3, 3},
            {2, 2, 3, 3, 0},
            {2, 2, 3, 3, 3},
            {2, 2, 3, 3, 0},
            {2, 2, 2, 3, 3},
            {3, 3, 3, 3, 0},
        };

    static
    {
        LAYOUTS.add(new Layout(CENTERS_FULL));
        LAYOUTS.add(new Layout(CENTERS_RHOMBUS));
        LAYOUTS.add(new Layout(CENTERS_V2));
        LAYOUTS.add(new Layout(CENTERS_V3));
        LAYOUTS.add(new Layout(CENTERS_V4));
        LAYOUTS.add(new Layout(CENTERS_V5));
        LAYOUTS.add(new Layout(CENTERS_V6));
        LAYOUTS.add(new Layout(CENTERS_V7));
        LAYOUTS.add(new Layout(CENTERS_V11));
        LAYOUTS.add(new Layout(CENTERS_V10));
        LAYOUTS.add(new Layout(CENTERS_V9));
        LAYOUTS.add(new Layout(CENTERS_V8));
    }

}
