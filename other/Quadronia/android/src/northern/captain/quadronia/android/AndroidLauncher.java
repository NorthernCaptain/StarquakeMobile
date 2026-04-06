package northern.captain.quadronia.android;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.DisplayCutout;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.widget.FrameLayout;

import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

import java.util.Locale;

import northern.captain.quadronia.TheGame;
import northern.captain.quadronia.android.achievements.AchieveManAnd;
import northern.captain.quadronia.android.common.AndroidContext;
import northern.captain.quadronia.android.common.MarketOpener;
import northern.captain.quadronia.android.common.NContextAnd;
import northern.captain.quadronia.android.common.NCoreQuadronia;
import northern.captain.quadronia.android.common.NativeNFactoryAnd;
import northern.captain.quadronia.android.common.PersistentConfigFactoryAnd;
import northern.captain.gamecore.glx.SoundManager2;
import northern.captain.quadronia.android.sharing.ShareManagerAnd;
import northern.captain.quadronia.game.Constants;
import northern.captain.gamecore.android.gplus.GoogleGamesFactoryAnd;
import northern.captain.gamecore.glx.NContext;
import northern.captain.gamecore.glx.NCore;
import northern.captain.gamecore.glx.tools.TimerManager;
import northern.captain.gamecore.glx.tools.loaders.ResLoader;
import northern.captain.gamecore.gplus.GoogleGamesFactory;
import northern.captain.tools.Log;
import northern.captain.tools.analytics.AnalyticsFactory;
import northern.captain.tools.analytics.AnalyticsToLog;
import northern.captain.tools.analytics.IAnalytics;

