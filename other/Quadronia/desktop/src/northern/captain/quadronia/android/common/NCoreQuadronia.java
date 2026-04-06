package northern.captain.quadronia.android.common;

import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

import java.io.File;

import northern.captain.gamecore.glx.INameDialog;
import northern.captain.gamecore.glx.NCore;
import northern.captain.quadronia.screens.ScreenWorkflowCurve;

public class NCoreQuadronia extends NCore
{
    public static void init()
    {
        singleton = new NCoreQuadronia();
        BUS = new Bus(ThreadEnforcer.ANY);
    }

    public NCoreQuadronia()
    {
        version = 101;
        versionName = "1.0.1";
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
//        AndroidContext.mainHandler.post(runnable);
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
        return null;
    }

    @Override
    public void toastLong(String text)
    {
//        AndroidContext.mainHandler.post(() ->
//                Toast.makeText(AndroidContext.activity, text, Toast.LENGTH_LONG).show());
    }

    @Override
    public void toastShort(String text)
    {
//        AndroidContext.mainHandler.post(() ->
//                Toast.makeText(AndroidContext.activity, text, Toast.LENGTH_SHORT).show());
    }

    @Override
    public File logFileDir()
    {
//        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        return new File(".");
    }
}
