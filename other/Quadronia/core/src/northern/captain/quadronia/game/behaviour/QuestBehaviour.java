package northern.captain.quadronia.game.behaviour;

import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.Array;

import java.util.ArrayList;
import java.util.List;

import northern.captain.quadronia.game.Cell;
import northern.captain.quadronia.game.Engine;
import northern.captain.quadronia.game.Field;
import northern.captain.quadronia.game.FieldConfig;
import northern.captain.quadronia.game.FieldLayouts;
import northern.captain.quadronia.game.IGameTimer;
import northern.captain.quadronia.game.IGraphicsContext;
import northern.captain.quadronia.game.perks.Barrier;
import northern.captain.quadronia.game.perks.ExitConnector;
import northern.captain.quadronia.game.perks.IFieldPerk;
import northern.captain.quadronia.game.solver.QuestPathSolver;
import northern.captain.quadronia.gfx.ExitConnectorDraw;
import northern.captain.quadronia.gfx.WhiteScreenDraw;
import northern.captain.gamecore.glx.NContext;
import northern.captain.gamecore.glx.tools.Animations;
import northern.captain.tools.AsyncGTask;

/**
 * Created by leo on 17.05.15.
 */
public class QuestBehaviour extends GameBehaviour
{
    private int maxTimeout = 45;
    private int currentLevel = 0;
    private int numExits = 3;

    private Array<Cell> availableCells = new Array<Cell>(false, 100);
    private WhiteScreenDraw whiteScreenDraw;
    private QuestPathSolver solver = new QuestPathSolver();

    public QuestBehaviour(Engine game)
    {
        super(game);

        timeout = maxTimeout;
    }

    @Override
    public void doOnTimeOut()
    {
        IGameTimer gameTimer = game.getGameTimer();
        gameTimer.resetTimer(getTimeOut());
        game.doFieldPerk();
    }

    @Override
    public void doOnNextMove()
    {
        IGameTimer gameTimer = game.getGameTimer();
        gameTimer.resetTimer(getTimeOut());
    }

    @Override
    public void doOnLevelUp(final int newLevel)
    {
        int delta = newLevel;
        timeout = Math.max(10, maxTimeout - delta);
    }

    @Override
    public int nextLevelUpThreshold(int forLevel)
    {
        return NO_LEVEL_UP;
    }

    @Override
    public boolean levelUpsAllowed()
    {
        return false;
    }

    private void nextLayout(final int newLevel)
    {
        List<FieldLayouts.Layout> layouts = FieldLayouts.getLAYOUTS();

        final int idx = newLevel % layouts.size();

        final FieldConfig cfg = game.getField().cfg;

        cfg.setLayout(layouts.get(idx));
        game.buildField(cfg);
        game.getGraphicsContext().onFieldRebuild();
    }

    private List<ExitConnector> connectors = new ArrayList<ExitConnector>();

    public List<ExitConnector> getConnectors()
    {
        return connectors;
    }

    public Array<Cell> getAvailableCells()
    {
        return availableCells;
    }

    private void fillAvailableCells(Field field)
    {
        availableCells.clear();
        Array<Cell> empties = field.getEmptyCellList();
        for(Cell cell : empties)
        {
            if(
                cell.canHaveElement(3)
                    && !cell.hasPerk()
                )
            {
                availableCells.add(cell);
            }
        }
    }

    private void generateExits()
    {
        connectors.clear();

        Field field = game.getField();

        fillAvailableCells(field);
        int limit = 4;

        for (int i = 0; i < numExits || connectors.size() < 2; i++)
        {
            ExitConnector connector = new ExitConnector(i);
            connector.setBehaviour(this);
            connector.setLimit(limit);

            if(connector.putToField(this.game, field))
            {
                connectors.add(connector);
            } else
            {
                i--;
                if(availableCells.size == 0)
                {
                    fillAvailableCells(field);
                    limit--;
                }
            }
        }

        solver.solve(connectors, field);
        generateObstacles(field);
    }

    final List<IFieldPerk> obstacles = new ArrayList<IFieldPerk>();