public class AndroidLauncher extends AndroidApplication
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        AndroidContext.app = this.getApplication();
        AndroidContext.activity = this;

        AndroidApplicationConfiguration cfg = new AndroidApplicationConfiguration();
        cfg.useAccelerometer = false;
        cfg.useCompass = false;
        cfg.useWakelock = true;
        cfg.useImmersiveMode = true;

        NCoreQuadronia.init();

        NativeNFactoryAnd.initialize();
        PersistentConfigFactoryAnd.initialize();

        NCore.instance().initialize();

        TimerManager.initialize();
        ResLoader.singleton().initialize();
        NContext.current = new NContextAnd();
        NContext.current.setLang(Locale.getDefault().toString());
        NContext.current.setLangCountry(Locale.getDefault().toString());

        MarketOpener.initialize();

        AnalyticsFactory.setSingleton(new AnalyticsFactory()
        {
            private IAnalytics analytic = new AnalyticsToLog();

            @Override
            public IAnalytics getAnalytics()
            {
                return analytic;
            }
        });


        final View mainView = initializeForView(new TheGame(), cfg);

        AndroidContext.mainHandler = new Handler(message->false);

        boolean isStatusBarOn = true; //FIXME options

        if(!isStatusBarOn)
        {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().clearFlags(
                    WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        }

        GoogleGamesFactoryAnd.initialize();
        GoogleGamesFactory.instance().getProcessor().addLeaderboard(Constants.LEADERBOARD_ARCADE_EASY, R.string.leaderboard_classic);
        GoogleGamesFactory.instance().getProcessor().addLeaderboard(Constants.LEADERBOARD_ARCADE_MEDIUM, R.string.leaderboard_classic);
        GoogleGamesFactory.instance().getProcessor().addLeaderboard(Constants.LEADERBOARD_EXPRESS_EASY, R.string.leaderboard_express_5_mins);
        GoogleGamesFactory.instance().getProcessor().addLeaderboard(Constants.LEADERBOARD_EXPRESS_MEDIUM, R.string.leaderboard_express_5_mins);

        setContentView(mainView, createLayoutParams());

        // Handle display cutout (camera notch) for SDK 28+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            mainView.setOnApplyWindowInsetsListener((v, insets) -> {
                DisplayCutout cutout = insets.getDisplayCutout();
                int statusBarHeight = insets.getSystemWindowInsetTop();
                float density = v.getResources().getDisplayMetrics().density;
                Log.i("game", "StatusBar inset: " + statusBarHeight + ", density: " + density);

                int topInset = 0;

                if (cutout != null) {
                    int safeInset = cutout.getSafeInsetTop();
                    Log.i("game", "Cutout safeInsetTop: " + safeInset);

                    List<Rect> cutoutRects = cutout.getBoundingRects();
                    Log.i("game", "Cutout rects count: " + cutoutRects.size());
                    for (Rect rect : cutoutRects) {
                        Log.i("game", "Cutout rect: " + rect + " height=" + rect.height());
                    }

                    // Try to get actual cutout height from bounding rects
                    for (Rect rect : cutoutRects) {
                        if (rect.top == 0 && rect.height() > topInset) {
                            topInset = rect.height();
                        }
                    }

                    // Fallback to safe inset if no rects
                    if (topInset == 0) {
                        topInset = safeInset;
                    }
                } else {
                    Log.i("game", "DisplayCutout is null");
                }

                // If we have a large status bar (> 25dp), it likely includes a notch/punch-hole
                // Estimate notch height by subtracting normal status bar height (~25dp)
                int normalStatusBarPx = (int) (25 * density);
                if (statusBarHeight > normalStatusBarPx) {
                    int estimatedNotch = statusBarHeight - normalStatusBarPx;
                    Log.i("game", "Large statusBar detected, estimated notch: " + estimatedNotch);
                    if (topInset == 0 || estimatedNotch < topInset) {
                        topInset = estimatedNotch;
                    }
                }

                Log.i("game", "Final topInset: " + topInset);

                if (topInset > 0) {
                    FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) v.getLayoutParams();
                    if (params.topMargin != topInset) {
                        params.topMargin = topInset;
                        v.setLayoutParams(params);
                    }
                }
                return insets;
            });
            // Request insets to trigger the listener
            mainView.requestApplyInsets();
        }

        final SoundManager2 soundManager2 = new SoundManager2();
        NCore.instance().setSoundman(soundManager2);
        soundManager2.start();

        ShareManagerAnd.initialize();
        AchieveManAnd.initialize();

        Log.i("game", "====== OnCreate completed =======");
    }

    protected FrameLayout.LayoutParams createLayoutParams ()
    {
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT); //MATCH_PARENT or FILL_PARENT
        layoutParams.gravity = Gravity.CENTER;
        return layoutParams;
    }


    /* (non-Javadoc)
     * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
//		AndroidContext.gameOptionsMenu.inflateMenu(menu);
        return true;
    }

    /* (non-Javadoc)
     * @see android.app.Activity#onPrepareOptionsMenu(android.view.Menu)
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
//		AndroidContext.gameOptionsMenu.prepareMenu(menu);
        NContext.current.post(new Runnable()
        {
            @Override
            public void run()
            {
                NCore.instance().openOptionsMenu();
            }
        });
        return false;
    }

    /* (non-Javadoc)
     * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
//        if(InAppBillingFactory.instance().inAppBilling().handleActivityResult(requestCode, resultCode, data))
//            return;
        GoogleGamesFactory.instance().getProcessor().onActivityResult(requestCode, resultCode, data);
        NCore.instance().onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        GoogleGamesFactory.instance().getProcessor().onStart();
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        NCore.instance().onFullSuspend();
        GoogleGamesFactory.instance().getProcessor().onStop();
    }

    @Override
    protected void onRestart()
    {
        super.onRestart();
        NCore.instance().onFullResume();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        NCore.instance().onDestroy();
    }

    @Override
    public void onConfigurationChanged(Configuration config)
    {
        super.onConfigurationChanged(config);
        //Display display = getWindowManager().getDefaultDisplay();
        //?? libgdx return old (before orientation change) width and height here ;(
        NContext.current.orientationChanged(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), Gdx.input.getRotation());
    }
}
