package northern.captain.quadronia.game;

import com.badlogic.gdx.utils.Array;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Set;

import northern.captain.quadronia.game.achievements.AchieveMan;
import northern.captain.quadronia.game.behaviour.ArcadeBehaviour;
import northern.captain.quadronia.game.behaviour.ExpressBehaviour;
import northern.captain.quadronia.game.behaviour.GameBehaviour;
import northern.captain.quadronia.game.behaviour.QuestBehaviour;
import northern.captain.quadronia.game.perks.ElePerk;
import northern.captain.quadronia.game.perks.ElePerkGenerator;
import northern.captain.quadronia.game.perks.FieldObstacleGenerator;
import northern.captain.quadronia.game.perks.IFieldPerk;
import northern.captain.gamecore.glx.NContext;
import northern.captain.gamecore.glx.res.SharedRes;
import northern.captain.tools.Helpers;
import northern.captain.tools.Randomizer;

/**
 * Created by leo on 10.03.15.
 */
public class Engine
{
    public static final int TYPE_ARCADE = 0;
    public static final int TYPE_EXPRESS = 1;
    public static final int TYPE_QUEST = 2;

    public static final int STATE_PLAYING = 1;
    public static final int STATE_PAUSED = 2;
    public static final int STATE_GAMEOVER = 3;


    private static final int[] levelUps = new int[] { 96, 96};

    public static IGameContext defaultGameContextOld = new SimpleGameContext();

    private Field field;
    public northern.captain.quadronia.game.solver.PathTracer tracer;
    public northern.captain.quadronia.game.solver.GameFieldSolver solver;
    public Item[] currentItem = new Item[3];
    public northern.captain.quadronia.game.solver.CircuitDetector detector;
    public Score score;

    private Randomizer rand, randLevelUp;
    private ElePerkGenerator bonusGenerator;
    private FieldObstacleGenerator obstacleGenerator;

    private IGameContext context;
    private IGraphicsContext graphicsContext;

    public static int mode = ItemFactory.MODE_TWO;

    private Deque<Item> backStack = new ArrayDeque<Item>();

    private IFieldPerk lastObstacle;
    private ElePerk lastBonus;

    private Array<IFieldPerk> allPerks = new Array<IFieldPerk>(false, 20);

    private Array<IFieldPerk> everyEleSetPerks = new Array<IFieldPerk>(false, 20);

    private int state;

    private int gameType = TYPE_ARCADE;

    private GameBehaviour behaviour;

    public Engine(IGameContext context)
    {
        this.context = context;
        score = new Score(context);
        gameType = context.getGameType();
        mode = context.getLastMode();
        context.clear();
    }

    public interface ResultsCallback
    {
        void itemDropped(Item item, Cell cell, int scoreAddon);
        void circuitBuilt(northern.captain.quadronia.game.solver.CircuitDetector detector, int scoreBonus);
        void allCleared(int scoreBonus);
        void itemRolledBack(Item item, int scoreMinus);
        void onLevelUp(int newLevel, int bonusScore);
        void bonusPerksApplied(Array<IFieldPerk> perks);
        void fieldPerkCreated(IFieldPerk perk);
        void fieldPerkRemoved(IFieldPerk perk);
        void gameOver();
        void swapUsed();
        void bonusApplied(int bonusType);
    }

    private ResultsCallback resultsCallback;

    public void setResultsCallback(ResultsCallback resultsCallback)
    {
        this.resultsCallback = resultsCallback;
    }

    public interface TouchCallback
    {
        void onTouchDown(int x, int y);
        void onDrag(int x, int y);
        void onTouchUp(int x, int y);
    }

    private TouchCallback touchCallback;

    public void setTouchCallback(TouchCallback touchCallback)
    {
        this.touchCallback = touchCallback;
    }

    private IGameTimer gameTimer;

    public void setGameTimer(IGameTimer gameTimer)
    {
        this.gameTimer = gameTimer;
    }

    public IGameTimer getGameTimer()
    {
        return gameTimer;
    }

    public IGameContext getContext()
    {
        return context;
    }

    public IGraphicsContext getGraphicsContext()
    {
        return graphicsContext;
    }

    public Field getField()
    {
        return field;
    }

    public void setGraphicsContext(IGraphicsContext graphicsContext)
    {
        this.graphicsContext = graphicsContext;
    }

