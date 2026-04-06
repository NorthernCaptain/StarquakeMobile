package northern.captain.quadronia.game.core;

import com.badlogic.gdx.utils.Pool;
import com.squareup.otto.Produce;
import com.squareup.otto.Subscribe;

import org.json.JSONException;
import org.json.JSONObject;

import northern.captain.gamecore.glx.ISoundMan;
import northern.captain.gamecore.glx.NContext;
import northern.captain.gamecore.glx.NCore;
import northern.captain.quadronia.b.NativeNFactory;
import northern.captain.quadronia.game.Engine;
import northern.captain.quadronia.game.IGameContext;
import northern.captain.quadronia.game.Point;
import northern.captain.quadronia.game.Score;
import northern.captain.quadronia.game.SimpleGameContext;
import northern.captain.quadronia.game.achievements.AchieveMan;
import northern.captain.quadronia.game.core.behaviour.ABehaviour;
import northern.captain.quadronia.game.core.behaviour.Behaviour;
import northern.captain.quadronia.game.core.behaviour.TimeLimitBehaviour;
import northern.captain.quadronia.game.events.EFieldChanged;
import northern.captain.quadronia.game.events.EFullReshuffle;
import northern.captain.quadronia.game.events.EGameLevelUp;
import northern.captain.quadronia.game.events.EGameOver;
import northern.captain.quadronia.game.events.EGameStart;
import northern.captain.quadronia.game.events.EGameTimeOver;
import northern.captain.quadronia.game.events.ENewTimePeriod;
import northern.captain.quadronia.game.events.EPerkDoBomb;
import northern.captain.quadronia.game.events.EPerkDoHint;
import northern.captain.quadronia.game.events.EPerkShowHint;
import northern.captain.quadronia.game.events.EQuadAreaHitFinish;
import northern.captain.quadronia.game.events.EQuadAreaHitMiddle;
import northern.captain.quadronia.game.events.EQuadAreaHitStart;
import northern.captain.quadronia.game.events.EQuadSelect;
import northern.captain.quadronia.game.events.EScoreChange;
import northern.captain.quadronia.game.events.ESolutionFound;
import northern.captain.quadronia.game.profile.UserManager;
import northern.captain.tools.AsyncGTask;
import northern.captain.tools.IDisposable;
import northern.captain.tools.IJSONSerializer;
import northern.captain.tools.Log;

/**
 * Created by leo on 30.08.15.
 */
public class Game implements IDisposable, IJSONSerializer
{
    public static final int TYPE_ARCADE = 0;
    public static final int TYPE_EXPRESS = 1;

    public static final Pool<Quad> QUAD_POOL = new Pool<Quad>()
    {
        @Override
        protected Quad newObject()
        {
            return new Quad();
        }
    };
    public static IGameContext defaultGameContext = new SimpleGameContext();

    private Field field;
    private Score score;
    private QuadCollector quadCollector;
    private QuadSolver quadSolver;
    public boolean isPlaying = false;
    public boolean isOver = false;
    private IColorGenerator colorGenerator = new BaseColorGenerator();
    private Behaviour behaviour;
    private IGameContext context;
    private int gameMode = TYPE_ARCADE;
    private int gameTurn = 0;

    private QuadArea bestSolution = null;

    public static final int TOUCH_MODE_QUAD = 0;
    public static final int TOUCH_MODE_BOMB = 1;
    private int touchMode = TOUCH_MODE_QUAD;

    public JSONObject savedGame = null;
    public int level;

    public Game(Config config)
    {
        field = new GField(config);
        context = defaultGameContext;
        NCore.busRegister(this);
        try
        {
            String buf = context.getSavedGame();
            if (buf != null) savedGame = new JSONObject(buf);
        }
        catch (JSONException jex) {}
    }