    private List<IFieldPerk> generateObstacles(Field field)
    {
        obstacles.clear();

        if(solver.isSolved())
        {
            for(int i= 0; i< numExits;i++)
            {
                Barrier barrier = new Barrier(i+1)
                {
                    @Override
                    protected boolean isCellAccepted(Cell cell)
                    {
                        return !solver.getSeenCells().contains(cell) && super.isCellAccepted(cell);
                    }
                };

                if(barrier.putToField(this.game, field))
                {
                    obstacles.add(barrier);
                }
            }
        }

        return obstacles;
    }

    /**
     * Detect if we have full circuit with all exits inside
     * @return
     */
    public boolean detectFullCircuit()
    {
        List<Cell> cells = game.detector.getCircuit();
        int exitsFound = 0;
        for(Cell cell : cells)
        {
            if (cell.hasPerk())
            {
                IFieldPerk perk = cell.getPerk();
                if(perk.getType() == IFieldPerk.PERK_EXIT)
                {
                    exitsFound++;
                }
            }
        }

        if(exitsFound == connectors.size())
        {
            doNextLevel();
            return true;
        }

        return false;
    }

    private int getExitLevelThreshold()
    {
        return FieldLayouts.getLAYOUTS().size();
    }

    /**
     * 1. We start animation of the current exits. They'll start spinning and moving to the center of the field
     * 2. Then we start white flash animation that covers the whole screen
     * 3. When the animation ends we start async task for constructing new level, generating exits and finding path
     * 4. At the end of the async task we start fading animation of the flash
     * 5. The flash has just disappeared and we finally create our graphic objects for exits and obstacles and
     * start to animate then
     * 6. User can now play...
     */
    private void doNextLevel()
    {
        IGraphicsContext graphicsContext = game.getGraphicsContext();
        Animations.HeliMove heliMove = new Animations.HeliMove(graphicsContext.getFieldCenterX(), graphicsContext.getFieldCenterY());

        for (ExitConnector connector : connectors)
        {
            connector.setActivated();
            ExitConnectorDraw draw = (ExitConnectorDraw)(connector.getDrawable());
            draw.setLiveAnimation(heliMove.create(0));
        }

        currentLevel++;

        if(currentLevel >= getExitLevelThreshold())
        {
            currentLevel = 0;
            numExits++;
        }

        NContext.current.postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                whiteScreenDraw = new WhiteScreenDraw(game);
                game.getGraphicsContext().getWidgetBoard().addDrawableToScene(whiteScreenDraw, true);
                whiteScreenDraw.setOnAnimationDone(atNext);
                game.doLevelUp();
            }
        }, 950);
    }

    private AsyncGTask<Boolean, Boolean> solveLevelTask = new AsyncGTask<Boolean, Boolean>()
    {
        @Override
        public Boolean doInBackground(Boolean... needAnimation)
        {
            generateExits();
            return needAnimation[0];
        }

        @Override
        public void onPostExecute(Boolean needAnimation)
        {
            if(needAnimation)
            {
                game.getGraphicsContext().getWidgetBoard().addDrawableToScene(whiteScreenDraw, false);
                whiteScreenDraw.setAction(Actions.fadeOut(1.0f));
                whiteScreenDraw.setOnAnimationDone(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        NContext.current.post(atEnd);
                    }
                });
            } else
            {
                atEnd.run();
            }
        }
    };

    private Runnable atNext = new Runnable()
    {
        @Override
        public void run()
        {
            nextLayout(currentLevel);
            solveLevelTask.execute(true);
        }
    };

    private Runnable atEnd = new Runnable()
    {
        @Override
        public void run()
        {
            game.getGraphicsContext().getWidgetBoard().removeDrawable(whiteScreenDraw);
            for(ExitConnector connector : connectors)
            {
                connector.setDrawable(game.getGraphicsContext().getWidgetBoard().generatePerkDraw(connector));
            }

            for(IFieldPerk obstacle : obstacles)
            {
                game.getGraphicsContext().getWidgetBoard().generatePerkDraw(obstacle);
            }

            obstacles.clear();
        }
    };

    @Override
    public void doOnStart(boolean resumed)
    {
        super.doOnStart(resumed);

        //Just generate exits in background without any animation
        solveLevelTask.execute(false);
    }

    public QuestPathSolver getSolver()
    {
        return solver;
    }
}
