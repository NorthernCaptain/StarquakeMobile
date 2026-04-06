package northern.captain.quadronia;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import northern.captain.gamecore.glx.NContext;
import northern.captain.gamecore.glx.NCore;
import northern.captain.gamecore.glx.res.SharedRes;
import northern.captain.gamecore.glx.screens.SplashScreen;
import northern.captain.gamecore.glx.tools.Animations;
import northern.captain.gamecore.glx.tools.Game;
import northern.captain.gamecore.glx.tools.TimerManager;
import northern.captain.tools.Log;

public class TheGame extends Game
{

    MyFpsLogger mfps;

    class MyFpsLogger
    {
        long startTime = NContext.current.getTimeMillis();
        int calls = 0;

        public void log()
        {
            calls++;
            if(calls > 100)
            {
                long curTime = NContext.current.getTimeMillis();
                long delta = curTime - startTime;
                float fps = (float)calls / (delta / 1000.0f);
//                Log.i("ncgame", "Current FPS: " + fps + " call/time: " + calls + "/" + (float) delta / 1000.0f);

                calls = 0;
                startTime = curTime;
            }
        }
    }

    public static TheGame instance;

    public TheGame()
    {
        instance = this;
    }

    @Override
    public void create()
    {
        // Register custom action classes for libGDX 1.14+ pool system
        Animations.registerActions();
        mfps = new MyFpsLogger();
//		TimerManager.instance.submitLooping(new TimerManager.TimeCall()
//		{
//
//			@Override
//			public void run()
//			{
//                float delta = (System.nanoTime() - NContext.current.getLastNano()) / 1000000000.0f;
//
//				Log.i("ncgame", "GFX stat: render count: " + NContext.current.getRefreshCounter() + "/"
//                        + NContext.current.getTempRefresh() + ", last render: " + delta
//                        + ", calls: " + NContext.current.statRederCalls
//                        + ", max batch: " + NContext.current.statMaxSpritesInBatch);
//				Log.i("ncgame", "GFX stat: java heap: " + Gdx.app.getJavaHeap()/1024/1024 + ", native heap: " + Gdx.app.getNativeHeap()/1024/1024);
//			}
//		}, 2000);
        NCore.instance().initGame();
    }

    @Override
    public void dispose()
    {
        TimerManager.instance.shutdown();
        super.dispose();
    }

    public static final float standardFrameDuration = 1f / 40f;

    @Override
    public void render()
    {
        //This one should be first to run in a render frame, but after processed input
        NContext.current.processMessagesOnGui();

        super.render();
        mfps.log();
        float delta = NContext.current.getRawDelta();

        //Trying to compensate frame rate and doing some sleep if we render too fast
        if(delta < standardFrameDuration)
        {
            long sleep = (long)((standardFrameDuration - delta) * 1000f);
            try
            {
                if(sleep > 1)
                {
//                    Log.d("ncgame", "Delta: " + delta + " sleep: " + sleep);
                    Thread.sleep(sleep);
                }
            } catch (InterruptedException e)
            {
                Log.d("ncgame", "Sleep INTERRUPT!");
            }
        }

        NContext.current.doRefresh();
    }

    @Override
    public void resize(int width, int height)
    {
        Log.d("ncgame", "===> Game resize called with " + width + "x" + height);
        if(getScreen() == null)
        {
            super.resize(width, height);
            NContext.current.initialize(width, height, true);
            SharedRes.instance.startLoadingPart1();

            setScreen(new SplashScreen(NCore.instance().getScreenFlow()));

            NCore.instance().loadData();
        }
    }

    @Override
    public void pause()
    {
        super.pause();
    }

    @Override
    public void resume()
    {
        super.resume();
    }
}
