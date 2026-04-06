package northern.captain.tools;

import org.json.JSONObject;

/**
 * Interface for serializing objects using JSON format
 * @author leo
 *
 */
public interface IJSONSerializer
{
	/**
	 * Serialize object into the JSONObject. Object should create a new JSONObject,
	 * put all data into it and then return this json to the caller.
	 * @param container
	 */
	JSONObject  serializeJSON();
	
	/**
	 * Deserialize object from the given JSONObject. The given object is not a container,
	 * the object that really contains the data for deserialization
	 * @param object
	 */
	void  deserializeJSON(JSONObject object);
}
