package northern.captain.quadronia.game.perks;

import northern.captain.quadronia.game.Cell;
import northern.captain.quadronia.game.Element;
import northern.captain.quadronia.game.Engine;
import northern.captain.quadronia.game.Face;
import northern.captain.quadronia.game.Field;
import northern.captain.tools.Helpers;

/**
 * Created by leo on 05.04.15.
 */
public abstract class ElePerk extends Element implements IFieldPerk
{
    protected int cycleLimit = 10;

    public ElePerk()
    {
        super(Face.FC_NORTH);
    }

    public ElePerk(int side1, int side2)
    {
        super(side1, side2);
    }

    public ElePerk(int side1)
    {
        super(side1);
    }

    @Override
    public boolean putToField(Engine game, Field field)
    {
        int width = field.fieldWid - 2;
        int height = field.fieldHei - 2;

        int x, y;
        for(int i=0; i < cycleLimit;i++)
        {
            x = Helpers.RND.nextInt(width);
            y = Helpers.RND.nextInt(height);

            cell = field.cell(x, y);
            if(cell == null || !cell.enabled) continue;

            if(isCellAccepted(cell))
            {
                activate(game);
                return true;
            }
        }
        return false;
    }

    protected boolean isCellAccepted(Cell cell)
    {
        return cell.canHaveElement() && !cell.hasPerk();
    }

    protected void activate(Engine game)
    {
        game.setElePerk(cell, this);
    }

    public abstract String getSpriteName();

    @Override
    public boolean removeFromField(Engine game)
    {
        game.setElePerk(cell, null);
        return false;
    }

    @Override
    public boolean applyOnSet(Engine game, Cell cell)
    {
        return false;
    }
}