    public boolean isGameOver()
    {
        return state == STATE_GAMEOVER;
    }

    public boolean isPaused()
    {
        return state == STATE_PAUSED;
    }

    public boolean isPlaying()
    {
        return state == STATE_PLAYING;
    }

    public GameBehaviour getBehaviour()
    {
        return behaviour;
    }

    public void init()
    {
        FieldConfig cfg = new FieldConfig(mode);
        int[] sizes = SharedRes.instance.getCommon().getIntArray("fieldsz");

        field = GameFactory.instance.newField(sizes[4], sizes[5]);
        tracer = new northern.captain.quadronia.game.solver.PathTracer(this, field);
        detector = new northern.captain.quadronia.game.solver.CircuitDetector(this);
        solver = new northern.captain.quadronia.game.solver.GameFieldSolver(this, field);

        buildField(cfg);
    }

    public void buildField(FieldConfig cfg)
    {
        field.setRadius(cfg);
        field.build();

        tracer.reset();
        solver.reset();

        allPerks.clear();
        everyEleSetPerks.clear();
        backStack.clear();
    }

    public void startGame()
    {
        state = STATE_PLAYING;
        context.clear();
        gameType = context.getGameType();
        mode = context.getLastMode();

        switch (gameType)
        {
            case TYPE_ARCADE:
                behaviour = new ArcadeBehaviour(this);
                break;
            case TYPE_EXPRESS:
                behaviour = new ExpressBehaviour(this);
                break;
            case TYPE_QUEST:
                behaviour = new QuestBehaviour(this);
                break;
            default:
                behaviour = new ArcadeBehaviour(this);
                break;
        }

        rand = new Randomizer(ItemFactory.instance.getItemNames(mode).length, mode == ItemFactory.MODE_TWO ? 0 : 1);
        randLevelUp = new Randomizer(ItemFactory.instance.getItemNames(ItemFactory.LEVELUP_MODES[mode]).length, 0);

        bonusGenerator = new ElePerkGenerator(this);
        obstacleGenerator = new FieldObstacleGenerator(this);

        lastObstacle = null;
        lastBonus = null;
        nextPerkGen = 63;
        gameTimer.resetTimer(behaviour.getTimeOut());
//        AchieveMan.instance().startGame(this);
        behaviour.doOnStart(false);
        context.setTillLevelUp(behaviour.nextLevelUpThreshold(context.getLevel()));
        nextMove(null, false);
    }

    private Runnable onTimedOut = new Runnable()
    {
        @Override
        public void run()
        {
            gameTimer.pauseTimer();

            behaviour.doOnTimeOut();

            if(isPlaying())
            {
                gameTimer.setOnTimedOut(onTimedOut);
                gameTimer.resumeTimer();
            }
        }
    };

    public void doFieldPerk()
    {
        if(generateFieldObstacle())
        {
            resultsCallback.fieldPerkCreated(lastObstacle);
            lastObstacle = null;
        }
    }


    private void nextMove(Item item, boolean levelUp)
    {
        behaviour.doOnNextMove();
        gameTimer.setOnTimedOut(onTimedOut);

        if(levelUp)
        {
            currentItem[0] = ItemFactory.instance.getItem(ItemFactory.LEVELUP_MODES[mode], randLevelUp.next());
        } else
        {
            currentItem[0] = currentItem[1];
            currentItem[1] = currentItem[2];

            if (currentItem[0] == null)
            {
                currentItem[0] = ItemFactory.instance.getItem(mode, rand.next());
            }

            if (currentItem[1] == null)
            {
                currentItem[1] = ItemFactory.instance.getItem(mode, rand.next());
            }

            currentItem[2] = ItemFactory.instance.getItem(mode, rand.next());
        }

        currentItemChanged();
    }

    private void currentItemChanged()
    {
        tracer.reset();
        tracer.setItem(currentItem);

        if(!clearCircuitActive && !solver.solve(currentItem))
        {
            doGameOver();
        }
    }

    public void doTouchDown(int x, int y)
    {
        if(state != STATE_PLAYING) return;

        tracer.startPath(x, y);
        if(touchCallback != null) touchCallback.onTouchDown(x, y);
    }

