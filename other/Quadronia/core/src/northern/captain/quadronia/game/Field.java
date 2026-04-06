package northern.captain.quadronia.game;

import com.badlogic.gdx.utils.Array;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import northern.captain.quadronia.game.perks.ElePerk;
import northern.captain.quadronia.game.perks.IFieldPerk;

/**
 * Created by leo on 2/27/15.
 */
public abstract class Field
{
    public static final float SIN3 = 0.5f;
    public static final float COS3 = 0.866f;

    public FieldConfig cfg;
    /**
     * Field area with cells
     */
    public Cell field[][];

    /**
     * Field length in units
     */
    public int fieldWid;
    /**
     * Field height in units
     */
    public int fieldHei;

    /**
     * External radius
     */
    public int rE;
    /**
     * Internal radius
     */
    public int rI;

    /**
     * X coord of first upper-left circle
     */
    int xC;
    /**
     * Y cooed of the first upper-left circle
     */
    int yC;

    /**
     * Upper left field corner X coordinate
     */
    public int cornerX;
    /**
     * Upper left field corner Y coordinate
     */
    public int cornerY;

    /**
     * Width of the field in pixels
     */
    public int pixelWid;

    /**
     * Height of the field in pixels
     */
    public int pixelHei;

    public Field(int sizeX, int sizeY)
    {
        fieldWid = sizeX;
        fieldHei = sizeY;

        field = new Cell[fieldWid][fieldHei];
    }


    public int rEI;
    public int cos3rEI;
    public int sin3rEI;
    public int cos3rE;
    public int sin3rE;
    public int cos3rI;
    public int sin3rI;

    public int deltaEleX;
    public int deltaEleY;
    public int eleR;

    Center centers[][];
    BoundingBox centerBoxes[][];

    /**
     * Sets the radiuses for big circles and upper left corner of the field
     * @param cfg
     * @return
     */
    public abstract Field setRadius(FieldConfig cfg);
    public abstract Field build();
    protected abstract void buildCell(int x, int y, Cell cell);

    public int getPixelWid()
    {
        return pixelWid;
    }

    public int getPixelHei()
    {
        return pixelHei;
    }

    public Center[][] getCenters()
    {
        return centers;
    }

    public BoundingBox[][] getCenterBoxes()
    {
        return centerBoxes;
    }

    public Cell cell(int x, int y)
    {
        return field[x][y];
    }

    public Cell getCellByCoord(int x, int y)
    {
        int cosr = cos3rEI;
        for(int iy = 0;iy< centers[0].length;iy++)
        {
            int ceny = centers[0][iy].y;
            if(y < ceny && y>= ceny - cosr)
            {
                for(int ix = 0;ix<fieldWid;ix++)
                {
                    Cell cell = field[ix][iy];
                    if(cell.isIn(x, y))
                    {
                        return cell;
                    }
                }
            }
        }
        return null;
    }

    Set<Cell> allocatedCells = new HashSet<Cell>();
    Array<Cell> allocatedCellList = new Array<Cell>(100);
    Set<Cell> emptyCells = new HashSet<Cell>();
    Array<Cell> emptyCellList = new Array<Cell>(100);

    public void clear()
    {
        allocatedCellList.clear();
        allocatedCells.clear();
        emptyCellList.clear();
        emptyCells.clear();
    }

    boolean hasCellsChanged = false;

    public boolean isHasCellsChanged()
    {
        return hasCellsChanged;
    }

    public void resetHasCellsChanged()
    {
        hasCellsChanged = false;
    }

    public void setHasCellsChanged()
    {
        hasCellsChanged = true;
    }

    public Set<Cell> getAllocatedCells()
    {
        return allocatedCells;
    }

    public Array<Cell> getAllocatedCellList()
    {
        return allocatedCellList;
    }

    public Array<Cell> getEmptyCellList()
    {
        return emptyCellList;
    }

    public Set<Cell> getEmptyCells()
    {
        return emptyCells;
    }

    public void setCellElement(Cell cell, Element element)
    {
        cell.setElement(element);
        if(element == null)
        {
            if(allocatedCells.remove(cell))
            {
                allocatedCellList.removeValue(cell, true);
            }

            if(emptyCells.add(cell))
            {
                cell.arrayIdx = emptyCellList.size;
                emptyCellList.add(cell);
            }
        } else
        {
            if(emptyCells.remove(cell))
            {
                emptyCellList.removeValue(cell, true);
            }

            if(allocatedCells.add(cell))
            {
                cell.arrayIdx = allocatedCellList.size;
                allocatedCellList.add(cell);
            }
        }
        hasCellsChanged = true;
    }

    Set<Cell> perkCells = new LinkedHashSet<Cell>();

    public void setElePerk(Cell cell, IFieldPerk perk)
    {
        cell.setPerk(perk);
        if(perk == null)
        {
            perkCells.remove(cell);
        } else
        {
            perkCells.add(cell);
        }

        if(cell.hasElement())
        {
            hasCellsChanged = true;
        }
    }

    public Set<Cell> getPerkCells()
    {
        return perkCells;
    }
}