    public void start()
    {
        gameMode = context.getGameType();
        context.setLastMode(this.gameMode);
        behaviour = gameMode == Game.TYPE_EXPRESS ? new TimeLimitBehaviour(this) : new ABehaviour(this);
        colorGenerator = new BehaviourColorGenerator(behaviour);
        field.initField(behaviour.getMaxColors(), colorGenerator);
        gameTurn = 0;
        level = 1;
        if(savedGame != null)
        {
            deserializeJSON(savedGame);
            context.setLastMode(this.gameMode);
            colorGenerator = new BehaviourColorGenerator(behaviour);
        } else
        {
            context.clear();
            context.setTillLevelUp(behaviour.nextLevelUpThreshold(context.getLevel()));
        }
        score = new Score(defaultGameContext);
        quadCollector = new QuadCollector(field);
        quadSolver = new QuadSolver(field);
        isPlaying = true;
        isOver = false;
        behaviour.doOnStart(false);
        touchMode = TOUCH_MODE_QUAD;
        AchieveMan.instance().startGame(this);
        NCore.busPost(new EGameStart(this, savedGame!=null));
        solveIt(gameTurn);
        if(score.getTotalScore() == 0)
        {
            NContext.current.postDelayed(()->{
                //show hint after 5 sec if you can't find any solution on the first level.
                if(score.getTotalScore() == 0
                        && this.context.getLevel() < 2) {
                    NCore.busPost(new EPerkDoHint());
                }
            }, 5000);
        }
    }

    public void setTouchMode(int touchMode)
    {
        this.touchMode = touchMode;
        if(touchMode == Game.TOUCH_MODE_BOMB) {
            //remove current selection in bomb mode
            NCore.busPost(new EQuadSelect(quadCollector, null, QuadCollector.QUAD_CLEAR_SEQ));
        }
    }

    public Field getField()
    {
        return field;
    }

    public Score getScore()
    {
        return score;
    }

    public IGameContext getContext()
    {
        return context;
    }

    public int getGameMode()
    {
        return gameMode;
    }

    public Behaviour getBehaviour()
    {
        return behaviour;
    }

    @Override
    public void dispose()
    {
        field.dispose();
        quadSolver.dispose();
        QUAD_POOL.clear();
        NCore.busUnregister(this);
    }

    public int selectQuad(Quad quad)
    {
        if(isOver || !isPlaying) return 0;

        if(quad.type == Quad.TYPE_NO) return 0;
        if(quad.type == Quad.TYPE_COINS) return 0;
        if(quad.type == Quad.TYPE_M200) return 0;
        if(quad.type == Quad.TYPE_TIME) return 0;

        int ret = QuadCollector.NEW_QUAD_SEQ;

        if(touchMode == TOUCH_MODE_QUAD)
        {
            ret = quadCollector.addQuad(quad);
            NCore.busPost(new EQuadSelect(quadCollector, quad, ret));
            if (ret == QuadCollector.QUAD_HIT)
            {
                int retScore = addScoreByArea(quadCollector.area);
                NCore.busPost(new EQuadAreaHitStart(quadCollector, touchMode));
                quadCollector.clearSelected();
                if(retScore <= 0 && behaviour.levelUpsAllowed())
                {
                    doLevelUp();
                }
                gameTurn++;
                behaviour.doOnNextMove();
            }
        } else
        {
            //Bomb selection mode
            quadCollector.selectAreaCenter(quad, 3);
            NativeNFactory.nci.p_();
            NCore.instance().getSoundman().playSound(ISoundMan.SND_BOMB, true);
        }

        return ret;
    }

    @Subscribe
    public void onFieldChangedExternally(EFieldChanged event)
    {
        solveIt(this.gameTurn);
    }

    @Subscribe
    public void doBombExplosion(EPerkDoBomb event)
    {
        int ret = addScoreByArea(quadCollector.area);
        NCore.busPost(new EQuadAreaHitStart(quadCollector, touchMode));
        quadCollector.clearSelected();
        if(ret <= 0 && behaviour.levelUpsAllowed())
        {
            doLevelUp();
        }
        gameTurn++;
        touchMode = TOUCH_MODE_QUAD;
    }

