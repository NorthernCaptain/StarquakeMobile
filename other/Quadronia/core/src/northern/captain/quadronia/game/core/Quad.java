package northern.captain.quadronia.game.core;

import java.util.HashMap;
import java.util.Map;

import northern.captain.quadronia.game.Constants;
import northern.captain.tools.Helpers;

/**
 * Created by leo on 30.08.15.
 */
public class Quad
{
    public static final int TYPE_NONE = 0;
    public static final int TYPE_BLUE = 1;
    public static final int TYPE_RED = 2;
    public static final int TYPE_AMBER = 3;
    public static final int TYPE_PURPLE = 4;
    public static final int TYPE_GREEN = 5;
    public static final int TYPE_CYAN = 6;
    public static final int TYPE_BLUEGRAY = 7;
    public static final int TYPE_PINK = 8;
    public static final int TYPE_LIGHTPURPLE = 9;
    public static final int TYPE_LIME = 10;
    public static final int TYPE_ORANGE = 11;
    public static final int TYPE_MULTI = Constants.MAX_COLORS;
    public static final int TYPE_X2 = Constants.MAX_COLORS + 1;
    public static final int TYPE_M200 = Constants.MAX_COLORS + 2;
    public static final int TYPE_NO = Constants.MAX_COLORS + 3;
    public static final int TYPE_COINS = Constants.MAX_COLORS + 4;
    public static final int TYPE_TIME = Constants.MAX_COLORS + 5;

    public static final Map<Integer, Integer> biColorMap = new HashMap<Integer, Integer>();
    public static final Map<Integer, Integer> x2BonusMap = new HashMap<Integer, Integer>();

    {
        biColorMap.put(Quad.TYPE_BLUE, Quad.TYPE_RED);
        biColorMap.put(Quad.TYPE_RED, Quad.TYPE_AMBER);
        biColorMap.put(Quad.TYPE_AMBER, Quad.TYPE_PURPLE);
        biColorMap.put(Quad.TYPE_PURPLE, Quad.TYPE_GREEN);
        biColorMap.put(Quad.TYPE_GREEN, Quad.TYPE_CYAN);
        biColorMap.put(Quad.TYPE_CYAN, Quad.TYPE_BLUEGRAY);
        biColorMap.put(Quad.TYPE_BLUEGRAY, Quad.TYPE_PINK);
        biColorMap.put(Quad.TYPE_PINK, Quad.TYPE_LIME);

        int delta = Constants.MAX_COLORS3;
        x2BonusMap.put(Quad.TYPE_BLUE, delta + Quad.TYPE_BLUE);
        x2BonusMap.put(Quad.TYPE_GREEN, delta + Quad.TYPE_GREEN);
        x2BonusMap.put(Quad.TYPE_LIME, delta + Quad.TYPE_LIME);
        x2BonusMap.put(Quad.TYPE_ORANGE, delta + Quad.TYPE_ORANGE);
        x2BonusMap.put(Quad.TYPE_RED, delta + Quad.TYPE_RED);
        x2BonusMap.put(Quad.TYPE_PURPLE, delta + Quad.TYPE_PURPLE);
    }

    public int x;
    public int y;
    /**
     * Color of the quad or special type
     * Colors have values > 0
     */
    public int type;
    public int subtype = 0;
    public boolean used = false;

    public Quad()
    {
    }

    public Quad(int x, int y)
    {
        init(x, y);
    }

    public Quad init(int x, int y)
    {
        this.x = x;
        this.y = y;
        subtype = 0;
        used = false;
        return this;
    }

    public Quad generate(Field field)
    {
        type = 1 + Helpers.RND.nextInt(field.maxColors);
        return this;
    }

    public boolean match(Quad quad2)
    {
        if(quad2 != null)
        {
            int t1 = type < 0 ? -type : (type == TYPE_X2 || type == TYPE_M200 ? subtype : type);
            int t2 = quad2.type < 0 ? -quad2.type :
                (quad2.type == TYPE_X2 || quad2.type == TYPE_M200 ? quad2.subtype : quad2.type);
            return t1 == t2 || t1 == TYPE_MULTI || t2 == TYPE_MULTI
                || (quad2.subtype != 0 && quad2.subtype == type)
                || (subtype != 0 && quad2.type == subtype)
                || (quad2.subtype != 0 && subtype != 0 && subtype == quad2.subtype);
        }

        return false;
    }

    public static class MatchResult
    {
        public int type = TYPE_NONE;
        public Quad quad = new Quad();

        public void set(MatchResult result)
        {
            type = result.type;
            quad.copyFrom(result.quad);
            quad.x = result.quad.x;
            quad.y = result.quad.y;
        }

        public void set(Quad quad)
        {
            type = quad.type;
            this.quad.copyFrom(quad);
            this.quad.x = quad.x;
            this.quad.y = quad.y;
        }

        public void reset()
        {
            type = TYPE_NONE;
            quad.clear();
        }
    }

    public void clear()
    {
        type = TYPE_NONE;
        subtype = 0;
    }

    public boolean match2(Quad quad2, MatchResult ret)
    {
        if(quad2 != null)
        {
            int t1 = type < 0 ? -type : (type == TYPE_X2 ? subtype : type);
            int t2 = quad2.type < 0 ? -quad2.type :
                (quad2.type == TYPE_X2 ? quad2.subtype : quad2.type);

            if(t1 == t2)
            {

                ret.set(type < 0 ? this : quad2);
                return true;
            }

            if(t1 == TYPE_MULTI)
            {
                ret.set(this);
                ret.type = TYPE_MULTI;
                return true;
            }

            if(t2 == TYPE_MULTI)
            {
                ret.set(quad2);
                ret.type = TYPE_MULTI;
                return true;
            }

            if(quad2.subtype != 0 && quad2.subtype == t1)
            {
                ret.set(quad2);
                ret.type = quad2.subtype;
                return true;
            }
            if(subtype != 0 && t2 == subtype)
            {
                ret.set(this);
                ret.type = subtype;
                return true;
            }

            if(quad2.subtype != 0 && subtype != 0 && subtype == quad2.subtype)
            {
                ret.set(this);
                ret.type = subtype;
                return true;
            }
        }

        return false;
    }

    public boolean isDiagonally(Quad quad2)
    {
        return quad2.x != x && quad2.y != y;
    }

    public void setType(int value)
    {
        type = value;
        if(value < 0)
        {
            subtype = biColorMap.get(-type);
        }

        if(value > Constants.MAX_COLORS3)
        {
            type = TYPE_X2;
            subtype = value - Constants.MAX_COLORS3;
        }

        if(value == Quad.TYPE_MULTI) {
            subtype = Helpers.RND.nextInt(100) % 4;
        }
    }

    public boolean theSame(Quad quad2)
    {
        return quad2.type == type && quad2.x == x && quad2.y == y;
    }

    public void copyFrom(Quad quad)
    {
        if(quad == null)
        {
            this.type = TYPE_NONE;
            this.subtype = 0;
        } else
        {
            this.type = quad.type;
            this.subtype = quad.subtype;
        }
    }
}
