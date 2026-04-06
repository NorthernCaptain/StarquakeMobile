package northern.captain.quadronia.game.events;

import northern.captain.gamecore.BusEvent;
import northern.captain.quadronia.game.Score;

/**
 * Created by leo on 05.09.15.
 */
public class EScoreChange implements BusEvent
{
    public int delta;
    public Score score;
    public int centerCX;
    public int centerCY;

    public EScoreChange(Score score, int delta, int x, int y)
    {
        this.delta = delta;
        this.score = score;
        centerCX = x;
        centerCY = y;
    }
}
