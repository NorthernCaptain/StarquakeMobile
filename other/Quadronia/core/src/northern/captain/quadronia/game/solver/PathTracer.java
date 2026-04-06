package northern.captain.quadronia.game.solver;

import java.util.ArrayList;
import java.util.List;

import northern.captain.quadronia.game.Cell;
import northern.captain.quadronia.game.Element;
import northern.captain.quadronia.game.Engine;
import northern.captain.quadronia.game.Face;
import northern.captain.quadronia.game.Field;
import northern.captain.quadronia.game.Item;
import northern.captain.tools.Log;

/**
 * Created by leo on 08.03.15.
 */
public class PathTracer
{
    private static final int MAX_JOKER_CELLS = 5;

    protected Field field;
    private boolean started = false;

    private int[] startTrio = new int[3];

    private Cell lastCell;
    private int lastSide;
    private int headingSide;

    private Item[] item;
    private Item chosenItem;

    protected Engine game;

    private int lastX, lastY;
    private int deltaR = 5;

    private boolean active = true;

    private boolean jokerMode = false;

    public static class PathPart extends Element
    {
        public int[] firstTrio = new int[3];

        public PathPart()
        {
            super(0, 0);
        }
        public PathPart(int sideType1, int sideType2)
        {
            super(sideType1, sideType2);
        }
    }

    public PathPart currentPart = new PathPart();

    public List<PathPart> path = new ArrayList<PathPart>(10);

    public int matchingSteps;

    public PathTracer(Engine game, Field field)
    {
        this.game = game;
        this.field = field;
    }

    private int maxCells;
    public void setItem(Item[] item)
    {
        this.item = item;
        maxCells = item[0].elements.length;
    }

    public void reset()
    {
        started = false;
        currentPart = null;
        path.clear();
        lastCell = null;
    }

    public boolean startPath(int x, int y)
    {
        Cell cell = field.getCellByCoord(x, y);

        lastX = x;
        lastY = y;

        started = false;

        if(cell != null && cell.canHaveElement() && cell.enabled)
        {
            currentPart = null;
            path.clear();

            setLastCell(cell);

            started = true;
        } else
        {
            Log.i("curveIt", "No suitable CELL for: " + x + ", " +y);
        }
        return started;
    }

    public boolean doDragTo(int x, int y)
    {
        if(!started)
        {
            return startPath(x, y);
        }

        int dx = lastX - x;
        int dy = lastY - y;
        if(dx > -deltaR && dx < deltaR && dy > -deltaR && dy < deltaR)
        {
            return true;
        }

        lastX = x;
        lastY = y;

        if(lastCell.isIn(x, y))
        {
            //We are still in our first cell
            //let's detect and set proper headingSide

            int side = lastCell.getHeadingSide(startTrio);

            Log.i("curveIt", "Side: " + lastSide + " heading for cell: " + side);

            if(side != lastSide)
            {
                setCurrentHeading(side);
            } else
            {
//                if (path.size()<=1 && lastCell != null)
                {
                    Face face = lastCell.getBestOppositeFace(lastSide);
                    side = face.type;
                    if(side != lastSide)
                    {
                        setCurrentHeading(side);
                    } else
                    {
                        removeCurrent();
                    }
                }
            }
        } else
        {
            //we've just jumped to another cell,
            //let's detect where it is.
            for (int i = 0; i < Face.HALF_MAX; i++)
            {
                if (lastCell.faces[i].enabled)
                {
                    Cell nextCell = lastCell.connected[i];
                    if (nextCell != null
                        && nextCell.canHaveElement()
                        && nextCell.isIn(x, y))
                    {
                        if(isFull())
                        {
                            removePartsTill(nextCell);
                            headingSide = Face.FACE_NONE;
                            currentPart = null;
                            lastCell = nextCell;
                        } else
                        {
                            putNextCell(nextCell, lastCell.faces[i]);
                        }
                        break;
                    }
                }
            }
        }

        return true;
    }

    private void removeCurrent()
    {
        if(currentPart != null)
        {
            path.remove(path.size()-1);
            currentPart = null;
        }
    }

