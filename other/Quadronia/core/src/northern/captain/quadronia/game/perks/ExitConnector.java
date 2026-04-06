package northern.captain.quadronia.game.perks;

import com.badlogic.gdx.utils.Array;

import java.util.List;

import northern.captain.quadronia.game.Cell;
import northern.captain.quadronia.game.Engine;
import northern.captain.quadronia.game.Field;
import northern.captain.quadronia.game.behaviour.QuestBehaviour;
import northern.captain.quadronia.gfx.IDraw;
import northern.captain.tools.Helpers;

/**
 * Created by leo on 23.07.15.
 */
public class ExitConnector extends ElePerk
{
    private int sequenceNumber;
    private QuestBehaviour behaviour;
    private IDraw drawable;
    private boolean activated = false;

    public ExitConnector(int seqNum)
    {
        super();
        sequenceNumber = seqNum;
        cycleLimit = 20;
    }

    @Override
    public boolean putToField(Engine game, Field field)
    {
        Array<Cell> cells = behaviour.getAvailableCells();

        while(cells.size > 0)
        {
            int idx = 0;

            if(cells.size > 1) idx = Helpers.RND.nextInt(cells.size);

            Cell cell = cells.get(idx);
            cells.removeIndex(idx);

            if(isCellAccepted(cell))
            {
                this.cell = cell;
                activate(game);
                return true;
            }
        }
        return false;
    }

    public void setCell(Cell cell, Engine game)
    {
        this.cell = cell;
        activate(game);
    }

    public int getSequenceNumber()
    {
        return sequenceNumber;
    }

    public IDraw getDrawable()
    {
        return drawable;
    }

    public void setDrawable(IDraw drawable)
    {
        this.drawable = drawable;
    }

    public QuestBehaviour getBehaviour()
    {
        return behaviour;
    }

    public void setBehaviour(QuestBehaviour behaviour)
    {
        this.behaviour = behaviour;
    }

    @Override
    public String getSpriteName()
    {
        return "exit1";
    }

    /**
     * Gets the type of the perk
     *
     * @return type of the perk, see contants in IFieldPerk
     */
    @Override
    public int getType()
    {
        return PERK_EXIT;
    }

    /**
     * Apply this bonus to the game
     *
     * @param game
     * @param appliedCell
     * @return
     */
    @Override
    public boolean applyBonus(Engine game, Cell appliedCell)
    {
        return activated || behaviour.detectFullCircuit();
    }

    int limit = 3;

    public void setLimit(int limit)
    {
        this.limit = limit;
    }

    @Override
    protected boolean isCellAccepted(Cell cell)
    {
        List<ExitConnector> connectors = behaviour.getConnectors();
        for (ExitConnector connector : connectors)
        {
            if (connector.cell.lDistance(cell) < limit) return false;
        }
        return true;
    }

    public void setActivated()
    {
        activated = true;
    }
}

