package northern.captain.quadronia.game.solver;

import com.badlogic.gdx.utils.Array;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import northern.captain.quadronia.game.Cell;
import northern.captain.quadronia.game.Element;
import northern.captain.quadronia.game.Face;
import northern.captain.quadronia.game.Field;
import northern.captain.quadronia.game.perks.ExitConnector;
import northern.captain.tools.Log;

/**
 * Created by leo on 02.08.15.
 */
public class QuestPathSolver
{
    private Field field;
    private Array<ExitConnector> exitConnectors = new Array<ExitConnector>(true, 10);

    public QuestPathSolver()
    {

    }

    public boolean solve(List<ExitConnector> connectors, Field field)
    {
        this.field = field;

        clear();
//        exitConnectors.add(connectors.get(0));

        boolean ret = buildAndSolve(connectors, 0, connectors.size());

        return ret;
    }

    private boolean buildAndSolve(List<ExitConnector> connectors, int startIdx, int len)
    {
        boolean ret;
        for(int i = startIdx; i<len;i++)
        {
            ExitConnector connector = connectors.get(i);
            if(!exitConnectors.contains(connector, true))
            {
                int idx = exitConnectors.size;
                exitConnectors.add(connector);
                if(exitConnectors.size == len)
                {
                    ret = doSolve();
                    seenCells.clear();
                } else
                {
                    ret = buildAndSolve(connectors, startIdx, len);
                }

                exitConnectors.removeIndex(idx);
                if(ret)
                {
                    for(int j=0;j<path.size;j++)
                    {
                        seenCells.add(path.get(j).cell);
                    }
                    return true;
                }
            }
        }

        return false;
    }

    public Set<Cell> getSeenCells()
    {
        return seenCells;
    }

    public Array<Element> getPath()
    {
        return path;
    }

    public boolean isSolved()
    {
        return path.size > 0;
    }

    private Array<Element> path = new Array<Element>(true, 100);
    private Set<Cell> seenCells = new HashSet<Cell>(100);

    private boolean doSolve()
    {
        ExitConnector first = exitConnectors.get(0);
        ExitConnector second = exitConnectors.get(1);

        Cell startCell = first.cell;
        Cell headingTo = second.cell;
        Cell curCell = startCell;

        Element element = Element.POOL.obtain();
        element.init(Face.FC_NOFACE, Face.FC_NOFACE);
        element.setRemovable(false);

        path.clear();
        seenCells.clear();
        int dist = 0;
        while(true)
        {
            Cell nextCell = curCell.getConnectedHeadingTo(headingTo, element);
            if(nextCell != null)
            {
                if(seenCells.contains(nextCell) && (nextCell != startCell || headingTo != startCell))
                {
                    element.shiftSides();
                    continue;
                }

                dist = nextCell.lDistance(headingTo);

//                Log.i("ncgame", "MV: " + curCell.cx + ", " + curCell.cy + " -> " + nextCell.cx + ", " + nextCell.cy);

                element.cell = curCell;
                element.buildMask();
                path.add(element);
                seenCells.add(curCell);

                element = Element.POOL.obtain().init(Face.SIDE_CONNECTOR[element.sides[Element.TWO]], Face.FC_NOFACE);
                element.cell = nextCell;

                if(nextCell == headingTo)
                {
                    element.setRemovable(false);
                    //go to next heading element or finish if it was the last one
                    if(nextCell == startCell)
                    {
                        //We've just reached the starting point, so we have a full circle and we are done
                        Element firstElement = path.first();
                        firstElement.sides[Element.ONE] = element.sides[Element.ONE];
                        firstElement.buildMask();
                        break;
                    } else
                    {
                        for(int idx = 0; idx < exitConnectors.size; idx++)
                        {
                            if(exitConnectors.get(idx).cell == headingTo)
                            {
                                idx = (idx + 1) % exitConnectors.size;
                                headingTo = exitConnectors.get(idx).cell;
                                break;
                            }
                        }
                    }
                }

                curCell = nextCell;
            } else
            {
                if(path.size > 0)
                {
                    //restore previous heading on rollback
                    if(!element.isRemovable())
                    {
                        headingTo = element.cell;
                        seenCells.remove(element.cell);
                    } else
                        seenCells.add(element.cell); //remove because we need to visit it again

                    Element.POOL.free(element);
                    int lastIdx = path.size -1;
                    element = path.get(lastIdx);
                    curCell = element.cell;
                    path.removeIndex(lastIdx);

//                    Log.i("ncgame", "BK: " + curCell.cx + ", " + curCell.cy + " <- ");

                    element.shiftSides();
                } else
                {
                    Element.POOL.free(element);
                    break;
                }
            }
        }

        return path.size > 0;
    }

    private void clear()
    {
        for(Element element : path)
        {
            Element.POOL.free(element);
        }

        path.clear();
        seenCells.clear();
    }
}
