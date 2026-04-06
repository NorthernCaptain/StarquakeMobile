package northern.captain.gamecore.glx.tools;

import com.badlogic.gdx.Gdx;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class TimerManager
{
	public static TimerManager instance;
	
	
	public static void initialize()
	{
		instance = new TimerManager();
	}
	
	public interface ITimeCall extends Runnable
	{
        void cancel();
        void setFuture(ScheduledFuture<?> f);
	}

    public static abstract class TimeCall implements ITimeCall
    {
        ScheduledFuture<?> future;

        @Override
        public void cancel()
        {
            if(future != null)
            {
                future.cancel(false);
                future = null;
            }
        }

        public void setFuture(ScheduledFuture<?> f)
        {
            cancel();
            future = f;
        }
    }

	public static class TimeCallMeBack extends TimeCall
	{
		private HandlerThread whom;
		private Message msg;
		public TimeCallMeBack(HandlerThread whom, Message msg)
		{
			this.whom = whom;
			this.msg = msg;
		}
		@Override
		public void run()
		{
			whom.send(msg);
		}
	}
	
	public static class TimeCallOnRender extends TimeCall
	{
		private Runnable runOnRender;
		
		public TimeCallOnRender(Runnable r)
		{
			runOnRender = r;
		}
		
		@Override
		public void run()
		{
			Gdx.app.postRunnable(runOnRender);
		}		
	}
	
	private ScheduledExecutorService processor;
	
	public TimerManager()
	{
		processor = Executors.newSingleThreadScheduledExecutor();
	}
	
	public void submit(TimeCall callable, long millis)
	{
        try
        {
		    callable.setFuture(processor.schedule(callable, millis, TimeUnit.MILLISECONDS));
        }
        catch(Exception ex) {}
	}	
	
	public void submitLooping(TimeCall callable, long millis)
	{
        try
        {
		    callable.setFuture(processor.scheduleAtFixedRate(callable, millis, millis, TimeUnit.MILLISECONDS));
        }
        catch(Exception ex) {}
	}
	
	
	public void shutdown()
	{
		processor.shutdown();
	}
}
