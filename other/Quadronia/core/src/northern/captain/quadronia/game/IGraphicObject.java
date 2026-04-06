package northern.captain.quadronia.game;

/**
 * Created by leo on 06.04.15.
 */
public interface IGraphicObject
{
    boolean hasLogicObject(Object obj);
    void processRemoval(Engine game);
}
