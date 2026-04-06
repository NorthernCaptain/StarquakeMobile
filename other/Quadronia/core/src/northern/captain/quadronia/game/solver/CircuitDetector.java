package northern.captain.quadronia.game.solver;

import java.util.ArrayList;
import java.util.List;

import northern.captain.quadronia.game.Cell;
import northern.captain.quadronia.game.Element;
import northern.captain.quadronia.game.Engine;
import northern.captain.quadronia.game.Face;

/**
 * Created by leo on 3/10/15.
 */
public class CircuitDetector
{
    private static final int MAX_ALLOWED_SIZE = 300;

    private List<Cell> circuit = new ArrayList<Cell>();
    private Cell startCell;
    private boolean isCircuit = false;
    private Engine game;
    private boolean hasPerk;

    public CircuitDetector(Engine game)
    {
        this.game = game;
    }

    public boolean detectCircuit(Cell startCell)
    {
        clear();

        if(startCell == null || !startCell.hasElement())
        {
            return false;
        }

        this.startCell = startCell;
        circuit.add(startCell);
        hasPerk = startCell.hasPerk();

        Cell prevCell = startCell;
        Cell cell = nextCell(prevCell, null);

        while(cell != null && circuit.size() < MAX_ALLOWED_SIZE)
        {
            hasPerk |= cell.hasPerk();
            if(cell == startCell)
            {
                //We are circled!
                isCircuit = true;
                return true;
            }
            circuit.add(cell);
            Cell oldCell = prevCell;
            prevCell = cell;
            cell = nextCell(cell, oldCell);
        }

        return false;
    }

    public void clear()
    {
        isCircuit = false;
        startCell = null;
        circuit.clear();
    }

    public void clearCircuitCells()
    {
        if(!isCircuit) return;

        for(Cell cell : circuit)
        {
            game.setCellElement(cell, null);
        }
    }

    private Cell nextCell(Cell cell, Cell visitedCell)
    {
        Element element = cell.element;
        Cell nextCell;

        nextCell = cell.connected[element.sides[Element.ONE]/2];
        if(nextCell != null && nextCell != visitedCell && nextCell.hasElement())
        {
            Element nextElement = nextCell.element;
            int side = Face.SIDE_CONNECTOR[element.sides[Element.ONE]];
            if(nextElement.sides[Element.ONE] == side || nextElement.sides[Element.TWO] == side)
            {
                return nextCell;
            }
        }

        nextCell = cell.connected[element.sides[Element.TWO]/2];
        if(nextCell != null && nextCell != visitedCell && nextCell.hasElement())
        {
            Element nextElement = nextCell.element;
            int side = Face.SIDE_CONNECTOR[element.sides[Element.TWO]];
            if(nextElement.sides[Element.ONE] == side || nextElement.sides[Element.TWO] == side)
            {
                return nextCell;
            }
        }

        return null;
    }

    public List<Cell> getCircuit()
    {
        return this.circuit;
    }

    public boolean hasPerkInside()
    {
        return hasPerk;
    }

    public Engine getGame()
    {
        return game;
    }
}
