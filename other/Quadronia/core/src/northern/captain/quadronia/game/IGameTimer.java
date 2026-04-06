package northern.captain.quadronia.game;

/**
 * Created by leo on 20.04.15.
 */
public interface IGameTimer
{
    void resetTimer(int timeout);
    void pauseTimer();
    void resumeTimer();
    void setOnTimedOut(Runnable callback);
}
