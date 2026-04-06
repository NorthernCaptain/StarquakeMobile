package northern.captain.quadronia.game.core;

/**
 * Created by leo on 04.09.15.
 */
public interface IColorGenerator
{
    /**
     * Generates color for the quad. Each call return random color
     * @return generated color
     */
    int generate(int maxColors, int x, int y, int width, int height);

    void init();
}
