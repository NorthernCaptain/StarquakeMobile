package northern.captain.quadronia.game;

import northern.captain.quadronia.game.perks.IFieldPerk;

/**
 * Created by leo on 3/4/15.
 */
public class Face
{
    public static final int FACE_SW    = 0;
    public static final int FACE_WEST  = 1;
    public static final int FACE_NW    = 2;
    public static final int FACE_NE    = 3;
    public static final int FACE_EAST  = 4;
    public static final int FACE_SE    = 5;

    public static final int FC_SW    = 0;
    public static final int FC_NW    = 1;
    public static final int FC_NORTH = 2;
    public static final int FC_NE    = 3;
    public static final int FC_SE    = 4;
    public static final int FC_SOUTH  = 5;
    public static final int FC_NOFACE  = -1;

	public static final int BIT_SW = 1 << FC_SW;
	public static final int BIT_NW = 1 << FC_NW;
	public static final int BIT_NORTH = 1 << FC_NORTH;
	public static final int BIT_NE = 1 << FC_NE;
	public static final int BIT_SE = 1 << FC_SE;
	public static final int BIT_SOUTH = 1 << FC_SOUTH;
	
	public static final int NORTH_AL = BIT_NORTH | BIT_SW | BIT_SE;
	public static final int NORTH_W_ = BIT_NORTH | BIT_SW;
	public static final int NORTH_E_ = BIT_NORTH | BIT_SE;
	public static final int NORTH_WE = BIT_SW | BIT_SE;
	
	public static final int SOUTH_AL = BIT_SOUTH | BIT_NE | BIT_NW;
	public static final int SOUTH_W_ = BIT_SOUTH | BIT_NW;
	public static final int SOUTH_E_ = BIT_SOUTH | BIT_NE;
	public static final int SOUTH_WE = BIT_NE | BIT_NW;
	public static final int NONENONE = 0;
	
    public static final int FACE_MAX   = 6;
    public static final int FACE_NONE   = -1;

    public static final int[][] FACE_MISSED = new int[][]
            {
                    { NONENONE, FACE_NONE, FACE_MAX },
                    { NORTH_AL, FACE_MAX },
                    { SOUTH_AL, FACE_MAX },
                    { NORTH_E_, FC_SW, FACE_MAX},
                    { NORTH_W_, FC_SE, FACE_MAX},
                    { NORTH_WE, FC_NORTH, FACE_MAX},
                    { SOUTH_E_, FC_NW, FACE_MAX},
                    { SOUTH_W_, FC_NE, FACE_MAX},
                    { SOUTH_WE, FC_SOUTH, FACE_MAX}
            };

    public static final int HALF_MAX   = FACE_MAX/2;

    public static final int[] SIDE_CONNECTOR = new int[]
            {
                    FC_NE,    // for FC_SW
                    FC_SE,  // for FC_NW
                    FC_SOUTH,    // for FC_NORTH
                    FC_SW,    // for FC_NE
                    FC_NW,  // for FC_SE
                    FC_NORTH,    // for FC_SOUTH
            };

    public static final int[] FLIP = new int[]
            {
                    FC_NW,    // for FC_SW
                    FC_SW,  // for FC_NW
                    FC_SOUTH,    // for FC_NORTH
                    FC_SE,    // for FC_NE
                    FC_NE,  // for FC_SE
                    FC_NORTH,    // for FC_SOUTH
            };

    public static final float[] SIDE_ANGLE = new float[]
            {
                    60,    // for FC_SW
                    -60,  // for FC_NW
                    0,    // for FC_NORTH
                    60,    // for FC_NE
                    -60,  // for FC_SE
                    180,    // for FC_SOUTH
            };

    /**
     * Sides for connected cells that forms a circle
     */
    public static final int[] AROUND_CENTER = new int[]
            {
                    FC_NE,
                    FC_SE,
                    FC_SOUTH,
                    FC_SW,
                    FC_NW,
                    FC_NORTH
            };

    public int type;
    public boolean enabled;
    /**
     * Perk set to this face
     */
    protected IFieldPerk perk = null;

    public void setPerk(IFieldPerk perk)
    {
        this.perk = perk;
    }

    public boolean hasPerk()
    {
        return perk != null;
    }

    public IFieldPerk getPerk()
    {
        return perk;
    }

    public Face(int type)
    {
        this.type = type;
    }

    public Face setEnabled(boolean value)
    {
        enabled = value;
        return  this;
    }

    /**
     * Rotate the face according to the steps
     * If steps > 0 - rotate clockwise
     * steps < 0 - rotate counterclockwise
     * @param steps - non zero value and abs(steps) < FACE_MAX
     */
    public void rotate(int steps)
    {
        type = (type + FACE_MAX + steps) % FACE_MAX;
    }

    /**
     * Get number of steps to be done to rotate the face to zero position
     * @return
     */
    public int stepsToZero()
    {
        return FACE_MAX - type;
    }

    public boolean canConnect(Face withFace)
    {
        return  (type + HALF_MAX) % FACE_MAX == withFace.type;
    }

    public int getMask()
    {
        return enabled ? getMask(type) : 0;
    }

    public static int getMask(int type)
    {
        return 1 << type;
    }

    public static int getPair(int from, int to)
    {
        return (to << 6) | from;
    }
}
