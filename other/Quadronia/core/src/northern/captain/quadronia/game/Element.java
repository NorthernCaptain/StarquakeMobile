package northern.captain.quadronia.game;

import com.badlogic.gdx.utils.Pool;

import northern.captain.tools.Helpers;

/**
 * Created by leo on 3/4/15.
 */
public class Element
{
    public static final Pool<Element> POOL = new Pool<Element>()
    {
        @Override
        protected Element newObject()
        {
            return new Element();
        }
    };

    public static final int NO_MATCH = -1;
    public static final int NUM_FACES = 2;
    public static final int ONE = 0;
    public static final int TWO = 1;

    public int[] sides = new int[NUM_FACES];

    public int thirdSide;

    public int mask;
    public int pair;

    public Item    parentItem;

    /**
     * The cell this element is assigned to
     * Currently used only for QuestPathSolver
     */
    public Cell    cell;

    public boolean removable = true;

    public Element()
    {

    }

    public Element(int side1, int side2)
    {
        init(side1, side2);
    }

    public Element init(int side1, int side2)
    {
        removable = true;
        cell = null;
        parentItem = null;
        sides[ONE] = side1;
        sides[TWO] = side2;
        thirdSide = Face.FC_NOFACE;
        buildMask();
        return this;
    }

    public Element(int side1)
    {
        sides[ONE] = side1;
        sides[TWO] = (side1 + (1 + Helpers.RND.nextInt(2))*2) % Face.FACE_MAX;
    }

    public void shiftSides()
    {
        int tmp = thirdSide;
        if(sides[0] == Face.FC_NOFACE && thirdSide != Face.FC_NOFACE)
        {
            thirdSide = sides[0];
            sides[0] = sides[1];
            sides[1] = tmp;
        } else
        {
            thirdSide = sides[1];
            sides[1] = tmp;
        }
    }

    public int getDelta(int side2)
    {
        return side2 - sides[ONE];
    }

    public int buildMask(int delta)
    {
        int v1 = (sides[ONE] + Face.FACE_MAX + delta) % Face.FACE_MAX;
        int v2 = (sides[TWO] + Face.FACE_MAX + delta) % Face.FACE_MAX;

        return v1 < v2 ? (v2 << 6 | v1) : (v1 << 6 | v2);
    }

    public void buildMask()
    {
        int v1 = sides[ONE];
        int v2 = sides[TWO];
        mask =  v1 < v2 ? (v2 << 6 | v1) : (v1 << 6 | v2);
        pair = Face.getPair(v1, v2);
    }

    /**
     * Rotate the element by given steps
     * @param steps
     */
    public void rotate(int steps)
    {
        if(steps == 0) return;

        int v1 = (sides[ONE] + Face.FACE_MAX + steps) % Face.FACE_MAX;
        int v2 = (sides[TWO] + Face.FACE_MAX + steps) % Face.FACE_MAX;

        sides[ONE] = v1;
        sides[TWO] = v2;

        buildMask();
    }

    /**
     * Check if the given element can fit into this one using rotation
     * @param with
     * @return =0 - exact fit, >0 number of steps to rotate ours to fit, <0 - do not fit
     */
    public int match(Element with)
    {
        int delta = with.sides[ONE] - sides[ONE];
        int ourMask = buildMask(delta);
        //We need to check rotation for ONE and TWO, not only ONE and ONE
        if(ourMask != with.mask)
        {
            delta = with.sides[TWO] - sides[ONE];
            ourMask = buildMask(delta);
        }
        return ourMask == with.mask ? (delta + Face.FACE_MAX) % Face.FACE_MAX : NO_MATCH;
    }

    /**
     * Check if the given element can fit into this one using rotation to the given steps
     * @param with
     * @return =0 - exact fit, >0 number of steps to rotate ours to fit, <0 - do not fit
     */
    public int match(Element with, int delta)
    {
        int ourMask = buildMask(delta);
        return ourMask == with.mask ? (delta + Face.FACE_MAX) % Face.FACE_MAX : NO_MATCH;
    }

    public void setParentItem(Item item)
    {
        parentItem = item;
    }

    public void setSides(int side1, int side2)
    {
        sides[0] = side1;
        sides[1] = side2;

        buildMask();
    }

    public void flip()
    {
        sides[ONE] = Face.FLIP[sides[ONE]];
        sides[TWO] = Face.FLIP[sides[TWO]];
        buildMask();
    }

    public boolean isRemovable()
    {
        return removable;
    }

    public void setRemovable(boolean removable)
    {
        this.removable = removable;
    }
}