    public void doDrag(int x, int y)
    {
        if(state != STATE_PLAYING) return;

        tracer.doDragTo(x, y);
        if(touchCallback != null) touchCallback.onDrag(x, y);
    }

    public void doTouchUp(int x, int y)
    {
        if(state != STATE_PLAYING) return;

        if(touchCallback != null) touchCallback.onTouchUp(x, y);
        boolean ret = tracer.finishPath(x, y);
        if(ret)
        {
            applyItem();
        } else
        {
            tracer.reset();
        }
    }

    public void doGameOver()
    {
        state = STATE_GAMEOVER;
        gameTimer.pauseTimer();
        context.onGameOver(mode);
        NContext.current.post(new Runnable()
        {
            @Override
            public void run()
            {
                if (resultsCallback != null)
                {
                    resultsCallback.gameOver();
                }
            }
        });
    }

    public void doPause()
    {
        if(isGameOver()) return;
        state = STATE_PAUSED;
        gameTimer.pauseTimer();
    }

    public void doResume()
    {
        if (isGameOver()) return;
        state = STATE_PLAYING;
        gameTimer.resumeTimer();
    }

    private void applyItem()
    {
        tracer.applyToField();
        Item chosenOne = tracer.getChosenItem();
        int scoreAdded = score.addScoreByItem(chosenOne);
        context.addFragment(chosenOne.elements.length);
        if(resultsCallback != null)
        {
            resultsCallback.itemDropped(chosenOne, tracer.getPathCell(), scoreAdded);
        }

        //Only one step back! Comment this line if need more
        backStack.clear();

        if(!tracer.isJokerMode())
        {
            backStack.push(chosenOne);
        } else
        {
            useJokerBonus(false);
        }

        checkCellForCircuit(tracer.getPathCell());
        nextMove(chosenOne, false);
    }

    private int nextPerkGen;

    private void checkForPerkToDrop()
    {
        int fragments = context.getFragments();
        if (fragments > nextPerkGen)
        {
            NContext.current.post(new Runnable()
            {
                @Override
                public void run()
                {
                    if (generateFieldObstacle())
                    {
                        nextPerkGen = context.getFragments() + 43 + Helpers.RND.nextInt(63);
                        resultsCallback.fieldPerkCreated(lastObstacle);
                        lastObstacle = null;
                    }
                }
            });
        }
    }

    public boolean checkCellForCircuit(final Cell cell)
    {
        if(!clearCircuitActive)
        {
            if(detector.detectCircuit(cell))
            {
                solver.setActive(false);
                backStack.clear();
                clearCircuitActive = true;
                NContext.current.postDelayed(clearCircuit, 400);
                return true;
            }
        } else
        {
            NContext.current.postDelayed(new Runnable()
            {
                @Override
                public void run()
                {
                    checkCellForCircuit(cell);
                }
            }, 200);
        }
        return false;
    }

    private void processCircuit()
    {
        context.addCircuit();
        int circuitBonus = score.addBonusByCircuit(detector);

        if(resultsCallback != null)
        {
            resultsCallback.circuitBuilt(detector, circuitBonus);
        }

        if(detector.hasPerkInside())
        {
            applyBonus(detector);
        } else
        {
            generateFieldBonus();
        }

        levelUpCloser(detector);

        if(field.getAllocatedCells().isEmpty())
        {
            int emptyFieldBonus = score.addBonusForClearField();
            if(resultsCallback != null)
            {
                resultsCallback.allCleared(emptyFieldBonus);
            }
        }
    }

    Array<IFieldPerk> perksFound = new Array<IFieldPerk>(false, 10);

    private void applyBonus(northern.captain.quadronia.game.solver.CircuitDetector detector)
    {
        List<Cell> cells = detector.getCircuit();
        perksFound.clear();
        lastBonus = null;
        for(Cell cell : cells)
        {
            if( cell.hasPerk() )
            {
                IFieldPerk perk = cell.getPerk();
                if(perk.applyBonus(this, cell))
                {
                    perksFound.add(perk);
                }
            }
        }

        if(perksFound.size > 0 && resultsCallback != null)
        {
            resultsCallback.bonusPerksApplied(perksFound);
        }
    }

    private boolean generateFieldObstacle()
    {
        lastObstacle = obstacleGenerator.getNextPerk();

        if (lastObstacle == null)
        {
            return false;
        }

        NContext.current.post(new Runnable()
        {
            @Override
            public void run()
            {
                currentItemChanged();
            }
        });
        return true;
    }

