package northern.captain.quadronia.game;

import northern.captain.tools.Log;

/**
 * Created by leo on 3/11/15.
 */
public class Score
{
    private IGameContext context;

    public int lastScoreDelta;
    public int lastBonusDelta;

    public Score(IGameContext context)
    {
        this.context = context;
    }

    public int getTotalScore()
    {
        return context.getScore();
    }

    public int addScoreByItem(Item putItem)
    {
        int value = putItem.elements.length * (Engine.mode + 2);
        context.addScore(value);
        lastScoreDelta = value;
        return value;
    }

    public int rollbackItem(Item item)
    {
        int value = item.elements.length * (Engine.mode + 2);
        context.addScore(-value);
        lastScoreDelta = value;
        return value;
    }

    public int addBonusByCircuit(northern.captain.quadronia.game.solver.CircuitDetector circuit)
    {
        int value = circuit.getCircuit().size();
        value = value * value * (Engine.mode + 2);
        context.addScore(value);
        lastBonusDelta = value;
        return value;
    }

    public int addBonusForClearField()
    {
        int value = lastBonusDelta * 3 / 2;
        context.addScore(value);
        return value;
    }

    public int addBonusForLevelUp(int newLevel)
    {
        int value = (newLevel + 2) * 87;
        context.addScore(value);
        Log.i("nagame", "Score: Current SCORE=" + context.getScore());
        return value;
    }

    public void addScore(int scoreDelta)
    {
        context.addScore(scoreDelta);
        lastScoreDelta = scoreDelta;
        Log.i("nagame", "Score: Current SCORE=" + context.getScore());
    }

    public void clear()
    {
        context.setScore(0);
        lastScoreDelta = lastBonusDelta = 0;
    }
}
