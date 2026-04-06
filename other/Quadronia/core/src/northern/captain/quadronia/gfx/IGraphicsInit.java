package northern.captain.quadronia.gfx;

import northern.captain.gamecore.glx.tools.loaders.XMLLayoutLoader;

/**
 * Created by leo on 16.05.15.
 */
public interface IGraphicsInit
{
    void initGraphics(XMLLayoutLoader loader, GraphicsInitContext gContext);
}
