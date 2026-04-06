package northern.captain.gamecore.glx.tools;

import northern.captain.tools.Log;

public class Message
{
	public static final int RUNNABLE_INSIDE = 894729322;
	public static final int STOP_THREAD = 573480345;
	public int arg1;
	public int arg2;
	public Object obj;
	public int what;
	
	public Message nextFree;
    public IDelivered deliverTo;

    public interface IDelivered
    {
        void messageDelivered(Message msg);
    }

	public Message()
	{
		
	}
	
	public Message init(int w)
	{
		what = w;
		obj = null;
		arg1 = arg2 = 0;
		return this;
	}
	
	public Message init(int w, int a)
	{
		what = w; arg1 = a;
		obj = null;
		arg2 = 0;
		return this;
	}

    public Message init(int w, Object data)
    {
        what = w; obj = data;
        return this;
    }

	public Message init(Runnable r)
	{
		what = RUNNABLE_INSIDE;
		obj = r;
		arg1 = arg2 = 0;
		return this;
	}

    public void deliver()
    {
        if(deliverTo != null)
        {
            deliverTo.messageDelivered(this);
        } else
        {
            Log.e("ncgame", "Message deliver to nothing with code: " + what);
        }
    }
}