    public boolean finishPath(int x, int y)
    {
        boolean ret = fullMatch();
        return ret && chosenItem.elements.length == path.size();
    }

    protected boolean isFull()
    {
        return path.size() == maxCells;
    }


    private void setLastCell(Cell cell)
    {
        Log.i("curveIt", "Last CELL to: " + cell.cx + ", " +cell.cy);
        lastCell = cell;
        int[] trio = cell.getTrioDeltas();

        startTrio[0] = trio[0];
        startTrio[1] = trio[1];
        startTrio[2] = trio[2];

        int nearSide = cell.getNearestSide(true);

        int connectedSide = Face.FACE_NONE;
        //Here try to find the side of our cell that has connected cell
        //and there is an element that faces our cell's side
        for(int i = 0; i<cell.connected.length;i++)
        {
            Cell nearCell = cell.connected[i];
            if(nearCell != null && nearCell.hasElement()
                    && (nearCell.connected[nearCell.element.sides[Element.ONE]/2] == cell
            || nearCell.connected[nearCell.element.sides[Element.TWO]/2] == cell))
            {
                connectedSide = cell.faces[i].type;
                if(connectedSide == nearSide) break;
            }
        }

        lastSide = connectedSide == Face.FACE_NONE ? nearSide : connectedSide;
        headingSide = Face.FACE_NONE;
        currentPart = null;
    }

    private void setCurrentHeading(int side)
    {
        if(headingSide == side)
            return;

        headingSide = side;
        if (currentPart == null)
        {
            if(isFull()) return;

            currentPart = new PathPart();
            currentPart.cell = lastCell;
            currentPart.sides[Element.ONE] = lastSide;
            currentPart.firstTrio[0] = startTrio[0];
            currentPart.firstTrio[1] = startTrio[1];
            currentPart.firstTrio[2] = startTrio[2];
            path.add(currentPart);
            Log.i("curveIt", "ADD PATH CELL: " +currentPart.cell.cx + ", " +currentPart.cell.cy);
        }

        currentPart.sides[Element.TWO] = headingSide;
        currentPart.buildMask();

        //Here we try to find proper position for the last cell in our path that matches our item
        if(isFull() && !fullMatch())
        {
            Cell cell = currentPart.cell;
            for(int i=0;i<cell.faces.length;i++)
            {
                if(!cell.faces[i].enabled) continue;

                int faceType = cell.faces[i].type;
                //find out the last third face that we haven't used yet.
                if(faceType != currentPart.sides[Element.ONE] &&
                        faceType != currentPart.sides[Element.TWO])
                {
                    currentPart.sides[Element.TWO] = faceType;
                    currentPart.buildMask();
                    //if this new face isn't matched to the whole item
                    //then fallback to the previous face
                    if(!fullMatch())
                    {
                        currentPart.sides[Element.TWO] = headingSide;
                        currentPart.buildMask();
                    } else
                    {
                        headingSide = faceType;
                    }

                    break;
                }
            }
        }

        Log.i("curveIt", "LAST Side: " + lastSide + " heading for cell: " + headingSide);
    }

    private void putNextCell(Cell nextCell, Face face)
    {
        int side = Face.SIDE_CONNECTOR[face.type];
        PathPart part = currentPart;

        setLastCell(nextCell);
        lastSide = side;
        if(part != null)
        {
            if(part.sides[Element.ONE] == face.type)
            {
                part.sides[Element.ONE] = part.sides[Element.TWO];
            }

            part.sides[Element.TWO] = face.type;
            part.buildMask();
            removePartsTill(nextCell);
        }
    }

    protected void addPathPart(Cell nextCell, int side1, int side2)
    {
        PathPart part = new PathPart(side1, side2);
        part.cell = nextCell;
        part.buildMask();
        path.add(part);
    }

    protected void removeLastPart()
    {
        int idx = path.size() - 1;
        if(idx >= 0)
        {
            path.remove(idx);
        }
    }

