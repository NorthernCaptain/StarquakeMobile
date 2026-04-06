package northern.captain.gamecore.gplus;

import org.json.JSONException;
import org.json.JSONObject;

import northern.captain.tools.IJSONSerializer;

/**
 * Class contains information about achievement
 */
public class Achievement implements IJSONSerializer
{
    public static final int STATE_LOCKED = 0;
    public static final int STATE_UNLOCKED = 1;
    /**
     * Google achievement id (string value)
     */
    public String id;
    public int internalId;

    /**
     * State of the achievement
     * 0 - locked, 1 - unlocked
     */
    public int state = STATE_LOCKED;


    public Achievement()
    {

    }


    public Achievement(String id)
    {
        this.id = id;
        state = STATE_UNLOCKED;
    }


    public Achievement(String id, int internalId)
    {
        this.id = id;
        this.internalId = internalId;
    }
    /**
     * Serialize object into the JSONObject. Object should create a new JSONObject,
     * put all data into it and then return this json to the caller.
     */
    @Override
    public JSONObject serializeJSON()
    {
        JSONObject json = new JSONObject();
        try
        {
            json.put("type", "ach");
            json.put("aid", id);
            json.put("ast", state);
        }
        catch (JSONException jex) {}

        return json;
    }

    /**
     * Deserialize object from the given JSONObject. The given object is not a container,
     * the object that really contains the data for deserialization
     *
     * @param object
     */
    @Override
    public void deserializeJSON(JSONObject object)
    {
        try
        {
            if(object.has("aid"))
            {
                id = object.getString("aid");
                state = object.getInt("ast");
            }
        }
        catch (JSONException jex)
        {
        }
    }
}
