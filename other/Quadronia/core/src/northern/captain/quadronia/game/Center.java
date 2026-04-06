package northern.captain.quadronia.game;

import northern.captain.quadronia.game.perks.IFieldPerk;

/**
 * Created by leo on 26.04.15.
 */
public class Center extends Point
{
    public Cell[] cellsAround = new Cell[6];

    public IFieldPerk perk;

    public Center()
    {
    }

    public Center(int x, int y)
    {
        super(x, y);
    }

    public void setStartCell(Cell startCell)
    {
        cellsAround[0] = startCell;
    }

    public boolean hasAroundCells()
    {
        return cellsAround[0] != null;
    }

    public void buildAround()
    {
        if(!hasAroundCells()) return;

        for(int i = 1; i<cellsAround.length;i++)
        {
            int side = Face.AROUND_CENTER[i-1];
            Cell cell = cellsAround[i-1];

            if(cell == null)
            {
                cellsAround[0] = null;
                break;
            }
            cellsAround[i] = cell.connected[side/2];
        }

        if(cellsAround[cellsAround.length - 1] == null)
        {
            cellsAround[0] = null;
        }
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
        return  perk != null;
    }

}
