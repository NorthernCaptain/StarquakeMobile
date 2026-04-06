package northern.captain.tools;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import java.io.InputStream;
import java.io.OutputStream;

public class JSONCfglessFile extends JSONFile 
{
	public JSONCfglessFile(String fileName) 
	{
		super(null, fileName);
	}

	@Override
	protected OutputStream openOutFile() 
	{
		try
		{
			FileHandle hnd = Gdx.files.local(fileName);
			return hnd.write(false);
		} catch (Exception e)
		{
			return null;
		}
	}

	@Override
	protected InputStream openInFile() 
	{
		try
		{
			FileHandle hnd = Gdx.files.local(fileName);
			return hnd.read();
		} catch (Exception e)
		{
			return null;
		}
	}	
}
