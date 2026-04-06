package northern.captain.tools;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JSONContainer
{
	/**
	 * Container unwrapped object processor interface
	 * @author leo
	 *
	 */
	public interface UnWrapProcessor
	{
		/**
		 * Process given json object and possibly return new result json object or null
		 * @param jobj
		 * @return
		 */
		JSONObject process(JSONObject jobj);
	}
	
	private String tag;
	private JSONArray jar = new JSONArray();
	
	public JSONContainer(String tag)
	{
		this.tag = tag;
	}
	
	public JSONContainer()
	{
		this(CNTTAG);
	}
	
	public void add(JSONObject jobj)
	{
		jar.put(jobj);
	}
	
	public void setArray(JSONArray jar)
	{
		this.jar = jar;
	}
	
	public JSONObject toJSON()
	{
		JSONObject json = new JSONObject();
		try
		{
			json.put("type", tag);
			json.put(tag, jar);
		}
		catch(JSONException jex) {}
		return json;
	}
	
	public static final String CNTTAG = "cnt";
	
	public static void unwrap(UnWrapProcessor proc, JSONObject contjson)
	{
		unwrapImpl(CNTTAG, proc, contjson, null);
	}
	
	public static void unwrap(String tag, UnWrapProcessor proc, JSONObject contjson)
	{
		unwrapImpl(tag, proc, contjson, null);
	}
	
	public static JSONArray unwrapWithResult(UnWrapProcessor proc, JSONObject contjson)
	{
		JSONArray jar = new JSONArray();
		unwrapImpl(CNTTAG, proc, contjson, jar);
		return jar;
	}
	
	private static void unwrapImpl(String tag, UnWrapProcessor proc, JSONObject json, JSONArray cnt)
	{
		try
		{
			if(
					json.has("type") 
					&& tag.equals(json.getString("type")) 
					&& json.has(tag)
			  )
			{
				JSONArray jar = json.getJSONArray(tag);
				for(int i=0;i<jar.length();i++)
				{
					unwrapImpl(tag, proc, jar.getJSONObject(i), cnt);
				}
			} else
			{
				JSONObject ret = proc.process(json);
				if(cnt != null && ret != null)
					cnt.put(ret);
			}
		} catch(JSONException jex) {}
	}
}
