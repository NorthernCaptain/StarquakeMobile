package northern.captain.quadronia.gfx;

/**
 * Created by leo on 09.05.15.
 */
public interface ICursorEventListener
{
    /**
     * Process touch down event if it's yours
     * @param fx
     * @param fy
     * @return true if event was consumed
     */
    boolean doTouchDown(int fx, int fy);

    /**
     * Process drag event
     * @param fx
     * @param fy
     * @return true if event was consumed
     */
    boolean doDrag(int fx, int fy);

    /**
     * Process 'release' touch up event
     * @param fx
     * @param fy
     * @return true if event was consumed
     */
    boolean doTouchUp(int fx, int fy);

}
