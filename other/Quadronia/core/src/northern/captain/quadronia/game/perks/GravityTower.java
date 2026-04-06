package northern.captain.quadronia.game.perks;

import java.util.Set;

import northern.captain.quadronia.game.Cell;
import northern.captain.quadronia.game.Center;
import northern.captain.quadronia.game.Element;
import northern.captain.quadronia.game.Engine;
import northern.captain.quadronia.game.Face;
import northern.captain.quadronia.game.Field;
import northern.captain.gamecore.glx.NContext;
import northern.captain.tools.Helpers;

/**
 * Created by leo on 27.04.15.
 */
public class GravityTower implements IFieldPerk
{
    private static final int[] SIDES = new int[]
            {
                    Face.FC_SOUTH, Face.FC_NE,
                    Face.FC_SW, Face.FC_SE,
                    Face.FC_NW, Face.FC_SOUTH,
                    Face.FC_NORTH, Face.FC_SW,
                    Face.FC_NE, Face.FC_NW,
                    Face.FC_SE, Face.FC_NORTH
            };

    private Center center;

    public Center getCenter()
    {
        return center;
    }

    @Override
    public boolean putToField(final Engine game, Field field)
    {
        Center[][] centers = field.getCenters();

        int width = field.fieldWid/2;
        int height = field.fieldHei;

//        Set<Cell> cells = field.getAllocatedCells();
//        cells.
        int x, y;
        for(int i=0; i < 20;i++)
        {
            x = Helpers.RND.nextInt(width);
            y = Helpers.RND.nextInt(height);

            Center center1 = centers[x][y];

            if(center1.hasAroundCells() && !center1.hasPerk()
                    && isCellsAvailable(center1.cellsAround, i > 14))
            {
                Cell lastEleCell = null;
                center = center1;
                center.setPerk(this);
                for(int j=0;j<center.cellsAround.length;j++)
                {
                    Cell cell = center.cellsAround[j];
                    cell.setPerk(this);
                    if(cell.hasElement())
                    {
                        int idx = j * 2;
                        Element element = new Element(SIDES[idx], SIDES[idx+1]);
                        element.setParentItem(cell.getElement().parentItem);
                        cell.setElement(element);
                        lastEleCell = cell;
                    }
                }
                game.addPerkOnEverySet(this);
                if(lastEleCell != null)
                {
                    field.setHasCellsChanged();
                    game.checkCellForCircuit(lastEleCell);
                }
                return true;
            }
        }
        return false;
    }

    private boolean isCellsAvailable(Cell[] cells, boolean noEle)
    {
        boolean hasEle = false;
        for(int i=0;i<cells.length;i++)
        {
            if(cells[i].hasPerk()) return false;
            hasEle |= cells[i].hasElement();
        }
        return hasEle | noEle;
    }

    @Override
    public boolean removeFromField(Engine game)
    {
        center.setPerk(null);
        for(int j=0;j<center.cellsAround.length;j++)
        {
            center.cellsAround[j].setPerk(null);
        }
        game.removePerkOnEverySet(this);
        game.getField().setHasCellsChanged();
        return true;
    }

    @Override
    public int getType()
    {
        return PERK_GRAVITY_TOWER;
    }

    @Override
    public boolean applyBonus(Engine game, Cell cell)
    {
        return false;
    }

    @Override
    public boolean applyOnSet(Engine game, Cell cell)
    {
        for(int i=0;i<center.cellsAround.length;i++)
        {
            if(center.cellsAround[i] == cell)
            {
                int idx = i*2;
                Element element = new Element(SIDES[idx], SIDES[idx+1]);
                element.setParentItem(cell.getElement().parentItem);
                cell.setElement(element);
                game.getField().setHasCellsChanged();
                if(game.checkCellForCircuit(cell))
                {
                    removeFromField(game);
                }
            }
        }
        return true;
    }
}
