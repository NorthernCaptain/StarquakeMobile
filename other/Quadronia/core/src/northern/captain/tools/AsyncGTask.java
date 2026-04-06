package northern.captain.tools;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import northern.captain.gamecore.glx.NContext;

/**
 * Created by leo on 07.08.15.
 */
public abstract class AsyncGTask<ParamT, ReturnT> implements Runnable
{
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

    private ParamT[] params;
    private ReturnT result = null;
    private boolean hasResult = false;

    public void execute(ParamT... params)
    {
        this.params = params;
        EXECUTOR.execute(this);
    }

    /**
     * When an object implementing interface <code>Runnable</code> is used
     * to create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p/>
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    @Override
    public void run()
    {
        if(!hasResult)
        {
            result = doInBackground(params);
            hasResult = true;
            params = null;
            NContext.current.post(this);
        } else
        {
            onPostExecute(result);
            result = null;
            hasResult = false;
        }
    }

    /**
     * Put your long time background job here
     * @param paramTs pass parametes to your job
     * @return the result of your background job
     */
    public abstract ReturnT doInBackground(ParamT... paramTs);

    /**
     * This will be executed on OpenGL thread after the background job is done
     * @param result - result return by your background job if any
     */
    public abstract void onPostExecute(ReturnT result);
}
