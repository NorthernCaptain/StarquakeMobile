package northern.captain.tools;

import com.badlogic.gdx.Gdx;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import northern.captain.gamecore.glx.NCore;

public class Log 
{
	public static boolean enabled = true;

	private static volatile File logF = null;
	private static final SimpleDateFormat timeFmt = new SimpleDateFormat("HH:mm:ss.SSS");

	public static File logFile()
	{
		if(logF == null)
		{
			synchronized (timeFmt)
			{
				SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
				String fname = "quadronialog-" + fmt.format(new Date()) + ".txt";
				File dir = NCore.instance().logFileDir();
				logF = new File(dir, fname);
			}
		}

		return logF;
	}


	private static void writeLog(File f, String tag, String logline)
	{
		synchronized (f)
		{
			try (FileOutputStream fs = new FileOutputStream(f, true))
			{
				String logString = timeFmt.format(System.currentTimeMillis()) + ": " + logline + "/" + tag + "\n";
				fs.write(logString.getBytes());
				fs.close();
			}
			catch (Exception ex)
			{
			}
		}
		if(Gdx.app != null) {
			Gdx.app.log(tag, logline);
		}
	}


	public static void d(String tag, String msg)
	{
		if(enabled)
		{
			writeLog(logFile(), tag, msg);
		}
	}
	
	public static void e(String tag, String msg)
	{
		if(enabled)
		{
			writeLog(logFile(), "ERR/" + tag, msg);
		}
	}
	
	public static void e(String tag, String msg, Throwable th)
	{
		if(enabled)
		{
			writeLog(logFile(), "ERR/" + tag, msg);
		}
	}
	
	public static void w(String tag, String msg, Throwable th)
	{
		if(enabled)
		{
			writeLog(logFile(), "WARN/" + tag, msg + th.getMessage());
		}
	}
	
	public static void w(String tag, String msg)
	{
		if(enabled)
		{
			writeLog(logFile(), "WARN/" + tag, msg);
		}
	}
	
	public static void i(String tag, String msg)
	{
		if(enabled)
		{
			writeLog(logFile(), tag, msg);
		}
	}
}
