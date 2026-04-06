package northern.captain.quadronia.android.common;

import java.io.File;

import northern.captain.gamecore.glx.INameDialog;
import northern.captain.gamecore.glx.NCore;
import northern.captain.quadronia.game.profile.UserManager;
import northern.captain.quadronia.screens.ScreenWorkflowCurve;

import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

import org.robovm.apple.foundation.NSCharacterSet;
import org.robovm.apple.foundation.NSString;
import org.robovm.apple.foundation.NSURL;
import org.robovm.apple.uikit.UIApplication;

public class NCoreQuadronia extends NCore
{
    public static void init()
    {
        singleton = new NCoreQuadronia();
        BUS = new Bus(ThreadEnforcer.ANY);
    }

    public NCoreQuadronia()
    {
        version = 163;
        versionName = "1.6.3";
    }


    /* (non-Javadoc)
     * @see northern.captain.seabattle.ISeabattleNC#initialize()
     */
    @Override
    public void initialize()
    {
        setGameOptionsMenu(new GameOptionsMenu() {
            @Override
            public void openFeedback() {
                String subj = "Feedback from user " + UserManager.instance.getCurrentUser().getName();
                String body = "Dear Quadronia team,\n";
                String nsubj = new NSString(subj).addPercentEncoding(NSCharacterSet.getURLQueryAllowedCharacterSet());
                String nbody = new NSString(body).addPercentEncoding(NSCharacterSet.getURLQueryAllowedCharacterSet());
                String url = "mailto:navalclash@gmail.com?subject=" + nsubj + "&body=" + nbody;
                UIApplication.getSharedApplication().openURL(new NSURL(url), null, (bool)->{});
            }
        });
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
