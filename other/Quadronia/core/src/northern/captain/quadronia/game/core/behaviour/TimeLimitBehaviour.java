package northern.captain.quadronia.game.core.behaviour;

import org.json.JSONException;
import org.json.JSONObject;

import northern.captain.gamecore.glx.NCore;
import northern.captain.quadronia.game.core.Game;
import northern.captain.quadronia.game.events.ETimeDeltaChange;

/**
 * Created by leo on 05.09.15.
 */
public class TimeLimitBehaviour extends Behaviour
{
    private int maxTimeout = 300;
    private int currentLevel = 1;

    public TimeLimitBehaviour(Game game)
    {
        super(game);
        timeout = maxTimeout;
    }

    @Override
    public void doOnTimeOut()
    {
        game.doGameOver();
    }

    @Override
    public void doOnNextMove()
    {
    }

    @Override
    public void doOnLevelUp(int newLevel)
    {
        super.doOnLevelUp(newLevel);
        int delta = newLevel;
        currentLevel = newLevel;
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
        return currentLevel % 5;
    }

    @Override
    public boolean needTimerRestartOnMove()
    {
        return false;
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
            timeout = jobj.getInt("tm");
            maxTimeout = jobj.optInt("mtm", maxTimeout);
            doOnLevelUp(currentLevel);
        }
        catch (JSONException jex)
        {

        }
    }

    @Override
    public void doOnTimeBonus()
    {
        timeout += 7;
        maxTimeout += 7;
        NCore.busPost(new ETimeDeltaChange(7));
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
