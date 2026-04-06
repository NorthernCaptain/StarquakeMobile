package northern.captain.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Date;

import com.badlogic.gdx.Gdx;

public class StorageDir
{
	//{ 664022030, 450226524, 86579330, 131811941,  1868677366, 347666355, 351325241, 1964226716, 1630918239, 1595356632, 859324022, 906497010, 702243840, 64891639, 1417394178, 1050607047, , 241778227, 1316280931, 898259074, 1015116482, 449425985, 787029611, 1370840518, 566572057, 605418268, 1930702662, 1626410860, 1667091380, 1320235718, 934004048, 1627894797, 556731705, 1553039287, 215265089, 131551010, 70651681, , 1395644196, 979725036, 1595975570, 148725513, 967493955, 104381848, 115389193, 210661211, 188765784, 895689587, 1823211846, 520573266, 1187051967, 537883754, 445345686, 348098992, 85405458, 244433015, 29390547, 1602182875, 640271123, 461996196, 1030879887, 283539194, 617895042, 513020855, 373049024, 1413414099, 1088723786, 52676000, 208282960, 1779743514, 201859246, 613000435, 819425078, 1751466072, 531584344, 1883851085, 1595476997, 793614169, 1226980585, 1845092825, 1322349382, 1543038472, 147871852, 1615140774, 1150888389, 713677466, 384222070, 467285851, 787551642, 1764960438, 266050130, 955068176, 1650356224, 888679389, 169237790, 1951485897, 605733443,  }
	
	public static final String RANDOM_FILE = "random";
	
	private static final String relPath = "data/Quadronia";
	private static boolean doStore = false;
	
	
	private File absPath;
	private boolean pathCreated = false;
	private String fname;
	
	
	public StorageDir(String fname)
	{
		if(RANDOM_FILE.equals(fname))
			this.fname = getRandomFileName();
		else
			this.fname = fname;
	}
	
	private String getRandomFileName()
	{
		long tim = new Date().getTime();
		return Long.toString(tim) + ".t";
	}
	
	public static boolean needStore()
	{
		return doStore;
	}
	
	public static void setNeedStore(boolean flag)
	{
		doStore = flag;
	}
	
	public static String ensurePath()
	{
		if(!Gdx.files.isExternalStorageAvailable())
			return "./";
				
		File absPath = new File(Gdx.files.getExternalStoragePath());
		absPath = new File(absPath, relPath);
		if(!absPath.exists())
		{
			absPath.mkdirs();
		}
		return absPath.getAbsolutePath();
	}
	
	public boolean checkPath()
	{
		if(!Gdx.files.isExternalStorageAvailable())
			return false;
		
		absPath = new File(Gdx.files.getExternalStoragePath());
		absPath = new File(absPath, relPath);
		if(!pathCreated && !absPath.exists())
		{
			absPath.mkdirs();
		}
		pathCreated = true;
		absPath = new File(absPath, fname);
		return true;
	}
	
	public FileOutputStream getOutStream()
	{
		try
		{
			return new FileOutputStream(absPath, false);
		}
		catch (FileNotFoundException ex)
		{
			return null;
		}
	}
	
	public FileInputStream getInStream()
	{
		try
		{
			return new FileInputStream(absPath);
		}
		catch (FileNotFoundException ex)
		{
			return null;
		}
	}
	
	public String getFullPath()
	{
		if(absPath == null)
			return "./";
		
		return absPath.getAbsolutePath();
	}
	
	public String getFileNameOnly()
	{
		return fname;
	}
}
