package northern.captain.quadronia.game.core.behaviour;

import org.json.JSONException;
import org.json.JSONObject;

import northern.captain.gamecore.glx.NCore;
import northern.captain.quadronia.game.core.Game;
import northern.captain.tools.Log;

/**
 * Created by leo on 05.09.15.
 */
public class ABehaviour extends Behaviour
{
    private int maxTimeout = 30;
    private int currentLevel = 1;
    private int timedOutTimes = 0;

    public ABehaviour(Game game)
    {
        super(game);
        timeout = maxTimeout;
    }

    @Override
    public void doOnTimeOut()
    {
        if(timedOutTimes < 1)
        {
            int score = game.getContext().getScore();
            int delta = score / 20;
            Log.i("ncgame", "Timeout reached - subtracting score=" + delta);
            game.subtractScore(-delta);
            NCore.busPost(game.produceNewTimePeriod());
            timedOutTimes++;
        } else
        {
            game.doGameOver();
        }
    }

    @Override
    public void doOnNextMove()
    {
        timedOutTimes = 0;
    }

    @Override
    public void doOnLevelUp(int newLevel)
    {
        super.doOnLevelUp(newLevel);
        int delta = ((newLevel-1)%4)*2;
        currentLevel = newLevel;
        timeout = Math.max(5, maxTimeout - delta);
    }


    @Override
    public boolean canBonusTime()
    {
        return super.canBonusTime() && maxTimeout < 73;
    }

    @Override
    public void doOnTimeBonus() {
        timeout += 2;
        maxTimeout += 2;
    }

    @Override
    public int nextLevelUpThreshold(int forLevel)
    {
        return 90;
    }

    @Override
    public int getMaxColors()
    {
        return 2 + currentLevel / 4;
    }

    @Override
    public int getDeltaColor()
    {
        return currentLevel % 9;
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
            currentLevel = jobj.getInt("cl");
            doOnLevelUp(currentLevel);
            timeout = jobj.optInt("tm", timeout);
            maxTimeout = jobj.optInt("mtm", maxTimeout);
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
            jobj.put("cl", currentLevel);
            jobj.put("tm", timeout);
            jobj.put("mtm", maxTimeout);
        }
        catch (JSONException jex)
        {
        }

        return jobj;
    }
}
