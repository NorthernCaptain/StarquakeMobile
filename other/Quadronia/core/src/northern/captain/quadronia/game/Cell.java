package northern.captain.quadronia.game;

import northern.captain.quadronia.game.perks.ElePerk;
import northern.captain.quadronia.game.perks.IFieldPerk;

import static northern.captain.quadronia.game.Face.FACE_NONE;
import static northern.captain.quadronia.game.Face.HALF_MAX;
import static northern.captain.quadronia.game.GeoFace.FACE_MAX;
import static northern.captain.quadronia.game.GeoFace.FACE_EAST;
import static northern.captain.quadronia.game.GeoFace.FACE_NE;
import static northern.captain.quadronia.game.GeoFace.FACE_NW;
import static northern.captain.quadronia.game.GeoFace.FACE_SE;
import static northern.captain.quadronia.game.GeoFace.FACE_SW;
import static northern.captain.quadronia.game.GeoFace.FACE_WEST;
import static northern.captain.quadronia.game.GeoFace.SIDE_CONNECTOR;

/**
 * Created by leo on 01.03.15.
 */

/**
 * Cell class that encapsulates one cell on the field and all it's processing logic
 */
public abstract class Cell extends BoundingBox
{
    /**
     * Index in the storage array (list)
     */
    public int arrayIdx = -1;

    /**
     * X coordinate in units
     */
    public int cx;
    /**
     * Y coordinate in units
     */
    public int cy;

    /**
     * X Center of the big mother circle that we belong to.
     */
    public int xN;
    /**
     * Y Center of the big mother circle that we belong to.
     */
    public int yN;

    /**
     * X index of the mother center in centers array
     */
    public int centerCx;
    /**
     * Y index of the mother center in centers array
     */
    public int centerCy;

    /**
     * Our six faces
     */
    public GeoFace[] faces = new GeoFace[HALF_MAX];

    /**
     * X center of the cell
     */
    public int x0;
    /**
     * Y center of the cell
     */
    public int y0;

    /**
     * X upper left corner for drawing element
     */
    public int eleX;

    /**
     * Y bottom left corner for drawing element
     */
    public int eleY;

    /**
     * Do this cell enabled at all?
     */
    public boolean enabled = true;

    /**
     * Nearest cells that we are connected to
     */
    public Cell[] connected = new Cell[HALF_MAX];

    /**
     * Ref to the field we are from
     */
    public Field field;

    /**
     * X coordinates of our bounding triangle
     */
    protected int xTriA, xTriB, xTriC;

    /**
     * Y coordinates of our bounding triangle
     */
    protected int yTriA, yTriB, yTriC;

    /**
     * Bitwise mask that represents this cell's exits
     */
    protected int mask;

    /**
     * Element that was set inside this cell. Currently we can have only one element in the cell at a time.
     */
    public Element element = null;

    protected IFieldPerk perk = null;

    protected boolean isSideUp;
    protected boolean isOnEdge;

    protected Cell(int cx, int cy, Field field)
    {
        this.cx = cx;
        this.cy = cy;
        this.field = field;
        for(int i=0;i<faces.length;i++)
        {
            faces[i] = new GeoFace(i);
        }
    }

    public abstract Cell build(int xNf, int yNf, FieldConfig cfg);

    private int facesEnabled = HALF_MAX;

    public int getFacesEnabled()
    {
        return facesEnabled;
    }

    public Cell faceEnable(int faceType, boolean enable)
    {
        Face face = faces[faceType/2];
        if(face.type == faceType && face.enabled != enable)
        {
            face.setEnabled(enable);
            buildMask();
        }
        return this;
    }

    /**
     * Enable cell faces by the given bits configuration Face.BIT_*
     * @param bits - Face.BIT_*
     * @return this
     */
    public Cell faceEnableByBits(int bits)
    {
        for(int i = 0; i< Face.FACE_MISSED.length; i++)
        {
            int[] faceNfo = Face.FACE_MISSED[i];
            if(faceNfo[0] == bits)
            {
                for(int j = 1;faceNfo[j] != Face.FACE_MAX;j++)
                {
                    if(faceNfo[j] == FACE_NONE)
                    {
                        setEnabled(false);
                        return this;
                    }

                    faceEnable(faceNfo[j], false);
                }
                break;
            }
        }
        return this;
    }

    public Cell setEnabled(boolean enabled)
    {
        this.enabled = enabled;
        return this;
    }

    public Cell setConnection(Cell connection, int side)
    {
        this.connected[side/2] = connection;
        int revSide = SIDE_CONNECTOR[side];
        connection.connected[revSide/2] = this;
        return this;
    }

    /**
     * Connect any neighbour seen around to itself according to faces configuration
     * @return self
     */
    public abstract Cell doSelfConnect();