    private int addScoreByArea(QuadArea area)
    {
        int numQuads = area.hei*area.len;

        int deltaScore = 0;
        int x2 = 1;

        for(int x = area.fromX; x<area.len+area.fromX;x++)
        {
            for(int y = area.fromY;y<area.hei+area.fromY;y++)
            {
                Quad quad = field.quads[x][y];
                if(quad.type == Quad.TYPE_X2)
                {
                    quad.used = true;
                    x2 *= 2;
                    NCore.instance().getSoundman().playSound(ISoundMan.SND_BONUS_X2, false);
                    continue;
                }
                if(quad.type == Quad.TYPE_M200)
                {
                    quad.used = true;
                    deltaScore -= 200;
                    numQuads--;
                    NCore.instance().getSoundman().playSound(ISoundMan.SND_BONUS_M200, false);
                    continue;
                }
                if(quad.type == Quad.TYPE_NO)
                {
                    numQuads--;
                    continue;
                }
                if(quad.type == Quad.TYPE_COINS)
                {
                    quad.used = true;
                    UserManager.instance.getCurrentUser().addCoins(9);
                    numQuads--;
                    NCore.instance().getSoundman().playSound(ISoundMan.SND_COINS_FOUND, false);
                    continue;
                }
                if(quad.type == Quad.TYPE_TIME)
                {
                    quad.used = true;
                    behaviour.doOnTimeBonus();
                    NCore.instance().getSoundman().playSound(ISoundMan.SND_TIME_ADD, false);
                    numQuads--;
                    continue;
                }
            }
        }

        double lg = Math.log(level);
        int bonusMultiplier = 1 + (int)(numQuads*lg/26.0);
        int num200 = deltaScore;
//        deltaScore *= ((Math.sin(Math.toRadians(Math.min((level-1)*4, 180)-90)) + 1) * 8 + 1);
        deltaScore += numQuads * bonusMultiplier * x2;
        Log.i("score", "New SCORE: Num200=" + num200 + ", quads=" + numQuads + ",x2=" + x2 + ", bonus=" + bonusMultiplier + ": score to add=" + deltaScore);
        score.addScore(deltaScore);

        int centerCx = area.fromX + area.len/2;
        int centerCy = area.fromY + area.hei/2;

        context.addFragment(numQuads);
        int ret = context.elementsUsed(numQuads);
        context.addCircuit();
        NCore.busPost(new EScoreChange(score, deltaScore, centerCx, centerCy));
        return ret;
    }

    private void doLevelUp()
    {
        level = context.registerLevelUp();
        Log.i("ncgame", "LevelUp, new level = " + level);
        int bonus = score.addBonusForLevelUp(level);
        context.setTillLevelUp(behaviour.nextLevelUpThreshold(level));
        behaviour.doOnLevelUp(level);
        Log.i("ncgame", "New color range: delta = " + behaviour.getDeltaColor() + ", max = " + behaviour.getMaxColors());
        NCore.busPost(new EGameLevelUp(bonus, level));
    }

    public void subtractScore(int delta)
    {
        score.addScore(delta);
        NCore.busPost(new EScoreChange(score, delta, 0, 0));
        NCore.instance().getSoundman().playSound(ISoundMan.SND_GAME_ALMOST_LOST, true);
    }

    public boolean isPaused()
    {
        return !isPlaying && !isOver;
    }

    public void doGameOver()
    {
        isPlaying = false;
        isOver = true;
        context.onGameOver(this.gameMode);
        NCore.busPost(new EGameOver(this));
        NCore.busPost(new EPerkDoHint());
        NCore.instance().getSoundman().playSound(ISoundMan.SND_GAME_YLOST, true);
    }