    private boolean generateFieldBonus()
    {
        lastBonus = null;
        if(!Helpers.RND.nextBoolean()) return false;

        ElePerk bonus = bonusGenerator.getNextPerk();
        if(bonus == null || !bonus.putToField(this, field))
        {
            return false;
        }
        lastBonus = bonus;
        return true;
    }

    public int numberPerksByType(int type)
    {
        Array<IFieldPerk> perks = getAllPerks();

        int num = 0;
        for(IFieldPerk perk : perks)
        {
            if(perk.getType() == type)
            {
                num++;
            }
        }
        return num;
    }

    public void doLevelUp()
    {
        int level = context.registerLevelUp();
        int bonus = score.addBonusForLevelUp(level);
        resultsCallback.onLevelUp(level, bonus);
        context.setTillLevelUp(behaviour.nextLevelUpThreshold(level));
        behaviour.doOnLevelUp(level);
    }

    private void levelUpCloser(northern.captain.quadronia.game.solver.CircuitDetector detector)
    {
        List<Cell> cells =  detector.getCircuit();

        int levelUp = context.elementsUsed(cells.size());
        boolean hadPerk = detector.hasPerkInside();
        detector.clearCircuitCells();
        if(levelUp <= 0 && behaviour.levelUpsAllowed())
        {
            doLevelUp();
            nextMove(null, true);
        } else
        {
            if(!hadPerk) checkForPerkToDrop();
        }
    }

    public boolean canSwapNexts()
    {
        return isPlaying() && currentItem[0].mode == mode && context.getBonusSwap() > 0;
    }

    public boolean swapNexts()
    {
        context.useBonusByIdx(IGameContext.BONUS_SWAP_IDX);
        Item item = currentItem[1];
        currentItem[1] = currentItem[0];
        currentItem[0] = item;
        currentItemChanged();
        if(resultsCallback != null)
        {
            resultsCallback.swapUsed();
        }
        return true;
    }

    public boolean rollbackLast()
    {
        if(isPlaying()
                && !backStack.isEmpty()
                && context.getBonusBackstep() > 0)
        {
            Item item = backStack.pop();
            for(int y = 0;y<field.fieldHei;y++)
            {
                for(int x = 0;x<field.fieldWid;x++)
                {
                    Cell cell = field.field[x][y];
                    if(cell.hasElement() && cell.element.parentItem == item)
                    {
                        setCellElement(cell, null);
                    }
                }
            }
            int scoreDelta = score.rollbackItem(item);
            context.addFragment(-item.elements.length);
            context.useBonusByIdx(IGameContext.BONUS_BACKSTEP_IDX);
            if(resultsCallback != null)resultsCallback.itemRolledBack(item, scoreDelta);
            currentItem[2] = currentItem[1];
            currentItem[1] = currentItem[0];
            currentItem[0] = item;
            currentItemChanged();
            return true;
        }
        return false;
    }

    private boolean clearCircuitActive = false;
    private Runnable clearCircuit = new Runnable()
    {
        @Override
        public void run()
        {
            processCircuit();
            clearCircuitActive = false;
            currentItemChanged();
        }
    };

    public Set<Cell> getAllocatedCells()
    {
        return field.getAllocatedCells();
    }

    public void setCellElement(Cell cell, Element element)
    {
        if(element != null)
        {
            field.setCellElement(cell, element);
            for (IFieldPerk perk : everyEleSetPerks)
            {
                perk.applyOnSet(this, cell);
            }
        } else
        {
            Element oldElement = cell.getElement();
            if(oldElement != null && oldElement.isRemovable())
            {
                field.setCellElement(cell, element);
            }
        }
    }

    public IFieldPerk getLastObstacle()
    {
        return lastObstacle;
    }

    public Array<IFieldPerk> getEveryEleSetPerks()
    {
        return everyEleSetPerks;
    }

    public void addPerkOnEverySet(IFieldPerk perk)
    {
        everyEleSetPerks.add(perk);
    }

    public void removePerkOnEverySet(IFieldPerk perk)
    {
        if(resultsCallback != null)
        {
            resultsCallback.fieldPerkRemoved(perk);
        }
        everyEleSetPerks.removeValue(perk, true);
    }