    private void removePartsTill(Cell cell)
    {
        for(int i=path.size()-1;i>=0;i--)
        {
            PathPart pathPart = path.get(i);
            if(pathPart.cell == cell)
            {
                while(i<path.size())
                {
                    PathPart p = path.get(i);
                    Log.i("curveIt", "REMOVE PATH CELL: " +p.cell.cx + ", " +p.cell.cy);
                    path.remove(i);
                }

                if(path.size()>0)
                {
                    pathPart = path.get(path.size()-1);
                    lastSide = Face.SIDE_CONNECTOR[pathPart.sides[Element.TWO]];
                } else
                {
                    lastSide = pathPart.sides[Element.ONE];
                    startTrio[0] = pathPart.firstTrio[0];
                    startTrio[1] = pathPart.firstTrio[1];
                    startTrio[2] = pathPart.firstTrio[2];
                }
                break;
            }
        }
    }

    public boolean fullMatch()
    {
        chosenItem = null;

        if(jokerMode)
        {
            chosenItem = doJokerMatch();
            return chosenItem != null;
        }

        for(int i=0;i>=0;i--)
        {
            if(item[i].elements.length == path.size()
                && fullMatch(item[i]))
            {
                chosenItem = item[i];
                return true;
            }
        }
        return false;
    }

    private Item doJokerMatch()
    {
        if(path.size() < 2) return null;

        Item item = new Item(path.size());
        item.mode = Engine.mode;

        int i=0;
        for(PathPart part : path)
        {
            Element element = new Element(part.sides[0], part.sides[1]);
            element.setParentItem(item);
            item.elements[i++] = element;
        }

        reverse = false;
        flip = false;
        matchingSteps = 0;

        return item;
    }


    public boolean fullMatch(Item item)
    {
        //first we do normal matching
        boolean ret = doMatch(item);
        if(!ret)
        { //No way, let's try the reverse order
            reverse = !reverse;
            ret = doMatch(item);
        }
        if(!ret)
        { //Not lucky, let's try flipping and reversing
            flip = !flip;
            ret = doMatch(item);
        }
        if(!ret)
        { //And the last chance - flipping and no reverse
            reverse = !reverse;
            ret = doMatch(item);
        }
        return ret;
    }

    private boolean reverse = false;
    private boolean flip = false;

    private Element spare = new Element(0, 0);

    public boolean doMatch(Item item)
    {
        if(path.isEmpty()) { return false;}

        int len = path.size();
        len = len > item.elements.length ? item.elements.length : len;
        int steps = Face.FACE_NONE;


        for(int i = 0;i<len;i++)
        {
            int eidx = reverse ? len - i - 1 : i;
            Element part = path.get(i);

            Element element = item.elements[eidx];

            if(flip)
            {
                spare.sides[Element.ONE] = element.sides[Element.ONE];
                spare.sides[Element.TWO] = element.sides[Element.TWO];
                spare.flip();
                element = spare;
            }

            if(steps == Face.FACE_NONE)
            {
                int ret = element.match(part);
                if(ret < 0) //no match at all
                {
                    return false;
                }

                steps = ret;
            } else
            {
                int ret = element.match(part, steps);
                if(ret < 0)
                {
                    return false;
                }
            }
        }

        matchingSteps = steps;
        return true;
    }

    /**
     * Apply selected and matched path to the field
     */
    public void applyToField()
    {
        int len = path.size();
        len = len > chosenItem.elements.length ? chosenItem.elements.length : len;

        for(int i = 0;i<len;i++)
        {
            PathPart part = path.get(i);
            Element element = chosenItem.elements[reverse ? len - i -1 : i];
            if(flip) element.flip();
            element.rotate(matchingSteps);
            element.buildMask();

            game.setCellElement(part.cell, element);
        }
    }

    public Cell getPathCell()
    {
        if(path.isEmpty())
        {
            return null;
        }
        return path.get(0).cell;
    }

    public Item getChosenItem()
    {
        return chosenItem;
    }

    public boolean isActive()
    {
        return active;
    }

    public void setActive(boolean active)
    {
        this.active = active;
    }

    public boolean isJokerMode()
    {
        return jokerMode;
    }

    public void setJokerMode(boolean jokerMode)
    {
        this.jokerMode = jokerMode;
        if(jokerMode)
        {
            maxCells = MAX_JOKER_CELLS;
        } else
        {
            maxCells = item[0].elements.length;
        }
    }
}