    private void solveIt(final int curTurn)
    {
        Log.i("ncgames", "Solver - run solving...");

        quadSolver.init(this.field);

        AsyncGTask<Integer, QuadArea> task = new AsyncGTask<Integer, QuadArea>()
        {
            int turn = curTurn;

            @Override
            public QuadArea doInBackground(Integer... integers)
            {
                try
                {
                    return quadSolver.solve(integers[0]);
                }
                catch(Throwable ex)
                {
                    turn = -1; //We set impossible turn number in order to restart solving algorithm
                    return null;
                }
            }

            @Override
            public void onPostExecute(QuadArea result)
            {
                bestSolution = null;
                if(turn == gameTurn)
                {
                    bestSolution = result;
                    if(bestSolution == null)
                    {
                        //No way to find anything, just do reshuffle
                        Log.i("ncgames", "Solver - no solution - reshuffle");
                        NCore.busPost(new EFullReshuffle());
                    } else
                    {
                        Log.i("ncgames", "Solver - solution found - broadcasting");
                        NCore.busPost(new ESolutionFound(bestSolution));
                    }
                } else
                {
                    //Let's do it again, but now for our turn number
                    if(turn == -1)
                    {
                        Log.i("ncgames", "Solver - exception - reshuffle");
                        NCore.busPost(new EFullReshuffle());
                    }
                    else {
                        Log.i("ncgames", "Solver - solve again");
                        solveIt(gameTurn);
                    }
                }
            }
        };

        task.execute(curTurn);
    }

    @Subscribe
    public void onQuadAreaOldDead(EQuadAreaHitMiddle event)
    {
        QuadArea area = event.area;
        area.dispose(); //we dispose the quads inside but leave size and starting point
        field.initFieldArea(area, colorGenerator);
        //if we have recoloring, wait until it finished then solve the field (via EFieldChanged event)
        if(!quadCollector.needRecolor())
        {
            solveIt(gameTurn);
        }
        NCore.busPost(new EQuadAreaHitFinish(area, quadCollector));
        NCore.busPost(new ENewTimePeriod(behaviour.needTimerRestartOnMove() ? behaviour.getTimeOut() : 0));
    }

    @Subscribe
    public void onTimeOver(EGameTimeOver event)
    {
        behaviour.doOnTimeOut();
    }

    @Produce
    public EGameStart produceGameStartEvent()
    {
        return isPlaying ? new EGameStart(this, savedGame != null) : null;
    }

    @Produce
    public ENewTimePeriod produceNewTimePeriod()
    {
        return new ENewTimePeriod(behaviour == null ? 15 : behaviour.getTimeOut());
    }

    public void doPause()
    {
        isPlaying = false;
    }

    public void doResume()
    {
        isPlaying = true;
    }

    /**
     * Deserialize object from the given JSONObject. The given object is not a container,
     * the object that really contains the data for deserialization
     *
     * @param jobj
     */
    @Override
    public void deserializeJSON(JSONObject jobj)
    {
        try
        {
            JSONObject jfld = jobj.getJSONObject("fld");
            int w = jfld.getInt("w");
            int h = jfld.getInt("h");

            if(w != field.width || h != field.height)
            {
                return;
            }

            gameMode = jobj.getInt("md");
            level = jobj.optInt("lv", 1);
            gameTurn = jobj.optInt("tn", 0);
            behaviour = gameMode == Game.TYPE_EXPRESS ? new TimeLimitBehaviour(this) : new ABehaviour(this);
            behaviour.deserializeJSON(jobj.getJSONObject("bh"));
            field.deserializeJSON(jfld);
        }
        catch (JSONException jex)
        {
        }
    }

    /**
     * Serialize object into the JSONObject. Object should create a new JSONObject,
     * put all data into it and then return this json to the caller.
     */
    @Override
    public JSONObject serializeJSON()
    {
        JSONObject jobj = new JSONObject();
        try
        {
            jobj.put("type", "gm");
            jobj.put("md", gameMode);
            jobj.put("lv", level);
            jobj.put("tn", gameTurn);
            jobj.put("fld", field.serializeJSON());
            jobj.put("bh", behaviour.serializeJSON());
        }
        catch (JSONException jex) {}
        return jobj;
    }
}
