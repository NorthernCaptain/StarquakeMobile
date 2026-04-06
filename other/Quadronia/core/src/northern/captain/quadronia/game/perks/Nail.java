package northern.captain.quadronia.game.perks;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import northern.captain.quadronia.game.Cell;
import northern.captain.quadronia.game.Engine;
import northern.captain.quadronia.game.Face;
import northern.captain.quadronia.game.Field;
import northern.captain.quadronia.game.GeoFace;
import northern.captain.gamecore.glx.NContext;
import northern.captain.tools.Helpers;

/**
 * Created by leo on 02.04.15.
 */
public class Nail implements IFieldPerk
{
    private Cell cell;
    private boolean active;

    public float cx, cy;

    /**
     * Find a place where this perk can be set to the field
     * @param field
     * @return true if the perk was put to the field, false otherwise
     */
    @Override
    public boolean putToField(Engine game, Field field)
    {
        Set<Cell> cells = field.getAllocatedCells();

        if(cells.isEmpty()) return false;

        for(int i=0; i < 10;i++)
        {
            int x = Helpers.RND.nextInt(cells.size());

            for(Iterator<Cell>it = cells.iterator();it.hasNext();)
            {
                cell = it.next();

                if(x == 0)
                {
                    if(cell.hasPerk()) continue;

                    activate(game, field);

                    return true;
                }
                x--;
            }
        }
        active = false;
        return false;
    }

    private void activate(Engine game, Field field)
    {
        cell.setPerk(this);
        cell.getElement().setRemovable(false);
        cx = cell.x0;
        cy = cell.y0;
        game.setIFieldPerk(this);
        field.setHasCellsChanged();
        active = true;
    }

    @Override
    public boolean removeFromField(Engine game)
    {
        active = false;
        cell.getElement().setRemovable(true);
        cell.setPerk(null);
        game.getField().setHasCellsChanged();
        game.unsetIFieldPerk(this);
        return true;
    }

    @Override
    public int getType()
    {
        return PERK_NAIL;
    }

    @Override
    public boolean applyBonus(final Engine game, Cell cell1)
    {
        NContext.current.postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                removeFromField(game);
            }
        }, 900);
        return true;
    }

    @Override
    public boolean applyOnSet(Engine game, Cell cell)
    {
        return false;
    }
}
