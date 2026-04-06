package northern.captain.tools;

import northern.captain.gamecore.glx.NCore;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Class stores and retrieves JSONObject to/from the file
 * Only one object could be stored or retrieved from the file
 * @author leo
 *
 */
public class JSONFile
{
	protected String fileName;
	private IPersistentConfig cfg;
	
	public JSONFile(IPersistentConfig cfg, String fileName)
	{
		this.fileName = fileName;
		this.cfg = cfg;
	}
	
	protected OutputStream openOutFile()
	{
		return cfg.getFileForWriting(fileName);
	}
	
	protected InputStream openInFile()
	{
		return cfg.getFileForReading(fileName);
	}
	/**
	 * Writes JSONObject into the file
	 * @param obj
	 * @return
	 */
	public boolean writeJSON(JSONObject obj)
	{
		OutputStream fout = openOutFile();
		if(fout == null)
			return false;
		boolean ret = writeJSONInt(obj, fout);
		
		if(StorageDir.needStore())
		{
			StorageDir file = new StorageDir(fileName);
			if(file.checkPath())
			{
				fout = file.getOutStream();
				if(fout == null)
					return ret;
				writeJSONInt(obj, fout);
			}
		}
		return ret;
	}
	
	protected boolean writeJSONInt(JSONObject obj, OutputStream fout)
	{
		if(cfg != null)
			cfg.setInt("vc", NCore.instance().version);
		try
		{
			byte[] buffer = Encodeco.encode2(obj.toString()).getBytes("UTF8");
			fout.write(buffer.length >> 8);
			fout.write(buffer.length);
			fout.write(buffer);			
		}
		catch(IOException ex)
		{
			Log.e("JSONFile", "could not write to file: " + fileName, ex);
			return false;
		}
		finally
		{
			if(fout != null)
			{
				try
				{
					fout.close();
				}
				catch(IOException ex)
				{
					Log.e("JSONFile", "could not close file after writing" + fileName, ex);
					return false;
				}
			}
		}
		return true;		
	}
	
	/**
	 * Reads JSONObject from the file and returns it
	 * @return
	 */
	public JSONObject readJSON()
	{
		InputStream fin = openInFile();
		if (fin == null)
		{
			StorageDir dir = new StorageDir(fileName);
			if(dir.checkPath())
			{
				fin = dir.getInStream();
			}
			if(fin == null)
				return new JSONObject();
		}
		JSONObject json = readJSONInt(fin);
		return json;
	}
	
	protected JSONObject readJSONInt(InputStream fin)
	{
		try
		{
			int len = 0;
			len = fin.read() << 8;
			len |= fin.read();
			if(len <= 0)
				return new JSONObject();
			byte[] buffer = new byte[len];
			fin.read(buffer);
			String str = new String(buffer, "UTF8");
			return new JSONObject(Encodeco.decode2(str));
		}
		catch(IOException ex)
		{
			Log.e("JSONFile", "could not read from file: " + fileName, ex);
		}
		catch(JSONException ex)
		{
			Log.e("JSONFile", "could not parse json on loading from " + fileName, ex);
		}
		catch(Throwable ex)
		{
			Log.e("JSONFile", "could not load json on loading from " + fileName, ex);			
		}
		finally
		{
			if(fin != null)
			{
				try
				{
					fin.close();
				}
				catch(IOException ex)
				{
				}
			}
		}
		return null;		
	}
}
