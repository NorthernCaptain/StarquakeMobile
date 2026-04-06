package northern.captain.quadronia.game.core;

import northern.captain.tools.Helpers;

/**
 * Created by leo on 04.09.15.
 */
public class BaseColorGenerator implements IColorGenerator
{
    /**
     * Generates color for the quad. Each call return random color
     *
     * @return generated color
     */
    @Override
    public int generate(int maxColors, int x, int y, int width, int height)
    {
        return 1 + Helpers.RND.nextInt(maxColors);
    }

    @Override
    public void init()
    {
    }
}