    protected void buildMask()
    {
        mask = 0;
        facesEnabled = 0;
        for(int i=0;i<faces.length;i++)
        {
            Face face=faces[faces.length - 1 - i];
            mask |= face.getMask() << i*FACE_MAX;
            if(face.enabled) facesEnabled++;
        }
    }

    protected void connectToMe(int dx, int dy, int side)
    {
        int x = cx + dx;
        int y = cy + dy;

        if(x < 0 || x >= field.fieldWid || y < 0 || y >= field.fieldHei)
            return;

        setConnection(field.field[x][y], side);
    }

    protected int boundingTrioLineDelta(int ax, int ay, int bx, int by, int px, int py)
    {
        return px * (by - ay) + py * (ax - bx) + ay * bx - ax * by;
    }

    protected int[] trioDeltas = new int[3];

    public int[] getTrioDeltas()
    {
        return trioDeltas;
    }

    public boolean isInTrio(int px, int py)
    {
        int ret1 = boundingTrioLineDelta(xTriA, yTriA, xTriB, yTriB, px, py);
        int ret2 = boundingTrioLineDelta(xTriB, yTriB, xTriC, yTriC, px, py);
        int ret3 = boundingTrioLineDelta(xTriC, yTriC, xTriA, yTriA, px, py);

        trioDeltas[0] = ret1 < 0 ? -ret1 : ret1;
        trioDeltas[1] = ret2 < 0 ? -ret2 : ret2;
        trioDeltas[2] = ret3 < 0 ? -ret3 : ret3;

        return (ret1>=0 && ret2>=0 && ret3>=0)
                || (ret1 < 0 && ret2 < 0 && ret3 < 0);
    }

    /**
     * Get the nearest side according to trioDelta information.
     * So call isIn or isInTrio once before calling this method to get proper side.
     * @param enabledOnly - pass true if we'd like to see only enabled sides, false to see any side
     * @return FACE_* or FC_* side notation
     */
    public abstract int getNearestSide(boolean enabledOnly);

    /**
     * Get the side we are heading to according to trioDelta and the given trio information.
     * So call isIn or isInTrio once before calling this method to get proper side.
     * @param lastTrio
     * @return
     */
    public abstract int getHeadingSide(int[] lastTrio);

    @Override
    public boolean isIn(int px, int py)
    {
        if(super.isIn(px, py))
        {
            return isInTrio(px, py);
        }

        return false;
    }

    public int getMask()
    {
        return enabled ? mask : 0;
    }

    public Face getBestOppositeFace(int side)
    {
        Face lastResort = null;
        for (GeoFace face : faces)
        {
            if (face.enabled)
            {
                lastResort = face;
                if (face.type != side)
                {
                    return face;
                }
            }
        }
        return lastResort;
    }

    public Element getElement() { return element;}
    public void setElement(Element element) { this.element = element;}

    public boolean hasElement() { return element != null;}

    public boolean canHaveElement()
    {
        return enabled && element == null && facesEnabled > 1;
    }

    public boolean canHaveElement(int facesRequired)
    {
        return enabled && element == null && facesEnabled >= facesRequired;
    }

    public IFieldPerk getPerk()
    {
        return perk;
    }

    public void setPerk(IFieldPerk perk)
    {
        this.perk = perk;
    }

    public boolean hasPerk()
    {
        return perk != null;
    }

    public boolean sideUp()
    {
        return isSideUp;
    }

    public boolean onEdge()
    {
        return isOnEdge;
    }

    /**
     * Calculates linear distance between this cell and the given one
     * @param toCell
     * @return
     */
    public int lDistance(Cell toCell)
    {
        int lastX = field != null ?  field.fieldWid-1 : 10;

        int dx = (cx == lastX ? -1 : cx) - (toCell.cx == lastX ? -1 : toCell.cx);
        int dy = cy - toCell.cy;
        return (dx < 0 ? -dx : dx) + (dy < 0 ? -dy : dy);
    }

    public Cell getConnected(int idx)
    {
        if(!enabled) return null;

        Face face = faces[idx];
        if(face.enabled)
        {
            return connected[idx];
        }

        return null;
    }

    /**
     * Get the nearest connected cell that is heading to the toCell
     * @param toCell
     * @return
     */
    public Cell getConnectedHeadingTo(Cell toCell, Element result)
    {
        if(!enabled) return null;

        int minDistance = 1000;
        Cell heading = null;

        int not1 = result.sides[Element.ONE];
        int not2 = result.sides[Element.TWO];
        int not3 = result.thirdSide;

        for(int i=0;i<HALF_MAX;i++)
        {
            Face face = faces[i];
            Cell con = connected[i];

            if(!face.enabled || con == null
                || face.type == not1
                || face.type == not2
                || face.type == not3) continue;

            int distance = con.lDistance(toCell);
            if(minDistance > distance)
            {
                minDistance = distance;
                result.sides[Element.TWO] = face.type;
                heading = con;
            }
        }

        return heading;
    }
}
