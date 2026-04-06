package northern.captain.gamecore.glx.tools;

import northern.captain.gamecore.exception.LogicExceptionStopThread;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class HandlerThread extends Thread
{
	protected BlockingQueue<Message> msgQueue = new LinkedBlockingQueue<Message>(100);
	
	public HandlerThread()
	{
	}

	public HandlerThread(String arg0)
	{
		super(arg0);
	}

	public HandlerThread(ThreadGroup arg0, String arg1)
	{
		super(arg0, arg1);
	}

	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run()
	{
		looping = true;
		Message msg;
		onLooperPrepared();
		while(looping)
		{
			try
			{
				while((msg = msgQueue.take()) != null)
				{
					if(processMsg(msg))
					{
						backToPool(msg);
					}
					if(!looping)
					{
						break;
					}
				}
			}
			catch (InterruptedException e)
			{
				if(isInterrupted())
					looping = false;
			}
            catch (LogicExceptionStopThread le)
            {
                looping = false;
            }
		}
	}

	protected Message freePool = null;
	
	protected boolean looping = false;
	
	public synchronized void backToPool(Message msg)
	{
        msg.obj = null;
		msg.nextFree = freePool;
		freePool = msg;
	}
	
	public Message obtain()
	{
		if(freePool != null)
		{
			synchronized (this)
			{
				if(freePool != null)
				{
					Message msg = freePool;
					freePool = msg.nextFree;
                    msg.nextFree = null;
					return msg;
				}
			}
		}
		return new Message();
	}
	
	public void send(Message msg)
	{
		try
		{
			msgQueue.put(msg);
		}
		catch (InterruptedException e)
		{
		}
	}
	
	public void send(int what)
	{
		send(obtain().init(what));
	}
	
	/**
	 * Process given message and return true if message can be reused (returned back to pool)
	 * @param msg
	 * @return
	 */
	protected boolean processMsg(Message msg)
	{
		if(msg.what == Message.RUNNABLE_INSIDE)
		{
			Runnable r = (Runnable)msg.obj;
			r.run();
		}
		if(msg.what == Message.STOP_THREAD)
		{
			looping = false;
		}
		return true;
	}
	
	protected void onLooperPrepared()
	{
		
	}
}
