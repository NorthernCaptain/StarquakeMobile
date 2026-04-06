package northern.captain.quadronia.game.solver;

import java.util.Set;

import northern.captain.quadronia.game.Cell;
import northern.captain.quadronia.game.Element;
import northern.captain.quadronia.game.Engine;
import northern.captain.quadronia.game.Face;
import northern.captain.quadronia.game.Field;
import northern.captain.quadronia.game.Item;

/**
 * Created by leo on 25.04.15.
 */
public class GameFieldSolver extends northern.captain.quadronia.game.solver.PathTracer
{
    public GameFieldSolver(Engine game, Field field)
    {
        super(game, field);
    }

    private Item[] swappedItems = new Item[2];

    public boolean solve(Item[] items)
    {
        boolean ret = subSolve(items);
        if(!ret && game.canSwapNexts())
        {
            swappedItems[0] = items[1];
            swappedItems[1] = items[0];

            ret = subSolve(swappedItems);
        }

        return ret;
    }

    private boolean subSolve(Item[] items)
    {
        setItem(items);
        reset();

        Set<Cell> cells = field.getAllocatedCells();
        for(Cell cell : cells)
        {
            if(traceConnectedCell(cell))
            {
                return true;
            }
        }

        for(int y=0;y<field.fieldHei;y++)
        {
            for (int x = 0; x < field.fieldWid; x++)
            {
                Cell cell = field.field[x][y];

                if(!cell.canHaveElement()) continue;

                if(tracePathFrom(cell))
                {
                    //We found first field position for our item
                    //exact positions and sequence of the cells see 'path' list
                    return true;
                }
            }
        }
        return false;
    }
    /**
     * This cell has element inside, we see if one of its edges doesn't have element
     * and try to connect our item to it
     * @param cell with the existing element
     * @return true if item was connected to this cell. 'path' list contains the path
     */
    private boolean traceConnectedCell(Cell cell)
    {
        boolean ret = false;
        Element element = cell.getElement();
        int side1 = element.sides[Element.ONE];
        Cell nextCell = cell.connected[side1/2];

        if(nextCell != null && nextCell.canHaveElement())
        {
            reset();
            ret = traceRecursively(nextCell, Face.SIDE_CONNECTOR[side1]/2);
        }

        if(!ret)
        {
            side1 = element.sides[Element.TWO];
            nextCell = cell.connected[side1/2];

            if(nextCell != null && nextCell.canHaveElement())
            {
                reset();
                ret = traceRecursively(nextCell, Face.SIDE_CONNECTOR[side1]/2);
            }
        }
        return ret;
    }

    /**
     * Take an empty cell and try to find a path to put the item starting from this cell
     * @param cell - empty cell to start with
     * @return true if the path was found. 'path' list contains the path
     */
    private boolean tracePathFrom(Cell cell)
    {
        reset();

        Face[] faces = cell.faces;

        for(int i=0;i<faces.length;i++)
        {
            if(faces[i].enabled
                &&
               traceRecursively(cell, i))
                return true;
        }

        return false;
    }

    private boolean traceRecursively(Cell cell, int faceIdx)
    {
        if(isFull()) return fullMatch();

        if(cell == null || !cell.faces[faceIdx].enabled) return false;

        Face[] faces = cell.faces;

        int faceSide1 = faces[faceIdx].type;
        int faceSide2;
        boolean ret;

        for(int i=0;i<faces.length;i++)
        {
            if(i != faceIdx && faces[i].enabled)
            {
                faceSide2 = faces[i].type;

                addPathPart(cell, faceSide1, faceSide2);

                if(isFull())
                {
                    if (fullMatch()) return true;
                    removeLastPart();
                    continue;
                }

                Cell nextCell = cell.connected[faceSide2/2];

                if(nextCell != null && nextCell.canHaveElement())
                {
                    int nextSide1 = Face.SIDE_CONNECTOR[faceSide2];

                    ret = traceRecursively(nextCell, nextSide1 / 2);

                    if (ret)
                    {
                        return true;
                    }
                }

                removeLastPart();
            }
        }

//        seenCells.add(cell);

        return false;
    }
}