    /**
     * Set or unset perks that placed to the field inside whole cell (cell.perk is set)
     * @param cell
     * @param perk
     */
    public void setElePerk(Cell cell, IFieldPerk perk)
    {
        if(perk == null)
        {
            allPerks.removeValue(cell.getPerk(), true);
        } else
        {
            allPerks.add(perk);
        }
        field.setElePerk(cell, perk);
    }

    /**
     * Sets non whole cell perks. Adds them to the allPerks list
     * @param perk
     */
    public void setIFieldPerk(IFieldPerk perk)
    {
        allPerks.add(perk);
    }

    public boolean unsetIFieldPerk(IFieldPerk perk)
    {
        if(resultsCallback != null)
        {
            resultsCallback.fieldPerkRemoved(perk);
        }
        return allPerks.removeValue(perk, true);
    }

    public Array<IFieldPerk> getAllPerks()
    {
        return allPerks;
    }

    public ElePerk getLastBonus()
    {
        return lastBonus;
    }

    public void addScore(int scoreDelta)
    {
        score.addScore(scoreDelta);
    }

    public void extraBackstep()
    {
        context.extraBonusBackstep();
        if(resultsCallback != null)
        {
            resultsCallback.bonusApplied(IFieldPerk.BONUS_BACKSTEP);
        }
    }

    public void extraSwap()
    {
        context.extraBonusSwap();
        if(resultsCallback != null)
        {
            resultsCallback.bonusApplied(IFieldPerk.BONUS_SWAP);
        }
    }

    public int useBombBonus(int fx, int fy, Cell cell)
    {
        if(canUseBombBonus())
        {
            getGraphicsContext().explosionEffect(fx, fy, 0);
            cell.getElement().setRemovable(true);
            setCellElement(cell, null);
            return context.useBonusByIdx(IGameContext.BONUS_BOMB_IDX);
        }
        return context.getBonusByIdx(IGameContext.BONUS_BOMB_IDX);
    }

    public boolean canUseJokerBonus()
    {
        if(isPlaying() && getContext().getBonusByIdx(IGameContext.BONUS_JOKER_IDX)>0)
        {
            return true;
        }

        return false;
    }

    public void useJokerBonus(boolean justStart)
    {
        if(canUseJokerBonus())
        {
            tracer.setJokerMode(justStart);
            solver.setJokerMode(justStart);
            if(!justStart)
            {
                context.useBonusByIdx(IGameContext.BONUS_JOKER_IDX);
                doBonusTouch(0, 0);
            }
        }
    }

    public void cancelJokerBonus()
    {
        tracer.setJokerMode(false);
        solver.setJokerMode(false);
    }

    public int useWorkerBonus(IFieldPerk perkDestroyed)
    {
        if(canUseWorkerBonus())
        {
            IGraphicObject draw = getGraphicsContext().findGraphicObject(perkDestroyed);
            if (draw != null)
            {
                draw.processRemoval(this);
            }
            perkDestroyed.removeFromField(this);
            return context.useBonusByIdx(IGameContext.BONUS_WORKER_IDX);
        }
        return context.getBonusByIdx(IGameContext.BONUS_WORKER_IDX);
    }

    public boolean canUseBombBonus()
    {
        if(isPlaying() && getContext().getBonusByIdx(IGameContext.BONUS_BOMB_IDX)>0)
        {
            return true;
        }

        return false;
    }

    public boolean canUseWorkerBonus()
    {
        if(isPlaying() && getContext().getBonusByIdx(IGameContext.BONUS_WORKER_IDX)>0)
        {
            return true;
        }

        return false;
    }

    public interface IBonusTouchCallback
    {
        boolean bonusTouch(int fx, int fy);
    }

    private IBonusTouchCallback bonusTouchCallback;

    public void setBonusTouchCallback(IBonusTouchCallback bonusTouchCallback)
    {
        this.bonusTouchCallback = bonusTouchCallback;
    }

    private boolean doBonusTouch(int fx, int fy)
    {
        boolean ret = false;
        if(bonusTouchCallback != null)
        {
            ret = bonusTouchCallback.bonusTouch(fx, fy);
        }
        bonusTouchCallback = null;
        return ret;
    }

    public int getGameType()
    {
        return gameType;
    }
}

