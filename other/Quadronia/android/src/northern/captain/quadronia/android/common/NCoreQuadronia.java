package northern.captain.quadronia.android.common;

import android.os.Environment;
import android.widget.Toast;

import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

import java.io.File;

import northern.captain.quadronia.android.GameOptionsMenu;
import northern.captain.quadronia.android.R;
import northern.captain.quadronia.screens.ScreenWorkflowCurve;
import northern.captain.gamecore.glx.INameDialog;
import northern.captain.gamecore.glx.NCore;

public class NCoreQuadronia extends NCore
{
	public static void init()
	{
		singleton = new NCoreQuadronia();
		BUS = new Bus(ThreadEnforcer.ANY);
	}
	
	public NCoreQuadronia()
	{
        version = AndroidContext.app.getResources().getInteger(R.integer.app_version_code);
		versionName = AndroidContext.app.getResources().getString(R.string.app_version);
	}

	
	/* (non-Javadoc)
	 * @see northern.captain.seabattle.ISeabattleNC#initialize()
	 */
	@Override
	public void initialize()
	{
		setGameOptionsMenu(new GameOptionsMenu());
		screenflow = new ScreenWorkflowCurve(this);
	}

    @Override
    public void postRunnableOnMain(Runnable runnable)
    {
        AndroidContext.mainHandler.post(runnable);
    }

    @Override
    public void initGame()
    {

    }

	@Override
	public void onOrientationChange()
	{

	}

	@Override
	public INameDialog newNameDialog()
	{
		return new NameDialog();
	}

	@Override
	public void toastLong(String text)
	{
        AndroidContext.mainHandler.post(() ->
		    Toast.makeText(AndroidContext.activity, text, Toast.LENGTH_LONG).show());
	}

	@Override
	public void toastShort(String text)
	{
        AndroidContext.mainHandler.post(() ->
    		Toast.makeText(AndroidContext.activity, text, Toast.LENGTH_SHORT).show());
	}

	@Override
	public File logFileDir()
	{
		File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
		return dir;
	}
}
