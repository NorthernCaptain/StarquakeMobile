package northern.captain.quadronia.game;

import northern.captain.tools.Log;

import static northern.captain.quadronia.game.Face.FC_NE;
import static northern.captain.quadronia.game.Face.FC_NORTH;
import static northern.captain.quadronia.game.Face.FC_NW;
import static northern.captain.quadronia.game.Face.FC_SE;
import static northern.captain.quadronia.game.Face.FC_SOUTH;
import static northern.captain.quadronia.game.Face.FC_SW;

/**
 * Created by leo on 01.03.15.
 */

/**
 * Cell class that encapsulates one cell on the field and all it's processing logic
 */
public class CellHoriz extends Cell
{
    public CellHoriz(int cx, int cy, Field field)
    {
        super(cx, cy, field);
        centerCx = cx / 2 + cx % 2;
        centerCy = cy / 2;
        centerCy = centerCy * 2 + (cy % 2 == 0 ? - (cx % 2) : cx % 2 );
    }

    @Override
    public CellHoriz build(int xNf, int yNf, FieldConfig cfg)
    {
        this.xN = xNf;
        this.yN = yNf;

        int x1 = xN - cfg.cellX[0];
        int x2 = xN - cfg.cellX[1];
        int x3 = xN - cfg.cellX[2];
        int x4 = xN - cfg.cellX[3];
        int x5 = xN - cfg.cellX[4];
        int x6 = xN - cfg.cellX[5];

        x0 = xN - cfg.cellCenterDx;// field.sin3rEI);

        eleX = xN - cfg.cellUpLeftX;

        if((cy % 2 == 0) == (cx % 2 == 0))
        { //even cell on X axis (left cell)
            int y3 = yN - cfg.cellY[2];
            int y4 = yN - cfg.cellY[3];

            y0 = yN - cfg.cellCenterDy; //y3
            eleY = yN + cfg.cellDownLeftY;


            xBox1 = x3;
            xBox2 = x6;
            yBox1 = y4;
            yBox2 = yN;

            faces[FC_SOUTH/2].setEnabled(true)   .setXY(x1, yN, x2, yN).type=FC_SOUTH;
            faces[FC_NW/2].setEnabled(true)      .setXY(x3, y3, x4, y4).type=FC_NW;
            faces[FC_NE/2].setEnabled(true)      .setXY(x5, y4, x6, y3).type=FC_NE;

            xTriA = xN;
            yTriA = yN;

            xTriB = (xN - field.rEI);
            yTriB = yN;

            xTriC = (xN - field.sin3rEI);
            yTriC = (yN - field.cos3rEI);

            isSideUp = false;
            isOnEdge = cy == field.fieldHei-1;

        } else
        {
            int y3 = yN + cfg.cellY[2];
            int y4 = yN + cfg.cellY[3];

            y0 = yN + cfg.cellCenterDy;
            eleY = yN + cfg.cellUpLeftY;


            xBox1 = x3;
            xBox2 = x6;
            yBox1 = yN;
            yBox2 = y4;

            faces[FC_NORTH/2].setEnabled(true)   .setXY(x1, yN, x2, yN).type=FC_NORTH;
            faces[FC_SW/2].setEnabled(true)      .setXY(x3, y3, x4, y4).type=FC_SW;
            faces[FC_SE/2].setEnabled(true)      .setXY(x5, y4, x6, y3).type=FC_SE;

            xTriA = xN;
            yTriA = yN;

            xTriB = (xN - field.rEI);
            yTriB = yN;

            xTriC = (xN - field.sin3rEI);
            yTriC = (yN + field.cos3rEI);

            isOnEdge = cy == 0;
            isSideUp = true;
        }

        buildMask();

        return this;
    }

    /**
     * Connect any neighbour seen around to itself according to faces configuration
     * @return self
     */
    @Override
    public CellHoriz doSelfConnect()
    {
        if(!enabled)
            return this;

        for(int i = 0;i< faces.length;i++)
        {
            if(faces[i].enabled)
            {
                int side = faces[i].type;
                switch (side)
                {
                    case FC_NORTH:
                        connectToMe(0, -1, side);
                        break;
                    case FC_SOUTH:
                        connectToMe(0, 1, side);
                        break;
                    case FC_SE:
                    case FC_NE:
                        connectToMe(1, 0, side);
                        break;
                    case FC_SW:
                    case FC_NW:
                        connectToMe(-1, 0, side);
                        break;
                }
            }
        }

        return this;
    }

    @Override
    protected void connectToMe(int dx, int dy, int side)
    {
        int x = cx + dx;
        int y = cy + dy;

        if(x == field.fieldWid) x = 0;

        if(x < 0 || x >= field.fieldWid || y < 0 || y >= field.fieldHei)
            return;

        setConnection(field.field[x][y], side);
    }

    @Override
    public int getNearestSide(boolean enabledOnly)
    {
        int r1 = trioDeltas[0];
        int r2 = trioDeltas[1];
        int r3 = trioDeltas[2];

        return getSidebyTrio(r1, r2, r3, enabledOnly);
    }

    private int getSidebyTrio(int r1, int r2, int r3, boolean enabledOnly)
    {
        boolean even = faces[0].type == FC_NW;
        int side;


        if(r1 < r2 && r1 < r3)
        {
            side = even ? FC_SOUTH : FC_NORTH;
            if(enabledOnly && !faces[side/2].enabled)
            {
                if(r2 < r3)
                {
                    side = even ? FC_NW : FC_SW;
                }
                else
                {
                    side = even ? FC_NE : FC_SE;
                }
            }
            return side;
        }

        if(r2 < r1 && r2 < r3)
        {
            side = even ? FC_NW : FC_SW;
            if(enabledOnly && !faces[side/2].enabled)
            {
                if (r1 < r3)
                {
                    side = even ? FC_SOUTH : FC_NORTH;
                } else
                {
                    side = even ? FC_NE : FC_SE;
                }
            }
            return side;
        }

        side = even ? FC_NE : FC_SE;
        if(enabledOnly && !faces[side/2].enabled)
        {
            if (r1 < r2)
            {
                side = even ? FC_SOUTH : FC_NORTH;
            } else
            {
                side = even ? FC_NW : FC_SW;
            }
        }

        return side;
    }

    @Override
    public int getHeadingSide(int[] lastTrio)
    {
        int r1 = trioDeltas[0] - lastTrio[0];
        int r2 = trioDeltas[1] - lastTrio[1];
        int r3 = trioDeltas[2] - lastTrio[2];

        return getSidebyTrio(r1, r2, r3, true);
    }
}
