package northern.captain.quadronia.android.common;

import com.badlogic.gdx.Gdx;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import northern.captain.tools.IOptionsChangeListener;
import northern.captain.tools.IPersistentConfig;
import northern.captain.tools.JSONCfglessFile;

public class PersistentConfigIOS implements IPersistentConfig
{
    private List<IOptionsChangeListener> changeListeners = new ArrayList<IOptionsChangeListener>();

    private boolean autoCommit = true;

    private JSONObject settings = new JSONObject();

    private static final String cfgFName = "cfg.gfc";

    public PersistentConfigIOS()
    {
        JSONCfglessFile file = new JSONCfglessFile(cfgFName);

        try
        {
            settings = file.readJSON();
        }
        catch (Exception e)
        {
        }
    }

    /* (non-Javadoc)
     * @see northern.captain.tools.IPersistentConfig#contains(java.lang.String)
     */
    @Override
    public boolean contains(String arg0)
    {
        return settings.has(arg0);
    }

    /* (non-Javadoc)
     * @see northern.captain.tools.IPersistentConfig#getBoolean(java.lang.String, boolean)
     */
    @Override
    public boolean getBoolean(String key, boolean defValue)
    {
        return settings.optBoolean(key, defValue);
    }

    /* (non-Javadoc)
     * @see northern.captain.tools.IPersistentConfig#getFloat(java.lang.String, float)
     */
    @Override
    public float getFloat(String key, float defValue)
    {
        return (float)settings.optDouble(key, defValue);
    }

    /* (non-Javadoc)
     * @see northern.captain.tools.IPersistentConfig#getInt(java.lang.String, int)
     */
    @Override
    public int getInt(String key, int defValue)
    {
        return settings.optInt(key, defValue);
    }

    /* (non-Javadoc)
     * @see northern.captain.tools.IPersistentConfig#getLong(java.lang.String, long)
     */
    @Override
    public long getLong(String key, long defValue)
    {
        return settings.optLong(key, defValue);
    }

    /* (non-Javadoc)
     * @see northern.captain.tools.IPersistentConfig#getString(java.lang.String, java.lang.String)
     */
    @Override
    public String getString(String key, String defValue)
    {
        return settings.optString(key, defValue);
    }

    private void signalChanges(String key)
    {
        for(IOptionsChangeListener listener : changeListeners)
        {
            listener.optionsChanged(this, key);
        }
    }

    /* (non-Javadoc)
     * @see northern.captain.tools.IPersistentConfig#setInt(java.lang.String, int)
     */
    @Override
    public void setInt(String key, int Value)
    {
        try
        {
            settings.put(key, Value);
            signalChanges(key);
        }
        catch (JSONException e)
        {
        }
        doCommit();
    }

    /* (non-Javadoc)
     * @see northern.captain.tools.IPersistentConfig#setBoolean(java.lang.String, boolean)
     */
    @Override
    public void setBoolean(String key, boolean Value)
    {
        try
        {
            settings.put(key, Value);
            signalChanges(key);
        }
        catch (JSONException e)
        {
        }
        doCommit();
    }

    /* (non-Javadoc)
     * @see northern.captain.tools.IPersistentConfig#setString(java.lang.String, java.lang.String)
     */
    @Override
    public void setString(String key, String Value)
    {
        try
        {
            settings.putOpt(key, Value);
            signalChanges(key);
        }
        catch (JSONException e)
        {
        }
        doCommit();
    }

    @Override
    public void addOptionsChangeListener(IOptionsChangeListener listener)
    {
        changeListeners.add(listener);
    }

    @Override
    public void removeOptionsChangeListener(IOptionsChangeListener listener)
    {
        changeListeners.remove(listener);
    }

    /* (non-Javadoc)
     * @see northern.captain.tools.IPersistentConfig#getFileForWriting(java.lang.String)
     */
    @Override
    public OutputStream getFileForWriting(String name)
    {
        try
        {
            return Gdx.files.internal(name).write(false);
        } catch (Exception e)
        {
            return null;
        }
    }

    /* (non-Javadoc)
     * @see northern.captain.tools.IPersistentConfig#getFileForReading(java.lang.String)
     */
    @Override
    public InputStream getFileForReading(String name)
    {
        try
        {
            return Gdx.files.internal(name).read();
        } catch (Exception e)
        {
            return null;
        }
    }

    private void doCommit()
    {
        if(!autoCommit)
            return;

        JSONCfglessFile file = new JSONCfglessFile(cfgFName);
        file.writeJSON(settings);
    }

    /* (non-Javadoc)
     * @see northern.captain.tools.IPersistentConfig#commitChanges()
     */
    @Override
    public void commitChanges()
    {
        autoCommit = true;
        doCommit();
    }


    /* (non-Javadoc)
     * @see northern.captain.tools.IPersistentConfig#setAutoCommit(boolean)
     */
    @Override
    public void setAutoCommit(boolean val)
    {
        autoCommit = val;
    }

}
